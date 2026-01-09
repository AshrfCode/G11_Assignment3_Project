package client;

import java.io.IOException;
import java.util.List;

import common.ChatIF;
import common.ClientRequest;
import common.Order;
import common.ReservationResponse;
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

        // ✅ Forward to active screen handler FIRST
        if (ClientSession.activeHandler != null) {
            ClientSession.activeHandler.accept(msg);
        }

        // -------------------------
        // Simple string messages
        // -------------------------
        if (msg instanceof String) {
            ui.display((String) msg);
            return;
        }

        // -------------------------
        // ReservationResponse
        // -------------------------
        if (msg instanceof ReservationResponse res) {
            ui.display(res.getMessage());
            return;
        }

        // -------------------------
        // Lists from server (generic handling only)
        // -------------------------
        if (msg instanceof List<?>) {
            List<?> list = (List<?>) msg;

            if (list.isEmpty()) {
                ui.display("ℹ️ Received an empty list from server.");
                return;
            }

            if (list.get(0) instanceof Order) {
                @SuppressWarnings("unchecked")
                List<Order> orders = (List<Order>) list;
                ui.display("📦 Received " + orders.size() + " orders.");
                return;
            }

            if (list.get(0) instanceof String) {
                @SuppressWarnings("unchecked")
                List<String> slots = (List<String>) list;
                ui.display("🕒 Available slots: " + String.join(", ", slots));
                return;
            }

            // ⛔ Tables are handled ONLY by activeHandler (GUI)
            return;
        }
        
        if (msg instanceof common.SubscriberHistoryResponse) {
            // it's handled by activeHandler already
            return;
        }
        
        if (ClientSession.activeHandler != null) {
            ClientSession.activeHandler.accept(msg);
            return;
        }


        // -------------------------
        // Fallback
        // -------------------------
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

    public void cancelReservationAsSubscriber(String confirmationCode, int subscriberId, String email, String phone) {
        sendRequest(new ClientRequest(ClientRequest.CMD_CANCEL_RESERVATION,
                new Object[]{confirmationCode, subscriberId, email, phone}));
    }
    
    public void payReservation(String confirmationCode) {
        sendRequest(new ClientRequest(ClientRequest.CMD_PAY_RESERVATION,
                new Object[]{confirmationCode}));
    }
    
    public void previewBill(String confirmationCode) {
        sendRequest(new ClientRequest(ClientRequest.CMD_PREVIEW_BILL,
                new Object[]{confirmationCode}));
    }
    
    public void requestSubscriberHistory(int subscriberId) {
    	sendRequest(new ClientRequest(ClientRequest.CMD_GET_SUBSCRIBER_HISTORY,
            new Object[] { subscriberId }
        ));
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
