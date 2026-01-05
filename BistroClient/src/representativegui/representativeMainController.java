package representativegui;

import common.UserRole;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class representativeMainController {

    public enum EntryMode {
        HOME,
        RESTAURANT
    }
    
    private UserRole role = UserRole.REPRESENTATIVE;

    @FXML private Label sectionTitle;
    @FXML private Label modeBadge;
    @FXML private Label heyUserLabel;

    @FXML private StackPane contentArea;

    @FXML private Button waitingListBtn;
    @FXML private Button reportsBtn;


    private EntryMode entryMode = EntryMode.HOME;
    private String username = "Subscriber";

    @FXML
    public void initialize() {
        applyModeUI();
        applyRoleUI();
        setUsername(username);
        showReservations(); // default page (you can change)
    }
    
    @FXML
    private void showManageTables() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/representativegui/ManageTables.fxml")
            );
            javafx.scene.Parent root = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
    
    public void setRole(UserRole role) {
        if (role != null) {
            this.role = role;
        }
        applyRoleUI();
        applyModeUI();
    }


    private void applyModeUI() {
        boolean isRestaurant = (entryMode == EntryMode.RESTAURANT);

        String modeText = isRestaurant ? "Restaurant Mode" : "Home Mode";
        String roleText = (role != null) ? role.name() : "UNKNOWN";

        if (modeBadge != null) {
            modeBadge.setText(roleText + " | " + modeText);
        }

        if (waitingListBtn != null) {
            waitingListBtn.setVisible(isRestaurant);
            waitingListBtn.setManaged(isRestaurant);
        }
    }


    // ---------------- NAV ACTIONS ----------------

    @FXML
    private void showReservations() {
        setSection("Today's Reservations");
        setPlaceholder("View and manage today's reservations.");
    }

    @FXML
    private void showWaitingList() {
        setSection("Waiting List");
        setPlaceholder("Manage waiting list for the restaurant.");
    }

    @FXML
    private void showOrders() {
        setSection("Orders");
        setPlaceholder("View and manage restaurant orders.");
    }

    @FXML
    private void showReports() {
        setSection("Reports");
        setPlaceholder("View restaurant activity reports.");
    }
    
    @FXML
    private void showVisualReports() {
        setSection("Visual");
        setPlaceholder("View visual restaurant activity reports.");
    }

    @FXML
    private void showCreateSubscriber() {
        setSection("Create Subscriber");
        setPlaceholder("Register a new subscriber and generate a subscription number.");
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
    
    private void applyRoleUI() {
        boolean isManager = (role == UserRole.MANAGER);

        if (reportsBtn != null) {
            reportsBtn.setVisible(isManager);
            reportsBtn.setManaged(isManager);
        }
    }

}
