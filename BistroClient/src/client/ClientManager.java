package client;

import common.ChatIF;

/**
 * Manages a single shared {@link ClientController} instance for the client application.
 * <p>
 * Provides configuration of host/port and centralized lifecycle management
 * (initialization, retrieval, and shutdown).
 */
public class ClientManager {

    /**
     * Singleton instance of the client controller.
     */
    private static ClientController client;
    
    /**
     * Default server host used when creating the {@link ClientController}.
     */
    private static String HOST = "localhost";

    /**
     * Default server port used when creating the {@link ClientController}.
     */
    private static int PORT = 5555;

    
    /**
     * Configures the server host and port used when initializing the client controller.
     *
     * @param host the server host name or IP address
     * @param port the server port
     */
    public static synchronized void configure(String host, int port) {
        HOST = host;
        PORT = port;
    }

    /**
     * Returns the singleton {@link ClientController}, creating it if needed.
     * <p>
     * If the client already exists, the provided UI is set as the current message target.
     *
     * @param ui the UI handler that will receive display messages
     * @return the initialized (or reused) {@link ClientController} instance
     * @throws RuntimeException if the client cannot be initialized
     */
    public static synchronized ClientController getClient(ChatIF ui) {
        try {
            if (client == null) {
                client = new ClientController(HOST, PORT, ui);
            } else {
                client.setUI(ui); // switch message target
            }
            return client;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ClientController", e);
        }
    }

    /**
     * Returns the already-initialized {@link ClientController} without changing the UI.
     *
     * @return the existing {@link ClientController} instance
     * @throws IllegalStateException if the client has not been initialized yet
     */
    public static synchronized ClientController getClient() {
        if (client == null) {
            throw new IllegalStateException(
                "Client not initialized yet. Call getClient(ui) first."
            );
        }
        return client;
    }
  

    /**
     * Closes the client connection and clears the singleton instance.
     * <p>
     * Intended to be called when the application exits.
     */
    public static synchronized void shutdown() {
        if (client != null) {
            try {
                client.closeConnection();
            } catch (Exception ignored) {}
            client = null;
        }
    }
}
