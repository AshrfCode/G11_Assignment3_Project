package guestgui;

import client.ClientController;
import client.ClientSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CheckInController {

    @FXML private TextField codeField;
    @FXML private Label statusLabel;

    private ClientController client;

    public void setClient(ClientController client) {
        this.client = client;
    }

    @FXML
    private void handleCheckIn() {
        if (client == null) {
            setStatus("❌ No client connection.");
            return;
        }

        String code = (codeField.getText() == null) ? "" : codeField.getText().trim();
        
        if (code.isEmpty()) {
            setStatus("❌ Please enter confirmation code.");
            return;
        }

        setStatus("Checking details...");

        // Define how to handle the server's response for THIS specific action
        ClientSession.activeHandler = (msg) -> {
            Platform.runLater(() -> {
                // Scenario 1: Success (Server returns the Table Number as Integer)
                if (msg instanceof Integer tableNumber) {
                    setStatus("✅ Success! Assigned to Table #" + tableNumber);
                    showSuccessAlert(tableNumber);
                } 
                // Scenario 2: Error (Server returns an error String)
                else if (msg instanceof String errorMsg) {
                    setStatus("❌ " + errorMsg);
                } 
                else {
                    setStatus("❌ Unexpected response from server.");
                }
            });
        };

        // Send the request
        client.checkInCustomer(code);
    }

    private void setStatus(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
            // Change color based on success/error
            if (text.startsWith("✅")) {
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        }
    }

    private void showSuccessAlert(int tableNumber) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Check In Successful");
        alert.setHeaderText("Welcome to Bistro!");
        alert.setContentText("Your table is ready.\nPlease proceed to Table Number: " + tableNumber);
        alert.showAndWait();
    }
}