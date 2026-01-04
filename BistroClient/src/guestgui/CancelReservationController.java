package guestgui;

import client.ClientController;
import client.ClientSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CancelReservationController {

    @FXML private TextField cancelCodeField;
    @FXML private Label statusLabel;

    private ClientController client;

    // If set -> we cancel as subscriber (ownership restriction)
    private boolean subscriberMode = false;
    private int subscriberId = -1;
    private String subscriberEmail = "";
    private String subscriberPhone = "";

    public void setClient(ClientController client) {
        this.client = client;
    }

    // Call this ONLY from SubscriberMainController
    public void setSubscriberInfo(int id, String email, String phone) {
        subscriberMode = true;
        subscriberId = id;
        subscriberEmail = (email == null) ? "" : email.trim();
        subscriberPhone = (phone == null) ? "" : phone.trim();
    }

    @FXML
    private void handleCancel() {
        if (client == null) {
            setStatus("❌ No client connection.");
            return;
        }

        String code = cancelCodeField.getText() == null ? "" : cancelCodeField.getText().trim();
        if (code.isEmpty()) {
            setStatus("❌ Enter confirmation code to cancel.");
            return;
        }

        setStatus("Canceling...");

        ClientSession.activeHandler = (msg) -> {
            if (msg instanceof String s) {
                Platform.runLater(() -> {
                    switch (s) {
                        case "CANCEL_OK" ->
                                setStatus("✅ Reservation canceled.");

                        case "CANCEL_FAIL_NOT_FOUND" ->
                                setStatus("❌ Confirmation code not found.");

                        case "CANCEL_FAIL_ALREADY_CANCELED" ->
                                setStatus("❌ This reservation is already canceled.");

                        case "CANCEL_FAIL_NOT_OWNER" ->
                                setStatus("❌ This reservation is not yours.");

                        default ->
                                setStatus("❌ Cancel failed (server error).");
                    }
                });
            }
        };


        // ✅ Guest vs Subscriber cancel
        if (subscriberMode && subscriberId > 0) {
            client.cancelReservationAsSubscriber(code, subscriberId, subscriberEmail, subscriberPhone);
        } else {
            client.cancelReservation(code);
        }
    }

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }
}
