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

/**
 * JavaFX controller for displaying the current waiting list.
 * <p>
 * Requests waiting list entries from the server and renders them in a {@link TableView}
 * of {@link WaitingListEntry}. Uses {@link ClientSession#activeHandler} to handle the
 * asynchronous server response.
 */
public class WaitingListController {

    /**
     * Connected client controller used to communicate with the server.
     */
    private ClientController client;

    /**
     * Table displaying waiting list entries.
     */
    @FXML private TableView<WaitingListEntry> waitingTable;

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
     * Starts this screen by performing an initial data refresh.
     */
    public void start() {
        refreshData();
    }

    /**
     * Requests the latest waiting list from the server and updates the table view.
     * <p>
     * Installs an active handler that expects a {@link List} of {@link WaitingListEntry}
     * objects (or an empty list) and updates the UI accordingly.
     */
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
