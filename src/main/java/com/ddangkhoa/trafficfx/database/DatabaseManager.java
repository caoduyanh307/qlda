package com.ddangkhoa.trafficfx.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {
    private static final Path DATA_DIR = Path.of("data");
    private static final Path DB_FILE = Path.of("data", "traffic_management.db");
    private static final String DATABASE_URL = "jdbc:sqlite:data/traffic_management.db";
    private static final int SCHEMA_VERSION = 6;

    private DatabaseManager() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    public static void initializeDatabase() {
        try {
            Files.createDirectories(DATA_DIR);
            resetIfSchemaOld();
            initializeFreshOrExistingDatabase();
        } catch (Exception firstException) {
            try {
                Files.deleteIfExists(DB_FILE);
                initializeFreshOrExistingDatabase();
            } catch (Exception secondException) {
                secondException.addSuppressed(firstException);
                throw new RuntimeException("Cannot initialize SQLite database", secondException);
            }
        }
    }

    private static void resetIfSchemaOld() throws Exception {
        if (!Files.exists(DB_FILE)) return;
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery("PRAGMA user_version")) {
                int version = rs.next() ? rs.getInt(1) : 0;
                if (version != SCHEMA_VERSION) {
                    Files.deleteIfExists(DB_FILE);
                }
            }
        }
    }

    private static void initializeFreshOrExistingDatabase() throws Exception {
        Files.createDirectories(DATA_DIR);
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            createTables(statement);
            seedAccount(statement);
            seedAccountRole(statement);
            seedEmployee(statement);
            seedArea(statement);
            seedAreaBoundary(statement);
            seedStreet(statement);
            seedNode(statement);
            seedSegment(statement);
            seedSegmentStatus(statement);
            seedSystemLogs(statement);
            createManagementViews(statement);
            statement.execute("PRAGMA user_version = " + SCHEMA_VERSION);
        }
    }

    private static void createTables(Statement statement) throws SQLException {
        statement.execute("""
                CREATE TABLE IF NOT EXISTS account (
                    account_id TEXT PRIMARY KEY,
                    full_name TEXT NOT NULL,
                    email TEXT UNIQUE,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    status TEXT NOT NULL CHECK(status IN ('ACTIVE','LOCKED','INACTIVE')),
                    created_at TEXT NOT NULL,
                    updated_at TEXT,
                    is_delete INTEGER DEFAULT 0 CHECK(is_delete IN (0,1))
                )
                """);
        statement.execute("""
                CREATE TABLE IF NOT EXISTS account_role (
                    account_role_id TEXT PRIMARY KEY,
                    account_id TEXT NOT NULL,
                    role_name TEXT NOT NULL CHECK(role_name IN ('ADMIN','TECHNICIAN','ANALYST')),
                    assigned_at TEXT NOT NULL,
                    is_active INTEGER DEFAULT 1 CHECK(is_active IN (0,1)),
                    is_delete INTEGER DEFAULT 0 CHECK(is_delete IN (0,1)),
                    FOREIGN KEY(account_id) REFERENCES account(account_id)
                )
                """);
        statement.execute("""
                CREATE TABLE IF NOT EXISTS employee (
                    employee_id TEXT PRIMARY KEY,
                    account_id TEXT,
                    fullname TEXT NOT NULL,
                    phonenumber TEXT NOT NULL,
                    email TEXT NOT NULL,
                    dateofbirth TEXT,
                    gender TEXT,
                    address TEXT,
                    salary TEXT,
                    status TEXT,
                    is_deleted INTEGER DEFAULT 0 CHECK(is_deleted IN (0,1)),
                    FOREIGN KEY(account_id) REFERENCES account(account_id)
                )
                """);
        statement.execute("""
                CREATE TABLE IF NOT EXISTS area (
                    area_id TEXT PRIMARY KEY,
                    area_name TEXT NOT NULL,
                    area_type TEXT NOT NULL CHECK(area_type IN ('Xã','Phường','Đặc khu')),
                    old_province TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    is_deleted INTEGER DEFAULT 0 CHECK(is_deleted IN (0,1))
                )
                """);
        statement.execute("""
                CREATE TABLE IF NOT EXISTS area_boundary (
                    area_id TEXT PRIMARY KEY,
                    boundary_wkt TEXT NOT NULL,
                    FOREIGN KEY(area_id) REFERENCES area(area_id)
                )
                """);
        statement.execute("""
                CREATE TABLE IF NOT EXISTS street (
                    street_id TEXT PRIMARY KEY,
                    street_name TEXT,
                    street_type TEXT NOT NULL,
                    road_level INTEGER NOT NULL CHECK(road_level IN (1,2,3,4)),
                    created_at TEXT NOT NULL,
                    is_deleted INTEGER DEFAULT 0 CHECK(is_deleted IN (0,1))
                )
                """);
        statement.execute("""
                CREATE TABLE IF NOT EXISTS node (
                    node_id TEXT PRIMARY KEY,
                    segment_id TEXT,
                    latitude TEXT NOT NULL,
                    longitude TEXT NOT NULL,
                    is_delete INTEGER DEFAULT 0 CHECK(is_delete IN (0,1))
                )
                """);
        statement.execute("""
                CREATE TABLE IF NOT EXISTS segment (
                    segment_id TEXT PRIMARY KEY,
                    street_id TEXT NOT NULL,
                    area_id TEXT,
                    start_node_id TEXT NOT NULL,
                    end_node_id TEXT NOT NULL,
                    segment_length TEXT CHECK(CAST(segment_length AS REAL) >= 0),
                    max_velocity TEXT CHECK(CAST(max_velocity AS REAL) >= 0),
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL,
                    is_deleted INTEGER DEFAULT 0 CHECK(is_deleted IN (0,1)),
                    FOREIGN KEY(street_id) REFERENCES street(street_id),
                    FOREIGN KEY(area_id) REFERENCES area(area_id),
                    FOREIGN KEY(start_node_id) REFERENCES node(node_id),
                    FOREIGN KEY(end_node_id) REFERENCES node(node_id)
                )
                """);
        statement.execute("""
                CREATE TABLE IF NOT EXISTS segment_status (
                    status_id TEXT PRIMARY KEY,
                    segment_id TEXT NOT NULL,
                    updated_at TEXT NOT NULL,
                    velocity TEXT NOT NULL CHECK(CAST(velocity AS REAL) >= 0),
                    traffic_level TEXT DEFAULT 'UNKNOWN' CHECK(traffic_level IN ('FREE','SLOW','CONGESTED','UNKNOWN')),
                    source_name TEXT DEFAULT 'segment_status.csv',
                    FOREIGN KEY(segment_id) REFERENCES segment(segment_id)
                )
                """);
        statement.execute("""
                CREATE TABLE IF NOT EXISTS system_log (
                    log_id TEXT PRIMARY KEY,
                    account_id TEXT,
                    behaviour TEXT NOT NULL,
                    target_table TEXT NOT NULL,
                    target_id TEXT NOT NULL,
                    old_value TEXT,
                    new_value TEXT,
                    log_status TEXT DEFAULT 'SUCCESS' CHECK(log_status IN ('SUCCESS','FAILED')),
                    created_at TEXT NOT NULL,
                    FOREIGN KEY(account_id) REFERENCES account(account_id)
                )
                """);
    }

    private static void createManagementViews(Statement statement) throws SQLException {
        statement.execute("DROP TRIGGER IF EXISTS account_management_insert");
        statement.execute("DROP TRIGGER IF EXISTS account_management_update");
        statement.execute("DROP TRIGGER IF EXISTS account_management_delete");
        statement.execute("DROP VIEW IF EXISTS account_management");
        statement.execute("""
                CREATE VIEW account_management AS
                SELECT
                    a.account_id,
                    a.full_name,
                    a.email,
                    a.username,
                    a.password,
                    COALESCE(r.role_name, '') AS role_name,
                    COALESCE(r.assigned_at, '') AS assigned_at,
                    COALESCE(CAST(r.is_active AS TEXT), '1') AS role_active,
                    a.status AS account_status,
                    a.created_at,
                    a.updated_at,
                    a.is_delete
                FROM account a
                LEFT JOIN account_role r ON r.account_id = a.account_id AND r.is_delete = 0
                """);
        statement.execute("""
                CREATE TRIGGER account_management_insert
                INSTEAD OF INSERT ON account_management
                BEGIN
                    INSERT INTO account(account_id, full_name, email, username, password, status, created_at, updated_at, is_delete)
                    VALUES (NEW.account_id, NEW.full_name, NEW.email, NEW.username, NEW.password, NEW.account_status, NEW.created_at, NEW.updated_at, NEW.is_delete);

                    INSERT INTO account_role(account_role_id, account_id, role_name, assigned_at, is_active, is_delete)
                    VALUES ('AR' || substr(NEW.account_id, 3), NEW.account_id, NEW.role_name, NEW.assigned_at, COALESCE(NEW.role_active, 1), 0);
                END
                """);
        statement.execute("""
                CREATE TRIGGER account_management_update
                INSTEAD OF UPDATE ON account_management
                BEGIN
                    UPDATE account
                    SET full_name = NEW.full_name,
                        email = NEW.email,
                        username = NEW.username,
                        password = NEW.password,
                        status = NEW.account_status,
                        created_at = NEW.created_at,
                        updated_at = NEW.updated_at,
                        is_delete = NEW.is_delete
                    WHERE account_id = OLD.account_id;

                    INSERT INTO account_role(account_role_id, account_id, role_name, assigned_at, is_active, is_delete)
                    SELECT 'AR' || substr(NEW.account_id, 3), NEW.account_id, NEW.role_name, NEW.assigned_at, COALESCE(NEW.role_active, 1), 0
                    WHERE NOT EXISTS (SELECT 1 FROM account_role WHERE account_id = OLD.account_id AND is_delete = 0);

                    UPDATE account_role
                    SET role_name = NEW.role_name,
                        assigned_at = NEW.assigned_at,
                        is_active = CASE WHEN COALESCE(NEW.is_delete, 0) = 1 THEN 0 ELSE COALESCE(NEW.role_active, 1) END,
                        is_delete = COALESCE(NEW.is_delete, 0)
                    WHERE account_id = OLD.account_id;
                END
                """);
        statement.execute("""
                CREATE TRIGGER account_management_delete
                INSTEAD OF DELETE ON account_management
                BEGIN
                    UPDATE account SET is_delete = 1, status = 'INACTIVE', updated_at = date('now') WHERE account_id = OLD.account_id;
                    UPDATE account_role SET is_delete = 1, is_active = 0 WHERE account_id = OLD.account_id;
                END
                """);

        statement.execute("DROP TRIGGER IF EXISTS area_management_insert");
        statement.execute("DROP TRIGGER IF EXISTS area_management_update");
        statement.execute("DROP TRIGGER IF EXISTS area_management_delete");
        statement.execute("DROP VIEW IF EXISTS area_management");
        statement.execute("""
                CREATE VIEW area_management AS
                SELECT
                    a.area_id,
                    a.area_name,
                    a.area_type,
                    a.old_province,
                    COALESCE(b.boundary_wkt, '') AS boundary_wkt,
                    a.created_at,
                    a.is_deleted
                FROM area a
                LEFT JOIN area_boundary b ON b.area_id = a.area_id
                """);
        statement.execute("""
                CREATE TRIGGER area_management_insert
                INSTEAD OF INSERT ON area_management
                BEGIN
                    INSERT INTO area(area_id, area_name, area_type, old_province, created_at, is_deleted)
                    VALUES (NEW.area_id, NEW.area_name, NEW.area_type, NEW.old_province, NEW.created_at, NEW.is_deleted);

                    INSERT INTO area_boundary(area_id, boundary_wkt)
                    VALUES (NEW.area_id, NEW.boundary_wkt);
                END
                """);
        statement.execute("""
                CREATE TRIGGER area_management_update
                INSTEAD OF UPDATE ON area_management
                BEGIN
                    UPDATE area
                    SET area_name = NEW.area_name,
                        area_type = NEW.area_type,
                        old_province = NEW.old_province,
                        created_at = NEW.created_at,
                        is_deleted = NEW.is_deleted
                    WHERE area_id = OLD.area_id;

                    INSERT INTO area_boundary(area_id, boundary_wkt)
                    SELECT NEW.area_id, NEW.boundary_wkt
                    WHERE NOT EXISTS (SELECT 1 FROM area_boundary WHERE area_id = OLD.area_id);

                    UPDATE area_boundary
                    SET boundary_wkt = NEW.boundary_wkt
                    WHERE area_id = OLD.area_id;
                END
                """);
        statement.execute("""
                CREATE TRIGGER area_management_delete
                INSTEAD OF DELETE ON area_management
                BEGIN
                    UPDATE area SET is_deleted = 1 WHERE area_id = OLD.area_id;
                END
                """);
    }

    private static void seedAccount(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO account(account_id, full_name, email, username, password, status, created_at, updated_at, is_delete) VALUES
                ('AC001', 'Nguyễn Quản Trị', 'admin@uit.edu.vn', 'admin', 'admin123', 'ACTIVE', '2026-05-18', '2026-05-18', 0),
                ('AC002', 'Trần Kỹ Thuật', 'tech@uit.edu.vn', 'tech', 'tech123', 'ACTIVE', '2026-05-18', '2026-05-18', 0),
                ('AC003', 'Lê Phân Tích', 'analyst@uit.edu.vn', 'analyst', 'analyst123', 'ACTIVE', '2026-05-18', '2026-05-18', 0),
                ('AC004', 'Cao Duy Anh', 'caoduyanh@uit.edu.vn', 'caoduyanh', '123456', 'INACTIVE', '2026-05-19', '2026-05-19', 0)
                """);
    }

    private static void seedAccountRole(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO account_role(account_role_id, account_id, role_name, assigned_at, is_active, is_delete) VALUES
                ('AR001', 'AC001', 'ADMIN', '2026-05-18', 1, 0),
                ('AR002', 'AC002', 'TECHNICIAN', '2026-05-18', 1, 0),
                ('AR003', 'AC003', 'ANALYST', '2026-05-18', 1, 0),
                ('AR004', 'AC004', 'TECHNICIAN', '2026-05-19', 0, 0)
                """);
    }

    private static void seedEmployee(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO employee(employee_id, account_id, fullname, phonenumber, email, dateofbirth, gender, address, salary, status, is_deleted) VALUES
                ('NV001', 'AC001', 'Nguyễn Quản Trị', '0901000001', 'admin@uit.edu.vn', '1995-01-10', 'Nam', 'Quận 1, TP.HCM', '25000000', 'Đang làm', 0),
                ('NV002', 'AC002', 'Trần Kỹ Thuật', '0901000002', 'tech@uit.edu.vn', '1998-04-15', 'Nam', 'Quận Bình Thạnh, TP.HCM', '18000000', 'Đang làm', 0),
                ('NV003', 'AC003', 'Lê Phân Tích', '0901000003', 'analyst@uit.edu.vn', '1999-07-22', 'Nữ', 'TP. Thủ Đức, TP.HCM', '19000000', 'Đang làm', 0),
                ('NV004', 'AC004', 'Phạm Minh An', '0901000004', 'minhan@uit.edu.vn', '2000-03-12', 'Nam', 'Quận 7, TP.HCM', '16000000', 'Tạm nghỉ', 0),
                ('NV005', NULL, 'Võ Hoàng Nam', '0901000005', 'hoangnam@uit.edu.vn', '1997-12-01', 'Nam', 'Quận Tân Bình, TP.HCM', '15000000', 'Đang làm', 0)
                """);
    }

    private static void seedArea(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO area(area_id, area_name, area_type, old_province, created_at, is_deleted) VALUES
                ('KV001', 'Phường Bến Thành', 'Phường', 'TP.HCM', '2026-05-18', 0),
                ('KV002', 'Phường Sài Gòn', 'Phường', 'TP.HCM', '2026-05-18', 0),
                ('KV003', 'Xã Bình Chánh', 'Xã', 'TP.HCM', '2026-05-18', 0),
                ('KV004', 'Đặc khu Côn Đảo', 'Đặc khu', 'Bà Rịa - Vũng Tàu', '2026-05-18', 0),
                ('KV005', 'Phường Thủ Dầu Một', 'Phường', 'Bình Dương', '2026-05-18', 0)
                """);
    }

    private static void seedAreaBoundary(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO area_boundary(area_id, boundary_wkt) VALUES
                ('KV001', 'POLYGON((106.695 10.770,106.710 10.770,106.710 10.785,106.695 10.785,106.695 10.770))'),
                ('KV002', 'POLYGON((106.700 10.765,106.715 10.765,106.715 10.780,106.700 10.780,106.700 10.765))'),
                ('KV003', 'POLYGON((106.540 10.660,106.590 10.660,106.590 10.710,106.540 10.710,106.540 10.660))')
                """);
    }

    private static void seedStreet(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO street(street_id, street_name, street_type, road_level, created_at, is_deleted) VALUES
                ('ST001', 'Đường Lê Duẩn', 'Đường đô thị', 1, '2026-05-18', 0),
                ('ST002', 'Đường Nguyễn Huệ', 'Phố đi bộ', 2, '2026-05-18', 0),
                ('ST003', 'Quốc lộ 1A', 'Quốc lộ', 1, '2026-05-18', 0),
                ('ST004', 'Đường Võ Văn Kiệt', 'Đại lộ', 2, '2026-05-18', 0)
                """);
    }

    private static void seedNode(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO node(node_id, segment_id, latitude, longitude, is_delete) VALUES
                ('ND001', 'SG001', '10.7765000', '106.7009000', 0),
                ('ND002', 'SG001', '10.7791000', '106.7042000', 0),
                ('ND003', 'SG002', '10.7722000', '106.7041000', 0),
                ('ND004', 'SG002', '10.7739000', '106.7063000', 0),
                ('ND005', 'SG003', '10.6950000', '106.5850000', 0),
                ('ND006', 'SG003', '10.7030000', '106.5950000', 0)
                """);
    }

    private static void seedSegment(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO segment(segment_id, street_id, area_id, start_node_id, end_node_id, segment_length, max_velocity, created_at, updated_at, is_deleted) VALUES
                ('SG001', 'ST001', 'KV001', 'ND001', 'ND002', '850', '50', '2026-05-18', '2026-05-18', 0),
                ('SG002', 'ST002', 'KV002', 'ND003', 'ND004', '620', '40', '2026-05-18', '2026-05-18', 0),
                ('SG003', 'ST003', 'KV003', 'ND005', 'ND006', '2300', '60', '2026-05-18', '2026-05-18', 0)
                """);
    }

    private static void seedSegmentStatus(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO segment_status(status_id, segment_id, updated_at, velocity, traffic_level, source_name) VALUES
                ('SS001', 'SG001', '2026-05-18 07:30:00', '28', 'SLOW', 'segment_status.csv'),
                ('SS002', 'SG002', '2026-05-18 08:00:00', '38', 'FREE', 'segment_status.csv'),
                ('SS003', 'SG003', '2026-05-18 08:15:00', '8', 'CONGESTED', 'segment_status.csv'),
                ('SS004', 'SG001', '2026-05-18 09:00:00', '33', 'FREE', 'segment_status.csv')
                """);
    }

    private static void seedSystemLogs(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO system_log(log_id, account_id, behaviour, target_table, target_id, old_value, new_value, log_status, created_at) VALUES
                ('LG001', 'AC001', 'Đăng nhập', 'ACCOUNT', 'AC001', '', 'LOGIN SUCCESS', 'SUCCESS', '2026-05-18 07:00:00'),
                ('LG002', 'AC002', 'Cập nhật dữ liệu', 'AREA', 'KV001', 'old province', 'TP.HCM', 'SUCCESS', '2026-05-18 07:05:00'),
                ('LG003', 'AC003', 'Xem báo cáo', 'SEGMENT_STATUS', 'SS001', '', 'VIEW', 'SUCCESS', '2026-05-18 07:10:00')
                """);
    }
}
