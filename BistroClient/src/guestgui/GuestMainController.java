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

    public enum EntryMode {
        HOME,
        RESTAURANT
    }

    private EntryMode entryMode = EntryMode.RESTAURANT; // default
    private ClientController client;

    @FXML
    private Button waitingListBtn;

    @FXML
    private Label sectionTitle;

    @FXML
    private Label modeBadge;

    @FXML
    private StackPane contentArea;

    public void setClient(ClientController client) {
        this.client = client;
        // Added: reload reservation screen after client arrives so ReservationController gets a real client.
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
        if (entryMode == EntryMode.HOME) {
            waitingListBtn.setVisible(false);
            waitingListBtn.setManaged(false);
            modeBadge.setText("Home Mode");
        } else {
            waitingListBtn.setVisible(true);
            waitingListBtn.setManaged(true);
            modeBadge.setText("Restaurant Mode");
        }
    }

    @FXML
    private void showReservation() {
        sectionTitle.setText("Make Reservation");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guestgui/ReservationView.fxml"));
            Parent view = loader.load();

            ReservationController controller = loader.getController();
            controller.setClient(client); // inject client (now it is not null)

            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
            contentArea.getChildren().setAll(new Label("❌ Failed to load reservation screen."));
        }
    }


    @FXML
    private void showWaitingList() {
    	sectionTitle.setText("Waiting List");
        contentArea.getChildren().setAll(
                new Label("Waiting list (restaurant only)")
        );
    }

    @FXML
    private void showPayment() {
    	sectionTitle.setText("Payment");
        contentArea.getChildren().setAll(
                new Label("Payment screen (guest)")
        );
    }
    
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/clientgui/BistroMain.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();

            // ✅ FORCE SAME SIZE
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setTitle("Bistro – Sign In");
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
