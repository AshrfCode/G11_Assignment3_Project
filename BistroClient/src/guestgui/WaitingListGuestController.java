package guestgui;

import client.ClientController;
import client.ClientSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class WaitingListGuestController {

    @FXML private TextField txtDiners;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCode;   // בשביל Leave לפי קוד
    @FXML private Label lblStatus;

    private ClientController client;

    public void init(ClientController client) {
        this.client = client;

        if (lblStatus != null) {
            lblStatus.setText("");
            lblStatus.setStyle("");
        }

        ClientSession.activeHandler = (msg) -> {
            if (!(msg instanceof String s)) return;
            System.out.println("CLIENT <- " + s);

            Platform.runLater(() -> {
                if (s.startsWith("WAITING_GUEST_JOIN_OK|")) {
                    String code = extractAfterPipe(s);
                    lblStatus.setText("Successfully added to waiting list. Code: " + code);
                    lblStatus.setStyle("-fx-text-fill: #1B8F3A; -fx-font-weight: bold;");
                    if (txtCode != null) txtCode.setText(code); // נוח להעתקה/Leave
                    return;
                }

                if (s.startsWith("WAITING_GUEST_JOIN_FAIL")) {
                    String reason = extractAfterPipe(s);
                    lblStatus.setText("Failed to add: " + (reason.isBlank() ? "DB error" : reason));
                    lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                    return;
                }

                if (s.startsWith("WAITING_GUEST_LEAVE_OK")) {
                    lblStatus.setText("Successfully removed from waiting list");
                    lblStatus.setStyle("-fx-text-fill: #1B8F3A; -fx-font-weight: bold;");
                    return;
                }

                if (s.startsWith("WAITING_GUEST_LEAVE_FAIL")) {
                    String reason = extractAfterPipe(s);
                    lblStatus.setText("Failed to remove: " + (reason.isBlank() ? "Not found" : reason));
                    lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                }
            });
        };
    }

    @FXML
    private void handleJoin() {
        if (client == null) { showError("Client is not connected"); return; }

        int diners;
        try {
            diners = Integer.parseInt(txtDiners.getText().trim());
            if (diners <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            showError("Please enter a valid diners number");
            return;
        }

        String phone = (txtPhone == null || txtPhone.getText() == null) ? "" : txtPhone.getText().trim();
        String email = (txtEmail == null || txtEmail.getText() == null) ? "" : txtEmail.getText().trim();

        if (phone.isBlank() && email.isBlank()) {
            showError("Please enter phone or email");
            return;
        }

        showInfo("Sending join request...");
        client.joinWaitingListAsGuest(diners, phone, email);
    }

    @FXML
    private void handleLeave() {
        if (client == null) { showError("Client is not connected"); return; }

        String code = (txtCode == null || txtCode.getText() == null) ? "" : txtCode.getText().trim();
        if (code.isBlank()) {
            showError("Please enter your waiting code (WLxxxxxx)");
            return;
        }

        showInfo("Sending leave request...");
        client.leaveWaitingListAsGuest(code);
    }

    private void showInfo(String msg) {
        lblStatus.setText(msg);
        lblStatus.setStyle("-fx-text-fill: #666666;");
    }
    

    private void showError(String msg) {
        lblStatus.setText(msg);
        lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
    }

    private String extractAfterPipe(String serverMsg) {
        int idx = serverMsg.indexOf('|');
        if (idx < 0 || idx + 1 >= serverMsg.length()) return "";
        return serverMsg.substring(idx + 1).trim();
    }
}
