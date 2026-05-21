package com.ddangkhoa.trafficfx.view;

import com.ddangkhoa.trafficfx.MainApp;
import com.ddangkhoa.trafficfx.model.AppUser;
import com.ddangkhoa.trafficfx.controller.DashboardController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DashboardView {
    private final Stage stage;
    private final AppUser user;
    private final BorderPane root = new BorderPane();
    private final StackPane contentPane = new StackPane();
    private final Map<String, Button> menuButtons = new HashMap<>();
    private final DashboardController dashboardController = new DashboardController();
    private Button activeButton;

    public DashboardView(Stage stage, AppUser user) {
        this.stage = stage;
        this.user = user;
    }

    public Parent getView() {
        root.getStyleClass().add("management-root");
        root.setLeft(buildSidebar());
        root.setCenter(contentPane);
        openFirstPage();
        return root;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("blue-sidebar");
        sidebar.setPrefWidth(250);

        HBox profileBox = new HBox(16);
        profileBox.getStyleClass().add("profile-box");
        profileBox.setAlignment(Pos.CENTER_LEFT);

        StackPane avatarPane = buildAvatarPane();

        Label roleLabel = new Label(user.getRole().getDisplayName());
        roleLabel.getStyleClass().add("profile-role");
        profileBox.getChildren().addAll(avatarPane, roleLabel);

        VBox menuBox = new VBox(14);
        menuBox.getStyleClass().add("sidebar-menu");

        for (String feature : dashboardController.getFeatures(user)) {
            Button button = createMenuButton(feature, dashboardController.getIconFor(feature));
            button.setOnAction(event -> {
                setActiveButton(button);
                showFeature(feature);
            });
            menuButtons.put(feature, button);
            menuBox.getChildren().add(button);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button accountInfoButton = createMenuButton("Thông tin tài khoản", "👤");
        accountInfoButton.getStyleClass().add("account-info-menu-button");
        accountInfoButton.setOnAction(event -> {
            setActiveButton(accountInfoButton);
            showFeature("Thông tin tài khoản");
        });

        Button logoutButton = createMenuButton("Đăng xuất", "⏻");
        logoutButton.getStyleClass().add("logout-menu-button");
        logoutButton.setOnAction(event -> {
            LoginView loginView = new LoginView(stage);
            Scene scene = new Scene(loginView.getView(), MainApp.WIDTH, MainApp.HEIGHT);
            URL cssUrl = getClass().getResource("/css/app.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            stage.setScene(scene);
        });

        sidebar.getChildren().addAll(profileBox, menuBox, spacer, accountInfoButton, logoutButton);
        return sidebar;
    }

    private StackPane buildAvatarPane() {
        StackPane avatarPane = new StackPane();
        avatarPane.getStyleClass().add("avatar-wrapper");

        ImageView avatarImage = new ImageView(loadDefaultAvatar());
        avatarImage.setFitWidth(48);
        avatarImage.setFitHeight(48);
        avatarImage.setPreserveRatio(false);
        Circle clip = new Circle(24, 24, 24);
        avatarImage.setClip(clip);

        Label plusButton = new Label("+");
        plusButton.getStyleClass().add("avatar-upload-button");
        StackPane.setAlignment(plusButton, Pos.BOTTOM_RIGHT);

        plusButton.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Chọn ảnh đại diện");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                avatarImage.setImage(new Image(selectedFile.toURI().toString(), 48, 48, false, true));
            }
        });

        avatarPane.getChildren().addAll(avatarImage, plusButton);
        return avatarPane;
    }

    private Image loadDefaultAvatar() {
        URL avatarUrl = getClass().getResource("/images/default-avatar.png");
        if (avatarUrl == null) {
            return null;
        }
        return new Image(avatarUrl.toExternalForm(), 48, 48, false, true);
    }

    private Button createMenuButton(String text, String icon) {
        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("sidebar-icon");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("sidebar-text");

        content.getChildren().addAll(iconLabel, textLabel);

        Button button = new Button();
        button.setGraphic(content);
        button.getStyleClass().add("sidebar-menu-button");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private void openFirstPage() {
        String firstFeature = dashboardController.getFeatures(user).get(0);
        Button firstButton = menuButtons.get(firstFeature);
        setActiveButton(firstButton);
        showFeature(firstFeature);
    }

    private void showFeature(String feature) {
        contentPane.getChildren().setAll(dashboardController.createFeatureView(feature, user));
    }

    private void setActiveButton(Button selectedButton) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-menu-button-active");
            if (!activeButton.getStyleClass().contains("sidebar-menu-button")) {
                activeButton.getStyleClass().add("sidebar-menu-button");
            }
        }
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("sidebar-menu-button");
            if (!selectedButton.getStyleClass().contains("sidebar-menu-button-active")) {
                selectedButton.getStyleClass().add("sidebar-menu-button-active");
            }
            activeButton = selectedButton;
        }
    }
}
