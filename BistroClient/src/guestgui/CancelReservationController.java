package guestgui;

import client.ClientController;
import client.ClientSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * JavaFX controller responsible for canceling reservations by confirmation code.
 * <p>
 * Supports both guest cancellation and subscriber cancellation. When subscriber information
 * is provided, cancellation is performed in subscriber mode, which may enforce ownership
 * restrictions on the server side.
 */
public class CancelReservationController {

    /**
     * Text field for entering the reservation confirmation code to cancel.
     */
    @FXML private TextField cancelCodeField;

    /**
     * Label used to display status messages and validation/errors to the user.
     */
    @FXML private Label statusLabel;

    /**
     * Connected client controller used to send cancellation requests to the server.
     */
    private ClientController client;

    /**
     * Indicates whether cancellation should be performed as a subscriber (ownership restriction).
     */
    // If set -> we cancel as subscriber (ownership restriction)
    private boolean subscriberMode = false;

    /**
     * Subscriber ID used when canceling in subscriber mode.
     */
    private int subscriberId = -1;

    /**
     * Subscriber email used when canceling in subscriber mode.
     */
    private String subscriberEmail = "";

    /**
     * Subscriber phone used when canceling in subscriber mode.
     */
    private String subscriberPhone = "";

    /**
     * Sets the client controller used for server communication.
     *
     * @param client the connected {@link ClientController}
     */
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Enables subscriber cancellation mode and stores subscriber identification details.
     * <p>
     * Intended to be called only from the subscriber flow (e.g., SubscriberMainController).
     *
     * @param id    subscriber ID
     * @param email subscriber email (may be null)
     * @param phone subscriber phone (may be null)
     */
    // Call this ONLY from SubscriberMainController
    public void setSubscriberInfo(int id, String email, String phone) {
        subscriberMode = true;
        subscriberId = id;
        subscriberEmail = (email == null) ? "" : email.trim();
        subscriberPhone = (phone == null) ? "" : phone.trim();
    }

    /**
     * Handles the cancel action.
     * <p>
     * Validates input, registers an active message handler to process server responses,
     * and sends the appropriate cancellation request (guest or subscriber).
     */
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

    /**
     * Updates the status label text if available.
     *
     * @param msg the message to display
     */
    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }
}
