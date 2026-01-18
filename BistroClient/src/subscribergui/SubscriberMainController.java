package subscribergui;

import java.net.URL;

import client.ClientController;
import client.ClientSession;
import guestgui.CancelReservationController;
import guestgui.ReservationController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * JavaFX main controller for the subscriber area.
 * <p>
 * Hosts navigation between subscriber-related screens (reservation, cancellation, waiting list,
 * payment, check-in, profile updates, and history) within a central {@link StackPane}.
 * Maintains subscriber context (id/email/phone) to support auto-filled flows and server requests.
 */
public class SubscriberMainController {

    /**
     * Defines the entry mode that affects available features and UI badges.
     */
    public enum EntryMode { HOME, RESTAURANT }

    /**
     * Section title label for the currently displayed view.
     */
    @FXML private Label sectionTitle;

    /**
     * Badge label showing the current mode (Home/Restaurant).
     */
    @FXML private Label modeBadge;

    /**
     * Greeting label shown to the subscriber.
     */
    @FXML private Label heyUserLabel;

    /**
     * Main content area where sub-views are loaded.
     */
    @FXML private StackPane contentArea;

    /**
     * Button for accessing the waiting list view (visible only in restaurant mode).
     */
    @FXML private Button waitingListBtn;

    /**
     * Button for accessing the check-in view (visible only in restaurant mode).
     */
    @FXML private Button checkInBtn;

    /**
     * Current entry mode (Home/Restaurant).
     */
    private EntryMode entryMode = EntryMode.HOME;

    /**
     * Display name for the subscriber.
     */
    private String username = "Subscriber";

    /**
     * Connected client controller used to communicate with the server.
     */
    private ClientController client;

    /**
     * Subscriber identifier used for subscriber-only server operations.
     */
    private int subscriberId = -1;

    /**
     * Subscriber email used for auto-fill and update operations.
     */
    private String subscriberEmail = "";

    /**
     * Subscriber phone used for auto-fill and update operations.
     */
    private String subscriberPhone = "";

    /**
     * JavaFX initialization hook.
     * <p>
     * Applies UI state based on the current mode and sets the initial greeting.
     */
    @FXML
    public void initialize() {
        applyModeUI();
        setUsername(username);
    }

    /**
     * Initializes the controller after a successful subscriber login.
     * <p>
     * Stores the connected client and subscriber context, applies the chosen entry mode,
     * updates the greeting, and loads the reservation screen by default.
     *
     * @param client   the connected {@link ClientController}
     * @param mode     the entry mode (home or restaurant)
     * @param username the display name of the subscriber
     * @param id       the subscriber ID
     * @param email    the subscriber email (used for auto-fill)
     * @param phone    the subscriber phone (used for auto-fill)
     */
    public void initAfterLogin(ClientController client, EntryMode mode,
                               String username, int id, String email, String phone) {

        this.client = client;
        this.entryMode = mode;

        this.subscriberId = id;
        this.subscriberEmail = (email == null) ? "" : email.trim();
        this.subscriberPhone = (phone == null) ? "" : phone.trim();

        setUsername(username);
        applyModeUI();

        showReservation();
    }

    /**
     * Updates the current entry mode and applies mode-specific UI visibility rules.
     *
     * @param mode the new entry mode
     */
    public void setEntryMode(EntryMode mode) {
        this.entryMode = mode;
        applyModeUI();
    }

    /**
     * Sets the displayed username and updates the greeting label.
     *
     * @param username the subscriber display name
     */
    public void setUsername(String username) {
        if (username != null && !username.trim().isEmpty()) this.username = username.trim();
        if (heyUserLabel != null) heyUserLabel.setText("Hey " + this.username);
    }

    /**
     * Applies UI changes based on the current {@link #entryMode}.
     * <p>
     * Toggles visibility/management of restaurant-only actions and updates the mode badge.
     */
    private void applyModeUI() {
        boolean isRestaurant = (entryMode == EntryMode.RESTAURANT);

        if (modeBadge != null) {
            modeBadge.setText(isRestaurant ? "Restaurant Mode" : "Home Mode");
        }

        if (waitingListBtn != null) {
            waitingListBtn.setVisible(isRestaurant);
            waitingListBtn.setManaged(isRestaurant);
        }
        
     // ✅ NEW: Check In only in Restaurant Mode
        if (checkInBtn != null) {
            checkInBtn.setVisible(isRestaurant);
            checkInBtn.setManaged(isRestaurant);
        }
    }

    // ---------------- NAV ACTIONS ----------------

    /**
     * Loads the reservation creation view into the content area in subscriber mode.
     * <p>
     * Clears any existing active handler and injects subscriber info for auto-fill and ownership logic.
     */
    @FXML
    private void showReservation() {
        setSection("Make Reservation");
        try {
            ClientSession.activeHandler = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guestgui/ReservationView.fxml"));
            Parent view = loader.load();

            ReservationController controller = loader.getController();
            controller.setClient(client);
            controller.setSubscriberInfo(subscriberId, subscriberEmail, subscriberPhone);

            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            setPlaceholder("❌ Failed to load reservation screen (Subscriber).");
        }
    }

    /**
     * Loads the reservation cancellation view into the content area in subscriber mode.
     * <p>
     * Clears any existing active handler and injects subscriber info to enforce ownership restrictions.
     */
    @FXML
    private void showCancelReservation() {
        setSection("Cancel Reservation");
        try {
            ClientSession.activeHandler = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guestgui/CancelReservationView.fxml"));
            Parent view = loader.load();

            CancelReservationController controller = loader.getController();
            controller.setClient(client);
            controller.setSubscriberInfo(subscriberId, subscriberEmail, subscriberPhone);

            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            setPlaceholder("❌ Failed to load cancel reservation screen.");
        }
    }

    /**
     * Loads the subscriber waiting list view into the content area and initializes it with subscriber context.
     * <p>
     * Clears any existing active handler before loading the view.
     */
    @FXML
    private void showWaitingList() {
        setSection("Waiting List");

        try {
            ClientSession.activeHandler = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("WaitingList.fxml"));
            Parent view = loader.load();

            WaitingListController controller = loader.getController();
            controller.init(client, subscriberId);

            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
            setPlaceholder("❌ Failed to load waiting list screen.");
        }
    }

    /**
     * Loads the payment view into the content area.
     * <p>
     * Clears any existing active handler and injects the connected client.
     */
    @FXML
    private void showPayment() {
        setSection("Pay");
        try {
            ClientSession.activeHandler = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guestgui/PaymentView.fxml"));
            Parent view = loader.load();

            guestgui.PaymentController controller = loader.getController();
            controller.setClient(client);

            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            setPlaceholder("❌ Failed to load payment screen.");
        }
    }

    /**
     * Loads the check-in view into the content area for restaurant mode usage.
     * <p>
     * Attempts to locate the check-in FXML under multiple expected names, injects client and
     * prefill data, requests active reservation codes for the subscriber, and passes them to the UI.
     *
     * @throws IllegalStateException if the check-in FXML resource cannot be found
     */
    @FXML
    private void showCheckIn() {

        setSection("Check In");
        try {
            ClientSession.activeHandler = null;

            // 1. Load FXML
            URL url = getClass().getResource("/guestgui/CheckIn.fxml");
            if (url == null) url = getClass().getResource("/guestgui/CheckInView.fxml");
            if (url == null) throw new IllegalStateException("CheckIn FXML not found.");

            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            guestgui.CheckInController controller = loader.getController();
            controller.setClient(client);

            // ✅ USE SIMPLE VARIABLES (No Subscriber Entity)
            // Assuming 'subscriberEmail' and 'subscriberPhone' are fields in this class
            controller.setPrefill(subscriberEmail, subscriberPhone);

            // 2. Set up the handler to receive the codes
            ClientSession.activeHandler = (msg) -> Platform.runLater(() -> {
                if (msg instanceof java.util.ArrayList) {
                    java.util.ArrayList<String> codes = (java.util.ArrayList<String>) msg;
                    
                    // Show the list in the UI
                    controller.setSubscriberReservations(codes);
                }
            });

            // 3. Send Request using your ID variable
            client.fetchSubscriberCodes(subscriberId); 

            contentArea.getChildren().setAll(view);
            
        } catch (Exception e) {
            System.out.println("EXCEPTION in showCheckIn:"); 
            e.printStackTrace();
            setPlaceholder("❌ Failed to load check-in screen.");
        }
    }

    /**
     * Loads the update details view and initializes it with the subscriber's current contact details.
     * <p>
     * Clears any existing active handler and provides a callback that updates local cached
     * email/phone fields after a successful update.
     */
    @FXML
    private void showUpdateDetails() {
        setSection("Update Details");
        try {
            ClientSession.activeHandler = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subscribergui/UpdateDetailsView.fxml"));
            Parent view = loader.load();

            UpdateDetailsController controller = loader.getController();
            controller.init(
                    client,
                    subscriberId,
                    subscriberEmail,
                    subscriberPhone,
                    (newEmail, newPhone) -> {
                        // update local fields after success
                        subscriberEmail = (newEmail == null) ? "" : newEmail.trim();
                        subscriberPhone = (newPhone == null) ? "" : newPhone.trim();
                    }
            );

            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
            setPlaceholder("❌ Failed to load update details screen.");
        }
    }

    /**
     * Loads the reservation history view and triggers a history request for the subscriber.
     * <p>
     * Clears any existing active handler and initializes the history controller with the subscriber ID.
     */
    @FXML
    private void showHistory() {
        setSection("View History");
        try {
            ClientSession.activeHandler = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subscribergui/HistoryView.fxml"));
            Parent view = loader.load();

            HistoryController controller = loader.getController();
            controller.init(client, subscriberId);

            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            setPlaceholder("❌ Failed to load history screen.");
        }
    }

    /**
     * Logs out the current user, clears session state, and navigates back to the sign-in screen.
     */
    @FXML
    private void handleLogout() {
        try {
            ClientSession.clear();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientgui/BistroMain.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.centerOnScreen();
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setTitle("Bistro – Sign In");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- HELPERS ----------------

    /**
     * Updates the section title displayed in the UI.
     *
     * @param title the new section title
     */
    private void setSection(String title) {
        if (sectionTitle != null) sectionTitle.setText(title);
    }

    /**
     * Displays a placeholder message in the content area.
     *
     * @param text placeholder message to show
     */
    private void setPlaceholder(String text) {
        contentArea.getChildren().clear();
        Label lbl = new Label(text);
        lbl.getStyleClass().add("placeholder-text");
        contentArea.getChildren().add(lbl);
    }
}
