package representativegui;

import client.ClientManager;
import common.ClientRequest;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddSubscriberController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox activeCheck;
    @FXML private Label msgLabel;

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

    @FXML
    private void close() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
