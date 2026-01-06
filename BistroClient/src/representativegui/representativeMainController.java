package representativegui;

import common.UserRole;
import java.util.List;

import client.ClientController;
import client.ClientSession;
import common.ClientRequest;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import client.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class representativeMainController {
	
	private ClientController client;

    public void setClient(ClientController client) {
        this.client = client;
    }


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

        if (client == null) {
            setPlaceholder("❌ No server connection (client is null).");
            return;
        }

        setPlaceholder("Loading today's reservations...");

        ListView<String> listView = new ListView<>();
        contentArea.getChildren().setAll(listView);

        ClientSession.activeHandler = (msg) -> {
            if (msg instanceof List<?> list) {
                Platform.runLater(() -> {
                    listView.getItems().clear();

                    if (list.isEmpty()) {
                        listView.getItems().add("No reservations for today ✅");
                        return;
                    }

                    if (list.get(0) instanceof String) {
                        @SuppressWarnings("unchecked")
                        List<String> items = (List<String>) list;
                        listView.getItems().addAll(items);
                    } else {
                        listView.getItems().add("⚠️ Unexpected data from server.");
                    }
                });
            }
        };

        client.sendRequest(new ClientRequest(ClientRequest.CMD_GET_TODAY_RESERVATIONS, new Object[]{}));
    }

    @FXML
    private void showWaitingList() {
        setSection("Waiting List");

        if (client == null) {
            setPlaceholder("❌ No server connection (client is null).");
            return;
        }

        setPlaceholder("Loading waiting list...");

        ListView<String> listView = new ListView<>();
        contentArea.getChildren().clear();
        contentArea.getChildren().add(listView);

        ClientSession.activeHandler = (msg) -> {
            if (msg instanceof List<?> list) {
                Platform.runLater(() -> {
                    listView.getItems().clear();

                    if (list.isEmpty()) {
                        listView.getItems().add("No one is currently waiting ✅");
                        return;
                    }

                    if (list.get(0) instanceof String) {
                        @SuppressWarnings("unchecked")
                        List<String> items = (List<String>) list;
                        listView.getItems().addAll(items);
                    } else {
                        listView.getItems().add("⚠️ Unexpected data from server.");
                    }
                });
            }
        };

        client.sendRequest(new ClientRequest(ClientRequest.CMD_GET_WAITING_LIST, new Object[]{}));
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
