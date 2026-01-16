package representativegui;

import client.ClientController;
import client.ClientSession;
import common.ClientRequest;
import common.WaitingListEntry; // ✅ using the common class, NOT WaitingItem
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.util.List;

public class WaitingListController {

    private ClientController client;

    // ✅ Table now uses WaitingListEntry
    @FXML private TableView<WaitingListEntry> waitingTable;
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

        statusLabel.setText("Loading...");
        
        ClientSession.activeHandler = (msg) -> {
            Platform.runLater(() -> {
                if (msg instanceof List<?>) {
                    List<?> list = (List<?>) msg;
                    
                    if (list.isEmpty()) {
                        waitingTable.setItems(FXCollections.emptyObservableList());
                        statusLabel.setText("Waiting list is empty.");
                    } 
                    // ✅ Check if it is our object
                    else if (!list.isEmpty() && list.get(0) instanceof WaitingListEntry) {
                        @SuppressWarnings("unchecked")
                        List<WaitingListEntry> entries = (List<WaitingListEntry>) list;
                        waitingTable.setItems(FXCollections.observableArrayList(entries));
                        statusLabel.setText("Loaded " + entries.size() + " entries.");
                    }
                } else {
                    statusLabel.setText("Error loading data.");
                }
            });
        };

        client.sendRequest(new ClientRequest(ClientRequest.CMD_GET_WAITING_LIST, new Object[]{}));
    }
}