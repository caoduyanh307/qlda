package com.ddangkhoa.trafficfx.view.management;

import com.ddangkhoa.trafficfx.controller.AreaManagementController;
import com.ddangkhoa.trafficfx.model.Area;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ddangkhoa.trafficfx.model.DataRecord;
import com.ddangkhoa.trafficfx.util.DataImportExportUtil;

public class AreaManagementView {
    private final AreaManagementController controller = new AreaManagementController();
    private final ObservableList<Area> areaList = FXCollections.observableArrayList();
    private final TableView<Area> areaTable = new TableView<>();
    private final Label totalLabel = new Label("0");
    private final TextField searchField = new TextField();

    public Parent getView() {
        VBox root = new VBox(22);
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(28, 38, 28, 40));

        searchField.getStyleClass().add("wide-search-field");
        searchField.setPromptText("Tìm kiếm khu vực...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadAreas(newValue));

        HBox summaryAndActions = new HBox(18);
        summaryAndActions.setAlignment(Pos.BOTTOM_LEFT);

        VBox statisticCard = new VBox(8);
        statisticCard.getStyleClass().add("statistic-card");
        statisticCard.setPrefSize(230, 106);
        Label statisticTitle = new Label("Tổng khu vực");
        statisticTitle.getStyleClass().add("statistic-title");
        totalLabel.getStyleClass().add("statistic-value");
        statisticCard.getChildren().addAll(statisticTitle, totalLabel);

        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, javafx.scene.layout.Priority.ALWAYS);

        Button insertButton = new Button("＋ Insert");
        insertButton.getStyleClass().add("insert-button");
        insertButton.setOnAction(event -> openInsertDialog());

        Button deleteButton = new Button("🗑 Delete");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(event -> deleteSelectedArea());

        Button updateButton = new Button("✎ Update");
        updateButton.getStyleClass().add("update-button");
        updateButton.setOnAction(event -> openUpdateDialog());

        MenuButton moreButton = new MenuButton("☰");
        moreButton.getStyleClass().add("more-menu-button");
        insertButton.setPrefSize(132, 46);
        deleteButton.setPrefSize(132, 46);
        updateButton.setPrefSize(132, 46);
        moreButton.setPrefSize(132, 46);
        MenuItem importItem = new MenuItem("↥ Import Excel/CSV");
        MenuItem exportItem = new MenuItem("↧ Export Excel/CSV");
        importItem.setOnAction(event -> importAreas());
        exportItem.setOnAction(event -> exportAreas());
        moreButton.getItems().addAll(importItem, exportItem);

        HBox actionBox = new HBox(14, insertButton, deleteButton, updateButton, moreButton);
        actionBox.setAlignment(Pos.BOTTOM_RIGHT);
        summaryAndActions.getChildren().addAll(statisticCard, actionSpacer, actionBox);

        Label tableTitle = new Label("Danh Sách Khu Vực");
        tableTitle.getStyleClass().add("section-title");

        configureAreaTable();
        loadAreas(null);

        root.getChildren().addAll(searchField, summaryAndActions, tableTitle, areaTable);
        return root;
    }

    private void configureAreaTable() {
        areaTable.getStyleClass().add("area-table");
        areaTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        areaTable.setPrefHeight(390);
        areaTable.setItems(areaList);

        TableColumn<Area, String> idColumn = createColumn("Mã Khu Vực", Area::getAreaId);
        TableColumn<Area, String> nameColumn = createColumn("Tên Khu Vực", Area::getAreaName);
        TableColumn<Area, String> typeColumn = createColumn("Loại Khu Vực", Area::getAreaType);
        typeColumn.setCellFactory(column -> badgeCell("highlight-info"));
        TableColumn<Area, String> statusColumn = createColumn("Trạng Thái", Area::getStatus);
        statusColumn.setCellFactory(column -> statusBadgeCell());
        TableColumn<Area, String> createdDateColumn = createColumn("Ngày Tạo", Area::getCreatedDate);
        areaTable.getColumns().setAll(idColumn, nameColumn, typeColumn, statusColumn, createdDateColumn);
        areaTable.setRowFactory(table -> {
            TableRow<Area> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openUpdateDialog();
                }
            });
            return row;
        });
    }

    private TableColumn<Area, String> createColumn(String title, java.util.function.Function<Area, String> mapper) {
        TableColumn<Area, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> new SimpleStringProperty(mapper.apply(cellData.getValue())));
        return column;
    }

    private void loadAreas(String keyword) {
        areaList.setAll(controller.searchAreas(keyword));
        totalLabel.setText(String.valueOf(areaList.size()));
    }
    private TableCell<Area, String> badgeCell(String styleClass) {
        return new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                badge.setText(item);
                badge.getStyleClass().setAll("cell-badge", styleClass);
                setGraphic(badge);
                setText(null);
            }
        };
    }

    private TableCell<Area, String> statusBadgeCell() {
        return new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                String style = item.toLowerCase().contains("ngừng") || item.toLowerCase().contains("khóa")
                        ? "highlight-danger"
                        : "highlight-success";
                badge.setText(item);
                badge.getStyleClass().setAll("cell-badge", style);
                setGraphic(badge);
                setText(null);
            }
        };
    }


    private void openInsertDialog() {
        Area area = new Area(generateNextAreaId(), "", "Phường", "Hoạt động", LocalDate.now().toString());
        Optional<Area> result = showAreaDialog("Insert Area", area, false);
        result.ifPresent(value -> {
            try {
                controller.insertArea(value);
                loadAreas(searchField.getText());
            } catch (RuntimeException exception) {
                showError("Không thể thêm khu vực", exception.getMessage());
            }
        });
    }

    private void openUpdateDialog() {
        Area selectedArea = areaTable.getSelectionModel().getSelectedItem();
        if (selectedArea == null) {
            showWarning("Vui lòng chọn một khu vực trước khi cập nhật.");
            return;
        }
        Area editableArea = new Area(selectedArea.getAreaId(), selectedArea.getAreaName(), selectedArea.getAreaType(), selectedArea.getStatus(), selectedArea.getCreatedDate());
        Optional<Area> result = showAreaDialog("Update Area", editableArea, true);
        result.ifPresent(value -> {
            try {
                controller.updateArea(value);
                loadAreas(searchField.getText());
            } catch (RuntimeException exception) {
                showError("Không thể cập nhật khu vực", exception.getMessage());
            }
        });
    }

    private void deleteSelectedArea() {
        Area selectedArea = areaTable.getSelectionModel().getSelectedItem();
        if (selectedArea == null) {
            showWarning("Vui lòng chọn một khu vực trước khi xóa.");
            return;
        }
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xóa mềm khu vực");
        confirmDialog.setHeaderText("Xóa mềm khu vực " + selectedArea.getAreaId() + "?");
        confirmDialog.setContentText("Hệ thống chỉ cập nhật trạng thái xóa mềm, không xóa cứng khỏi database.");
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                controller.deleteArea(selectedArea.getAreaId());
                loadAreas(searchField.getText());
            } catch (RuntimeException exception) {
                showError("Không thể xóa khu vực", exception.getMessage());
            }
        }
    }

    private void importAreas() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import dữ liệu khu vực");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel hoặc CSV", "*.xlsx", "*.csv"),
                new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"),
                new FileChooser.ExtensionFilter("CSV UTF-8", "*.csv")
        );
        File file = fileChooser.showOpenDialog(areaTable.getScene().getWindow());
        if (file == null) return;
        try {
            List<DataRecord> records = DataImportExportUtil.readRecords(file.toPath(), areaColumns(), areaTitles());
            int success = 0;
            for (DataRecord record : records) {
                String id = record.get("area_id").isBlank() ? controller.generateNextAreaId() : record.get("area_id");
                controller.insertArea(new Area(
                        id,
                        record.get("area_name"),
                        record.get("area_type").isBlank() ? "Phường" : record.get("area_type"),
                        record.get("status").isBlank() ? "Hoạt động" : record.get("status"),
                        record.get("created_date").isBlank() ? LocalDate.now().toString() : record.get("created_date")
                ));
                success++;
            }
            loadAreas(searchField.getText());
            showInfo("Import thành công", "Đã import " + success + " dòng dữ liệu từ file: " + file.getName());
        } catch (Exception exception) {
            showError("Không thể import dữ liệu khu vực", exception.getMessage());
        }
    }

    private void exportAreas() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export dữ liệu khu vực");
        fileChooser.setInitialFileName("quan_ly_khu_vuc.xlsx");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"),
                new FileChooser.ExtensionFilter("CSV UTF-8", "*.csv")
        );
        File file = fileChooser.showSaveDialog(areaTable.getScene().getWindow());
        if (file == null) return;
        try {
            List<DataRecord> records = controller.findAllAreas().stream().map(area -> {
                DataRecord record = new DataRecord();
                record.put("area_id", area.getAreaId());
                record.put("area_name", area.getAreaName());
                record.put("area_type", area.getAreaType());
                record.put("status", area.getStatus());
                record.put("created_date", area.getCreatedDate());
                return record;
            }).toList();
            DataImportExportUtil.writeRecords(file.toPath(), areaColumns(), areaTitles(), records);
            showInfo("Export thành công", "Đã xuất dữ liệu ra file: " + file.getName());
        } catch (Exception exception) {
            showError("Không thể export dữ liệu khu vực", exception.getMessage());
        }
    }

    private List<String> areaColumns() {
        return List.of("area_id", "area_name", "area_type", "status", "created_date");
    }

    private Map<String, String> areaTitles() {
        Map<String, String> titles = new LinkedHashMap<>();
        titles.put("area_id", "Mã Khu Vực");
        titles.put("area_name", "Tên Khu Vực");
        titles.put("area_type", "Loại Khu Vực");
        titles.put("status", "Trạng Thái");
        titles.put("created_date", "Ngày Tạo");
        return titles;
    }

    private Optional<Area> showAreaDialog(String title, Area source, boolean lockId) {
        Dialog<Area> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField idField = new TextField(source.getAreaId());
        idField.setDisable(lockId);
        TextField nameField = new TextField(source.getAreaName());
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("Phường", "Xã", "Đặc khu", "Quận", "Huyện", "Thành phố"));
        typeBox.setValue(source.getAreaType());
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("Hoạt động", "Tạm khóa", "Ngừng hoạt động"));
        statusBox.setValue(source.getStatus());
        TextField createdDateField = new TextField(source.getCreatedDate());

        GridPane formGrid = new GridPane();
        formGrid.setHgap(12);
        formGrid.setVgap(12);
        formGrid.setPadding(new Insets(18));
        formGrid.addRow(0, new Label("Area ID"), idField);
        formGrid.addRow(1, new Label("Area Name"), nameField);
        formGrid.addRow(2, new Label("Area Type"), typeBox);
        formGrid.addRow(3, new Label("Status"), statusBox);
        formGrid.addRow(4, new Label("Created Date"), createdDateField);

        dialog.getDialogPane().setContent(formGrid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                if (idField.getText().isBlank() || nameField.getText().isBlank() || createdDateField.getText().isBlank()) {
                    showWarning("Mã khu vực, tên khu vực và ngày tạo không được để trống.");
                    return null;
                }
                return new Area(
                        idField.getText().trim(),
                        nameField.getText().trim(),
                        typeBox.getValue(),
                        statusBox.getValue(),
                        createdDateField.getText().trim()
                );
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private String generateNextAreaId() {
        return controller.generateNextAreaId();
    }

    private void showInfo(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
