package clientgui;

import client.ClientController;
import common.ChatIF;

import java.net.InetAddress;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConnectViewController {

    @FXML
    private TextField ipField;

    @FXML
    private TextField portField;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            ipField.setText(localIp);
        } catch (Exception e) {
            ipField.setText("localhost");
        }
        portField.setText("5555");
    }

    @FXML
    private void handleConnect() {
        String ip = ipField.getText().trim();
        int port;

        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid port number.");
            return;
        }

        try {
            // Configure client connection BEFORE first use
            client.ClientManager.configure(ip, port);

            // Load Bistro Sign-In Screen
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/clientgui/BistroMain.fxml")
            );
            Parent root = loader.load();

            // IMPORTANT:
            // Do NOT create client here
            // BistroMainController.initialize() will call ClientManager.getClient(this)

            Stage stage = (Stage) ipField.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setTitle("Bistro – Sign In");
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            statusLabel.setText("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
