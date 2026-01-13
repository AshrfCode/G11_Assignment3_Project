package subscribergui;

import client.ClientController;
import guestgui.CancelReservationController;
import guestgui.ReservationController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

public class SubscriberMainController {

    public enum EntryMode { HOME, RESTAURANT }

    @FXML private Label sectionTitle;
    @FXML private Label modeBadge;
    @FXML private Label heyUserLabel;
    @FXML private StackPane contentArea;
    @FXML private Button waitingListBtn;

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
    }

    // ---------------- NAV ACTIONS ----------------

    @FXML
    private void showReservation() {
        setSection("Make Reservation");
        try {
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
    private void showUpdateDetails() {
        setSection("Update Details");
        setPlaceholder("Update Details screen (Subscriber).");
    }

    @FXML
    private void showHistory() {
        setSection("View History");
        try {
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
