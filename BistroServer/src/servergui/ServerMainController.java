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
import server.NotificationService;
import server.GmailSmtpNotificationService;


/**
 * JavaFX controller for the server main screen.
 * <p>
 * Responsible for initializing UI defaults, starting/stopping the {@link BistroServer},
 * configuring the database connection pool, and maintaining a live table of connected clients.
 * </p>
 */
public class ServerMainController {

    /**
     * Text fields for server and database configuration input.
     */
    @FXML private TextField serverIpField, serverPortField, dbIpField, dbPortField, dbUserField;

    /**
     * Password field for database password input.
     */
    @FXML private PasswordField dbPassField;

    /**
     * Label used to display current server status and error messages.
     */
    @FXML private Label statusLabel;

    /**
     * Buttons used to connect/disconnect the server.
     */
    @FXML private Button connectButton, disconnectButton;

    /**
     * Table view listing connected clients.
     */
    @FXML private TableView<ClientInfo> clientTable;

    /**
     * Table columns for client IP, host, and connection status.
     */
    @FXML private TableColumn<ClientInfo, String> ipColumn, hostColumn, statusColumn;

    /**
     * The underlying server instance managed by this controller.
     */
    private BistroServer server;

    /**
     * Observable list backing the client table view.
     */
    private ObservableList<ClientInfo> clients = FXCollections.observableArrayList();

    /**
     * Initializes the controller after the FXML fields are injected.
     * <p>
     * Sets default values for server/DB fields and configures the client table columns.
     * </p>
     */
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

    /**
     * Starts the server using the configuration provided in the UI and initializes the database connection pool.
     * <p>
     * Prevents double-start if the server is already listening. On success, configures the JDBC URL,
     * initializes the pool, performs a test connection acquisition, and starts the {@link BistroServer}.
     * </p>
     */
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
                    "/bistro?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true";

            // 1. Configure DB Pool
            MySQLConnectionPool.configure(jdbcUrl, user, pass);

            // 2. Test connection
            PooledConnection testConn = MySQLConnectionPool.getInstance().getConnection();

            // 3. Start server safely
            server = new BistroServer(serverPort, this);
         // after you start the server:
            server.listen();

            // ✅ TEMP TEST (delete after it works)
            NotificationService notifier =
                new GmailSmtpNotificationService(
                    System.getenv("BISTRO_GMAIL_FROM"),
                    System.getenv("BISTRO_GMAIL_APP_PASSWORD")
                );
            
            System.out.println("=== EMAIL TEST START ===");
            System.out.println("FROM=" + System.getenv("BISTRO_GMAIL_FROM"));
            System.out.println("PASS_SET=" + (System.getenv("BISTRO_GMAIL_APP_PASSWORD") != null));

            
         /*  notifier.sendEmail(
                "asadrezek123@gmail.com",
                "Bistro Test Email",
                "If you got this, Gmail SMTP works ✅"
            );*/


            statusLabel.setText("🟢 Server started on port " + serverPort);

        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // ---------------------------------------------------------------
    // HANDLE DISCONNECT
    // ---------------------------------------------------------------

    /**
     * Stops the server, closes all client connections, and shuts down the database connection pool.
     * <p>
     * Clears the client list and refreshes the UI table after shutdown.
     * </p>
     */
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

    /**
     * Exits the application, attempting to close the server first if it is running.
     */
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

    /**
     * Adds a connected client entry to the GUI client table.
     * <p>
     * This method schedules the UI update on the JavaFX application thread.
     * </p>
     *
     * @param ip the client's IP address
     * @param host the client's host name
     * @param id the client's internal identifier
     */
    public void addClient(String ip, String host, int id) {
        Platform.runLater(() -> clients.add(new ClientInfo(ip, host, "Connected", id)));
    }

    /**
     * Updates the status of an existing client entry in the GUI client table.
     * <p>
     * This method schedules the UI update on the JavaFX application thread and refreshes the table.
     * </p>
     *
     * @param id the client's internal identifier
     * @param status the new status string to set
     */
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
