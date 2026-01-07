package guestgui;

import client.ClientController;
import client.ClientSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PaymentController {

    @FXML private TextField codeField;
    @FXML private Label dinersLabel;
    @FXML private Label totalLabel;
    @FXML private Label discountLabel;
    @FXML private Label finalLabel;
    @FXML private Label statusLabel;
    @FXML private Button payBtn;
    private String lastValidCode = null;


    private ClientController client;

    public void setClient(ClientController client) {
        this.client = client;
    }

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

    private void setStatus(String s) {
        if (statusLabel != null) statusLabel.setText(s);
    }
    
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
