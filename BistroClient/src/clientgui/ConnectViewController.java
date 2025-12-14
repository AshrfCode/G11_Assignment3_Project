package clientgui;

import client.ClientController;
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
      this.ipField.setText(localIp);
    } catch (Exception e) {
      this.ipField.setText("localhost");
    } 
    this.portField.setText("5555");
  }
  
  @FXML
  private void handleConnect() {
    int port;
    String ip = this.ipField.getText().trim();
    try {
      port = Integer.parseInt(this.portField.getText().trim());
    } catch (NumberFormatException e) {
      this.statusLabel.setText("Invalid port number.");
      return;
    } 
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientgui/ClientView.fxml"));
      Scene scene = new Scene((Parent)loader.load());
      ClientGUIController controller = (ClientGUIController)loader.getController();
      if (controller == null) {
        this.statusLabel.setText("Failed: Controller not loaded from FXML.");
        return;
      } 
      ClientController client = new ClientController(ip, port, controller);
      controller.setClient(client);
      Stage stage = (Stage)this.ipField.getScene().getWindow();
      stage.setTitle("Client Dashboard");
      stage.setScene(scene);
      stage.show();
    } catch (Exception e) {
      this.statusLabel.setText("Failed to load client view: " + e.getMessage());
      e.printStackTrace();
    } 
  }
}
