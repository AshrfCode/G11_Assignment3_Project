package representativegui;

import common.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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

    // =========================
    // INITIALIZE
    // =========================

    @FXML
    public void initialize() {
        applyModeUI();
        applyRoleUI();
        setUsername(username);
        showReservations(); // default page
    }

    // =========================
    // NAVIGATION – LOAD INTO contentArea
    // =========================

    @FXML
    private void showManageSubscribers() {
        loadContent("/representativegui/ManageSubscribers.fxml",
                "Manage Subscribers");
    }

    @FXML
    private void showManageTables() {
        loadContent("/representativegui/ManageTables.fxml",
                "Manage Tables");
    }

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
        setSection("Visual Reports");
        setPlaceholder("View visual restaurant activity reports.");
    }

    @FXML
    private void showCreateSubscriber() {
        setSection("Create Subscriber");
        setPlaceholder("Register a new subscriber.");
    }

    // =========================
    // LOGOUT
    // =========================

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/clientgui/BistroMain.fxml")
            );
            Parent root = loader.load();

            javafx.stage.Stage stage =
                    (javafx.stage.Stage) contentArea.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, 1000, 700));
            stage.centerOnScreen();
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setTitle("Bistro – Sign In");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // HELPERS
    // =========================

    private void loadContent(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
            setSection(title);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSection(String title) {
        if (sectionTitle != null) {
            sectionTitle.setText(title);
        }
    }
    
    public void setEntryMode(EntryMode mode) {
        if (mode != null) {
            this.entryMode = mode;
        }
        applyModeUI();
    }


    private void setPlaceholder(String text) {
        contentArea.getChildren().clear();
        Label lbl = new Label(text);
        lbl.getStyleClass().add("placeholder-text");
        contentArea.getChildren().add(lbl);
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

        String roleText = (role != null) ? role.name() : "UNKNOWN";
        String modeText = isRestaurant ? "Restaurant Mode" : "Home Mode";

        if (modeBadge != null) {
            modeBadge.setText(roleText + " | " + modeText);
        }

        if (waitingListBtn != null) {
            waitingListBtn.setVisible(isRestaurant);
            waitingListBtn.setManaged(isRestaurant);
        }
    }

    private void applyRoleUI() {
        boolean isManager = (role == UserRole.MANAGER);

        if (reportsBtn != null) {
            reportsBtn.setVisible(isManager);
            reportsBtn.setManaged(isManager);
        }
    }
}
