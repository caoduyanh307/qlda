PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS area (
    area_id TEXT PRIMARY KEY,
    area_name TEXT NOT NULL,
    area_type TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'Hoạt động',
    created_date TEXT NOT NULL DEFAULT (date('now'))
);

CREATE TABLE IF NOT EXISTS staff (
    staff_id TEXT PRIMARY KEY,
    full_name TEXT NOT NULL,
    position TEXT NOT NULL,
    phone TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    status TEXT NOT NULL DEFAULT 'Hoạt động',
    created_date TEXT NOT NULL DEFAULT (date('now'))
);

CREATE TABLE IF NOT EXISTS account (
    account_id TEXT PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role TEXT NOT NULL CHECK(role IN ('ADMIN', 'TECHNICIAN', 'ANALYST')),
    staff_id TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'Hoạt động',
    created_date TEXT NOT NULL DEFAULT (date('now')),
    FOREIGN KEY(staff_id) REFERENCES staff(staff_id)
);

CREATE TABLE IF NOT EXISTS traffic_monitoring (
    monitor_id TEXT PRIMARY KEY,
    segment_name TEXT NOT NULL,
    area_name TEXT NOT NULL,
    vehicle_count TEXT NOT NULL,
    avg_speed TEXT NOT NULL,
    congestion_level TEXT NOT NULL,
    recorded_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS analysis_report (
    report_id TEXT PRIMARY KEY,
    report_name TEXT NOT NULL,
    analysis_type TEXT NOT NULL,
    area_name TEXT NOT NULL,
    result_summary TEXT NOT NULL,
    analyst_name TEXT NOT NULL,
    created_date TEXT NOT NULL DEFAULT (date('now'))
);

CREATE TABLE IF NOT EXISTS system_log (
    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id TEXT,
    action_name TEXT NOT NULL,
    table_name TEXT NOT NULL,
    record_id TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);
