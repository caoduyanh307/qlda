package com.ddangkhoa.trafficfx.dao;

import com.ddangkhoa.trafficfx.database.DatabaseManager;
import com.ddangkhoa.trafficfx.model.DataRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class GenericCrudDao {
    private final String tableName;
    private final String primaryKeyColumn;
    private final List<String> columns;
    private final String username;

    public GenericCrudDao(String tableName, String primaryKeyColumn, List<String> columns, String username) {
        this.tableName = tableName;
        this.primaryKeyColumn = primaryKeyColumn;
        this.columns = columns;
        this.username = username;
    }

    public String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public List<DataRecord> findAll() {
        String sql = "SELECT " + String.join(", ", columns) + " FROM " + tableName + activeOnlyClause() + " ORDER BY " + primaryKeyColumn;
        return queryRecords(sql);
    }

    public List<DataRecord> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        StringJoiner where = new StringJoiner(" OR ");
        for (String column : columns) {
            where.add("lower(CAST(" + column + " AS TEXT)) LIKE ?");
        }
        String sql = "SELECT " + String.join(", ", columns) + " FROM " + tableName + " WHERE " + activeOnlyConditionPrefix() + "(" + where + ") ORDER BY " + primaryKeyColumn;
        List<DataRecord> records = new ArrayList<>();
        String value = "%" + keyword.trim().toLowerCase() + "%";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= columns.size(); i++) {
                statement.setString(i, value);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapResultSet(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Cannot search " + tableName, exception);
        }
        return records;
    }

    public void insert(DataRecord record) {
        String placeholders = String.join(", ", columns.stream().map(column -> "?").toList());
        String sql = "INSERT INTO " + tableName + "(" + String.join(", ", columns) + ") VALUES (" + placeholders + ")";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < columns.size(); i++) {
                statement.setString(i + 1, record.get(columns.get(i)));
            }
            statement.executeUpdate();
            writeLog(connection, "Thêm dữ liệu", record.get(primaryKeyColumn), "", record.getValues().toString());
        } catch (SQLException exception) {
            throw new RuntimeException("Cannot insert into " + tableName, exception);
        }
    }

    public void update(DataRecord record) {
        List<String> updateColumns = columns.stream().filter(column -> !column.equals(primaryKeyColumn)).toList();
        StringJoiner setClause = new StringJoiner(", ");
        for (String column : updateColumns) {
            setClause.add(column + " = ?");
        }
        String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + primaryKeyColumn + " = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            for (String column : updateColumns) {
                statement.setString(index++, record.get(column));
            }
            statement.setString(index, record.get(primaryKeyColumn));
            statement.executeUpdate();
            writeLog(connection, "Cập nhật dữ liệu", record.get(primaryKeyColumn), "", record.getValues().toString());
        } catch (SQLException exception) {
            throw new RuntimeException("Cannot update " + tableName, exception);
        }
    }

    public void deleteById(String id) {
        String softDeleteColumn = softDeleteColumn();
        if (softDeleteColumn == null) {
            throw new RuntimeException("Bảng " + tableName + " không có cột IS_DELETED/IS_DELETE nên không thể xóa mềm. Hệ thống đã chặn xóa cứng để bảo toàn dữ liệu.");
        }
        String sql = "UPDATE " + tableName + " SET " + softDeleteColumn + " = 1 WHERE " + primaryKeyColumn + " = ?";
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            int affected = statement.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Không tìm thấy dữ liệu cần xóa mềm: " + id);
            }
            writeLog(connection, "Xóa mềm dữ liệu", id, softDeleteColumn + "=0", softDeleteColumn + "=1");
        } catch (SQLException exception) {
            throw new RuntimeException("Cannot soft delete from " + tableName, exception);
        }
    }

    private String softDeleteColumn() {
        if (columns.contains("is_deleted")) {
            return "is_deleted";
        }
        if (columns.contains("is_delete")) {
            return "is_delete";
        }
        return null;
    }

    private String activeOnlyClause() {
        String softDeleteColumn = softDeleteColumn();
        return softDeleteColumn == null ? " " : " WHERE COALESCE(" + softDeleteColumn + ", 0) = 0 ";
    }

    private String activeOnlyConditionPrefix() {
        String softDeleteColumn = softDeleteColumn();
        return softDeleteColumn == null ? "" : "COALESCE(" + softDeleteColumn + ", 0) = 0 AND ";
    }

    public String generateNextId(String prefix, int digits) {
        String sql = "SELECT " + primaryKeyColumn + " FROM " + tableName + " WHERE " + primaryKeyColumn + " LIKE ?";
        int maxNumber = 0;
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, prefix + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String id = resultSet.getString(1);
                    if (id != null && id.matches(prefix + "\\d+")) {
                        maxNumber = Math.max(maxNumber, Integer.parseInt(id.substring(prefix.length())));
                    }
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Cannot generate id for " + tableName, exception);
        }
        return prefix + String.format("%0" + digits + "d", maxNumber + 1);
    }

    private List<DataRecord> queryRecords(String sql) {
        List<DataRecord> records = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                records.add(mapResultSet(resultSet));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Cannot load " + tableName, exception);
        }
        return records;
    }

    private DataRecord mapResultSet(ResultSet resultSet) throws SQLException {
        DataRecord record = new DataRecord();
        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i);
            String value = resultSet.getString(i);
            record.put(columnName, value == null ? "" : value);
        }
        return record;
    }

    private void writeLog(Connection connection, String behaviour, String targetId, String oldValue, String newValue) throws SQLException {
        if ("system_log".equalsIgnoreCase(tableName)) {
            return;
        }
        String accountId = findAccountId(connection);
        String logId = nextLogId(connection);
        String sql = "INSERT INTO system_log(log_id, account_id, behaviour, target_table, target_id, old_value, new_value, log_status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, 'SUCCESS', ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, logId);
            statement.setString(2, accountId);
            statement.setString(3, behaviour);
            statement.setString(4, tableName.toUpperCase());
            statement.setString(5, targetId == null || targetId.isBlank() ? "N/A" : targetId);
            statement.setString(6, oldValue == null ? "" : oldValue);
            statement.setString(7, newValue == null ? "" : newValue);
            statement.setString(8, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            statement.executeUpdate();
        }
    }

    private String findAccountId(Connection connection) throws SQLException {
        String sql = "SELECT account_id FROM account WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return null;
    }

    private String nextLogId(Connection connection) throws SQLException {
        int maxNumber = 0;
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery("SELECT log_id FROM system_log WHERE log_id LIKE 'LG%'")) {
            while (rs.next()) {
                String id = rs.getString(1);
                if (id != null && id.matches("LG\\d+")) {
                    maxNumber = Math.max(maxNumber, Integer.parseInt(id.substring(2)));
                }
            }
        }
        return "LG" + String.format("%03d", maxNumber + 1);
    }
}
