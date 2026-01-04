package server;

import java.security.SecureRandom;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import common.Order;

public class DBController {
	
	// ================== Cancel result codes ==================
	public static final String CANCEL_OK = "CANCEL_OK";
	public static final String CANCEL_FAIL_NOT_FOUND = "CANCEL_FAIL_NOT_FOUND";
	public static final String CANCEL_FAIL_NOT_OWNER = "CANCEL_FAIL_NOT_OWNER";
	public static final String CANCEL_FAIL_ALREADY_CANCELED = "CANCEL_FAIL_ALREADY_CANCELED";
	public static final String CANCEL_FAIL_ERROR = "CANCEL_FAIL_ERROR";

    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

    // ------------------------------------------------------------
    // GET ALL ORDERS
    // ------------------------------------------------------------
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `Order`";

        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            pConn.touch();
            Connection conn = pConn.getConnection();

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

        try {
            pConn = pool.getConnection();
            pConn.touch();
            Connection conn = pConn.getConnection();

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

        try {
            pConn = pool.getConnection();
            pConn.touch();
            Connection conn = pConn.getConnection();

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
    // RESERVATIONS
    // ============================================================

    public List<String> getAvailableReservationSlots(String dateStr, int diners) {
        List<String> slots = new ArrayList<>();

        PooledConnection pConn = null;

        try {
            LocalDate date = LocalDate.parse(dateStr);

            pConn = pool.getConnection();
            pConn.touch();
            Connection conn = pConn.getConnection();

            HoursRange hours = getOpeningHoursOrDefault(conn, date);

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

    /**
     * CREATE RESERVATION
     * ✅ NEW BEHAVIOR:
     * - If customerIdOrEmail is a subscriberId -> use it
     * - Else (guest): if email/phone belongs to a subscriber in users table -> auto-upgrade and save subscriber_id
     */
    public String createReservation(String dateTimeStr, int diners, String customerIdOrEmail, String phone, String email) {
        PooledConnection pConn = null;

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
            Connection conn = pConn.getConnection();

            HoursRange hours = getOpeningHoursOrDefault(conn, date);

            if (time.isBefore(hours.open) || end.toLocalTime().isAfter(hours.close)) {
                return null;
            }

            Integer tableId = findBestAvailableTable(conn, start, diners);
            if (tableId == null) {
                return null;
            }

            String safePhone = (phone == null) ? "" : phone.trim();
            String safeEmail = (email == null) ? "" : email.trim();

            Integer subscriberId = null;

         // ✅ Only treat it as subscriber id if it was sent as SUB:<id>
         if (customerIdOrEmail != null && customerIdOrEmail.startsWith("SUB:")) {
             subscriberId = tryParseInt(customerIdOrEmail.substring(4));
         }


            // 2) Guest case: try to auto-detect subscriber by email/phone
            if (subscriberId == null) {
                subscriberId = findSubscriberIdByEmailOrPhone(conn, safeEmail, safePhone);
            }

            String code = generateConfirmationCode();

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

                // keep guest info too (helps cancel from subscriber if old records exist)
                stmt.setString(6, safePhone);
                stmt.setString(7, safeEmail);

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

   
 // ------------------------------------------------------------
 // CANCEL RESERVATION (Guest / Subscriber)
 // ------------------------------------------------------------

    /**
     * Guest cancel (by code only)
     */
    public String cancelReservation(String confirmationCode) {
        return cancelReservation(confirmationCode, null, "", "", false);
    }

    /**
     * Subscriber cancel (restricted to owner when restrictToOwner=true)
     */
    public String cancelReservation(String confirmationCode,
                                    Integer requesterSubscriberId,
                                    String requesterEmail,
                                    String requesterPhone,
                                    boolean restrictToOwner) {

        PooledConnection pConn = null;

        try {
            String code = (confirmationCode == null) ? "" : confirmationCode.trim();
            String safeEmail = (requesterEmail == null) ? "" : requesterEmail.trim();
            String safePhone = (requesterPhone == null) ? "" : requesterPhone.trim();

            if (code.isEmpty()) return CANCEL_FAIL_NOT_FOUND;

            pConn = pool.getConnection();
            pConn.touch();
            Connection conn = pConn.getConnection();

            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                // 1) Lock the reservation row so check + update are safe
                String selectSql =
                        "SELECT status, subscriber_id, guest_email, guest_phone " +
                        "FROM `Reservation` WHERE confirmation_code=? FOR UPDATE";

                String status;
                Integer subId;
                String gEmail;
                String gPhone;

                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setString(1, code);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return CANCEL_FAIL_NOT_FOUND;
                        }

                        status = rs.getString("status");
                        int raw = rs.getInt("subscriber_id");
                        subId = rs.wasNull() ? null : raw;

                        gEmail = rs.getString("guest_email");
                        gPhone = rs.getString("guest_phone");
                    }
                }

                // 2) If it’s not ACTIVE -> already canceled (or not active)
                if (!"ACTIVE".equalsIgnoreCase(status)) {
                    conn.rollback();
                    return CANCEL_FAIL_ALREADY_CANCELED;
                }

                // 3) Ownership check (only for subscriber cancel)
                if (restrictToOwner) {
                    boolean okOwner = false;

                    if (requesterSubscriberId != null && subId != null && subId.equals(requesterSubscriberId)) {
                        okOwner = true;
                    }

                    if (!okOwner && !safeEmail.isEmpty() && gEmail != null && !gEmail.trim().isEmpty()) {
                        okOwner = gEmail.trim().equalsIgnoreCase(safeEmail);
                    }

                    if (!okOwner && !safePhone.isEmpty() && gPhone != null && !gPhone.trim().isEmpty()) {
                        okOwner = gPhone.trim().equals(safePhone);
                    }

                    if (!okOwner) {
                        conn.rollback();
                        return CANCEL_FAIL_NOT_OWNER;
                    }
                }

                // 4) Update
                String updateSql =
                        "UPDATE `Reservation` SET status='CANCELED' " +
                        "WHERE confirmation_code=? AND status='ACTIVE'";

                int rows;
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, code);
                    rows = ps.executeUpdate();
                }

                if (rows > 0) {
                    conn.commit();
                    return CANCEL_OK;
                } else {
                    conn.rollback();
                    return CANCEL_FAIL_ALREADY_CANCELED;
                }

            } finally {
                conn.setAutoCommit(oldAuto);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return CANCEL_FAIL_ERROR;
        } finally {
            pool.releaseConnection(pConn);
        }
    }


    // -------------------- subscriber lookup helper --------------------

    /**
     * If a guest entered phone/email that already belongs to a subscriber,
     * return that subscriber's users.id (so reservation becomes subscriber reservation).
     */
    private Integer findSubscriberIdByEmailOrPhone(Connection conn, String email, String phone) {
        try {
            // Prefer email match
            if (email != null && !email.isEmpty()) {
                String sql = "SELECT id FROM `users` WHERE role='SUBSCRIBER' AND is_active=1 AND email=? LIMIT 1";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, email);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) return rs.getInt("id");
                    }
                }
            }

            // Then phone match
            if (phone != null && !phone.isEmpty()) {
                String sql = "SELECT id FROM `users` WHERE role='SUBSCRIBER' AND is_active=1 AND phone=? LIMIT 1";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, phone);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) return rs.getInt("id");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
        LocalTime open = LocalTime.of(10, 0);
        LocalTime close = LocalTime.of(22, 0);

        HoursRange dbHours = tryReadHours(conn, date);
        if (dbHours != null) return dbHours;

        return new HoursRange(open, close);
    }

    private HoursRange tryReadHours(Connection conn, LocalDate date) {
        int dowJava = date.getDayOfWeek().getValue(); // 1=Mon..7=Sun
        int dowZeroBased = dowJava % 7;              // 0=Sun..6=Sat

        String[] tables = {"RestaurantHours", "restaurant_hours", "hours"};
        for (String table : tables) {
            HoursRange r1 = tryReadHoursFromTable(conn, table, "day_of_week", dowJava);
            if (r1 != null) return r1;

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
