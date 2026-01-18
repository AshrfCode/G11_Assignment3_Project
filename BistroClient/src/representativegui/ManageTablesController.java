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

/**
 * JavaFX controller for managing restaurant tables.
 * <p>
 * Displays tables in a {@link TableView}, supports refreshing data from the server,
 * and provides CRUD operations (add, update, delete) for {@link Table} entities.
 * Uses {@link ClientSession#activeHandler} to handle asynchronous server responses.
 */
public class ManageTablesController {

    /**
     * Table view displaying all tables.
     */
    @FXML private TableView<Table> tablesTable;

    /**
     * Column displaying the table number.
     */
    @FXML private TableColumn<Table, Integer> colNumber;

    /**
     * Column displaying the table capacity.
     */
    @FXML private TableColumn<Table, Integer> colCapacity;

    /**
     * Column displaying the table location.
     */
    @FXML private TableColumn<Table, String> colLocation;

    /**
     * Column displaying the table status.
     */
    @FXML private TableColumn<Table, String> colStatus;

    /**
     * Input field for entering/editing the table number.
     */
    @FXML private TextField numberField;

    /**
     * Input field for entering/editing the table capacity.
     */
    @FXML private TextField capacityField;

    /**
     * Combo box for selecting the table location.
     */
    @FXML private ComboBox<String> locationCombo;

    /**
     * Label used to display status messages and validation/errors to the user.
     */
    @FXML private Label msgLabel;

    /**
     * JavaFX initialization hook.
     * <p>
     * Installs the server message handler, binds table columns to {@link Table} properties,
     * configures location options and selection behavior, and loads the initial tables list.
     */
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

    /**
     * Handles messages received from the server related to table management.
     * <p>
     * Supports:
     * <ul>
     *   <li>A {@link List} of {@link Table} entities to populate the table view</li>
     *   <li>A {@link String} message for status feedback</li>
     * </ul>
     *
     * @param msg the message received from the server
     */
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

    /**
     * Requests the full list of tables from the server.
     * <p>
     * Sends a {@code GET_TABLES} request using the shared client.
     */
    private void loadTables() {
        try {
            ClientManager.getClient().sendToServer(new ClientRequest("GET_TABLES", new Object[0]));
        } catch (IOException e) {
            e.printStackTrace();
            msgLabel.setText("❌ Failed to load tables");
        }
    }

    /**
     * Refreshes the tables list by reloading data from the server.
     */
    @FXML
    private void refresh() {
        loadTables();
    }

    /**
     * Sends a request to add a new table based on the form input values.
     * <p>
     * Validates location selection and numeric input for table number and capacity,
     * then sends an {@code ADD_TABLE} request containing a {@link Table} object.
     */
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

    /**
     * Sends a request to update the selected table with new form values.
     * <p>
     * Requires a table selection. Updates the selected {@link Table} instance locally
     * (capacity and location) and sends an {@code UPDATE_TABLE} request containing it.
     */
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

    /**
     * Sends a request to delete the selected table.
     * <p>
     * Requires a table selection. Sends a {@code DELETE_TABLE} request with the selected
     * table number as parameter.
     */
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
