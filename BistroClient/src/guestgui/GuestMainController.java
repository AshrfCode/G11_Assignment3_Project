package guestgui;

import client.ClientController;
import client.ClientSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class GuestMainController {

    public enum EntryMode { HOME, RESTAURANT }

    private EntryMode entryMode = EntryMode.HOME; // ✅ safer default
    private ClientController client;

    @FXML private Button waitingListBtn;
    @FXML private Button checkInBtn;        // ✅ must exist in FXML with fx:id="checkInBtn"
    @FXML private Label sectionTitle;
    @FXML private Label modeBadge;
    @FXML private StackPane contentArea;

    public void setClient(ClientController client) {
        this.client = client;
        showReservation();
    }

    @FXML
    public void initialize() {
        applyEntryMode();
    }

    public void setEntryMode(EntryMode mode) {
        this.entryMode = mode;
        applyEntryMode();
    }

    private void applyEntryMode() {
        boolean isRestaurant = (entryMode == EntryMode.RESTAURANT);

        if (waitingListBtn != null) {
            waitingListBtn.setVisible(isRestaurant);
            waitingListBtn.setManaged(isRestaurant);
        }

        // ✅ Check In only in Restaurant Mode
        if (checkInBtn != null) {
            checkInBtn.setVisible(isRestaurant);
            checkInBtn.setManaged(isRestaurant);
        }

        if (modeBadge != null) {
            modeBadge.setText(isRestaurant ? "Restaurant Mode" : "Home Mode");
        }
    }

    @FXML
    private void showReservation() {
        ClientSession.activeHandler = null; // ✅ avoid old handlers
        if (sectionTitle != null) sectionTitle.setText("Make Reservation");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guestgui/ReservationView.fxml"));
            Parent view = loader.load();

            ReservationController controller = loader.getController();
            controller.setClient(client);

            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
            contentArea.getChildren().setAll(new Label("❌ Failed to load reservation screen."));
        }
    }

    @FXML
    private void showCancelReservation() {
        ClientSession.activeHandler = null;
        if (sectionTitle != null) sectionTitle.setText("Cancel Reservation");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guestgui/CancelReservationView.fxml"));
            Parent view = loader.load();

            CancelReservationController controller = loader.getController();
            controller.setClient(client);

            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
            contentArea.getChildren().setAll(new Label("❌ Failed to load cancel screen."));
        }
    }

    @FXML
    private void showWaitingList() {
        ClientSession.activeHandler = null;
        if (sectionTitle != null) sectionTitle.setText("Waiting List");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guestgui/WaitingListGuestView.fxml"));
            Parent view = loader.load();

            WaitingListGuestController controller = loader.getController();
            controller.init(client);

            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
            contentArea.getChildren().setAll(new Label("❌ Failed to load waiting list screen."));
        }
    }

    @FXML
    private void showPayment() {
        ClientSession.activeHandler = null;
        if (sectionTitle != null) sectionTitle.setText("Payment");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guestgui/PaymentView.fxml"));
            Parent view = loader.load();

            PaymentController controller = loader.getController();
            controller.setClient(client);

            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
            contentArea.getChildren().setAll(new Label("❌ Failed to load payment screen."));
        }
    }

    // ✅ Make sure FXML button calls onAction="#showCheckIn"
    @FXML
    private void showCheckIn() {
        ClientSession.activeHandler = null;
        if (sectionTitle != null) sectionTitle.setText("Check In");

        try {
            // ✅ unified name to match subscriber version
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guestgui/CheckIn.fxml"));
            Parent view = loader.load();

            CheckInController controller = loader.getController();
            controller.setClient(client);

            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
            contentArea.getChildren().setAll(new Label("❌ Failed to load check-in screen."));
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientgui/BistroMain.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Bistro – Sign In");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
