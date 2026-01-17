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

public class SubscriberMainController {

    public enum EntryMode { HOME, RESTAURANT }

    @FXML private Label sectionTitle;
    @FXML private Label modeBadge;
    @FXML private Label heyUserLabel;
    @FXML private StackPane contentArea;
    @FXML private Button waitingListBtn;
    @FXML private Button checkInBtn;


    private EntryMode entryMode = EntryMode.HOME;
    private String username = "Subscriber";

    private ClientController client;
    private int subscriberId = -1;
    private String subscriberEmail = "";
    private String subscriberPhone = "";

    @FXML
    public void initialize() {
        applyModeUI();
        setUsername(username);
    }

    // Called after login
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

    public void setEntryMode(EntryMode mode) {
        this.entryMode = mode;
        applyModeUI();
    }

    public void setUsername(String username) {
        if (username != null && !username.trim().isEmpty()) this.username = username.trim();
        if (heyUserLabel != null) heyUserLabel.setText("Hey " + this.username);
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
        
     // ✅ NEW: Check In only in Restaurant Mode
        if (checkInBtn != null) {
            checkInBtn.setVisible(isRestaurant);
            checkInBtn.setManaged(isRestaurant);
        }
    }

    // ---------------- NAV ACTIONS ----------------

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
