package client;

import common.ChatIF;

public class ClientManager {

    private static ClientController client;
    
    private static String HOST = "localhost";
    private static int PORT = 5555;

    
    public static synchronized void configure(String host, int port) {
        HOST = host;
        PORT = port;
    }

    /**
     * Initialize (or reuse) the single ClientController instance.
     * This should be called at least once with a valid UI (ChatIF).
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
     * Get the already-initialized client (no UI change).
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
     * Optional: close connection when app exits.
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
