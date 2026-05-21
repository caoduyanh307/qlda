package com.ddangkhoa.trafficfx.dao;

import com.ddangkhoa.trafficfx.database.DatabaseManager;
import com.ddangkhoa.trafficfx.model.AccountInfo;
import com.ddangkhoa.trafficfx.model.AppUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class AccountInfoDao {
    public AccountInfo findByUsername(AppUser user) {
        String sql = """
                SELECT a.account_id, a.full_name, a.username, a.email, a.status AS account_status,
                       a.created_at, a.updated_at,
                       ar.account_role_id, ar.role_name, ar.assigned_at, ar.is_active,
                       e.employee_id, e.phonenumber, e.dateofbirth, e.gender, e.address, e.salary,
                       e.status AS employee_status
                FROM account a
                JOIN account_role ar ON a.account_id = ar.account_id AND ar.is_delete = 0
                LEFT JOIN employee e ON a.account_id = e.account_id AND e.is_deleted = 0
                WHERE a.username = ?
                ORDER BY ar.is_active DESC
                LIMIT 1
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new AccountInfo(
                            safe(rs.getString("account_id")),
                            safe(rs.getString("full_name")),
                            safe(rs.getString("username")),
                            safe(rs.getString("email")),
                            safe(rs.getString("role_name")),
                            safe(rs.getString("account_status")),
                            safe(rs.getString("created_at")),
                            safe(rs.getString("updated_at")),
                            safe(rs.getString("account_role_id")),
                            safe(rs.getString("assigned_at")),
                            "1".equals(rs.getString("is_active")) ? "Đang hiệu lực" : "Hết hiệu lực",
                            safe(rs.getString("employee_id")),
                            safe(rs.getString("phonenumber")),
                            safe(rs.getString("dateofbirth")),
                            safe(rs.getString("gender")),
                            safe(rs.getString("address")),
                            safe(rs.getString("salary")),
                            safe(rs.getString("employee_status"))
                    );
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Không thể tải thông tin tài khoản", exception);
        }
        return new AccountInfo("", user.getFullName(), user.getUsername(), "", user.getRole().name(), "ACTIVE", "", "", "", "", "", "", "", "", "", "", "", "");
    }

    public void updateProfile(AccountInfo info, String fullName, String email, String phone, String address) {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement accountStatement = connection.prepareStatement("""
                    UPDATE account
                    SET full_name = ?, email = ?, updated_at = ?
                    WHERE account_id = ?
                    """)) {
                accountStatement.setString(1, fullName);
                accountStatement.setString(2, email);
                accountStatement.setString(3, LocalDate.now().toString());
                accountStatement.setString(4, info.getAccountId());
                accountStatement.executeUpdate();
            }
            if (info.getEmployeeId() != null && !info.getEmployeeId().isBlank()) {
                try (PreparedStatement employeeStatement = connection.prepareStatement("""
                        UPDATE employee
                        SET fullname = ?, email = ?, phonenumber = ?, address = ?
                        WHERE employee_id = ?
                        """)) {
                    employeeStatement.setString(1, fullName);
                    employeeStatement.setString(2, email);
                    employeeStatement.setString(3, phone);
                    employeeStatement.setString(4, address);
                    employeeStatement.setString(5, info.getEmployeeId());
                    employeeStatement.executeUpdate();
                }
            }
            connection.commit();
        } catch (SQLException exception) {
            throw new RuntimeException("Không thể cập nhật thông tin tài khoản. Email có thể đã tồn tại.", exception);
        }
    }

    public boolean isCurrentPasswordCorrect(String username, String oldPassword) {
        String sql = "SELECT 1 FROM account WHERE username = ? AND password = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, oldPassword);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Không thể kiểm tra mật khẩu hiện tại", exception);
        }
    }

    public void updatePassword(String username, String newPassword) {
        String sql = "UPDATE account SET password = ?, updated_at = ? WHERE username = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newPassword);
            statement.setString(2, LocalDate.now().toString());
            statement.setString(3, username);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Không thể đổi mật khẩu", exception);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
