package clientgui;

import entities.Table;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * JavaFX controller for managing restaurant tables.
 * <p>
 * Configures the table view columns and input controls used to view and manage
 * {@link Table} entities (e.g., table number, capacity, and status).
 */
public class ManageTablesController {

    /**
     * Table view displaying the list of {@link Table} entities.
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
     * Column displaying the table status.
     */
    @FXML private TableColumn<Table, String> colStatus;

    /**
     * Input field for entering/editing a table number.
     */
    @FXML private TextField numberField;

    /**
     * Input field for entering/editing a table capacity.
     */
    @FXML private TextField capacityField;

    /**
     * Combo box for selecting a table status.
     */
    @FXML private ComboBox<String> statusCombo;

    /**
     * JavaFX initialization hook.
     * <p>
     * Populates the status combo box, binds table columns to {@link Table} properties,
     * and triggers loading of the current tables list.
     */
    @FXML
    private void initialize() {
        statusCombo.getItems().addAll("FREE", "RESERVED", "OCCUPIED");

        colNumber.setCellValueFactory(
                new PropertyValueFactory<>("tableNumber")
        );
        colCapacity.setCellValueFactory(
                new PropertyValueFactory<>("capacity")
        );
        colStatus.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        loadTables();
    }

    /**
     * Loads the current tables list from the server and populates {@link #tablesTable}.
     * <p>
     * This method is expected to issue a request (e.g., {@code GET_TABLES}) and update the UI
     * when the response is received.
     */
    private void loadTables() {
        // כאן שליחת GET_TABLES לשרת
    }
}
