package guestgui;

import client.ClientController;
import client.ClientSession;
import javafx.application.Platform;
import java.util.List;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * JavaFX controller for the check-in flow.
 * <p>
 * Allows guests (and subscribers) to check in using a reservation confirmation code.
 * Supports optional quick-selection of active subscriber reservation codes and a
 * "forgot confirmation code" dialog that requests email and phone.
 */
public class CheckInController {

    /**
     * Text field for entering a reservation confirmation code.
     */
    @FXML private TextField codeField;

    /**
     * Label used to display status messages and errors to the user.
     */
    @FXML private Label statusLabel;
    
    /**
     * Container for the quick-select list of active reservation codes (subscriber flow).
     */
    @FXML private VBox quickSelectBox;

    /**
     * List view displaying active reservation codes for quick selection.
     */
    @FXML private ListView<String> codeList;

    /**
     * Connected client controller used to communicate with the server.
     */
    private ClientController client;

    /**
     * Email value used to prefill the "forgot confirmation code" dialog.
     */
    private String prefillEmail = "";

    /**
     * Phone value used to prefill the "forgot confirmation code" dialog.
     */
    private String prefillPhone = "";

    /**
     * Sets optional prefill values for the "forgot confirmation code" dialog.
     *
     * @param email email to prefill (may be null)
     * @param phone phone to prefill (may be null)
     */
    public void setPrefill(String email, String phone) {
        this.prefillEmail = (email == null) ? "" : email.trim();
        this.prefillPhone = (phone == null) ? "" : phone.trim();
    }

    /**
     * Sets the client controller used for server requests.
     *
     * @param client the connected {@link ClientController}
     */
    public void setClient(ClientController client) {
        this.client = client;
    }
    
    /**
     * Displays a quick-select list of active reservation codes for a subscriber.
     * <p>
     * If no codes are provided, the quick-select UI is hidden.
     * Selecting a code fills the confirmation code field; double-clicking can trigger check-in.
     *
     * @param activeCodes list of active reservation confirmation codes (may be null/empty)
     */
    /**
     * ✅ NEW METHOD: Call this ONLY from SubscriberGUI.
     * If the user has active reservations, show them.
     */
    public void setSubscriberReservations(List<String> activeCodes) {
        if (activeCodes == null || activeCodes.isEmpty()) {
            // Keep hidden if no codes
            quickSelectBox.setVisible(false);
            quickSelectBox.setManaged(false);
            return;
        }

        // Show the list
        quickSelectBox.setVisible(true);
        quickSelectBox.setManaged(true);

        ObservableList<String> items = FXCollections.observableArrayList(activeCodes);
        codeList.setItems(items);

        // Listener: When a user clicks a code, fill the text field
        codeList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                codeField.setText(newVal);
                setStatus(""); // Clear previous errors
            }
        });
        
        // Optional Perk: Double-click to auto-submit
        codeList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && codeList.getSelectionModel().getSelectedItem() != null) {
                 handleCheckIn();
            }
        });
    }

    /**
     * Handles the check-in action.
     * <p>
     * Validates the confirmation code, registers an active handler to process the server response,
     * and sends a check-in request. On success, the server is expected to return a table number.
     */
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

    /**
     * Handles the "forgot confirmation code" action.
     * <p>
     * Prompts the user for the same email and phone used in the reservation (with optional prefills),
     * registers an active handler for the server reply, and sends the forgot-code request.
     */
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

    /**
     * Updates the status label text and applies simple styling based on success/error state.
     *
     * @param text the status text to display
     */
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

    /**
     * Displays a modal success alert indicating the assigned table number.
     *
     * @param tableNumber the assigned table number returned by the server
     */
    private void showSuccessAlert(int tableNumber) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Check In Successful");
        alert.setHeaderText("Welcome to Bistro!");
        alert.setContentText("Your table is ready.\nPlease proceed to Table Number: " + tableNumber);
        alert.showAndWait();
    }
}
