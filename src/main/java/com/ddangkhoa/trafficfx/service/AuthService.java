package com.ddangkhoa.trafficfx.service;

import com.ddangkhoa.trafficfx.database.DatabaseManager;
import com.ddangkhoa.trafficfx.model.AppUser;
import com.ddangkhoa.trafficfx.model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
    public Optional<AppUser> login(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        String sql = """
                SELECT a.username, a.full_name, ar.role_name
                FROM account a
                JOIN account_role ar ON a.account_id = ar.account_id
                WHERE a.username = ?
                  AND a.password = ?
                  AND a.status = 'ACTIVE'
                  AND a.is_delete = 0
                  AND ar.is_active = 1
                  AND ar.is_delete = 0
                """;
        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            statement.setString(2, password.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new AppUser(
                            resultSet.getString("username"),
                            resultSet.getString("full_name"),
                            Role.valueOf(resultSet.getString("role_name"))
                    ));
                }
            }
        } catch (SQLException | IllegalArgumentException exception) {
            return Optional.empty();
        }
        return Optional.empty();
    }
}
