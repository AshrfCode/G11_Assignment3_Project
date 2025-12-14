package client;

import clientgui.ClientGUIController;
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

    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof String) {
            // Server returns strings for updates and error messages
            String response = (String) msg;
            this.ui.display(response);
            return;
        } else if (msg instanceof List<?>) {
            List<?> list = (List<?>) msg;
            if (!list.isEmpty() && list.get(0) instanceof Order) {
                @SuppressWarnings("unchecked")
                List<Order> orders = (List<Order>) list;

                if (this.ui instanceof ClientGUIController) {
                    ClientGUIController gui = (ClientGUIController) this.ui;
                    gui.displayOrders(orders);
                } else {
                    this.ui.display("📦 Received " + orders.size() + " orders.");
                }
                return;
            }
        }

        // Debug: see what was received if unrecognized
        System.err.println("⚠️ Unrecognized response from server: " + msg.getClass().getName());
        this.ui.display("⚠️ Unrecognized response from server.");
    }

    public void sendRequest(ClientRequest request) {
        try {
            sendToServer(request);
        } catch (IOException e) {
            this.ui.display("❌ Failed to send request to server: " + e.getMessage());
        }
    }

    public void closeConnectionSafely() {
        try {
            closeConnection();
        } catch (IOException e) {
            this.ui.display("❌ Failed to close connection: " + e.getMessage());
        }
    }

    @Override
    protected void connectionClosed() {
        this.ui.display("🔌 Client disconnected from server.");
    }
}
