package servergui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import server.BistroServer;
import server.MySQLConnectionPool;
import server.PooledConnection;

import java.net.InetAddress;

public class ServerMainController {

    @FXML private TextField serverIpField, serverPortField, dbIpField, dbPortField, dbUserField;
    @FXML private PasswordField dbPassField;
    @FXML private Label statusLabel;
    @FXML private Button connectButton, disconnectButton;
    @FXML private TableView<ClientInfo> clientTable;
    @FXML private TableColumn<ClientInfo, String> ipColumn, hostColumn, statusColumn;

    private BistroServer server;

    private ObservableList<ClientInfo> clients = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            serverIpField.setText(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            serverIpField.setText("Unknown");
        }

        dbIpField.setText("localhost");
        dbPortField.setText("3306");
        dbUserField.setText("root");
        dbPassField.setText("Aa123456");
        serverPortField.setText("5555");

        ipColumn.setCellValueFactory(new PropertyValueFactory<>("ip"));
        hostColumn.setCellValueFactory(new PropertyValueFactory<>("host"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        clientTable.setItems(clients);
    }

    // ---------------------------------------------------------------
    // HANDLE CONNECT (updated for connection pool)
    // ---------------------------------------------------------------
    @FXML
    void handleConnect() {
        // Prevent double-start
        if (server != null && server.isListening()) {
            statusLabel.setText("⚠️ Server is already running!");
            return;
        }

        try {
            String dbIp = dbIpField.getText();
            String dbPort = dbPortField.getText();
            String user = dbUserField.getText();
            String pass = dbPassField.getText();
            int serverPort = Integer.parseInt(serverPortField.getText());

            String jdbcUrl = "jdbc:mysql://" + dbIp + ":" + dbPort +
                    "/Bistro?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true";

            // 1. Configure DB Pool
            MySQLConnectionPool.configure(jdbcUrl, user, pass);

            // 2. Test connection
            PooledConnection testConn = MySQLConnectionPool.getInstance().getConnection();

            // 3. Start server safely
            server = new BistroServer(serverPort, this);
            server.listen();

            statusLabel.setText("🟢 Server started on port " + serverPort);

        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // ---------------------------------------------------------------
    // HANDLE DISCONNECT
    // ---------------------------------------------------------------
    @FXML
    void handleDisconnect() {
        if (server == null) {
            statusLabel.setText("⚠️ Server is not running.");
            return;
        }

        try {
            // Very important for OCSF!
            server.stopListening();

            // Close all client connections
            server.close();

            server = null;

            // Close connection pool (otherwise DB still works!)
            MySQLConnectionPool.getInstance().shutdown();

            clients.clear();
            clientTable.refresh();

            statusLabel.setText("🔴 Server stopped.");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Failed to stop server properly.");
        }
    }


    // ---------------------------------------------------------------
    // HANDLE EXIT
    // ---------------------------------------------------------------
    @FXML
    void handleExit() {
        try {
            if (server != null) {
                server.close();
                server = null;
                System.out.println("[SERVER] Server closed before exit.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    // ---------------------------------------------------------------
    // CLIENT TABLE UPDATES (used by BistroServer)
    // ---------------------------------------------------------------
    public void addClient(String ip, String host, int id) {
        Platform.runLater(() -> clients.add(new ClientInfo(ip, host, "Connected", id)));
    }

    public void updateClientStatus(int id, String status) {
        Platform.runLater(() -> {
            for (ClientInfo client : clients) {
                if (client.getId() == id) {
                    client.setStatus(status);
                    clientTable.refresh();
                    break;
                }
            }
        });
    }
}
