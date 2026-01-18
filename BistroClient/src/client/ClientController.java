package client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import common.ChatIF;
import common.ClientRequest;
import common.Order;
import common.ReservationResponse;
import ocsf.client.AbstractClient;

/**
 * Client-side controller responsible for managing the network connection to the server
 * and providing convenient request-sending methods for UI screens.
 * <p>
 * Extends {@link AbstractClient} (OCSF) to receive and dispatch messages from the server,
 * while delegating user-visible output to a {@link ChatIF} implementation.
 */
public class ClientController extends AbstractClient {

    /**
     * UI handler used to display messages and errors to the user.
     */
    private ChatIF ui;

    /**
     * Creates a new client controller, opens a connection to the server, and stores the UI handler.
     *
     * @param host the server host name or IP address
     * @param port the server port
     * @param ui   UI callback used to display messages to the user
     * @throws IOException if opening the network connection fails
     */
    public ClientController(String host, int port, ChatIF ui) throws IOException {
        super(host, port);
        this.ui = ui;
        openConnection();
    }

    /**
     * Updates the UI handler used for displaying messages.
     *
     * @param ui the new UI handler
     */
    public void setUI(ChatIF ui) {
        this.ui = ui;
    }

    /**
     * Handles messages received from the server.
     * <p>
     * If an active screen handler exists, the message is delivered to it first. Some message types
     * are short-circuited to prevent double-handling. Otherwise, common message types are handled
     * here and forwarded to the UI.
     *
     * @param msg the message object received from the server
     */
   @Override
	protected void handleMessageFromServer(Object msg) {
	
	    // ✅ First: deliver to the active screen handler (if exists)
	    if (ClientSession.activeHandler != null) {
	        ClientSession.activeHandler.accept(msg);
	
	        // ✅ Prevent double-handling for common primitive replies
	        // (Otherwise UI + active screen both react)
	        if (msg instanceof String || msg instanceof Integer) {
	            return;
	        }
	    }
	
	    // ✅ Then: general UI handling
	    if (msg instanceof String) {
	        ui.display((String) msg);
	        return;
	    }
	    
	    if (msg instanceof Map) {
	        return; 
	    }
	
	    if (msg instanceof ReservationResponse res) {
	        ui.display(res.getMessage());
	        return;
	    }
	
	    if (msg instanceof Integer) {
	        int tableNum = (Integer) msg;
	        ui.display("✅ Check-in Successful! Please proceed to Table #" + tableNum);
	        return;
	    }
	
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

        return;
    }

    // ✅ keep: special response that activeHandler handles
    if (msg instanceof common.SubscriberHistoryResponse) {
        return; // activeHandler handles it
    }

    System.err.println("⚠️ Unrecognized message from server: " + msg);
    ui.display("⚠️ Unrecognized message from server.");
}



    /**
     * Sends a {@link ClientRequest} to the server.
     * <p>
     * If sending fails, an error message is displayed through the UI.
     *
     * @param request the request to send to the server
     */
    public void sendRequest(ClientRequest request) {
        try {
            sendToServer(request);
        } catch (IOException e) {
            ui.display("❌ Failed to send request: " + e.getMessage());
        }
    }

    /**
     * Requests the list of available reservation time slots for a given date and party size.
     *
     * @param dateYYYYMMDD the date in {@code YYYY-MM-DD} format
     * @param diners       number of diners (party size)
     */
    // Added 3 small helper methods to send reservation requests (get slots / create / cancel).
    public void requestAvailableSlots(String dateYYYYMMDD, int diners) {
        sendRequest(new ClientRequest(ClientRequest.CMD_GET_AVAILABLE_SLOTS,
                new Object[]{dateYYYYMMDD, diners}));
    }

    /**
     * Requests creation of a new reservation.
     *
     * @param dateTimeYYYYMMDD_HHMM reservation date/time in {@code YYYY-MM-DD HH:MM} format
     * @param diners               number of diners (party size)
     * @param customerIdOrEmail    customer identifier or email used by the server
     * @param phone                customer phone number
     * @param email                customer email address
     */
    public void createReservation(String dateTimeYYYYMMDD_HHMM, int diners, String customerIdOrEmail, String phone, String email) {
        sendRequest(new ClientRequest(ClientRequest.CMD_CREATE_RESERVATION,
                new Object[]{dateTimeYYYYMMDD_HHMM, diners, customerIdOrEmail, phone, email}));
    }

    /**
     * Requests cancellation of a reservation by confirmation code.
     *
     * @param confirmationCode the reservation confirmation code
     */
    public void cancelReservation(String confirmationCode) {
        sendRequest(new ClientRequest(ClientRequest.CMD_CANCEL_RESERVATION,
                new Object[]{confirmationCode}));
    }

    /**
     * Requests cancellation of a reservation by a subscriber, including subscriber identification details.
     *
     * @param confirmationCode the reservation confirmation code
     * @param subscriberId     the subscriber's ID
     * @param email            subscriber email
     * @param phone            subscriber phone number
     */
    public void cancelReservationAsSubscriber(String confirmationCode, int subscriberId, String email, String phone) {
        sendRequest(new ClientRequest(ClientRequest.CMD_CANCEL_RESERVATION,
                new Object[]{confirmationCode, subscriberId, email, phone}));
    }
    
    /**
     * Requests the monthly time report for the specified month and year.
     *
     * @param month the month value expected by the server (e.g., {@code "01"}..{@code "12"})
     * @param year  the year value expected by the server (e.g., {@code "2026"})
     */
    public void requestMonthlyTimeReport(String month, String year) {
        // Sends [month, year] to the server
        sendRequest(new ClientRequest(ClientRequest.CMD_GET_MONTHLY_TIME_REPORT,
                new Object[]{ month, year }));
    }
    
    /**
     * Requests the subscriber report for the specified month and year.
     *
     * @param month the month value expected by the server (e.g., {@code "01"}..{@code "12"})
     * @param year  the year value expected by the server (e.g., {@code "2026"})
     */
    public void requestSubscriberReport(String month, String year) {
        sendRequest(new ClientRequest(ClientRequest.CMD_GET_SUBSCRIBER_REPORT,
                new Object[]{ month, year }));
    }
    
    /**
     * Requests to pay for a reservation identified by its confirmation code.
     *
     * @param confirmationCode the reservation confirmation code
     */
    public void payReservation(String confirmationCode) {
        sendRequest(new ClientRequest(ClientRequest.CMD_PAY_RESERVATION,
                new Object[]{confirmationCode}));
    }
    
    /**
     * Requests a bill preview for a reservation identified by its confirmation code.
     *
     * @param confirmationCode the reservation confirmation code
     */
    public void previewBill(String confirmationCode) {
        sendRequest(new ClientRequest(ClientRequest.CMD_PREVIEW_BILL,
                new Object[]{confirmationCode}));
    }
    
    /**
     * Requests the reservation/order history for a subscriber.
     *
     * @param subscriberId the subscriber ID
     */
    public void requestSubscriberHistory(int subscriberId) {
    	sendRequest(new ClientRequest(ClientRequest.CMD_GET_SUBSCRIBER_HISTORY,
            new Object[] { subscriberId }
        ));
    }
    
    /**
     * Requests the list of subscriber confirmation codes associated with a user.
     *
     * @param userId the user ID
     */
    public void fetchSubscriberCodes(int userId) {
        sendRequest(new ClientRequest(ClientRequest.CMD_GET_SUBSCRIBER_CODES, 
            new Object[] { userId } 
        ));
    }
    
    /**
     * Requests a customer check-in using a reservation confirmation code.
     *
     * @param confirmationCode the reservation confirmation code
     */
    public void checkInCustomer(String confirmationCode) {
        sendRequest(new ClientRequest(ClientRequest.CMD_CHECK_IN, 
                new Object[]{confirmationCode}));
    }

    /**
     * Closes the client connection to the server, displaying an error message on failure.
     */
    public void closeConnectionSafely() {
        try {
            closeConnection();
        } catch (IOException e) {
            ui.display("❌ Failed to close connection: " + e.getMessage());
        }
    }

    /**
     * Callback invoked by {@link AbstractClient} when the connection is closed.
     * Displays a user-facing message through the UI.
     */
    @Override
    protected void connectionClosed() {
        ui.display("🔌 Client disconnected from server.");
    }
    
    
    /**
     * Requests joining the waiting list as a subscriber.
     *
     * @param subscriberId the subscriber ID
     * @param diners       number of diners (party size)
     * @param phone        contact phone number
     * @param email        contact email address
     */
    public void joinWaitingListAsSubscriber(int subscriberId, int diners, String phone, String email) {
        sendRequest(new ClientRequest(ClientRequest.CMD_JOIN_WAITING_LIST,
                new Object[]{subscriberId, diners, phone, email}));
    }

    /**
     * Requests joining the waiting list as a guest.
     *
     * @param diners number of diners (party size)
     * @param phone  contact phone number
     * @param email  contact email address
     */
    public void joinWaitingListAsGuest(int diners, String phone, String email) {
        sendRequest(new ClientRequest(ClientRequest.CMD_JOIN_WAITING_LIST_GUEST,
                new Object[]{diners, phone, email}));
    }
    
    

    /**
     * Requests leaving the waiting list as a guest using the provided confirmation code.
     *
     * @param confirmationCode the waiting list confirmation code
     */
    public void leaveWaitingListAsGuest(String confirmationCode) {
        sendRequest(new ClientRequest(ClientRequest.CMD_LEAVE_WAITING_LIST_GUEST,
                new Object[]{confirmationCode}));
    }

    /**
     * Requests help retrieving a reservation confirmation code based on contact information.
     *
     * @param email the email associated with the reservation
     * @param phone the phone associated with the reservation
     */
    public void forgotConfirmationCode(String email, String phone) {
        sendRequest(new ClientRequest(ClientRequest.CMD_FORGOT_CONFIRMATION_CODE,
                new Object[]{email, phone}));
    }

    /**
     * Requests updating a subscriber's contact details.
     *
     * @param subscriberId the subscriber ID
     * @param newEmail     the new email address
     * @param newPhone     the new phone number
     */
    public void updateSubscriberDetails(int subscriberId, String newEmail, String newPhone) {
        sendRequest(new ClientRequest(ClientRequest.CMD_UPDATE_SUBSCRIBER_DETAILS,
                new Object[]{ subscriberId, newEmail, newPhone }));
    }




}
