package clientgui;

import client.ClientController;
import client.ClientManager;
import common.ChatIF;
import common.ClientRequest;
import guestgui.GuestMainController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

/**
 * Main JavaFX controller for the Bistro client landing screen.
 * <p>
 * Handles login flows (email/password and member-code quick access), guest access routing,
 * and navigation to role-specific views based on server responses.
 * Implements {@link ChatIF} to receive and display asynchronous server messages.
 */
public class BistroMainController implements ChatIF {

    /**
     * Status label used to display login feedback and errors.
     */
    @FXML private Label statusLabel;

    /**
     * Input field for the user's email/username.
     */
    @FXML private TextField txtUsername;

    /**
     * Input field for the user's password.
     */
    @FXML private PasswordField txtPassword;

    /**
     * Toggle indicating HOME entry mode (as opposed to RESTAURANT mode).
     */
    @FXML private ToggleButton toggleHome;

    /**
     * Toggle indicating RESTAURANT entry mode (as opposed to HOME mode).
     */
    @FXML private ToggleButton toggleRestaurant;

    /**
     * Input field for quick access member code (e.g., card/scan code).
     */
    @FXML private TextField txtMemberCode;

    /**
     * Connected client controller used to communicate with the server.
     */
    private ClientController client;

    /**
     * JavaFX initialization hook.
     * <p>
     * Retrieves the singleton {@link ClientController} and binds it to this UI instance
     * so that incoming server messages are routed to {@link #display(String)}.
     */
    @FXML
    public void initialize() {
        // Always get the singleton client and bind it to THIS UI
        this.client = ClientManager.getClient(this);
    }

    /**
     * Displays a message originating from the server.
     * <p>
     * Handles login success/failure protocol messages and triggers navigation
     * to the appropriate role-specific screens.
     *
     * @param message the message to display/process
     */
    @Override
    public void display(String message) {

        javafx.application.Platform.runLater(() -> {

            if (message == null) return;

            // LOGIN FAILED
            if (message.startsWith("LOGIN_FAIL")) {
                String[] parts = message.split("\\|", 2);
                statusLabel.setText(parts.length > 1 ? parts[1] : "Login failed");
                return;
            }

            // LOGIN SUCCESS
            if (message.startsWith("LOGIN_OK")) {
                String[] parts = message.split("\\|");

                // Changed: now we expect at least 6 parts: LOGIN_OK|ROLE|NAME|ID|EMAIL|PHONE
                if (parts.length < 6) {
                    statusLabel.setText("Invalid login response");
                    return;
                }

                String role = parts[1];
                String username = parts[2];

                // Added: parse subscriber data so reservation can be automatic.
                int userId = -1;
                try { userId = Integer.parseInt(parts[3]); } catch (Exception ignored) {}
                String email = parts[4] == null ? "" : parts[4];
                String phone = parts[5] == null ? "" : parts[5];

                switch (role) {
                    case "SUBSCRIBER" -> openSubscriberView(username, userId, email, phone);
                    case "REPRESENTATIVE" -> openRepresentativeView(username);
                    case "MANAGER" -> openManagerView(username, role);
                    
                    default -> statusLabel.setText("Unknown role");
                }
            }
        });
    }

    /**
     * Handles the email/password login action.
     * <p>
     * Validates required input fields and sends a login request to the server.
     */
    @FXML
    private void handleLogin() {

        String email = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter email and password.");
            return;
        }

        try {
            client.sendToServer(
                new common.LoginRequest(email, password)
            );
            statusLabel.setText("Logging in...");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to contact server.");
        }
    }

    /**
     * Handles quick access login using a member code (e.g., card scan).
     * <p>
     * Forces RESTAURANT mode, then sends a card-based login request to the server.
     */
    @FXML
    private void handleQuickAccess() {
        String code = txtMemberCode.getText().trim();

        if (code.isEmpty()) {
            statusLabel.setText("Please scan or enter a member code.");
            return;
        }

        // 1. Force the UI to Restaurant mode as requested
        // This ensures that when display() processes the LOGIN_OK, 
        // it opens the Subscriber view in RESTAURANT mode.
        toggleRestaurant.setSelected(true); 

        // 2. Send the new request type
        try {
            client.sendToServer(new common.CardLoginRequest(code));
            statusLabel.setText("Verifying card...");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to contact server.");
        }
    }

    /**
     * Handles guest access navigation.
     * <p>
     * Loads the guest UI, configures entry mode based on toggles, passes the connected client,
     * and switches the current stage scene to the guest view.
     */
    @FXML
    private void handleGuestAccess() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/guestgui/GuestMain.fxml")
            );

            Parent root = loader.load();

            // Get Guest controller
            GuestMainController controller = loader.getController();

            // Decide entry mode based on toggle
            if (toggleHome.isSelected()) {
                controller.setEntryMode(GuestMainController.EntryMode.HOME);
            } else {
                controller.setEntryMode(GuestMainController.EntryMode.RESTAURANT);
            }

            // pass the connected client to the guest controller
            controller.setClient(client);

            // Switch scene (same window)
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);

            stage.centerOnScreen();
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setTitle("Bistro – Guest");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to open Guest view.");
        }
    }

    // --------------------------------------------------------
    // DISCONNECT
    // --------------------------------------------------------
    /**
     * Handles application exit.
     * <p>
     * If connected, requests a disconnect from the server and closes the connection,
     * then terminates the JVM.
     */
    @FXML
    public void handleExit() {
        if (client != null && client.isConnected()) {
            client.sendRequest(new ClientRequest("DISCONNECT", new Object[]{}));
            client.closeConnectionSafely();
        }
        System.exit(0);
    }

    // --- kept as-is (stub) ---

    /**
     * Opens a subscriber view using stub/demo data while using the real client connection.
     * <p>
     * Intended for development/testing flows where a full login is not required.
     */
    @FXML
    private void handleSubscriberStub() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subscribergui/SubscriberMain.fxml"));
            Parent root = loader.load();

            subscribergui.SubscriberMainController controller = loader.getController();

            subscribergui.SubscriberMainController.EntryMode mode =
                    toggleHome.isSelected()
                            ? subscribergui.SubscriberMainController.EntryMode.HOME
                            : subscribergui.SubscriberMainController.EntryMode.RESTAURANT;

            // fake data + real client (so reservation works)
            controller.initAfterLogin(client, mode, "Ashrf", 1, "ashrf@test.com", "0500000000");

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.centerOnScreen();
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setTitle("Bistro – Subscriber (STUB)");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to open Subscriber (stub).");
        }
    }

    /**
     * Opens the subscriber main view after a successful login.
     * <p>
     * Determines entry mode from UI toggles and passes the connected client and subscriber
     * identity/contact details to the subscriber controller.
     *
     * @param username the logged-in subscriber name
     * @param userId   the logged-in subscriber/user ID
     * @param email    the logged-in subscriber email
     * @param phone    the logged-in subscriber phone
     */
    // Changed: accept id/email/phone and pass to SubscriberMainController.
    private void openSubscriberView(String username, int userId, String email, String phone) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/subscribergui/SubscriberMain.fxml")
            );

            Parent root = loader.load();
            subscribergui.SubscriberMainController controller = loader.getController();

            subscribergui.SubscriberMainController.EntryMode mode =
                    toggleHome.isSelected()
                            ? subscribergui.SubscriberMainController.EntryMode.HOME
                            : subscribergui.SubscriberMainController.EntryMode.RESTAURANT;

            // ✅ IMPORTANT: init after login (client + subscriber info)
            controller.initAfterLogin(client, mode, username, userId, email, phone);

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.centerOnScreen();
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setTitle("Bistro – Subscriber");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to open Subscriber view.");
        }
    }


    /**
     * Opens the representative main view after a successful login.
     * <p>
     * Determines entry mode from UI toggles, passes username and client connection,
     * and switches the current stage scene to the representative view.
     *
     * @param username the logged-in representative name
     */
    private void openRepresentativeView(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/representativegui/RepresentativeMain.fxml")
            );

            Parent root = loader.load();
            representativegui.representativeMainController controller =
                    loader.getController();

            if (toggleHome.isSelected()) {
                controller.setEntryMode(
                    representativegui.representativeMainController.EntryMode.HOME
                );
            } else {
                controller.setEntryMode(
                    representativegui.representativeMainController.EntryMode.RESTAURANT
                );
            }

            controller.setUsername(username);
            controller.setClient(client);
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);

            stage.centerOnScreen();
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setTitle("Bistro – Representative");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to open Representative view.");
        }
    }
    
    /**
     * Opens the manager view after a successful login.
     * <p>
     * Reuses the representative UI, sets entry mode from toggles, assigns username and role,
     * passes the client connection, and switches the scene on the current stage.
     *
     * @param username the logged-in manager name
     * @param role     the role string used to derive {@link common.UserRole}
     */
    private void openManagerView(String username, String role) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/representativegui/RepresentativeMain.fxml")
            );

            Parent root = loader.load();
            representativegui.representativeMainController controller =
                    loader.getController();

            if (toggleHome.isSelected()) {
                controller.setEntryMode(
                    representativegui.representativeMainController.EntryMode.HOME
                );
            } else {
                controller.setEntryMode(
                    representativegui.representativeMainController.EntryMode.RESTAURANT
                );
            }

            controller.setUsername(username);
            controller.setRole(common.UserRole.valueOf(role));
            controller.setClient(client);
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);

            stage.centerOnScreen();
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setTitle("Bistro – Representative");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to open Representative view.");
        }
    }
}
