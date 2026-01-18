package subscribergui;

import client.ClientController;
import client.ClientSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.function.BiConsumer;

/**
 * JavaFX controller for updating a subscriber's contact details.
 * <p>
 * Allows a subscriber to update their email and phone number. Sends an update request to the
 * server and updates both UI and local session data upon success. A callback can be provided
 * to propagate the updated values back to the parent controller.
 */
public class UpdateDetailsController {

    /**
     * Text field for editing the subscriber's email address.
     */
    @FXML private TextField emailField;

    /**
     * Text field for editing the subscriber's phone number.
     */
    @FXML private TextField phoneField;

    /**
     * Button used to trigger saving the updated details.
     */
    @FXML private Button saveBtn;

    /**
     * Label used to display status messages and validation errors.
     */
    @FXML private Label statusLabel;

    /**
     * Connected client controller used to communicate with the server.
     */
    private ClientController client;

    /**
     * Subscriber identifier whose details are being updated.
     */
    private int subscriberId = -1;

    /**
     * Callback invoked after a successful update to propagate the new email and phone
     * back to the owning controller (e.g., {@code SubscriberMainController}).
     */
    private BiConsumer<String, String> onUpdated;

    /**
     * Initializes this controller with the required context and pre-fills the form fields.
     *
     * @param client       the connected {@link ClientController} used to send the update request
     * @param subscriberId the subscriber identifier to update
     * @param currentEmail the currently stored email to pre-fill in the form
     * @param currentPhone the currently stored phone to pre-fill in the form
     * @param onUpdated    callback invoked with {@code (newEmail, newPhone)} after success
     */
    public void init(ClientController client,
                     int subscriberId,
                     String currentEmail,
                     String currentPhone,
                     BiConsumer<String, String> onUpdated) {

        this.client = client;
        this.subscriberId = subscriberId;
        this.onUpdated = onUpdated;

        if (emailField != null) emailField.setText(currentEmail == null ? "" : currentEmail.trim());
        if (phoneField != null) phoneField.setText(currentPhone == null ? "" : currentPhone.trim());

        setStatus("", false);
    }

    /**
     * Handles the save action for updating subscriber details.
     * <p>
     * Performs basic validation, sets an active handler to process the server response,
     * and sends the update request to the server.
     */
    @FXML
    private void handleSave() {
        if (client == null) {
            setStatus("❌ No client connection.", false);
            return;
        }
        if (subscriberId <= 0) {
            setStatus("❌ Invalid subscriber.", false);
            return;
        }

        String newEmail = (emailField.getText() == null) ? "" : emailField.getText().trim();
        String newPhone = (phoneField.getText() == null) ? "" : phoneField.getText().trim();

        // basic validation (simple, practical)
        if (newEmail.isEmpty() || !newEmail.contains("@") || !newEmail.contains(".")) {
            setStatus("❌ Please enter a valid email.", false);
            return;
        }

        if (newPhone.isEmpty() || newPhone.length() < 7) {
            setStatus("❌ Please enter a valid phone number.", false);
            return;
        }

        setStatus("Saving...", true);

        ClientSession.activeHandler = (msg) -> Platform.runLater(() -> {
            try {
                if (msg instanceof String s) {

                    if (s.startsWith("UPDATE_SUB_OK|")) {
                        // UPDATE_SUB_OK|email|phone
                        String[] parts = s.split("\\|", -1);
                        String email = (parts.length >= 2) ? parts[1] : newEmail;
                        String phone = (parts.length >= 3) ? parts[2] : newPhone;

                        // update session too (helps autofill in other screens)
                        ClientSession.userEmail = email;
                        ClientSession.userPhone = phone;

                        if (onUpdated != null) onUpdated.accept(email, phone);

                        setStatus("✅ Updated successfully.", true);
                        ClientSession.activeHandler = null;
                        return;
                    }

                    if (s.startsWith("UPDATE_SUB_FAIL|")) {
                        // UPDATE_SUB_FAIL|reason
                        String reason = s.substring("UPDATE_SUB_FAIL|".length());
                        setStatus("❌ " + reason, false);
                        ClientSession.activeHandler = null;
                        return;
                    }
                }

                setStatus("❌ Unexpected server response.", false);
                ClientSession.activeHandler = null;

            } catch (Exception ex) {
                ex.printStackTrace();
                setStatus("❌ Error while handling response.", false);
                ClientSession.activeHandler = null;
            }
        });

        client.updateSubscriberDetails(subscriberId, newEmail, newPhone);
    }

    /**
     * Updates the status label styling and optionally disables the save button while saving.
     *
     * @param text the message to show
     * @param ok   {@code true} to show a success style; {@code false} to show an error style
     */
    private void setStatus(String text, boolean ok) {
        if (statusLabel == null) return;
        statusLabel.setText(text == null ? "" : text);
        statusLabel.setStyle(ok
                ? "-fx-text-fill: #2E7D32; -fx-font-weight: bold;"
                : "-fx-text-fill: #C62828; -fx-font-weight: bold;");
        if (saveBtn != null) saveBtn.setDisable("Saving...".equals(text));
    }
}
