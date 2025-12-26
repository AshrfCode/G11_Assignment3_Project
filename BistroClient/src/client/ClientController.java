package client;

import common.ChatIF;
import common.ClientRequest;
import common.Order;
import common.ReservationResponse;

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

        // ✅ Forward to active screen handler first (before any returns)
        if (ClientSession.activeHandler != null) {
            ClientSession.activeHandler.accept(msg);
        }

        // Simple string messages (status, errors, confirmations)
        if (msg instanceof String) {
            ui.display((String) msg);
            return;
        }

        // ReservationResponse messages
        if (msg instanceof ReservationResponse res) {
            ui.display(res.getMessage());
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

            // Added support for reservation slots list (List<String>) so it won’t show as “unrecognized”.
            if (!list.isEmpty() && list.get(0) instanceof String) {
                @SuppressWarnings("unchecked")
                List<String> slots = (List<String>) list;

                ui.display("🕒 Available slots: " + String.join(", ", slots));
                return;
            }

            if (list.isEmpty()) {
                ui.display("ℹ️ Received an empty list from server.");
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

    // Added 3 small helper methods to send reservation requests (get slots / create / cancel).
    public void requestAvailableSlots(String dateYYYYMMDD, int diners) {
        sendRequest(new ClientRequest(ClientRequest.CMD_GET_AVAILABLE_SLOTS,
                new Object[]{dateYYYYMMDD, diners}));
    }

    public void createReservation(String dateTimeYYYYMMDD_HHMM, int diners, String customerIdOrEmail, String phone, String email) {
        sendRequest(new ClientRequest(ClientRequest.CMD_CREATE_RESERVATION,
                new Object[]{dateTimeYYYYMMDD_HHMM, diners, customerIdOrEmail, phone, email}));
    }

    public void cancelReservation(String confirmationCode) {
        sendRequest(new ClientRequest(ClientRequest.CMD_CANCEL_RESERVATION,
                new Object[]{confirmationCode}));
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
