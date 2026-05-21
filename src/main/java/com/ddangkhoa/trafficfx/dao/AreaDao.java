package com.ddangkhoa.trafficfx.dao;

import com.ddangkhoa.trafficfx.database.DatabaseManager;
import com.ddangkhoa.trafficfx.model.Area;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AreaDao {
    public List<Area> findAll() {
        String sql = "SELECT area_id, area_name, area_type, status, created_date FROM area ORDER BY area_id";
        List<Area> areas = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                areas.add(mapResultSet(resultSet));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Cannot load areas", exception);
        }
        return areas;
    }

    public List<Area> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String sql = """
                SELECT area_id, area_name, area_type, status, created_date
                FROM area
                WHERE lower(area_id) LIKE ?
                   OR lower(area_name) LIKE ?
                   OR lower(area_type) LIKE ?
                   OR lower(status) LIKE ?
                ORDER BY area_id
                """;
        String value = "%" + keyword.trim().toLowerCase() + "%";
        List<Area> areas = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) {
                statement.setString(i, value);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    areas.add(mapResultSet(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Cannot search areas", exception);
        }
        return areas;
    }

    public void insert(Area area) {
        String sql = "INSERT INTO area(area_id, area_name, area_type, status, created_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, area.getAreaId());
            statement.setString(2, area.getAreaName());
            statement.setString(3, area.getAreaType());
            statement.setString(4, area.getStatus());
            statement.setString(5, area.getCreatedDate());
            statement.executeUpdate();
            writeLog(connection, "INSERT", area.getAreaId());
        } catch (SQLException exception) {
            throw new RuntimeException("Cannot insert area", exception);
        }
    }

    public void update(Area area) {
        String sql = "UPDATE area SET area_name = ?, area_type = ?, status = ?, created_date = ? WHERE area_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, area.getAreaName());
            statement.setString(2, area.getAreaType());
            statement.setString(3, area.getStatus());
            statement.setString(4, area.getCreatedDate());
            statement.setString(5, area.getAreaId());
            statement.executeUpdate();
            writeLog(connection, "UPDATE", area.getAreaId());
        } catch (SQLException exception) {
            throw new RuntimeException("Cannot update area", exception);
        }
    }

    public void deleteById(String areaId) {
        String sql = "UPDATE area SET is_deleted = 1 WHERE area_id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, areaId);
            statement.executeUpdate();
            writeLog(connection, "SOFT_DELETE", areaId);
        } catch (SQLException exception) {
            throw new RuntimeException("Cannot soft delete area", exception);
        }
    }

    private Area mapResultSet(ResultSet resultSet) throws SQLException {
        return new Area(
                resultSet.getString("area_id"),
                resultSet.getString("area_name"),
                resultSet.getString("area_type"),
                resultSet.getString("status"),
                resultSet.getString("created_date")
        );
    }

    private void writeLog(Connection connection, String actionName, String recordId) throws SQLException {
        String sql = "INSERT INTO system_log(account_id, action_name, table_name, record_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "tech");
            statement.setString(2, actionName);
            statement.setString(3, "area");
            statement.setString(4, recordId);
            statement.executeUpdate();
        }
    }
}
