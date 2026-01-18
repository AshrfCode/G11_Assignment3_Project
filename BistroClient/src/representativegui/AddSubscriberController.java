package representativegui;

import client.ClientManager;
import common.ClientRequest;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * JavaFX controller for adding a new subscriber by a representative/manager.
 * <p>
 * Collects subscriber details from the form and sends an {@code ADD_SUBSCRIBER} request to the server.
 */
public class AddSubscriberController {

    /**
     * Text field for entering the subscriber's name.
     */
    @FXML private TextField nameField;

    /**
     * Text field for entering the subscriber's email address.
     */
    @FXML private TextField emailField;

    /**
     * Text field for entering the subscriber's phone number.
     */
    @FXML private TextField phoneField;

    /**
     * Password field for entering the subscriber's password.
     */
    @FXML private PasswordField passwordField;

    /**
     * Checkbox indicating whether the subscriber is active.
     */
    @FXML private CheckBox activeCheck;

    /**
     * Label used to display success/error messages to the user.
     */
    @FXML private Label msgLabel;

    /**
     * Handles saving a new subscriber.
     * <p>
     * Builds the request parameters from form fields and sends an {@code ADD_SUBSCRIBER} request
     * to the server via the shared client. On success, closes the current window.
     */
    @FXML
    private void saveSubscriber() {
        try {
            Object[] params = {
                    nameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    passwordField.getText(),
                    activeCheck.isSelected()
            };

            ClientManager.getClient().sendToServer(
                    new ClientRequest("ADD_SUBSCRIBER", params)
            );

            close();

        } catch (Exception e) {
            msgLabel.setText("❌ Failed to add subscriber");
        }
    }

    /**
     * Closes the current window.
     */
    @FXML
    private void close() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
