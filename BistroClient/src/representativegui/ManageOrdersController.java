package representativegui;

import client.ClientController;
import client.ClientSession;
import common.ClientRequest;
import common.ManageOrderEntry; // ✅ Uses the new common class
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.util.List;

/**
 * JavaFX controller for viewing and managing today's orders/reservations.
 * <p>
 * Requests data from the server and displays it in a {@link TableView} of {@link ManageOrderEntry}
 * instances. Uses {@link ClientSession#activeHandler} to process the asynchronous server response.
 */
public class ManageOrdersController {

    /**
     * Connected client controller used to communicate with the server.
     */
    private ClientController client;

    /**
     * Table view displaying order/reservation entries for management.
     */
    @FXML private TableView<ManageOrderEntry> ordersTable;

    /**
     * Label used to display loading state, results, and error messages.
     */
    @FXML private Label statusLabel;

    /**
     * Sets the client controller used for server communication.
     *
     * @param client the connected {@link ClientController}
     */
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Starts this screen by loading the initial dataset.
     */
    public void start() {
        refreshData();
    }

    /**
     * Requests the latest management data from the server and refreshes the table.
     * <p>
     * Installs an active handler that expects a {@link List} of {@link ManageOrderEntry}
     * objects (or an empty list) and updates the UI accordingly.
     */
    @FXML
    private void refreshData() {
        if (client == null) {
            statusLabel.setText("❌ No connection");
            return;
        }

        statusLabel.setText("Loading orders...");
        
        ClientSession.activeHandler = (msg) -> {
            Platform.runLater(() -> {
                if (msg instanceof List<?>) {
                    List<?> list = (List<?>) msg;
                    
                    if (list.isEmpty()) {
                        ordersTable.setItems(FXCollections.emptyObservableList());
                        statusLabel.setText("No orders found for today.");
                    } 
                    else if (!list.isEmpty() && list.get(0) instanceof ManageOrderEntry) {
                        @SuppressWarnings("unchecked")
                        List<ManageOrderEntry> entries = (List<ManageOrderEntry>) list;
                        ordersTable.setItems(FXCollections.observableArrayList(entries));
                        statusLabel.setText("Loaded " + entries.size() + " orders.");
                    }
                } else {
                    statusLabel.setText("Error loading data.");
                }
            });
        };

        // Reusing the command for "Today's Reservations"
        client.sendRequest(new ClientRequest(ClientRequest.CMD_GET_ALL_RESERVATIONS, new Object[]{}));
    }
}
