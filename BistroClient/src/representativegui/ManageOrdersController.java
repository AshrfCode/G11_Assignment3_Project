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

public class ManageOrdersController {

    private ClientController client;

    @FXML private TableView<ManageOrderEntry> ordersTable;
    @FXML private Label statusLabel;

    public void setClient(ClientController client) {
        this.client = client;
    }

    public void start() {
        refreshData();
    }

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