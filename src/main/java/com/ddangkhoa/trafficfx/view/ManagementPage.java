package com.ddangkhoa.trafficfx.view;

import com.ddangkhoa.trafficfx.model.AppUser;
import com.ddangkhoa.trafficfx.model.DemoRecord;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ManagementPage {
    private final String title;
    private final AppUser user;
    private final ObservableList<DemoRecord> records = FXCollections.observableArrayList();

    public ManagementPage(String title, AppUser user) {
        this.title = title;
        this.user = user;
        seedData();
    }

    public Parent getView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(32));

        Label pageTitle = new Label(title);
        pageTitle.getStyleClass().add("page-title");

        Label desc = new Label("Màn hình mô phỏng chức năng “" + title + "” theo quyền của " + user.getRole().getDisplayName() + ". Các thao tác minh họa gồm thêm, cập nhật, xóa mềm và tra cứu dữ liệu.");
        desc.getStyleClass().add("page-desc");
        desc.setWrapText(true);

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField search = new TextField();
        search.setPromptText("Nhập từ khóa tra cứu...");
        search.getStyleClass().add("search-input");
        search.setPrefWidth(300);

        Button add = new Button("Thêm");
        add.getStyleClass().add("action-button");
        Button update = new Button("Cập nhật");
        update.getStyleClass().add("action-button");
        Button softDelete = new Button("Xóa mềm");
        softDelete.getStyleClass().add("danger-button");

        toolbar.getChildren().addAll(search, add, update, softDelete);

        TableView<DemoRecord> table = new TableView<>(records);
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DemoRecord, String> idCol = new TableColumn<>("Mã");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<DemoRecord, String> nameCol = new TableColumn<>("Tên dữ liệu");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<DemoRecord, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<DemoRecord, String> noteCol = new TableColumn<>("Ghi chú");
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));

        table.getColumns().addAll(idCol, nameCol, statusCol, noteCol);
        table.setPrefHeight(380);

        add.setOnAction(e -> showMessage("Thêm dữ liệu", "Mô phỏng mở form thêm mới cho chức năng: " + title));
        update.setOnAction(e -> showMessage("Cập nhật dữ liệu", "Mô phỏng mở form cập nhật bản ghi được chọn."));
        softDelete.setOnAction(e -> showMessage("Xóa mềm", "Mô phỏng cập nhật IS_DELETED = 1, không xóa vật lý khỏi cơ sở dữ liệu."));

        search.textProperty().addListener((obs, oldValue, newValue) -> filterData(newValue, table));

        root.getChildren().addAll(pageTitle, desc, toolbar, table);
        return root;
    }

    private void seedData() {
        records.addAll(
                new DemoRecord("R001", title + " 01", "ACTIVE", "Dữ liệu mô phỏng"),
                new DemoRecord("R002", title + " 02", "INACTIVE", "Chờ cập nhật"),
                new DemoRecord("R003", title + " 03", "LOCKED", "Bị khóa nghiệp vụ"),
                new DemoRecord("R004", title + " 04", "ACTIVE", "Đang sử dụng")
        );
    }

    private void filterData(String keyword, TableView<DemoRecord> table) {
        if (keyword == null || keyword.isBlank()) {
            table.setItems(records);
            return;
        }
        String lower = keyword.toLowerCase();
        ObservableList<DemoRecord> filtered = records.filtered(record ->
                record.getId().toLowerCase().contains(lower)
                        || record.getName().toLowerCase().contains(lower)
                        || record.getStatus().toLowerCase().contains(lower)
                        || record.getNote().toLowerCase().contains(lower)
        );
        table.setItems(filtered);
    }

    private void showMessage(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
