package subscribergui;

import client.ClientController;
import client.ClientSession;
import common.ClientRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class WaitingListController {

    @FXML private TextField txtDiners;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private Label lblStatus;

    private ClientController client;
    private int subscriberId;

    public void init(ClientController client, int subscriberId) {
        this.client = client;
        this.subscriberId = subscriberId;

        if (lblStatus != null) {
            lblStatus.setText("");
            lblStatus.setStyle("");
        }

        // handler רק למסך הזה
        ClientSession.activeHandler = (msg) -> {
            if (!(msg instanceof String s)) return;

            // 🔎 חשוב לראות מה מגיע מהשרת (להשאיר בזמן בדיקה)
            System.out.println("CLIENT <- " + s);

            Platform.runLater(() -> {
                if (s.startsWith("WAITING_JOIN_OK|")) {
                    String code = extractAfterPipe(s);

                    // ✅ מציגים רק אם זה באמת נראה כמו קוד
                    if (isValidWaitingCode(code)) {
                        lblStatus.setText("Successfully added to waiting list. Code: " + code);
                    } else {
                        // אם השרת שלח משהו אחר (כמו "Inserted to DB") נדע מיד
                        lblStatus.setText("Successfully added to waiting list. (Missing/invalid code: " + code + ")");
                    }
                    lblStatus.setStyle("-fx-text-fill: #1B8F3A; -fx-font-weight: bold;");
                    return;
                }

                if (s.startsWith("WAITING_JOIN_FAIL")) {
                    String reason = extractAfterPipe(s);
                    lblStatus.setText(reason.isEmpty()
                            ? "Failed to add to waiting list"
                            : "Failed to add to waiting list: " + reason);
                    lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                    return;
                }

                if (s.startsWith("WAITING_LEAVE_OK")) {
                    lblStatus.setText("Successfully removed from waiting list");
                    lblStatus.setStyle("-fx-text-fill: #1B8F3A; -fx-font-weight: bold;");
                    return;
                }

                if (s.startsWith("WAITING_LEAVE_FAIL")) {
                    String reason = extractAfterPipe(s);
                    lblStatus.setText(reason.isEmpty()
                            ? "Failed to remove from waiting list"
                            : "Failed to remove from waiting list: " + reason);
                    lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                }
            });
        };
    }

    @FXML
    private void handleJoin() {
        if (client == null) {
            showError("Client is not connected");
            return;
        }
        if (subscriberId <= 0) {
            showError("Subscriber not set");
            return;
        }

        int diners;
        try {
            diners = Integer.parseInt(txtDiners.getText().trim());
            if (diners <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            showError("Please enter a valid diners number");
            return;
        }

        showInfo("Sending join request...");

        String phone = (txtPhone == null || txtPhone.getText() == null) ? "" : txtPhone.getText().trim();
        String email = (txtEmail == null || txtEmail.getText() == null) ? "" : txtEmail.getText().trim();

        client.joinWaitingListAsSubscriber(subscriberId, diners, phone, email);
    }

    @FXML
    private void handleLeave() {
        if (client == null) {
            showError("Client is not connected");
            return;
        }
        if (subscriberId <= 0) {
            showError("Subscriber not set");
            return;
        }

        showInfo("Sending leave request...");

        client.sendRequest(new ClientRequest(
                ClientRequest.CMD_LEAVE_WAITING_LIST,
                new Object[]{ subscriberId }
        ));
    }

    private void showInfo(String msg) {
        Platform.runLater(() -> {
            lblStatus.setText(msg);
            lblStatus.setStyle("-fx-text-fill: #666666;");
        });
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            lblStatus.setText(msg);
            lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
        });
    }

    // מחלץ את מה שאחרי ה-| אם קיים
    private String extractAfterPipe(String serverMsg) {
        int idx = serverMsg.indexOf('|');
        if (idx < 0 || idx + 1 >= serverMsg.length()) return "";
        return serverMsg.substring(idx + 1).trim();
    }

    // ✅ ולידציה לקוד המתנה (WL + 6 ספרות)
    private boolean isValidWaitingCode(String code) {
        if (code == null) return false;
        return code.matches("^WL\\d{6}$");
    }
}
