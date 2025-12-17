package guestgui;

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

    @FXML
    private Button waitingListBtn;

    @FXML
    private Label sectionTitle;

    @FXML
    private Label modeBadge;

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        applyEntryMode();
        showReservation();
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
        contentArea.getChildren().setAll(
                new Label("Reservation screen (guest)")
        );
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
