package com.ddangkhoa.trafficfx;

import com.ddangkhoa.trafficfx.database.DatabaseManager;
import com.ddangkhoa.trafficfx.view.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class MainApp extends Application {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 760;

    @Override
    public void start(Stage stage) {
        DatabaseManager.initializeDatabase();
        LoginView loginView = new LoginView(stage);
        Scene scene = new Scene(loginView.getView(), WIDTH, HEIGHT);
        URL cssUrl = getClass().getResource("/css/app.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setTitle("Quản lý và phân tích lưu lượng giao thông TP.HCM");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
