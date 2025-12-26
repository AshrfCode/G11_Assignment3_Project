package server;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import common.ClientRequest;
import common.Order;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import servergui.ServerMainController;

import common.LoginRequest;
import entities.User;
import server.dao.MySQLUserDAO;

public class BistroServer extends AbstractServer {

    private Map<ConnectionToClient, String[]> clientInfoMap = new ConcurrentHashMap<>();
    private DBController db = new DBController();

    private ServerMainController guiController;

    public BistroServer(int port, ServerMainController guiController) {
        super(port);
        this.guiController = guiController;
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

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
                    // Changed: include id + email + phone so subscriber reservation can be auto-filled.
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

        else if (msg instanceof ClientRequest request) {
            try {
                String command = request.getCommand();
                Object[] params = request.getParams();

                switch (command) {

                    case "GET_ALL_ORDERS":
                        List<Order> orders = db.getAllOrders();
                        client.sendToClient(orders);
                        break;

                    case "UPDATE_ORDER_DATE":
                        int orderId = Integer.parseInt(params[0].toString());
                        java.sql.Date newDate = java.sql.Date.valueOf(params[1].toString());

                        boolean dateUpdated = db.updateOrderDate(orderId, newDate);

                        if (dateUpdated) {
                            client.sendToClient("OK_DATE");
                        } else {
                            client.sendToClient("ERROR_DATE");
                        }

                        break;

                    case "UPDATE_NUMBER_OF_GUESTS":
                        int orderIdGuests = Integer.parseInt(params[0].toString());
                        int newGuests = Integer.parseInt(params[1].toString());

                        boolean guestsUpdated = db.updateNumberOfGuests(orderIdGuests, newGuests);

                        if (guestsUpdated) {
                            client.sendToClient("OK_GUESTS");
                        } else {
                            client.sendToClient("ERROR_GUESTS");
                        }
                        break;

                    // Reservation commands
                    case ClientRequest.CMD_GET_AVAILABLE_SLOTS:
                        String dateStr = params[0].toString();
                        int diners = Integer.parseInt(params[1].toString());

                        List<String> slots = db.getAvailableReservationSlots(dateStr, diners);
                        client.sendToClient(slots);
                        break;

                    case ClientRequest.CMD_CREATE_RESERVATION:
                        String dateTimeStr = params[0].toString();
                        int dinersToReserve = Integer.parseInt(params[1].toString());
                        String customerIdOrEmail = params[2].toString();
                        String phone = params[3].toString();
                        String email = (params.length >= 5 && params[4] != null) ? params[4].toString() : "";

                        String code = db.createReservation(dateTimeStr, dinersToReserve, customerIdOrEmail, phone, email);

                        if (code != null && !code.isEmpty()) {
                            client.sendToClient("RESERVATION_OK|" + code);
                        } else {
                            client.sendToClient("RESERVATION_FAIL");
                        }
                        break;

                    case ClientRequest.CMD_CANCEL_RESERVATION:
                        String reservationCode = params[0].toString();
                        boolean canceled = db.cancelReservation(reservationCode);

                        if (canceled) {
                            client.sendToClient("CANCEL_OK");
                        } else {
                            client.sendToClient("CANCEL_FAIL");
                        }
                        break;

                    case "DISCONNECT":
                        client.close();
                        break;

                    default:
                        client.sendToClient("❌ Unknown command: " + command);
                }

            } catch (Exception e) {
                System.err.println("❌ Error handling client request: " + e.getMessage());
                e.printStackTrace();
                try {
                    client.sendToClient("❌ Error processing command: " + e.getMessage());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        } else {
            System.err.println("⚠️ Received unsupported message type: " + msg.getClass().getName());
        }
    }

    @Override
    protected void clientConnected(ConnectionToClient client) {
        String ip = client.getInetAddress().getHostAddress();
        String host = client.getInetAddress().getHostName();
        int id = client.hashCode();

        clientInfoMap.put(client, new String[] { ip, host });

        if (guiController != null) {
            guiController.addClient(ip, host, id);
        }

        System.out.println("✅ Client connected: " + ip + " / " + host);
    }

    @Override
    protected synchronized void clientDisconnected(ConnectionToClient client) {
        String[] info = clientInfoMap.getOrDefault(client, new String[] { "unknown", "unknown" });
        String ip = info[0];
        String host = info[1];
        int id = client.hashCode();

        if (guiController != null) {
            guiController.updateClientStatus(id, "Disconnected");
        }

        System.out.println("❌ Client disconnected: " + ip + " / " + host);
        clientInfoMap.remove(client);
    }
}
