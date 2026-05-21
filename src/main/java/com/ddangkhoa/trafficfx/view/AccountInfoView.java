package com.ddangkhoa.trafficfx.view;

import com.ddangkhoa.trafficfx.controller.AccountInfoController;
import com.ddangkhoa.trafficfx.model.AccountInfo;
import com.ddangkhoa.trafficfx.model.AppUser;

import java.util.Optional;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AccountInfoView {
    private final AppUser user;
    private final AccountInfoController controller = new AccountInfoController();
    private VBox root;

    public AccountInfoView(AppUser user) {
        this.user = user;
    }

    public Parent getView() {
        root = new VBox(22);
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(28, 38, 28, 40));
        renderContent();
        return root;
    }

    private void renderContent() {
        AccountInfo info = controller.loadAccountInfo(user);
        root.getChildren().clear();

        Label title = new Label("Thông tin tài khoản");
        title.getStyleClass().add("window-title");

        HBox profileHeader = new HBox(18);
        profileHeader.getStyleClass().add("account-profile-card");
        profileHeader.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(initials(info.getFullName()));
        avatar.getStyleClass().add("account-profile-avatar");

        VBox headerText = new VBox(6);
        Label fullName = new Label(info.getFullName());
        fullName.getStyleClass().add("account-profile-name");
        Label username = new Label("@" + info.getUsername());
        username.getStyleClass().add("muted-text");
        Label roleBadge = new Label(displayRole(info.getRole()) + "  •  " + displayStatus(info.getAccountStatus()));
        roleBadge.getStyleClass().addAll("cell-badge", "highlight-info");
        headerText.getChildren().addAll(fullName, username, roleBadge);
        profileHeader.getChildren().addAll(avatar, headerText);

        HBox content = new HBox(18);
        content.getChildren().addAll(
                infoCard("Hồ sơ tài khoản", new String[][]{
                        {"Mã tài khoản", info.getAccountId()},
                        {"Họ tên", info.getFullName()},
                        {"Email", info.getEmail()},
                        {"Tên đăng nhập", info.getUsername()},
                        {"Trạng thái", displayStatus(info.getAccountStatus())},
                        {"Ngày tạo", info.getCreatedAt()},
                        {"Ngày cập nhật", info.getUpdatedAt()}
                }),
                infoCard("Phân quyền", new String[][]{
                        {"Mã phân quyền", info.getAccountRoleId()},
                        {"Vai trò", displayRole(info.getRole())},
                        {"Ngày gán quyền", info.getAssignedAt()},
                        {"Trạng thái quyền", info.getRoleActive()}
                }),
                infoCard("Thông tin nhân viên", new String[][]{
                        {"Mã nhân viên", info.getEmployeeId()},
                        {"Số điện thoại", info.getPhone()},
                        {"Ngày sinh", info.getDateOfBirth()},
                        {"Giới tính", info.getGender()},
                        {"Địa chỉ", info.getAddress()},
                        {"Lương", info.getSalary()},
                        {"Trạng thái", info.getEmployeeStatus()}
                })
        );

        HBox actions = new HBox(14);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button updateButton = new Button("✎ Cập nhật thông tin");
        updateButton.getStyleClass().add("update-button");
        Button passwordButton = new Button("🔒 Đổi mật khẩu");
        passwordButton.getStyleClass().add("insert-button");
        updateButton.setOnAction(event -> openUpdateProfileDialog(info));
        passwordButton.setOnAction(event -> openChangePasswordDialog(info));
        actions.getChildren().addAll(updateButton, passwordButton);

        root.getChildren().addAll(title, profileHeader, content, actions);
    }

    private VBox infoCard(String title, String[][] rows) {
        VBox card = new VBox(16);
        card.getStyleClass().add("account-info-card");
        card.setPrefWidth(300);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title-small");

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(13);
        for (int i = 0; i < rows.length; i++) {
            Label key = new Label(rows[i][0]);
            key.getStyleClass().add("account-info-key");
            Label value = new Label(rows[i][1] == null || rows[i][1].isBlank() ? "Chưa có dữ liệu" : rows[i][1]);
            value.getStyleClass().add("account-info-value");
            grid.addRow(i, key, value);
        }
        card.getChildren().addAll(titleLabel, grid);
        return card;
    }

    private void openUpdateProfileDialog(AccountInfo info) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật thông tin tài khoản");
        dialog.setHeaderText("Chỉnh sửa thông tin cá nhân");

        ButtonType saveButton = new ButtonType("Lưu cập nhật", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        TextField fullNameField = new TextField(info.getFullName());
        TextField emailField = new TextField(info.getEmail());
        TextField phoneField = new TextField(info.getPhone());
        TextField addressField = new TextField(info.getAddress());

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Họ tên"), fullNameField);
        grid.addRow(1, new Label("Email"), emailField);
        grid.addRow(2, new Label("Số điện thoại"), phoneField);
        grid.addRow(3, new Label("Địa chỉ"), addressField);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> button == saveButton);

        Optional<Boolean> result = dialog.showAndWait();
        if (result.orElse(false)) {
            if (fullNameField.getText().isBlank() || emailField.getText().isBlank() || phoneField.getText().isBlank()) {
                showError("Thiếu thông tin", "Vui lòng nhập đầy đủ họ tên, email và số điện thoại.");
                return;
            }
            try {
                controller.updateProfile(info, fullNameField.getText().trim(), emailField.getText().trim(), phoneField.getText().trim(), addressField.getText().trim());
                showInfo("Cập nhật thành công", "Thông tin tài khoản đã được cập nhật.");
                renderContent();
            } catch (RuntimeException exception) {
                showError("Cập nhật thất bại", exception.getMessage());
            }
        }
    }

    private void openChangePasswordDialog(AccountInfo info) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Đổi mật khẩu");
        dialog.setHeaderText("Đổi mật khẩu tài khoản hiện tại");

        ButtonType saveButton = new ButtonType("Đổi mật khẩu", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Nhập mật khẩu hiện tại");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nhập mật khẩu mới");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Nhập lại mật khẩu mới");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Mật khẩu hiện tại"), oldPasswordField);
        grid.addRow(1, new Label("Mật khẩu mới"), newPasswordField);
        grid.addRow(2, new Label("Nhập lại mật khẩu"), confirmPasswordField);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> button == saveButton);

        Optional<Boolean> result = dialog.showAndWait();
        if (result.orElse(false)) {
            String oldPassword = oldPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                showError("Thiếu thông tin", "Vui lòng nhập đầy đủ mật khẩu hiện tại và mật khẩu mới.");
                return;
            }
            if (newPassword.length() < 6) {
                showError("Mật khẩu chưa hợp lệ", "Mật khẩu mới phải có ít nhất 6 ký tự.");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                showError("Mật khẩu không khớp", "Mật khẩu mới và phần nhập lại không giống nhau.");
                return;
            }
            try {
                boolean changed = controller.changePassword(info.getUsername(), oldPassword, newPassword);
                if (changed) {
                    showInfo("Đổi mật khẩu thành công", "Mật khẩu đã được cập nhật. Lần đăng nhập sau hãy dùng mật khẩu mới.");
                } else {
                    showError("Đổi mật khẩu thất bại", "Mật khẩu hiện tại không đúng.");
                }
            } catch (RuntimeException exception) {
                showError("Đổi mật khẩu thất bại", exception.getMessage());
            }
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String displayRole(String role) {
        return switch (role) {
            case "ADMIN" -> "Quản trị viên";
            case "TECHNICIAN" -> "Kỹ thuật viên";
            case "ANALYST" -> "Phân tích viên";
            default -> role;
        };
    }

    private String displayStatus(String status) {
        return switch (status) {
            case "ACTIVE" -> "Hoạt động";
            case "LOCKED" -> "Bị khóa";
            case "INACTIVE" -> "Ngưng";
            default -> status;
        };
    }
}
