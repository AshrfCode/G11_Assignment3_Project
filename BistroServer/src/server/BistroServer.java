package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import common.ClientRequest;
import common.LoginRequest;
import common.ReservationHistoryRow;
import common.SubscriberHistoryResponse;
import entities.Table;
import entities.User;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import server.dao.MySQLUserDAO;
import server.dao.ReservationDAO;
import server.dao.TableDAO;
import servergui.ServerMainController;

public class BistroServer extends AbstractServer {

    private Map<ConnectionToClient, String[]> clientInfoMap = new ConcurrentHashMap<>();
    private DBController db = new DBController();
    private TableDAO tableDAO = new TableDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();

    private ServerMainController guiController;
    
 // simple anti-spam cooldown: key=email|phone -> lastSentMillis
    private final Map<String, Long> forgotCooldown = new ConcurrentHashMap<>();
    
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // For now: prints "sent" to server console (you can swap later to real Email/SMS)
    private final NotificationService notifier =
    	    new GmailSmtpNotificationService(
    	        System.getenv("BISTRO_GMAIL_FROM"),
    	        System.getenv("BISTRO_GMAIL_APP_PASSWORD")
    	    );


    
    public BistroServer(int port, ServerMainController guiController) {
        super(port);
        this.guiController = guiController;
 
        scheduler.scheduleAtFixedRate(() -> {
            try {
                int reminders = db.sendReservationReminders(notifier);
                if (reminders > 0) System.out.println("🔔 Reservation reminders sent: " + reminders);

                int invited = db.processWaitingListInvites(notifier);
                if (invited > 0) System.out.println("📨 Waiting list invites sent: " + invited);

                int n = db.cancelNoShowReservations(notifier);
                if (n > 0) System.out.println("⏰ Auto-canceled no-show reservations: " + n);
                
                int autoFinished = db.autoCompleteFinishedReservations(notifier);
                if (autoFinished > 0) System.out.println("💰 Auto-completed & billed reservations: " + autoFinished);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 10, 60, TimeUnit.SECONDS);


      
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        System.out.println("=== SERVER RECEIVED MESSAGE === " + msg);

        /* =========================
           LOGIN
           ========================= */
        if (msg instanceof LoginRequest loginRequest) {

            try {
                MySQLUserDAO userDAO = new MySQLUserDAO();

                User user = userDAO.authenticate(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                );

                if (user == null) {
                    client.sendToClient("LOGIN_FAIL|Invalid email or password");
                } else {

                    // שמירת ROLE בצד שרת לצורך הרשאות
                    clientInfoMap.put(client, new String[] {
                            user.getRole().toString(),
                            user.getName(),String.valueOf(user.getId())
                    });

                    String safeEmail = (user.getEmail() == null) ? "" : user.getEmail();
                    String safePhone = (user.getPhone() == null) ? "" : user.getPhone();

                    client.sendToClient(
                            "LOGIN_OK|" + user.getRole() + "|" + user.getName()
                                    + "|" + user.getId()
                                    + "|" + safeEmail
                                    + "|" + safePhone
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    client.sendToClient("LOGIN_FAIL|Server error during login");
                } catch (Exception ignored) {}
            }

            return;
        }
        
        /* =========================
        CARD READER SIGN IN
        ========================= */

        else if (msg instanceof common.CardLoginRequest cardRequest) {
            try {
                MySQLUserDAO userDAO = new MySQLUserDAO();
                
                // Call the new DAO method
                User user = userDAO.authenticateByCard(cardRequest.getDigitalCode());

                if (user == null) {
                    client.sendToClient("LOGIN_FAIL|Invalid card code");
                } else {
                    // Update server-side client info
                    clientInfoMap.put(client, new String[] {
                            user.getRole().toString(),
                            user.getName(),
                            String.valueOf(user.getId())
                    });

                    String safeEmail = (user.getEmail() == null) ? "" : user.getEmail();
                    String safePhone = (user.getPhone() == null) ? "" : user.getPhone();

                    // Send the exact same protocol string the client expects
                    client.sendToClient(
                            "LOGIN_OK|" + user.getRole() + "|" + user.getName()
                                    + "|" + user.getId()
                                    + "|" + safeEmail
                                    + "|" + safePhone
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    client.sendToClient("LOGIN_FAIL|Server error during card login");
                } catch (Exception ignored) {}
            }
        }

        /* =========================
           CLIENT REQUESTS
           ========================= */
        else if (msg instanceof ClientRequest request) {

            try {
                String command = request.getCommand();
                Object[] params = request.getParams();

                switch (command) {
                	
                case ClientRequest.CMD_GET_SUBSCRIBER_CODES: {
                    Object[] data = (Object[]) request.getParams(); // or getParameters()

                
                    if (data != null && data.length > 0 && data[0] instanceof Integer) { // Check for Integer again
                        int userId = (int) data[0]; 
                        

                        ArrayList<String> codes = db.getSubscriberActiveCodes(userId);
                        client.sendToClient(codes);
                    }
                    break;
                }

                    case "UPDATE_ORDER_DATE": {
                        int orderId = Integer.parseInt(params[0].toString());
                        java.sql.Date newDate = java.sql.Date.valueOf(params[1].toString());

                        boolean ok = db.updateOrderDate(orderId, newDate);
                        client.sendToClient(ok ? "OK_DATE" : "ERROR_DATE");
                        break;
                    }
                    case ClientRequest.CMD_GET_TODAY_RESERVATIONS: {
                        var list = db.getTodayReservations();
                        client.sendToClient(list);
                        break;
                    }

                    case "GET_ALL_ORDERS": {
                        List<String> orders = db.getAllOrders();
                        client.sendToClient(orders);
                        break;
                    }


                    case "UPDATE_NUMBER_OF_GUESTS": {
                        int orderId = Integer.parseInt(params[0].toString());
                        int guests = Integer.parseInt(params[1].toString());

                        boolean ok = db.updateNumberOfGuests(orderId, guests);
                        client.sendToClient(ok ? "OK_GUESTS" : "ERROR_GUESTS");
                        break;
                    }
                     // =========================
                    // WAITING LIST
                    // =========================
                    case ClientRequest.CMD_GET_WAITING_LIST: {
                        var list = db.getWaitingList();
                        client.sendToClient(list);
                        break;
                    }
                    
                  
                    
                    case ClientRequest.CMD_GET_OPENING_HOURS: {
                        client.sendToClient(db.getOpeningHours());
                        break;
                    }

                    case ClientRequest.CMD_UPDATE_OPENING_HOURS: {
                        String day = params[0].toString();
                        String open = params[1].toString();
                        String close = params[2].toString();

                        boolean ok = db.updateOpeningHours(day, open, close);

                        if (ok) {
                            String reason = "Opening hours were updated for " + day + " (" + open + " - " + close + ").";
                            int canceled = db.cancelReservationsImpactedByWeeklyHoursChange(day, notifier, reason);
                            if (canceled > 0) System.out.println("🧾 Canceled due to weekly hours change: " + canceled);
                        }

                        client.sendToClient(ok ? "OPENING_UPDATED" : "OPENING_UPDATE_FAIL");
                        break;
                    }


                    case ClientRequest.CMD_GET_SPECIAL_OPENING: {
                        // params: date "YYYY-MM-DD"
                        String date = params[0].toString();
                        client.sendToClient(db.getSpecialOpeningByDate(date));
                        break;
                    }

                    case ClientRequest.CMD_UPSERT_SPECIAL_OPENING: {
                        // params: date, openTime, closeTime, isClosed
                        String date = params[0].toString();
                        String open = (params[1] == null) ? null : params[1].toString();   // יכול להיות null אם סגור
                        String close = (params[2] == null) ? null : params[2].toString();
                        boolean isClosed = Boolean.parseBoolean(params[3].toString());
                        boolean ok = db.upsertSpecialOpening(date, open, close, isClosed);
                        if (ok) {
                            String reason;
                            if (isClosed) {
                                reason = "The restaurant will be closed on " + date + " (special opening hours update).";
                            } else {
                                reason = "Special opening hours updated on " + date + " (" + open + " - " + close + ").";
                            }
                            int canceled = db.cancelReservationsImpactedByDateHoursChange(date, notifier, reason);
                            if (canceled > 0) System.out.println("🧾 Canceled due to special hours change: " + canceled);
                        }

                        client.sendToClient(ok ? "SPECIAL_OPENING_SAVED" : "SPECIAL_OPENING_SAVE_FAIL");
                        break;
                    }

                    case ClientRequest.CMD_DELETE_SPECIAL_OPENING: {
                        String date = params[0].toString();

                        boolean ok = db.deleteSpecialOpening(date);

                        if (ok) {
                            String reason = "Special opening hours were removed for " + date + ". Updated schedule applies.";
                            int canceled = db.cancelReservationsImpactedByDateHoursChange(date, notifier, reason);
                            if (canceled > 0) System.out.println("🧾 Canceled due to special hours deletion: " + canceled);
                        }

                        client.sendToClient(ok ? "SPECIAL_OPENING_DELETED" : "SPECIAL_OPENING_DELETE_FAIL");
                        break;
                    }

                    case ClientRequest.CMD_PAY_RESERVATION: {
                        String code = params[0].toString();
                        String result = db.payReservation(code);
                        client.sendToClient(result);
                        break;
                    }
                    
                    case ClientRequest.CMD_PREVIEW_BILL: {
                        String code = params[0].toString();
                        String result = db.previewBill(code);
                        client.sendToClient(result);
                        break;
                    }
                    
                    case ClientRequest.CMD_GET_SUBSCRIBER_HISTORY: {
                        int subscriberId = (int) request.getParams()[0];

                        List<ReservationHistoryRow> reservations =
                                db.getSubscriberReservationHistory(subscriberId);

                        int visits =
                                db.countSubscriberVisits(subscriberId);

                        SubscriberHistoryResponse response =
                                new SubscriberHistoryResponse(reservations, visits);

                        client.sendToClient(response);
                        break;
                    }
                    
                    case ClientRequest.CMD_JOIN_WAITING_LIST_GUEST: {
                        int diners = Integer.parseInt(params[0].toString());
                        String phone = (params.length >= 2 && params[1] != null) ? params[1].toString().trim() : "";
                        String email = (params.length >= 3 && params[2] != null) ? params[2].toString().trim() : "";

                        Integer tableNow = db.tryAssignWalkInToEmptyTable(diners);

                        if (tableNow != null && tableNow > 0) {
                            client.sendToClient("WAITING_DIRECT_TABLE|Added to an empty table. No need to join the waiting list.|" + tableNow);
                            break;
                        }


                        // ✅ 2) אם אין שולחן פנוי -> ממשיכים בדיוק ללוגיקה הישנה
                        String code = db.joinWaitingListAsGuest(diners, phone, email);

                        if ("CLOSED".equals(code)) client.sendToClient("WAITING_CLOSED");
                        else if (code != null && !code.isBlank()) client.sendToClient("WAITING_GUEST_JOIN_OK|" + code);
                        else client.sendToClient("WAITING_GUEST_JOIN_FAIL|DB error");

                        break;
                    }
                    case ClientRequest.CMD_JOIN_WAITING_LIST: {
                        int subscriberId = Integer.parseInt(params[0].toString());
                        int diners = Integer.parseInt(params[1].toString());
                        String phone = (params.length >= 3 && params[2] != null) ? params[2].toString().trim() : "";
                        String email = (params.length >= 4 && params[3] != null) ? params[3].toString().trim() : "";

                        Integer tableNow = db.tryAssignWalkInToEmptyTable(diners);

                        if (tableNow != null && tableNow > 0) {
                            client.sendToClient("WAITING_DIRECT_TABLE|Added to an empty table. No need to join the waiting list.|" + tableNow);
                            break;
                        }



                        // ✅ 2) אם אין שולחן פנוי -> ממשיכים בדיוק ללוגיקה הישנה
                        String code = db.joinWaitingListAsSubscriber(subscriberId, diners, phone, email);

                        if ("CLOSED".equals(code)) client.sendToClient("WAITING_CLOSED");
                        else if (code != null && !code.isBlank()) client.sendToClient("WAITING_JOIN_OK|" + code);
                        else client.sendToClient("WAITING_JOIN_FAIL|DB error");

                        break;
                    }



                    case ClientRequest.CMD_LEAVE_WAITING_LIST: {
                        int userId = Integer.parseInt(params[0].toString());

                        boolean ok = db.leaveWaitingListAsSubscriber(userId);

                        if (ok) client.sendToClient("WAITING_LEAVE_OK|Removed from DB");
                        else client.sendToClient("WAITING_LEAVE_FAIL|Not in waiting list");

                        
                        break;
                    }

                    
                    case ClientRequest.CMD_LEAVE_WAITING_LIST_GUEST: {
                        String code = (params[0] == null) ? "" : params[0].toString().trim();

                        boolean ok = db.leaveWaitingListAsGuest(code);

                        if (ok) client.sendToClient("WAITING_GUEST_LEAVE_OK|Removed from DB");
                        else client.sendToClient("WAITING_GUEST_LEAVE_FAIL|Not in waiting list");

                        break;
                    }





                    // =========================
                    // TABLES
                    // =========================
                    case "GET_TABLES": {
                        List<Table> tables = tableDAO.getAllTables();
                        client.sendToClient(tables);
                        break;
                    }
                    
                    case "ADD_TABLE": {
                        Table t = (Table) params[0];
                        tableDAO.addTable(t);
                        client.sendToClient("✅ Table added");
                        client.sendToClient(tableDAO.getAllTables());
                        break;
                    }

                    case "UPDATE_TABLE": {
                        Table t = (Table) params[0];
                        tableDAO.updateTable(t);
                        client.sendToClient("✅ Table updated");
                        client.sendToClient(tableDAO.getAllTables());
                        break;
                    }

                    case "DELETE_TABLE": {
                        int tableNumber = Integer.parseInt(params[0].toString());
                        tableDAO.deleteTable(tableNumber);

                        int canceled = db.cancelFutureReservationsDueToTableRemoval(
                            tableNumber,
                            notifier,
                            "Table " + tableNumber + " was removed from the restaurant."
                        );
                        if (canceled > 0) System.out.println("🧾 Canceled due to table removal: " + canceled);

                        client.sendToClient("✅ Table deleted");
                        client.sendToClient(tableDAO.getAllTables());
                        break; // ✅ MUST HAVE
                    }

                    
                    case "ADD_SUBSCRIBER": {

                        if (!isRepresentative(client)) {
                            client.sendToClient("❌ Unauthorized");
                            break;
                        }

                        try {
                            String name = params[0].toString();
                            String email = params[1].toString();
                            String phone = params[2].toString();
                            String password = params[3].toString();
                            boolean active = (boolean) params[4];

                            MySQLUserDAO userDAO = new MySQLUserDAO();
                            userDAO.addSubscriber(name, email, phone, password, active);

                            client.sendToClient("✅ Subscriber added");

                        } catch (Exception e) {
                            e.printStackTrace();
                            client.sendToClient("❌ Failed to add subscriber");
                        }
                        break;
                    }


                    /* =========================
                       RESERVATIONS
                       ========================= */
                    case ClientRequest.CMD_GET_AVAILABLE_SLOTS: {
                        String dateStr = params[0].toString();
                        int diners = Integer.parseInt(params[1].toString());

                        client.sendToClient(
                                db.getAvailableReservationSlots(dateStr, diners)
                        );
                        break;
                    }
                    case ClientRequest.CMD_GET_ALL_SUBSCRIBERS: {

                        // 🔐 Authorization: REPRESENTATIVE only
                        if (!isRepresentative(client)) {
                            client.sendToClient("❌ Unauthorized: Representative only");
                            break;
                        }

                        try {
                            MySQLUserDAO userDAO = new MySQLUserDAO();
                            List<User> subscribers = userDAO.getAllSubscribers();
                            client.sendToClient(subscribers);
                        } catch (Exception e) {
                            e.printStackTrace();
                            client.sendToClient("❌ Error loading subscribers");
                        }
                        break;
                    }

                    case ClientRequest.CMD_CREATE_RESERVATION: {
                        String dateTime = params[0].toString();
                        int diners = Integer.parseInt(params[1].toString());
                        String customer = params[2].toString();
                        String phone = params[3].toString();
                        String email = (params.length >= 5 && params[4] != null)
                                ? params[4].toString() : "";

                        String result = db.createReservation(dateTime, diners, customer, phone, email);

                        if ("FULL".equals(result)) {
                            client.sendToClient("RESERVATION_FULL");
                        } else if (result != null && !result.isBlank()) {
                            client.sendToClient("RESERVATION_OK|" + result);
                        } else {
                            client.sendToClient("RESERVATION_FAIL");
                        }

                        break;
                    }

                    case ClientRequest.CMD_CANCEL_RESERVATION: {
                        String reservationCode = params[0].toString();
                        String result;

                        if (params.length == 1) {
                            result = db.cancelReservation(reservationCode);
                        } else {
                            Integer id = Integer.parseInt(params[1].toString());
                            String email = (params.length >= 3 && params[2] != null)
                                    ? params[2].toString() : "";
                            String phone = (params.length >= 4 && params[3] != null)
                                    ? params[3].toString() : "";

                            result = db.cancelReservation(
                                    reservationCode, id, email, phone, true
                            );
                        }

                        client.sendToClient(result);
                        break;
                    }
                    
                    //Check In
                    case ClientRequest.CMD_CHECK_IN: {
                        String code = params[0].toString();
                        // This calls the DAO method we wrote in the previous step
                        Object result = reservationDAO.checkInCustomer(code);
                        client.sendToClient(result);
                        break;
                    }
                    
                    case ClientRequest.CMD_GET_MONTHLY_TIME_REPORT: {
                        // 1. Parse parameters safely using toString() first, then parseInt
                        int month = Integer.parseInt(params[0].toString());
                        int year = Integer.parseInt(params[1].toString());

                        // 2. Call the non-static method on your 'db' instance and send result
                        client.sendToClient(
                                db.generateMonthlyTimeReport(month, year)
                        );
                        break;
                    }
                    
                    case ClientRequest.CMD_GET_ALL_RESERVATIONS: {
                        // Call the new method we just wrote
                        client.sendToClient(db.getAllReservations());
                        break;
                    }
                    
                    case ClientRequest.CMD_GET_SUBSCRIBER_REPORT: {
                        int m = Integer.parseInt(params[0].toString());
                        int y = Integer.parseInt(params[1].toString());

                        client.sendToClient(
                            db.generateMonthlySubscriberReport(m, y)
                        );
                        break;
                    }
                    
                    case "DISCONNECT":
                        client.close();
                        break;
                        
                    case ClientRequest.CMD_FORGOT_CONFIRMATION_CODE: {
                    	
                    	

                        // If your client sends {phone, email} then KEEP this order:
                    	String email = (params.length > 0 && params[0] != null) ? params[0].toString().trim() : "";
                    	String phone = (params.length > 1 && params[1] != null) ? params[1].toString().trim() : "";
                    	
                    	System.out.println("FORGOT: email=" + email + " phone=" + phone);


                        String genericReply = "FORGOT_OK|If a reservation matches these details, the code was sent.";

                        // require BOTH (recommended)
                        if (email.isEmpty() || phone.isEmpty()) {
                            client.sendToClient(genericReply);
                            break;
                        }

                        String key = email.toLowerCase() + "|" + phone;
                        long now = System.currentTimeMillis();

                        Long last = forgotCooldown.get(key);
                        if (last != null && (now - last) < 60_000) {
                            client.sendToClient(genericReply);
                            break;
                        }

                        // Find code (may be null)
                        String code = db.findConfirmationCodeByEmailAndPhone(email, phone);

                    	System.out.println("FORGOT: codeFound=" + code);

                        // If found, send it
                        if (code != null && !code.isBlank()) {
                            forgotCooldown.put(key, now);

                            String msgText = "Your Bistro confirmation code is: " + code;

                            // email
                            notifier.sendEmail(email, "Bistro confirmation code", msgText);

                            // sms
                            notifier.sendSms(phone, msgText);
                        }

                        // Always generic reply
                        client.sendToClient(genericReply);
                        break;
                    }
                    case ClientRequest.CMD_UPDATE_SUBSCRIBER_DETAILS: {

                        // params: subscriberId, newEmail, newPhone
                        int requestedId = Integer.parseInt(params[0].toString());
                        String newEmail = (params[1] == null) ? "" : params[1].toString().trim();
                        String newPhone = (params[2] == null) ? "" : params[2].toString().trim();

                        // basic validation
                        if (newEmail.isEmpty() || !newEmail.contains("@") || !newEmail.contains(".")) {
                            client.sendToClient("UPDATE_SUB_FAIL|Invalid email.");
                            break;
                        }
                        if (newPhone.isEmpty() || newPhone.length() < 7) {
                            client.sendToClient("UPDATE_SUB_FAIL|Invalid phone.");
                            break;
                        }

                        // auth: allow self-update OR representative/manager
                        String[] info = clientInfoMap.get(client);
                        String role = (info != null && info.length > 0) ? info[0] : "UNKNOWN";
                        int loggedInId = -1;
                        if (info != null && info.length >= 3) {
                            try { loggedInId = Integer.parseInt(info[2]); } catch (Exception ignored) {}
                        }

                        boolean isRep = "REPRESENTATIVE".equals(role) || "MANAGER".equals(role);
                        boolean isSelf = (loggedInId > 0 && loggedInId == requestedId);

                        if (!isSelf && !isRep) {
                            client.sendToClient("UPDATE_SUB_FAIL|Unauthorized.");
                            break;
                        }

                        try {
                            MySQLUserDAO userDAO = new MySQLUserDAO();
                            boolean ok = userDAO.updateUserContact(requestedId, newEmail, newPhone);

                            if (ok) {
                                client.sendToClient("UPDATE_SUB_OK|" + newEmail + "|" + newPhone);
                            } else {
                                // most common cause: email already taken
                                client.sendToClient("UPDATE_SUB_FAIL|Email is already used by another user.");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            client.sendToClient("UPDATE_SUB_FAIL|Server error.");
                        }

                        break;
                    }




                    default:
                        client.sendToClient("❌ Unknown command: " + command);
                }

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    client.sendToClient("❌ Error processing command: " + e.getMessage());
                } catch (IOException ignored) {}
            }
        }
    }

    /* =========================
    AUTHORIZATION
    ========================= */
	 private boolean isRepresentative(ConnectionToClient client) {
	     String[] info = clientInfoMap.get(client);
	     if (info == null) return false;
	
	     String role = info[0];
	     return "REPRESENTATIVE".equals(role) || "MANAGER".equals(role);
	 }


    /* =========================
       CLIENT CONNECT / DISCONNECT
       ========================= */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        String ip = client.getInetAddress().getHostAddress();
        String host = client.getInetAddress().getHostName();

        clientInfoMap.putIfAbsent(client, new String[] { "UNKNOWN", "UNKNOWN" });

        if (guiController != null) {
            guiController.addClient(ip, host, client.hashCode());
        }

        System.out.println("✅ Client connected: " + ip + " / " + host);
    }

    @Override
    protected synchronized void clientDisconnected(ConnectionToClient client) {
        clientInfoMap.remove(client);

        if (guiController != null) {
            guiController.updateClientStatus(client.hashCode(), "Disconnected");
        }

        System.out.println("❌ Client disconnected");
    }
    
    @Override
    protected void serverStopped() {
        System.out.println("🛑 Server stopped. Shutting down scheduler...");
        scheduler.shutdownNow();
    }

}