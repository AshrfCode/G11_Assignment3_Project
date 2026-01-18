package guestgui;

import client.ClientController;
import client.ClientSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * JavaFX controller for reservation bill preview and payment.
 * <p>
 * Allows a user to:
 * <ul>
 *   <li>Preview a bill for a reservation confirmation code ("Find")</li>
 *   <li>Pay the reservation after a successful preview ("Pay")</li>
 * </ul>
 * Uses {@link ClientSession#activeHandler} to receive asynchronous responses from the server.
 */
public class PaymentController {

    /**
     * Text field for entering the reservation confirmation code.
     */
    @FXML private TextField codeField;

    /**
     * Label displaying the number of diners for the reservation.
     */
    @FXML private Label dinersLabel;

    /**
     * Label displaying the total amount before discounts.
     */
    @FXML private Label totalLabel;

    /**
     * Label displaying the discount amount.
     */
    @FXML private Label discountLabel;

    /**
     * Label displaying the final amount to pay after discounts.
     */
    @FXML private Label finalLabel;

    /**
     * Label used to display status messages and errors to the user.
     */
    @FXML private Label statusLabel;

    /**
     * Button used to submit the payment request.
     */
    @FXML private Button payBtn;

    /**
     * Stores the last confirmation code that was successfully previewed.
     * <p>
     * Payment is allowed only when the current entered code matches this value.
     */
    private String lastValidCode = null;


    /**
     * Connected client controller used to communicate with the server.
     */
    private ClientController client;

    /**
     * Sets the client controller used for server communication.
     *
     * @param client the connected {@link ClientController}
     */
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Handles the pay action.
     * <p>
     * Validates that a bill was previewed for the current code before allowing payment.
     * Registers an active handler to process payment responses and sends the pay request.
     */
    @FXML
    private void handlePay() {
        if (client == null) {
            setStatus("❌ No client connection.");
            return;
        }
        

        String code = (codeField.getText() == null) ? "" : codeField.getText().trim();
        if (code.isEmpty()) {
            setStatus("❌ Please enter confirmation code.");
            return;
        }
        
        if (lastValidCode == null || !lastValidCode.equals(code)) {
            setStatus("❌ Click Find first to preview the bill.");
            return;
        }


        setStatus("Processing payment...");

        ClientSession.activeHandler = (msg) -> {
            Platform.runLater(() -> {
                if (msg instanceof String s) {
                    // Success format:
                    // PAY_OK|billNumber|diners|total|discount|final
                    if (s.startsWith("PAY_OK|")) {
                        String[] p = s.split("\\|");
                        // p[0]=PAY_OK p[1]=billNumber p[2]=diners p[3]=total p[4]=discount p[5]=final
                        dinersLabel.setText(p[2]);
                        totalLabel.setText(p[3] + "₪");
                        discountLabel.setText(p[4] + "₪");
                        finalLabel.setText(p[5] + "₪");

                        setStatus("✅ Paid successfully! Bill #" + p[1]);

                        Alert a = new Alert(Alert.AlertType.INFORMATION);
                        a.setTitle("Payment");
                        a.setHeaderText("Payment successful");
                        a.setContentText("Bill #" + p[1] + " was created.\nFinal total: " + p[5] + "₪");
                        a.showAndWait();
                    }
                    else if (s.startsWith("PAY_FAIL|")) {
                        setStatus("❌ " + s.substring("PAY_FAIL|".length()));
                    } else {
                        setStatus("❌ Unexpected response: " + s);
                    }
                }
            });
        };

        client.payReservation(code);
    }

    /**
     * Updates the status label text if available.
     *
     * @param s the status message to display
     */
    private void setStatus(String s) {
        if (statusLabel != null) statusLabel.setText(s);
    }
    
    /**
     * Handles the bill preview ("Find") action.
     * <p>
     * Sends a preview request for the entered confirmation code and registers an active handler
     * to populate bill details (diners/total/discount/final). Enables payment only after a
     * successful preview.
     */
    @FXML
    private void handleFind() {
        if (client == null) { setStatus("❌ No client connection."); return; }

        String code = (codeField.getText() == null) ? "" : codeField.getText().trim();
        if (code.isEmpty()) { setStatus("❌ Please enter confirmation code."); return; }

        setStatus("Searching reservation...");

        ClientSession.activeHandler = (msg) -> {
            Platform.runLater(() -> {
                if (msg instanceof String s) {
                    if (s.startsWith("PREVIEW_OK|")) {
                        String[] p = s.split("\\|");
                        dinersLabel.setText(p[1]);
                        totalLabel.setText(p[2] + "₪");
                        discountLabel.setText(p[3] + "₪");
                        finalLabel.setText(p[4] + "₪");

                        lastValidCode = code;
                        if (payBtn != null) payBtn.setDisable(false);
                        setStatus("✅ Reservation found. You can pay now.");
                    } else if (s.startsWith("PREVIEW_FAIL|")) {
                        lastValidCode = null;
                        if (payBtn != null) payBtn.setDisable(true);
                        setStatus("❌ " + s.substring("PREVIEW_FAIL|".length()));
                    }
                }
            });
        };

        client.previewBill(code);
    }

}
