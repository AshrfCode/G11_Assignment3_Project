package representativegui;

import client.ClientManager;
import client.ClientSession;
import common.ClientRequest;
import entities.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;

/**
 * JavaFX controller for managing subscribers.
 * <p>
 * Displays all subscribers in a table, supports refreshing the list, opening a dialog
 * to add a subscriber, and deactivating a selected subscriber.
 * Uses {@link ClientSession#activeHandler} to receive and process server messages.
 */
public class ManageSubscribersController {

    // =========================
    // TABLE
    // =========================

    /**
     * Table view displaying the list of subscribers.
     */
    @FXML private TableView<User> subscribersTable;

    /**
     * Column displaying the subscriber user ID.
     */
    @FXML private TableColumn<User, Integer> colId;

    /**
     * Column displaying the subscriber name.
     */
    @FXML private TableColumn<User, String> colName;

    /**
     * Column displaying the subscriber email.
     */
    @FXML private TableColumn<User, String> colEmail;

    /**
     * Column displaying the subscriber phone.
     */
    @FXML private TableColumn<User, String> colPhone;

    /**
     * Column indicating whether the subscriber is active.
     */
    @FXML private TableColumn<User, Boolean> colActive;

    /**
     * Column displaying the subscriber creation timestamp.
     */
    @FXML private TableColumn<User, Timestamp> colCreatedAt;

    /**
     * Column displaying the subscriber number.
     */
    @FXML private TableColumn<User, String> colSubNumber;

    /**
     * Column displaying the digital card identifier/value.
     */
    @FXML private TableColumn<User, String> colDigitalCard;

    // =========================
    // FORM
    // =========================

    /**
     * Label used to display status messages and results of operations.
     */
    @FXML private Label msgLabel;

    // =========================
    // INITIALIZE
    // =========================

    /**
     * JavaFX initialization hook.
     * <p>
     * Installs the server message handler, binds table columns to {@link User} properties,
     * and triggers loading the initial list of subscribers.
     */
    @FXML
    private void initialize() {

        ClientSession.activeHandler = this::handleServerMessage;

        colSubNumber.setCellValueFactory(new PropertyValueFactory<>("subscriberNumber"));
        colDigitalCard.setCellValueFactory(new PropertyValueFactory<>("digitalCard"));
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        loadSubscribers();
    }

    // =========================
    // SERVER HANDLER
    // =========================

    /**
     * Handles messages received from the server.
     * <p>
     * Supports:
     * <ul>
     *   <li>A {@link List} of {@link User} objects representing subscribers</li>
     *   <li>A {@link String} message for general feedback</li>
     * </ul>
     *
     * @param msg the message received from the server
     */
    private void handleServerMessage(Object msg) {

        // ✅ רשימת מנויים
        if (msg instanceof List<?> list && (list.isEmpty() || list.get(0) instanceof User)) {

            @SuppressWarnings("unchecked")
            List<User> users = (List<User>) list;

            Platform.runLater(() -> {
                subscribersTable.setItems(FXCollections.observableArrayList(users));
                msgLabel.setText("Loaded " + users.size() + " subscribers.");
            });
            return;
        }

        // ✅ הודעת טקסט
        if (msg instanceof String s) {
            Platform.runLater(() -> msgLabel.setText(s));
        }
    }

    // =========================
    // LOAD
    // =========================

    /**
     * Requests the full list of subscribers from the server and updates the table on response.
     * <p>
     * Sends {@link ClientRequest#CMD_GET_ALL_SUBSCRIBERS} using the shared client.
     */
    private void loadSubscribers() {
        try {
            ClientManager.getClient().sendToServer(
                new ClientRequest(ClientRequest.CMD_GET_ALL_SUBSCRIBERS, new Object[0])
            );
        } catch (IOException e) {
            e.printStackTrace();
            msgLabel.setText("❌ Failed to load subscribers");
        }
    }

    // =========================
    // ACTIONS
    // =========================

    /**
     * Refreshes the subscribers table by reloading data from the server.
     */
    @FXML
    private void refresh() {
        loadSubscribers();
    }
    
    /**
     * Opens a modal dialog for adding a new subscriber.
     * <p>
     * After the dialog closes, reloads the subscribers list to refresh the table.
     */
    @FXML
    private void openAddSubscriber() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/representativegui/AddSubscriber.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Subscriber");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadSubscribers(); // refresh table

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Sends a request to deactivate the currently selected subscriber.
     * <p>
     * Requires a selection in the subscribers table. Sends a {@code DEACTIVATE_SUBSCRIBER}
     * request with the selected subscriber ID.
     */
    @FXML
    private void deactivateSubscriber() {

        User selected = subscribersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            msgLabel.setText("❌ Select subscriber first");
            return;
        }

        try {
            ClientManager.getClient().sendToServer(
                new ClientRequest("DEACTIVATE_SUBSCRIBER",
                    new Object[]{ selected.getId() })
            );
        } catch (IOException e) {
            e.printStackTrace();
            msgLabel.setText("❌ Action failed");
        }
    }
}
