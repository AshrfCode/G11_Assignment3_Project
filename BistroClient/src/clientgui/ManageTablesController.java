package clientgui;

import entities.Table;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.PropertyValueFactory;

public class ManageTablesController {

    @FXML private TableView<Table> tablesTable;
    @FXML private TableColumn<Table, Integer> colNumber;
    @FXML private TableColumn<Table, Integer> colCapacity;
    @FXML private TableColumn<Table, String> colStatus;

    @FXML private TextField numberField;
    @FXML private TextField capacityField;
    @FXML private ComboBox<String> statusCombo;

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

    private void loadTables() {
        // כאן שליחת GET_TABLES לשרת
    }
}
