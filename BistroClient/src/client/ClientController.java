package client;

import common.ChatIF;
import common.ClientRequest;
import common.Order;

import java.io.IOException;
import java.util.List;

import ocsf.client.AbstractClient;

public class ClientController extends AbstractClient {

    private ChatIF ui;

    public ClientController(String host, int port, ChatIF ui) throws IOException {
        super(host, port);
        this.ui = ui;
        openConnection();
    }
    
    public void setUI(ChatIF ui) {
        this.ui = ui;
    }


    @Override
    protected void handleMessageFromServer(Object msg) {

        // Simple string messages (status, errors, confirmations)
        if (msg instanceof String) {
            ui.display((String) msg);
            return;
        }

        // Lists from server (e.g., orders, reservations, reports)
        if (msg instanceof List<?>) {
            List<?> list = (List<?>) msg;

            if (!list.isEmpty() && list.get(0) instanceof Order) {
                @SuppressWarnings("unchecked")
                List<Order> orders = (List<Order>) list;

                // For now: generic display
                ui.display("📦 Received " + orders.size() + " orders.");
                return;
            }
        }

        // Fallback (debug-safe)
        System.err.println("⚠️ Unrecognized message from server: " + msg);
        ui.display("⚠️ Unrecognized message from server.");
    }

    public void sendRequest(ClientRequest request) {
        try {
            sendToServer(request);
        } catch (IOException e) {
            ui.display("❌ Failed to send request: " + e.getMessage());
        }
    }

    public void closeConnectionSafely() {
        try {
            closeConnection();
        } catch (IOException e) {
            ui.display("❌ Failed to close connection: " + e.getMessage());
        }
    }

    @Override
    protected void connectionClosed() {
        ui.display("🔌 Client disconnected from server.");
    }
}
