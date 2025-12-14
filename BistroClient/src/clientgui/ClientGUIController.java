package clientgui;

import client.ClientController;
import common.ChatIF;
import common.ClientRequest;
import common.Order;

import java.sql.Date;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ClientGUIController implements ChatIF {

    @FXML private TableView<Order> orderTable;

    @FXML private TableColumn<Order, Integer> idCol;
    @FXML private TableColumn<Order, Integer> guestsCol;           // UPDATED
    @FXML private TableColumn<Order, String> dateCol;
    @FXML private TableColumn<Order, Integer> confirmationCodeCol;
    @FXML private TableColumn<Order, Integer> subscriberIdCol;
    @FXML private TableColumn<Order, String> datePlacedCol;

    @FXML private TextField orderIdField;
    @FXML private TextField newGuestsField;                        // UPDATED

    @FXML private TextField orderIdDateField;
    @FXML private TextField newDateField;

    @FXML private Label statusLabel;

    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private ClientController client;

    public void setClient(ClientController client) {
        this.client = client;
    }

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getOrderNumber()).asObject());

        guestsCol.setCellValueFactory(cd ->                      // UPDATED
                new SimpleIntegerProperty(cd.getValue().getNumberOfGuests()).asObject());

        dateCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getOrderDate().toString()));

        confirmationCodeCol.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getConfirmationCode()).asObject());

        subscriberIdCol.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getSubscriberId()).asObject());

        datePlacedCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getDateOfPlacingOrder().toString()));

        orderTable.setItems(orders);
    }

    // --------------------------------------------------------
    // REFRESH ORDERS
    // --------------------------------------------------------
    @FXML
    public void handleRefreshOrders() {
        if (client != null && client.isConnected()) {
            client.sendRequest(new ClientRequest("GET_ALL_ORDERS", new Object[]{}));
        } else {
            statusLabel.setText("Not connected.");
        }
    }

    // --------------------------------------------------------
    // UPDATE NUMBER OF GUESTS   (REPLACES UPDATE_PARKING_SPACE)
    // --------------------------------------------------------
    @FXML
    public void handleUpdateGuests() {
        try {
            int id = Integer.parseInt(orderIdField.getText());
            int guests = Integer.parseInt(newGuestsField.getText());

            client.sendRequest(new ClientRequest("UPDATE_NUMBER_OF_GUESTS",
                    new Object[]{id, guests}));

            statusLabel.setText("Sent update for number of guests.");
        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Invalid Order ID (guests update failed).");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // --------------------------------------------------------
    // UPDATE ORDER DATE
    // --------------------------------------------------------
    @FXML
    public void handleUpdateDate() {
        try {
            int id = Integer.parseInt(orderIdDateField.getText());
            Date date = Date.valueOf(newDateField.getText());

            client.sendRequest(new ClientRequest("UPDATE_ORDER_DATE",
                    new Object[]{id, date}));

            statusLabel.setText("Sent update for order date.");
        } catch (Exception e) {
            statusLabel.setText("❌ Invalid Order ID/Date (date update failed).");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // --------------------------------------------------------
    // DISCONNECT
    // --------------------------------------------------------
    @FXML
    public void handleExit() {
        if (client != null && client.isConnected()) {
            client.sendRequest(new ClientRequest("DISCONNECT", new Object[]{}));
            client.closeConnectionSafely();
        }
        System.exit(0);
    }

    // --------------------------------------------------------
    // DISPLAY MESSAGE
    // --------------------------------------------------------
    @Override
    public void display(String message) {

        Platform.runLater(() -> {

            switch (message) {

                case "OK_GUESTS":
                    statusLabel.setText("✔ Number of guests updated.");
                    statusLabel.setStyle("-fx-text-fill: green;");
                    break;

                case "ERROR_GUESTS":
                    statusLabel.setText("❌ Invalid Order ID (guests update failed).");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    break;

                case "OK_DATE":
                    statusLabel.setText("✔ Order date updated.");
                    statusLabel.setStyle("-fx-text-fill: green;");
                    break;

                case "ERROR_DATE":
                    statusLabel.setText("❌ Invalid Order ID (date update failed).");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    break;

                default:
                    // Unknown messages or plain text
                    statusLabel.setText(message);
                    statusLabel.setStyle("-fx-text-fill: black;");
                    break;
            }

        });
    }


    // --------------------------------------------------------
    // DISPLAY ORDER LIST
    // --------------------------------------------------------
    public void displayOrders(List<Order> orderList) {
        Platform.runLater(() -> {
            orders.setAll(orderList);
            statusLabel.setText("Orders refreshed.");
        });
    }
}
