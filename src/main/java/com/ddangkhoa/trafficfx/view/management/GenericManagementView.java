package com.ddangkhoa.trafficfx.view.management;

import com.ddangkhoa.trafficfx.controller.GenericManagementController;
import com.ddangkhoa.trafficfx.model.DataRecord;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ddangkhoa.trafficfx.util.DataImportExportUtil;

public class GenericManagementView {
    private final String pageTitle;
    private final String searchPrompt;
    private final String statisticTitle;
    private final String tableTitle;
    private final String primaryKeyColumn;
    private final String idPrefix;
    private final int idDigits;
    private final List<String> columns;
    private final Map<String, String> columnTitles;
    private final Map<String, String> defaultValues;
    private final boolean editable;
    private final GenericManagementController controller;
    private final ObservableList<DataRecord> recordList = FXCollections.observableArrayList();
    private final TableView<DataRecord> table = new TableView<>();
    private final Label totalLabel = new Label("0");
    private final TextField searchField = new TextField();

    public GenericManagementView(String pageTitle,
                                 String searchPrompt,
                                 String statisticTitle,
                                 String tableTitle,
                                 String tableName,
                                 String primaryKeyColumn,
                                 String idPrefix,
                                 int idDigits,
                                 List<String> columns,
                                 Map<String, String> columnTitles,
                                 Map<String, String> defaultValues,
                                 boolean editable,
                                 String accountId) {
        this.pageTitle = pageTitle;
        this.searchPrompt = searchPrompt;
        this.statisticTitle = statisticTitle;
        this.tableTitle = tableTitle;
        this.primaryKeyColumn = primaryKeyColumn;
        this.idPrefix = idPrefix;
        this.idDigits = idDigits;
        this.columns = columns;
        this.columnTitles = columnTitles;
        this.defaultValues = defaultValues;
        this.editable = editable;
        this.controller = new GenericManagementController(tableName, primaryKeyColumn, columns, accountId);
    }

    public Parent getView() {
        VBox root = new VBox(22);
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(28, 38, 28, 40));

        Label pageLabel = new Label(pageTitle);
        pageLabel.getStyleClass().add("window-title");

        searchField.getStyleClass().add("wide-search-field");
        searchField.setPromptText(searchPrompt);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadRecords(newValue));

        HBox summaryAndActions = new HBox(18);
        summaryAndActions.setAlignment(Pos.BOTTOM_LEFT);

        VBox statisticCard = new VBox(8);
        statisticCard.getStyleClass().add("statistic-card");
        statisticCard.setPrefSize(230, 106);
        Label statisticLabel = new Label(statisticTitle);
        statisticLabel.getStyleClass().add("statistic-title");
        totalLabel.getStyleClass().add("statistic-value");
        statisticCard.getChildren().addAll(statisticLabel, totalLabel);

        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);

        Button insertButton = new Button("＋ Insert");
        insertButton.getStyleClass().add("insert-button");
        insertButton.setOnAction(event -> openInsertDialog());

        Button deleteButton = new Button("🗑 Delete");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(event -> deleteSelectedRecord());

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
        importItem.setOnAction(event -> importData());
        exportItem.setOnAction(event -> exportData());
        moreButton.getItems().addAll(importItem, exportItem);

        HBox actionBox = new HBox(14, insertButton, deleteButton, updateButton, moreButton);
        actionBox.setAlignment(Pos.BOTTOM_RIGHT);
        actionBox.setVisible(editable);
        actionBox.setManaged(editable);
        summaryAndActions.getChildren().addAll(statisticCard, actionSpacer, actionBox);

        Label titleLabel = new Label(tableTitle);
        titleLabel.getStyleClass().add("section-title");

        configureTable();
        loadRecords(null);

        root.getChildren().addAll(pageLabel, searchField, summaryAndActions, titleLabel, table);
        return root;
    }

    private void configureTable() {
        table.getStyleClass().add("area-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(360);
        table.setItems(recordList);

        for (String columnName : columns) {
            TableColumn<DataRecord, String> column = new TableColumn<>(columnTitles.getOrDefault(columnName, columnName));
            column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(columnName)));
            if (isImportantColumn(columnName)) {
                column.setCellFactory(currentColumn -> importantCell(columnName));
            }
            table.getColumns().add(column);
        }
        table.setRowFactory(currentTable -> {
            TableRow<DataRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (editable && event.getClickCount() == 2 && !row.isEmpty()) {
                    openUpdateDialog();
                }
            });
            return row;
        });
    }

    private void loadRecords(String keyword) {
        recordList.setAll(controller.searchRecords(keyword));
        totalLabel.setText(String.valueOf(recordList.size()));
    }
    private boolean isImportantColumn(String columnName) {
        String name = columnName.toLowerCase();
        return name.contains("status")
                || name.contains("role")
                || name.contains("position")
                || name.contains("action")
                || name.contains("analysis_type")
                || name.contains("congestion")
                || name.contains("vehicle_count")
                || name.contains("avg_speed")
                || name.contains("velocity")
                || name.contains("traffic_level")
                || name.contains("road_level")
                || name.contains("node_type")
                || name.contains("street_type")
                || name.contains("result_summary");
    }

    private TableCell<DataRecord, String> importantCell(String columnName) {
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
                badge.getStyleClass().setAll("cell-badge", resolveHighlightStyle(columnName, item));
                setGraphic(badge);
                setText(null);
            }
        };
    }

    private String resolveHighlightStyle(String columnName, String value) {
        String name = columnName.toLowerCase();
        String text = value.toLowerCase();
        if (name.contains("status")) {
            return text.contains("ngừng") || text.contains("khóa") || text.contains("lỗi")
                    ? "highlight-danger"
                    : "highlight-success";
        }
        if (name.contains("traffic_level")) {
            if (text.contains("congested")) {
                return "highlight-danger";
            }
            if (text.contains("slow")) {
                return "highlight-warning";
            }
            return "highlight-success";
        }
        if (name.contains("congestion")) {
            if (text.contains("cao") || text.contains("nặng")) {
                return "highlight-danger";
            }
            if (text.contains("trung")) {
                return "highlight-warning";
            }
            return "highlight-success";
        }
        if (name.contains("vehicle_count") || name.contains("avg_speed") || name.contains("result_summary")) {
            return "highlight-warning";
        }
        return "highlight-info";
    }


    private void openInsertDialog() {
        DataRecord record = new DataRecord();
        for (String column : columns) {
            record.put(column, defaultValueFor(column));
        }
        record.put(primaryKeyColumn, controller.generateNextId(idPrefix, idDigits));
        Optional<DataRecord> result = showRecordDialog("Insert " + pageTitle, record, false);
        result.ifPresent(value -> {
            try {
                controller.insertRecord(value);
                loadRecords(searchField.getText());
            } catch (RuntimeException exception) {
                showError("Không thể thêm dữ liệu", exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage());
            }
        });
    }

    private void openUpdateDialog() {
        DataRecord selectedRecord = table.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) {
            showWarning("Vui lòng chọn một dòng dữ liệu trước khi cập nhật.");
            return;
        }
        DataRecord editableRecord = new DataRecord(new LinkedHashMap<>(selectedRecord.getValues()));
        Optional<DataRecord> result = showRecordDialog("Update " + pageTitle, editableRecord, true);
        result.ifPresent(value -> {
            try {
                controller.updateRecord(value);
                loadRecords(searchField.getText());
            } catch (RuntimeException exception) {
                showError("Không thể cập nhật dữ liệu", exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage());
            }
        });
    }

    private void deleteSelectedRecord() {
        DataRecord selectedRecord = table.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) {
            showWarning("Vui lòng chọn một dòng dữ liệu trước khi xóa.");
            return;
        }
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xóa mềm dữ liệu");
        confirmDialog.setHeaderText("Xóa mềm dữ liệu " + selectedRecord.get(primaryKeyColumn) + "?");
        confirmDialog.setContentText("Hệ thống chỉ cập nhật cột IS_DELETED/IS_DELETE = 1, không xóa cứng khỏi database.");
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                controller.deleteRecord(selectedRecord.get(primaryKeyColumn));
                loadRecords(searchField.getText());
            } catch (RuntimeException exception) {
                showError("Không thể xóa dữ liệu", exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage());
            }
        }
    }

    private void importData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import dữ liệu " + pageTitle);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel hoặc CSV", "*.xlsx", "*.csv"),
                new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"),
                new FileChooser.ExtensionFilter("CSV UTF-8", "*.csv")
        );
        File file = fileChooser.showOpenDialog(table.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            List<DataRecord> importedRecords = DataImportExportUtil.readRecords(file.toPath(), columns, columnTitles);
            int success = controller.importRecords(importedRecords, idPrefix, idDigits);
            loadRecords(searchField.getText());
            showInfo("Import thành công", "Đã import " + success + " dòng dữ liệu từ file: " + file.getName());
        } catch (Exception exception) {
            showError("Không thể import dữ liệu", exception.getMessage());
        }
    }

    private void exportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export dữ liệu " + pageTitle);
        fileChooser.setInitialFileName(pageTitle.toLowerCase().replace(" ", "_") + ".xlsx");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"),
                new FileChooser.ExtensionFilter("CSV UTF-8", "*.csv")
        );
        File file = fileChooser.showSaveDialog(table.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            DataImportExportUtil.writeRecords(file.toPath(), columns, columnTitles, controller.findAllRecords());
            showInfo("Export thành công", "Đã xuất dữ liệu ra file: " + file.getName());
        } catch (Exception exception) {
            showError("Không thể export dữ liệu", exception.getMessage());
        }
    }

    private Optional<DataRecord> showRecordDialog(String dialogTitle, DataRecord source, boolean lockId) {
        Dialog<DataRecord> dialog = new Dialog<>();
        dialog.setTitle(dialogTitle);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(12);
        formGrid.setVgap(12);
        formGrid.setPadding(new Insets(18));

        Map<String, TextField> fields = new LinkedHashMap<>();
        int row = 0;
        for (String column : columns) {
            TextField field = new TextField(source.get(column));
            field.setMinWidth(270);
            if (lockId && column.equals(primaryKeyColumn)) {
                field.setDisable(true);
            }
            fields.put(column, field);
            formGrid.addRow(row++, new Label(columnTitles.getOrDefault(column, column)), field);
        }

        dialog.getDialogPane().setContent(formGrid);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                DataRecord result = new DataRecord();
                for (String column : columns) {
                    String value = fields.get(column).getText().trim();
                    if (value.isBlank()) {
                        showWarning("Không được để trống: " + columnTitles.getOrDefault(column, column));
                        return null;
                    }
                    result.put(column, value);
                }
                return result;
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private String defaultValueFor(String column) {
        if (defaultValues.containsKey(column)) {
            return defaultValues.get(column);
        }
        if (column.endsWith("_date")) {
            return LocalDate.now().toString();
        }
        if (column.endsWith("_at")) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return "";
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
