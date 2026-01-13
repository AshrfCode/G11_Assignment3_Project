package guestgui;

import client.ClientController;
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

    private EntryMode entryMode = EntryMode.RESTAURANT;
    private ClientController client;

    @FXML private Button waitingListBtn;
    @FXML private Label sectionTitle;
    @FXML private Label modeBadge;
    @FXML private StackPane contentArea;

    public void setClient(ClientController client) {
        this.client = client;
        // Reload reservation after client arrives
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
        if (modeBadge != null) {
            modeBadge.setText(isRestaurant ? "Restaurant Mode" : "Home Mode");
        }
    }

    @FXML
    private void showReservation() {
        if (sectionTitle != null) sectionTitle.setText("Make Reservation");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guestgui/ReservationView.fxml"));
            Parent view = loader.load();

            ReservationController controller = loader.getController();
            controller.setClient(client); // inject client

            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
            contentArea.getChildren().setAll(new Label("❌ Failed to load reservation screen."));
        }
    }

    // NEW: cancel page as a separate category
    @FXML
    private void showCancelReservation() {
        if (sectionTitle != null) sectionTitle.setText("Cancel Reservation");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guestgui/CancelReservationView.fxml"));
            Parent view = loader.load();

            CancelReservationController controller = loader.getController();
            controller.setClient(client); // inject client

            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
            contentArea.getChildren().setAll(new Label("❌ Failed to load cancel screen."));
        }
    }

    @FXML
    private void showWaitingList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guestgui/WaitingListGuestView.fxml"));
            Parent view = loader.load();

            
            WaitingListGuestController controller = loader.getController();
            controller.init(client); // או setClient(client) לפי איך את בונה

            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void showPayment() {
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
