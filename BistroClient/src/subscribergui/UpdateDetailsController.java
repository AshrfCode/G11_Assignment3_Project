package subscribergui;

import client.ClientController;
import client.ClientSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.function.BiConsumer;

public class UpdateDetailsController {

    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Button saveBtn;
    @FXML private Label statusLabel;

    private ClientController client;
    private int subscriberId = -1;

    // callback to update SubscriberMainController fields after success
    private BiConsumer<String, String> onUpdated;

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

    private void setStatus(String text, boolean ok) {
        if (statusLabel == null) return;
        statusLabel.setText(text == null ? "" : text);
        statusLabel.setStyle(ok
                ? "-fx-text-fill: #2E7D32; -fx-font-weight: bold;"
                : "-fx-text-fill: #C62828; -fx-font-weight: bold;");
        if (saveBtn != null) saveBtn.setDisable("Saving...".equals(text));
    }
}
