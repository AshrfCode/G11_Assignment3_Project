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

/**
 * Main JavaFX controller for the guest area.
 * <p>
 * Manages navigation between guest screens (reservation, cancellation, waiting list, payment, check-in),
 * applies entry mode behavior (HOME vs RESTAURANT), and routes server responses by resetting
 * {@link ClientSession#activeHandler} when switching sections.
 */
public class GuestMainController {

    /**
     * Entry mode for the guest UI, controlling which features are available.
     */
    public enum EntryMode { HOME, RESTAURANT }

    /**
     * Current entry mode; defaults to {@link EntryMode#HOME}.
     */
    private EntryMode entryMode = EntryMode.HOME; // ✅ safer default

    /**
     * Connected client controller used to communicate with the server.
     */
    private ClientController client;

    /**
     * Button for opening the waiting list screen (restaurant mode only).
     */
    @FXML private Button waitingListBtn;

    /**
     * Button for opening the check-in screen (restaurant mode only).
     */
    @FXML private Button checkInBtn;        // ✅ must exist in FXML with fx:id="checkInBtn"

    /**
     * Label showing the currently selected section title.
     */
    @FXML private Label sectionTitle;

    /**
     * Label indicating the current entry mode (HOME/RESTAURANT).
     */
    @FXML private Label modeBadge;

    /**
     * Container area where selected screens are loaded and displayed.
     */
    @FXML private StackPane contentArea;

    /**
     * Sets the client controller and shows the default reservation screen.
     *
     * @param client the connected {@link ClientController}
     */
    public void setClient(ClientController client) {
        this.client = client;
        showReservation();
    }

    /**
     * JavaFX initialization hook.
     * <p>
     * Applies the current {@link #entryMode} to configure UI visibility.
     */
    @FXML
    public void initialize() {
        applyEntryMode();
    }

    /**
     * Sets the current entry mode and updates the UI accordingly.
     *
     * @param mode the desired {@link EntryMode}
     */
    public void setEntryMode(EntryMode mode) {
        this.entryMode = mode;
        applyEntryMode();
    }

    /**
     * Applies the current {@link #entryMode} to the UI by showing/hiding controls
     * and updating the mode badge text.
     */
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

    /**
     * Loads and displays the reservation creation screen.
     * <p>
     * Clears any previously registered {@link ClientSession#activeHandler} to avoid
     * handling responses in the wrong screen.
     */
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

    /**
     * Loads and displays the reservation cancellation screen.
     * <p>
     * Clears any previously registered {@link ClientSession#activeHandler} to avoid
     * handling responses in the wrong screen.
     */
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

    /**
     * Loads and displays the waiting list screen for guests.
     * <p>
     * Clears any previously registered {@link ClientSession#activeHandler} to avoid
     * handling responses in the wrong screen.
     */
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

    /**
     * Loads and displays the payment screen.
     * <p>
     * Clears any previously registered {@link ClientSession#activeHandler} to avoid
     * handling responses in the wrong screen.
     */
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

    
    /**
     * Loads and displays the check-in screen.
     * <p>
     * Clears any previously registered {@link ClientSession#activeHandler} to avoid
     * handling responses in the wrong screen.
     */
    @FXML
    private void showCheckIn() {
        ClientSession.activeHandler = null;
        if (sectionTitle != null) sectionTitle.setText("Check In");

        try {
            // ✅ unified name to match subscriber version
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guestgui/CheckInView.fxml"));
            Parent view = loader.load();

            CheckInController controller = loader.getController();
            controller.setClient(client);

            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
            contentArea.getChildren().setAll(new Label("❌ Failed to load check-in screen."));
        }
    }

    /**
     * Navigates back to the sign-in screen.
     * <p>
     * Clears the client session state and replaces the current scene with the main sign-in UI.
     */
    @FXML
    private void handleBack() {
        try {
        	ClientSession.clear();
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
