package representativegui;

import client.ClientManager;
import client.ClientSession;
import common.ClientRequest;
import entities.Table;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.List;

public class ManageTablesController {

    @FXML private TableView<Table> tablesTable;
    @FXML private TableColumn<Table, Integer> colNumber;
    @FXML private TableColumn<Table, Integer> colCapacity;
    @FXML private TableColumn<Table, String> colLocation;
    @FXML private TableColumn<Table, String> colStatus;

    @FXML private TextField numberField;
    @FXML private TextField capacityField;
    @FXML private ComboBox<String> locationCombo;

    @FXML private Label msgLabel;

    @FXML
    private void initialize() {

        ClientSession.activeHandler = this::handleServerMessage;

        // ✅ חיבור העמודות ל-Table properties
        colNumber.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        locationCombo.getItems().addAll("Inside", "Outside", "VIP Room");

        // ✅ כשבוחרים שורה - למלא שדות
        tablesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, sel) -> {
            if (sel != null) {
                numberField.setText(String.valueOf(sel.getTableNumber()));
                capacityField.setText(String.valueOf(sel.getCapacity()));
                locationCombo.setValue(sel.getLocation());
            }
        });

        loadTables();
    }

    private void handleServerMessage(Object msg) {

        // ✅ קבלת טבלאות
        if (msg instanceof List<?> list && (list.isEmpty() || list.get(0) instanceof Table)) {
            @SuppressWarnings("unchecked")
            List<Table> tables = (List<Table>) list;

            Platform.runLater(() -> {
                tablesTable.setItems(FXCollections.observableArrayList(tables));
                msgLabel.setText("Loaded " + tables.size() + " tables.");
            });
            return;
        }

        // ✅ הודעות מהשרת
        if (msg instanceof String s) {
            Platform.runLater(() -> msgLabel.setText(s));
        }
    }

    private void loadTables() {
        try {
            ClientManager.getClient().sendToServer(new ClientRequest("GET_TABLES", new Object[0]));
        } catch (IOException e) {
            e.printStackTrace();
            msgLabel.setText("❌ Failed to load tables");
        }
    }

    @FXML
    private void refresh() {
        loadTables();
    }

    @FXML
    private void addTable() {
        try {
            int num = Integer.parseInt(numberField.getText().trim());
            int cap = Integer.parseInt(capacityField.getText().trim());
            String loc = locationCombo.getValue();

            if (loc == null || loc.isBlank()) {
                msgLabel.setText("❌ Choose location");
                return;
            }

            Table t = new Table(num, cap, loc);

            ClientManager.getClient().sendToServer(new ClientRequest("ADD_TABLE", new Object[]{t}));
            // רענון יגיע אחרי תשובת OK מהשרת (או אפשר לקרוא כאן loadTables)
        } catch (Exception e) {
            msgLabel.setText("❌ Invalid input");
        }
    }

    @FXML
    private void updateTable() {
        Table selected = tablesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            msgLabel.setText("❌ Select a table first");
            return;
        }

        try {
            int cap = Integer.parseInt(capacityField.getText().trim());
            String loc = locationCombo.getValue();

            if (loc == null || loc.isBlank()) {
                msgLabel.setText("❌ Choose location");
                return;
            }

            selected.setCapacity(cap);
            selected.setLocation(loc);

            ClientManager.getClient().sendToServer(new ClientRequest("UPDATE_TABLE", new Object[]{selected}));
        } catch (Exception e) {
            msgLabel.setText("❌ Invalid input");
        }
    }

    @FXML
    private void deleteTable() {
        Table selected = tablesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            msgLabel.setText("❌ Select a table first");
            return;
        }

        try {
            ClientManager.getClient().sendToServer(
                    new ClientRequest("DELETE_TABLE", new Object[]{selected.getTableNumber()})
            );
        } catch (IOException e) {
            e.printStackTrace();
            msgLabel.setText("❌ Delete failed");
        }
    }
}
