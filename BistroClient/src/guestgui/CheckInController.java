package guestgui;

import client.ClientController;
import client.ClientSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CheckInController {

    @FXML private TextField codeField;
    @FXML private Label statusLabel;

    private ClientController client;

    private String prefillEmail = "";
    private String prefillPhone = "";

    public void setPrefill(String email, String phone) {
        this.prefillEmail = (email == null) ? "" : email.trim();
        this.prefillPhone = (phone == null) ? "" : phone.trim();
    }

    public void setClient(ClientController client) {
        this.client = client;
    }

    @FXML
    private void handleCheckIn() {
        if (client == null) {
            setStatus("❌ No client connection.");
            return;
        }

        String code = (codeField.getText() == null) ? "" : codeField.getText().trim();

        if (code.isEmpty()) {
            setStatus("❌ Please enter confirmation code.");
            return;
        }

        setStatus("Checking details...");

        ClientSession.activeHandler = (msg) -> Platform.runLater(() -> {
            if (msg instanceof Integer tableNumber) {
                setStatus("✅ Success! Assigned to Table #" + tableNumber);
                showSuccessAlert(tableNumber);
            } else if (msg instanceof String errorMsg) {
                setStatus("❌ " + errorMsg);
            } else {
                setStatus("❌ Unexpected response from server.");
            }
        });

        client.checkInCustomer(code);
    }

    @FXML
    private void handleForgot() {
        if (client == null) {
            setStatus("❌ No client connection.");
            return;
        }

        Dialog<Object[]> dialog = new Dialog<>();
        dialog.setTitle("Forgot Confirmation Code");
        dialog.setHeaderText("Enter the same email + phone used in the reservation:");

        ButtonType sendBtn = new ButtonType("Send", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendBtn, ButtonType.CANCEL);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");

        // ✅ Prefill
        if (!prefillEmail.isEmpty()) emailField.setText(prefillEmail);
        if (!prefillPhone.isEmpty()) phoneField.setText(prefillPhone);

        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(10, emailField, phoneField);
        box.setPadding(new javafx.geometry.Insets(10));
        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(bt -> {
            if (bt == sendBtn) {
                return new Object[]{ emailField.getText().trim(), phoneField.getText().trim() };
            }
            return null;
        });

        var result = dialog.showAndWait();
        if (result.isEmpty() || result.get() == null) return;

        Object[] data = result.get();
        String email = (String) data[0];
        String phone = (String) data[1];

        if (email.isEmpty() || phone.isEmpty()) {
            setStatus("❌ Please enter both email and phone.");
            return;
        }

        setStatus("Sending code (if details match)...");

        ClientSession.activeHandler = (msg) -> Platform.runLater(() -> {
            // server sends generic reply, so always show generic success
            setStatus("✅ If details match, the code was sent.");
        });

        // ✅ Use your client helper (keeps one consistent place)
        client.forgotConfirmationCode(email, phone);
    }

    private void setStatus(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
            if (text.startsWith("✅")) {
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        }
    }

    private void showSuccessAlert(int tableNumber) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Check In Successful");
        alert.setHeaderText("Welcome to Bistro!");
        alert.setContentText("Your table is ready.\nPlease proceed to Table Number: " + tableNumber);
        alert.showAndWait();
    }
}
