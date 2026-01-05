package server;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import common.ClientRequest;
import common.Order;
import common.LoginRequest;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

import servergui.ServerMainController;
import server.dao.MySQLUserDAO;
import server.dao.TableDAO;

import entities.User;
import entities.Table;

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

                    case "GET_ALL_ORDERS":
                        List<Order> orders = db.getAllOrders();
                        client.sendToClient(orders);
                        break;

                    case "UPDATE_ORDER_DATE": {
                        int orderId = Integer.parseInt(params[0].toString());
                        java.sql.Date newDate = java.sql.Date.valueOf(params[1].toString());

                        boolean ok = db.updateOrderDate(orderId, newDate);
                        client.sendToClient(ok ? "OK_DATE" : "ERROR_DATE");
                        break;
                    }

                    case "UPDATE_NUMBER_OF_GUESTS": {
                        int orderId = Integer.parseInt(params[0].toString());
                        int guests = Integer.parseInt(params[1].toString());

                        boolean ok = db.updateNumberOfGuests(orderId, guests);
                        client.sendToClient(ok ? "OK_GUESTS" : "ERROR_GUESTS");
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

                    /* =========================
                    MANAGE TABLES (REPRESENTATIVE ONLY)
                    ========================= */
                 case "GET_TABLES": {
                     List<Table> tables = tableDAO.getAllTables();
                     client.sendToClient(tables);
                     break;
                 }

                 case "ADD_TABLE": {
                     Table t = (Table) params[0];
                     tableDAO.addTable(t);
                     client.sendToClient("✅ Table added");
                     client.sendToClient(tableDAO.getAllTables()); // refresh
                     break;
                 }

                 case "UPDATE_TABLE": {
                     Table t = (Table) params[0];
                     tableDAO.updateTable(t);
                     client.sendToClient("✅ Table updated");
                     client.sendToClient(tableDAO.getAllTables()); // refresh
                     break;
                 }

                 case "DELETE_TABLE": {
                	    int tableNumber = Integer.parseInt(params[0].toString());
                	    tableDAO.deleteTable(tableNumber);
                	    client.sendToClient("✅ Table deleted");
                	    client.sendToClient(tableDAO.getAllTables()); // refresh
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
