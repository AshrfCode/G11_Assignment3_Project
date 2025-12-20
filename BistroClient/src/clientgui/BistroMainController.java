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

public class BistroMainController implements ChatIF {

    @FXML private Label statusLabel;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ToggleButton toggleHome;
    @FXML private ToggleButton toggleRestaurant;

    private ClientController client;
    
    @FXML
    public void initialize() {
        // Always get the singleton client and bind it to THIS UI
        this.client = ClientManager.getClient(this);
    }


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

                if (parts.length < 3) {
                    statusLabel.setText("Invalid login response");
                    return;
                }

                String role = parts[1];
                String username = parts[2];

                switch (role) {
                    case "SUBSCRIBER" -> openSubscriberView(username);
                    case "REPRESENTATIVE", "MANAGER" -> openRepresentativeView(username, role);


                    default -> statusLabel.setText("Unknown role");
                }
            }
        });
    }



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

    
    
    @FXML
    private void handleQuickAccess() {
    	statusLabel.setText("Quick clicked (not implemented yet)");
    }
    
    
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
    @FXML
    public void handleExit() {
        if (client != null && client.isConnected()) {
            client.sendRequest(new ClientRequest("DISCONNECT", new Object[]{}));
            client.closeConnectionSafely();
        }
        System.exit(0);
    }
    
    @FXML
    private void handleSubscriberStub() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/subscribergui/SubscriberMain.fxml")
            );

            Parent root = loader.load();

            // Get controller
            subscribergui.SubscriberMainController controller = loader.getController();

            // Fake entry mode (based on toggle)
            if (toggleHome.isSelected()) {
                controller.setEntryMode(
                    subscribergui.SubscriberMainController.EntryMode.HOME
                );
            } else {
                controller.setEntryMode(
                    subscribergui.SubscriberMainController.EntryMode.RESTAURANT
                );
            }

            // Fake logged-in user
            controller.setUsername("Ashrf");

            // Switch scene
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);

            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setTitle("Bistro – Subscriber (STUB)");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to open Subscriber (stub).");
        }
    }


    @FXML
    private void openSubscriberView(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/subscribergui/SubscriberMain.fxml")
            );

            Parent root = loader.load();
            subscribergui.SubscriberMainController controller = loader.getController();

            // mode from toggles
            if (toggleHome.isSelected()) {
                controller.setEntryMode(subscribergui.SubscriberMainController.EntryMode.HOME);
            } else {
                controller.setEntryMode(subscribergui.SubscriberMainController.EntryMode.RESTAURANT);
            }

            controller.setUsername(username);

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);

            stage.centerOnScreen();
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setTitle("Bistro – Subscriber");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to open Subscriber view.");
        }
    }
    
    @FXML
    private void openRepresentativeView(String username, String role){
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/representativegui/RepresentativeMain.fxml")
            );

            Parent root = loader.load();
            representativegui.representativeMainController controller =
                    loader.getController();

            // Representatives usually work in restaurant context
            // but we still respect the toggle for consistency
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
