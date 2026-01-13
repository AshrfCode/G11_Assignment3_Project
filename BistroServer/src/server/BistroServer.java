package server;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import common.ClientRequest;
import common.LoginRequest;
import common.ReservationHistoryRow;
import common.SubscriberHistoryResponse;
import entities.Table;
import entities.User;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import server.dao.MySQLUserDAO;
import server.dao.TableDAO;
import server.dao.WaitingListDAO;
import servergui.ServerMainController;

public class BistroServer extends AbstractServer {

    private Map<ConnectionToClient, String[]> clientInfoMap = new ConcurrentHashMap<>();
    private DBController db = new DBController();
    private TableDAO tableDAO = new TableDAO();

    private ServerMainController guiController;

    public BistroServer(int port, ServerMainController guiController) {
        super(port);
        this.guiController = guiController;
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

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
                            user.getName()
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
           CLIENT REQUESTS
           ========================= */
        else if (msg instanceof ClientRequest request) {

            try {
                String command = request.getCommand();
                Object[] params = request.getParams();

                switch (command) {


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
                        // params: day, openTime, closeTime
                        String day = params[0].toString();        // "SUNDAY" וכו'
                        String open = params[1].toString();       // "10:00"
                        String close = params[2].toString();      // "22:00"
                        boolean ok = db.updateOpeningHours(day, open, close);
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
                        client.sendToClient(ok ? "SPECIAL_OPENING_SAVED" : "SPECIAL_OPENING_SAVE_FAIL");
                        break;
                    }

                    case ClientRequest.CMD_DELETE_SPECIAL_OPENING: {
                        String date = params[0].toString();
                        boolean ok = db.deleteSpecialOpening(date);
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

                    case ClientRequest.CMD_JOIN_WAITING_LIST: {
                        int userId = Integer.parseInt(params[0].toString());
                        int diners = Integer.parseInt(params[1].toString());

                        String phone = (params.length >= 3 && params[2] != null) ? params[2].toString().trim() : "";
                        String email = (params.length >= 4 && params[3] != null) ? params[3].toString().trim() : "";

                        String code = db.joinWaitingListAsSubscriber(userId, diners, phone, email);

                        if (code != null && !code.isBlank())
                            client.sendToClient("WAITING_JOIN_OK|" + code);
                        else
                            client.sendToClient("WAITING_JOIN_FAIL|DB error");

                        break;
                    }



                    case ClientRequest.CMD_LEAVE_WAITING_LIST: {
                        int userId = Integer.parseInt(params[0].toString());

                        boolean ok = db.leaveWaitingListAsSubscriber(userId);

                        if (ok) client.sendToClient("WAITING_LEAVE_OK|Removed from DB");
                        else client.sendToClient("WAITING_LEAVE_FAIL|Not in waiting list");

                        
                        break;
                    }

                    case ClientRequest.CMD_JOIN_WAITING_LIST_GUEST: {
                        int diners = Integer.parseInt(params[0].toString());
                        String phone = (params.length >= 2 && params[1] != null) ? params[1].toString().trim() : "";
                        String email = (params.length >= 3 && params[2] != null) ? params[2].toString().trim() : "";

                        String code = db.joinWaitingListAsGuest(diners, phone, email);

                        if (code != null && !code.isBlank())
                            client.sendToClient("WAITING_GUEST_JOIN_OK|" + code);
                        else
                            client.sendToClient("WAITING_GUEST_JOIN_FAIL|DB error");

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
                        client.sendToClient("✅ Table deleted");
                        client.sendToClient(tableDAO.getAllTables());
                        
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

                        String code = db.createReservation(
                                dateTime, diners, customer, phone, email
                        );

                        client.sendToClient(
                                (code != null && !code.isEmpty())
                                        ? "RESERVATION_OK|" + code
                                        : "RESERVATION_FAIL"
                        );
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

                      
                    case "DISCONNECT":
                        client.close();
                        break;

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
        return info != null && "REPRESENTATIVE".equals(info[0]);
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
}
