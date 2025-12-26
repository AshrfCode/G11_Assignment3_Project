package server;

import java.security.SecureRandom;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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

    // ============================================================
    // RESERVATIONS (availability / create / cancel)
    // ============================================================

    public List<String> getAvailableReservationSlots(String dateStr, int diners) {
        List<String> slots = new ArrayList<>();

        PooledConnection pConn = null;
        Connection conn = null;

        try {
            LocalDate date = LocalDate.parse(dateStr);

            pConn = pool.getConnection();
            pConn.touch();
            conn = pConn.getConnection();

            HoursRange hours = getOpeningHoursOrDefault(conn, date);

            // reservation is 2 hours, so last possible start is close - 2h
            LocalTime lastStart = hours.close.minusHours(2);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime minAllowed = now.plusHours(1);
            LocalDateTime maxAllowed = now.plusMonths(1);

            LocalTime t = hours.open;
            while (!t.isAfter(lastStart)) {
                LocalDateTime start = LocalDateTime.of(date, t);

                if (!start.isBefore(minAllowed) && !start.isAfter(maxAllowed)) {
                    if (isSlotAvailable(conn, start, diners)) {
                        slots.add(t.toString()); // "HH:mm"
                    }
                }

                t = t.plusMinutes(30);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.releaseConnection(pConn);
        }

        return slots;
    }

    public String createReservation(String dateTimeStr, int diners, String customerIdOrEmail, String phone, String email) {
        PooledConnection pConn = null;
        Connection conn = null;

        try {
            String[] parts = dateTimeStr.trim().split("\\s+");
            LocalDate date = LocalDate.parse(parts[0]);
            LocalTime time = LocalTime.parse(parts[1]);
            LocalDateTime start = LocalDateTime.of(date, time);
            LocalDateTime end = start.plusHours(2);

            LocalDateTime now = LocalDateTime.now();
            if (start.isBefore(now.plusHours(1)) || start.isAfter(now.plusMonths(1))) {
                return null;
            }

            pConn = pool.getConnection();
            pConn.touch();
            conn = pConn.getConnection();

            HoursRange hours = getOpeningHoursOrDefault(conn, date);

            // must be inside opening hours and allow full 2 hours
            if (time.isBefore(hours.open) || end.toLocalTime().isAfter(hours.close)) {
                return null;
            }

            Integer tableId = findBestAvailableTable(conn, start, diners);
            if (tableId == null) {
                return null;
            }

            String code = generateConfirmationCode();
            Integer subscriberId = tryParseInt(customerIdOrEmail);

            String sql =
                    "INSERT INTO `Reservation` " +
                    "(`confirmation_code`, `start_time`, `end_time`, `diners`, `subscriber_id`, `guest_phone`, `guest_email`, `status`, `created_at`, `table_id`) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', NOW(), ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, code);
                stmt.setTimestamp(2, Timestamp.valueOf(start));
                stmt.setTimestamp(3, Timestamp.valueOf(end));
                stmt.setInt(4, diners);

                if (subscriberId == null) stmt.setNull(5, Types.INTEGER);
                else stmt.setInt(5, subscriberId);

                stmt.setString(6, phone == null ? "" : phone);
                stmt.setString(7, email == null ? "" : email);

                stmt.setInt(8, tableId);

                int rows = stmt.executeUpdate();
                if (rows > 0) return code;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.releaseConnection(pConn);
        }

        return null;
    }

    public boolean cancelReservation(String confirmationCode) {
        String sql = "UPDATE `Reservation` SET `status`='CANCELED' WHERE `confirmation_code`=? AND `status`='ACTIVE'";

        PooledConnection pConn = null;
        Connection conn = null;

        try {
            pConn = pool.getConnection();
            pConn.touch();
            conn = pConn.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, confirmationCode);
                return stmt.executeUpdate() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    // -------------------- table-based helpers --------------------

    private boolean isSlotAvailable(Connection conn, LocalDateTime start, int diners) throws SQLException {
        return findBestAvailableTable(conn, start, diners) != null;
    }

    private Integer findBestAvailableTable(Connection conn, LocalDateTime start, int diners) throws SQLException {
        LocalDateTime end = start.plusHours(2);

        String sql =
                "SELECT t.table_id " +
                "FROM `table` t " +
                "WHERE t.capacity >= ? " +
                "AND NOT EXISTS ( " +
                "   SELECT 1 FROM `Reservation` r " +
                "   WHERE r.status='ACTIVE' " +
                "     AND r.table_id = t.table_id " +
                "     AND r.start_time < ? AND r.end_time > ? " +
                ") " +
                "ORDER BY t.capacity ASC, t.table_id ASC " +
                "LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, diners);
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            stmt.setTimestamp(3, Timestamp.valueOf(start));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("table_id");
            }
        }
        return null;
    }

    // -------------------- opening hours --------------------

    private HoursRange getOpeningHoursOrDefault(Connection conn, LocalDate date) {
        // Changed: default hours are 10:00-22:00 (so you can see morning times if you don't have RestaurantHours)
        LocalTime open = LocalTime.of(10, 0);
        LocalTime close = LocalTime.of(22, 0);

        // Try to read from DB (supports day_of_week as 1-7 or 0-6)
        HoursRange dbHours = tryReadHours(conn, date);
        if (dbHours != null) return dbHours;

        return new HoursRange(open, close);
    }

    // Comment: Made RestaurantHours lookup flexible (table/column + day numbering).
    private HoursRange tryReadHours(Connection conn, LocalDate date) {
        int dowJava = date.getDayOfWeek().getValue(); // 1=Mon..7=Sun
        int dowZeroBased = dowJava % 7;              // 0=Sun..6=Sat

        String[] tables = {"RestaurantHours", "restaurant_hours", "hours"};
        for (String table : tables) {
            // try day_of_week = 1..7
            HoursRange r1 = tryReadHoursFromTable(conn, table, "day_of_week", dowJava);
            if (r1 != null) return r1;

            // try day_of_week = 0..6
            HoursRange r2 = tryReadHoursFromTable(conn, table, "day_of_week", dowZeroBased);
            if (r2 != null) return r2;
        }

        return null;
    }

    private HoursRange tryReadHoursFromTable(Connection conn, String table, String dayColumn, int dayValue) {
        try {
            String sql = "SELECT `open_time`, `close_time` FROM `" + table + "` WHERE `" + dayColumn + "`=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, dayValue);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Time o = rs.getTime("open_time");
                        Time c = rs.getTime("close_time");
                        if (o != null && c != null) {
                            return new HoursRange(o.toLocalTime(), c.toLocalTime());
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    // -------------------- misc helpers --------------------

    private String generateConfirmationCode() {
        SecureRandom rnd = new SecureRandom();
        int num = 100000 + rnd.nextInt(900000);
        return String.valueOf(num);
    }

    private Integer tryParseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static class HoursRange {
        final LocalTime open;
        final LocalTime close;

        HoursRange(LocalTime open, LocalTime close) {
            this.open = open;
            this.close = close;
        }
    }
}
