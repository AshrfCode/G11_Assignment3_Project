package subscribergui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class SubscriberMainController {

    public enum EntryMode {
        HOME,
        RESTAURANT
    }

    @FXML private Label sectionTitle;
    @FXML private Label modeBadge;
    @FXML private Label heyUserLabel;

    @FXML private StackPane contentArea;

    @FXML private Button waitingListBtn;

    private EntryMode entryMode = EntryMode.HOME;
    private String username = "Subscriber";

    @FXML
    public void initialize() {
        applyModeUI();
        setUsername(username);
        showReservation(); // default page (you can change)
    }

    public void setEntryMode(EntryMode mode) {
        this.entryMode = mode;
        applyModeUI();
    }

    public void setUsername(String username) {
        if (username != null && !username.trim().isEmpty()) {
            this.username = username.trim();
        }
        heyUserLabel.setText("Hey " + this.username);
    }

    private void applyModeUI() {
        boolean isRestaurant = (entryMode == EntryMode.RESTAURANT);

        if (modeBadge != null) {
            modeBadge.setText(isRestaurant ? "Restaurant Mode" : "Home Mode");
        }

        if (waitingListBtn != null) {
            waitingListBtn.setVisible(isRestaurant);
            waitingListBtn.setManaged(isRestaurant);
        }
    }

    // ---------------- NAV ACTIONS ----------------

    @FXML
    private void showReservation() {
        setSection("Make Reservation");
        setPlaceholder("Reservation screen (Subscriber) – not implemented yet.");
    }

    @FXML
    private void showWaitingList() {
        setSection("Waiting List");
        setPlaceholder("Waiting list screen (Subscriber) – only in Restaurant Mode.");
    }

    @FXML
    private void showPayment() {
        setSection("Pay");
        setPlaceholder("Payment screen (Subscriber) – includes 10% discount logic later.");
    }

    @FXML
    private void showUpdateDetails() {
        setSection("Update Details");
        setPlaceholder("Update Details – allow editing only Email + Phone Number.");
    }

    @FXML
    private void showHistory() {
        setSection("View History");
        setPlaceholder("History – reservations + visits (read-only).");
    }

    // ---------------- LOGOUT ----------------

    @FXML
    private void handleLogout() {
        // for now: just go back to sign-in (same stage)
        // later you can also clear session/user data here
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/clientgui/BistroMain.fxml")
            );
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 700);
            stage.setScene(scene);

            stage.centerOnScreen();
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setTitle("Bistro – Sign In");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- HELPERS ----------------

    private void setSection(String title) {
        if (sectionTitle != null) sectionTitle.setText(title);
    }

    private void setPlaceholder(String text) {
        contentArea.getChildren().clear();
        Label lbl = new Label(text);
        lbl.getStyleClass().add("placeholder-text");
        contentArea.getChildren().add(lbl);
    }
}
