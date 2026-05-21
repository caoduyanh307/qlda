package com.ddangkhoa.trafficfx.view;

import java.util.Optional;
import java.net.URL;

import com.ddangkhoa.trafficfx.MainApp;
import com.ddangkhoa.trafficfx.model.AppUser;
import com.ddangkhoa.trafficfx.controller.LoginController;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class LoginView {
    private final Stage stage;
    private final LoginController loginController = new LoginController();

    public LoginView(Stage stage) {
        this.stage = stage;
    }

    public Parent getView() {
        StackPane root = new StackPane();
        root.getStyleClass().add("login-root");

        HBox layout = new HBox();
        layout.getStyleClass().add("login-shell");
        layout.setMaxWidth(1180);
        layout.setMaxHeight(650);
        layout.setAlignment(Pos.CENTER);

        StackPane left = buildHeroPanel();
        VBox right = buildLoginPanel();

        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.NEVER);
        layout.getChildren().addAll(left, right);
        root.getChildren().add(layout);
        return root;
    }

    private StackPane buildHeroPanel() {
        StackPane left = new StackPane();
        left.getStyleClass().add("hero-panel-light");
        left.setPrefWidth(720);
        left.setMinWidth(620);

        VBox titleBox = new VBox(10);
        titleBox.getStyleClass().add("hero-center-box");
        titleBox.setMaxWidth(500);
        titleBox.setAlignment(Pos.CENTER);

        Label tag = new Label("SMART TRAFFIC MANAGEMENT");
        tag.getStyleClass().add("hero-center-tag");

        Label centerTitle = new Label("HỆ THỐNG\nQUẢN LÍ GIAO THÔNG\nTP.HCM");
        centerTitle.getStyleClass().add("hero-center-title");
        centerTitle.setAlignment(Pos.CENTER);
        centerTitle.setTextAlignment(TextAlignment.CENTER);
        centerTitle.setWrapText(true);

        
        titleBox.getChildren().addAll(tag, centerTitle);
        StackPane.setAlignment(titleBox, Pos.CENTER);
        left.getChildren().add(titleBox);
        return left;
    }

    private VBox buildLoginPanel() {
        VBox wrapper = new VBox();
        wrapper.getStyleClass().add("login-side");
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefWidth(460);
        wrapper.setMinWidth(430);

        VBox card = new VBox(18);
        card.getStyleClass().add("login-card");
        card.setPadding(new Insets(34));
        card.setMaxWidth(390);

        Label title = new Label("Đăng nhập hệ thống");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label("Nhập tài khoản để hệ thống tự động mở giao diện đúng với quyền được phân công.");
        subtitle.getStyleClass().add("login-subtitle");
        subtitle.setWrapText(true);

        TextField username = new TextField();
        username.setPromptText("Tên đăng nhập");
        username.getStyleClass().add("login-input");

        PasswordField password = new PasswordField();
        password.setPromptText("Mật khẩu");
        password.getStyleClass().add("login-input");

        Hyperlink forgotPassword = new Hyperlink("Quên mật khẩu?");
        forgotPassword.getStyleClass().add("forgot-password-link");
        HBox forgotBox = new HBox(forgotPassword);
        forgotBox.setAlignment(Pos.CENTER_RIGHT);
        forgotPassword.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Quên mật khẩu");
            alert.setHeaderText("Khôi phục mật khẩu");
            alert.setContentText("Vui lòng liên hệ quản trị viên hệ thống để được cấp lại mật khẩu.");
            alert.showAndWait();
        });

        Label error = new Label();
        error.getStyleClass().add("login-error");
        error.setVisible(false);
        error.setManaged(false);

        Button loginButton = new Button("Đăng nhập");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        Runnable loginAction = () -> {
            Optional<AppUser> user = loginController.authenticate(username.getText(), password.getText());
            if (user.isPresent()) {
                DashboardView dashboardView = new DashboardView(stage, user.get());
                Scene scene = new Scene(dashboardView.getView(), MainApp.WIDTH, MainApp.HEIGHT);
                URL cssUrl = getClass().getResource("/css/app.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
                stage.setScene(scene);
            } else {
                error.setText("Sai tên đăng nhập hoặc mật khẩu.");
                error.setVisible(true);
                error.setManaged(true);
            }
        };

        loginButton.setOnAction(e -> loginAction.run());
        username.setOnAction(e -> loginAction.run());
        password.setOnAction(e -> loginAction.run());

        card.getChildren().addAll(title, subtitle, username, password, forgotBox, error, loginButton);
        wrapper.getChildren().add(card);
        return wrapper;
    }
}
