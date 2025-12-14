package server;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import common.Order;

public class DBController {

    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

    // ------------------------------------------------------------
    // GET ALL ORDERS
    // ------------------------------------------------------------
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `Order`";

        PooledConnection pConn = null;
        Connection conn = null;

        try {
            pConn = pool.getConnection();
            pConn.touch();
            conn = pConn.getConnection();

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    orders.add(new Order(
                            rs.getInt("order_number"),
                            rs.getDate("order_date"),
                            rs.getInt("number_of_guests"),
                            rs.getInt("confirmation_code"),
                            rs.getInt("subscriber_id"),
                            rs.getDate("date_of_placing_order")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.releaseConnection(pConn);
        }

        return orders;
    }

    // ------------------------------------------------------------
    // UPDATE ORDER DATE
    // ------------------------------------------------------------
    public boolean updateOrderDate(int orderNumber, Date newDate) {
        String sql = "UPDATE `Order` SET order_date = ? WHERE order_number = ?";

        PooledConnection pConn = null;
        Connection conn = null;

        try {
            pConn = pool.getConnection();
            pConn.touch();
            conn = pConn.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, newDate);
                stmt.setInt(2, orderNumber);

                return stmt.executeUpdate() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    // ------------------------------------------------------------
    // UPDATE NUMBER OF GUESTS
    // ------------------------------------------------------------
    public boolean updateNumberOfGuests(int orderNumber, int guests) {
        String sql = "UPDATE `Order` SET number_of_guests = ? WHERE order_number = ?";

        PooledConnection pConn = null;
        Connection conn = null;

        try {
            pConn = pool.getConnection();
            pConn.touch();
            conn = pConn.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, guests);
                stmt.setInt(2, orderNumber);

                return stmt.executeUpdate() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.releaseConnection(pConn);
        }
    }
}
