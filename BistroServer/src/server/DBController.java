package server;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import common.ReservationHistoryRow;

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
    public List<String> getAllOrders() {
        List<String> result = new ArrayList<>();

        String sql =
            "SELECT id, reserve_date, reserve_time, dinners_number, reservation_code, reservation_status, table_number, subscriber_number " +
            "FROM reservations " +
            "ORDER BY reserve_date DESC, reserve_time DESC";

        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    result.add(
                        "#" + rs.getInt("id") +
                        " | " + rs.getDate("reserve_date") +
                        " " + rs.getTime("reserve_time") +
                        " | diners=" + rs.getInt("dinners_number") +
                        " | code=" + rs.getString("reservation_code") +
                        " | status=" + rs.getString("reservation_status") +
                        " | table=" + rs.getInt("table_number") +
                        " | sub=" + rs.getString("subscriber_number")
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.clear();
            result.add("❌ DB error: " + e.getMessage());
        } finally {
            if (pConn != null) pool.releaseConnection(pConn);
        }

        return result;
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

    public List<String> getAvailableReservationSlots(String dateStr, int dinners) {
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
                    if (isSlotAvailable(conn, start, dinners)) {
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

    public List<String> getTodayReservations() {
    List<String> result = new ArrayList<>();

    String sql =
        "SELECT start_time, dinners_number, table_number, confirmation_code " +
        "FROM reservations " +
        "WHERE DATE(start_time) = CURDATE() AND status = 'ACTIVE' " +
        "ORDER BY start_time";

    PooledConnection pConn = null;

    try {
        pConn = pool.getConnection();
        Connection conn = pConn.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String time = rs.getTimestamp("start_time").toLocalDateTime().toLocalTime().toString();
                int diners = rs.getInt("dinners_number");
                int table = rs.getInt("table_number");
                String code = rs.getString("confirmation_code");

                result.add(
                    "🕒 " + time +
                    " | 👥 " + diners +
                    " | 🍽️ Table " + table +
                    " | 🔑 " + code
                );
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
        result.clear();
        result.add("❌ DB error: " + e.getMessage());
    } finally {
        if (pConn != null) pool.releaseConnection(pConn);
    }

    return result;
}


    /**
     * CREATE RESERVATION
     * ✅ NEW BEHAVIOR:
     * - If customerIdOrEmail is a subscriberId -> use it
     * - Else (guest): if email/phone belongs to a subscriber in users table -> auto-upgrade and save subscriber_number
     */
    public String createReservation(String dateTimeStr, int dinners, String customerIdOrEmail, String phone, String email) {
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

            Integer tableId = findBestAvailableTable(conn, start, dinners);
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
                    "INSERT INTO `Reservations` " +
                    "(`confirmation_code`, `start_time`, `end_time`, `dinners_number`, `subscriber_number`, `phone`, `email`, `status`, `created_at`, `table_number`) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', NOW(), ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, code);
                stmt.setTimestamp(2, Timestamp.valueOf(start));
                stmt.setTimestamp(3, Timestamp.valueOf(end));
                stmt.setInt(4, dinners);

                if (subscriberId == null) {
                    stmt.setNull(5, Types.VARCHAR);
                } else {
                    String subNumber = getSubscriberNumberByUserId(conn, subscriberId);
                    if (subNumber == null || subNumber.isBlank()) {
                        stmt.setNull(5, Types.VARCHAR); // fallback, treat as guest
                    } else {
                        stmt.setString(5, subNumber);   // ✅ THIS is the correct value ("SUB123")
                    }
                }


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
 // SPECIAL OPENING HOURS
 // ------------------------------------------------------------

 public String getSpecialOpeningByDate(String dateYYYYMMDD) {
     String sql = "SELECT special_date, open_time, close_time, is_closed " +
                  "FROM special_opening_hours WHERE special_date = ?";

     PooledConnection pConn = null;

     try {
         pConn = pool.getConnection();
         Connection conn = pConn.getConnection();

         try (PreparedStatement ps = conn.prepareStatement(sql)) {
             ps.setString(1, dateYYYYMMDD);

             try (ResultSet rs = ps.executeQuery()) {
                 if (!rs.next()) {
                     return "No special opening set for " + dateYYYYMMDD;
                 }

                 boolean closed = rs.getBoolean("is_closed");
                 Time open = rs.getTime("open_time");
                 Time close = rs.getTime("close_time");

                 if (closed) return dateYYYYMMDD + " : CLOSED";

                 return dateYYYYMMDD + " : " + open + " - " + close;
             }
         }

     } catch (Exception e) {
         e.printStackTrace();
         return "ERROR: " + e.getMessage();

     } finally {
         pool.releaseConnection(pConn);
     }
 }

 public boolean upsertSpecialOpening(String dateYYYYMMDD, String openTime, String closeTime, boolean isClosed) {
     String sql =
         "INSERT INTO special_opening_hours (special_date, open_time, close_time, is_closed) " +
         "VALUES (?, ?, ?, ?) " +
         "ON DUPLICATE KEY UPDATE " +
         "open_time = VALUES(open_time), " +
         "close_time = VALUES(close_time), " +
         "is_closed = VALUES(is_closed)";

     PooledConnection pConn = null;

     try {
         pConn = pool.getConnection();
         Connection conn = pConn.getConnection();

         try (PreparedStatement ps = conn.prepareStatement(sql)) {
             ps.setString(1, dateYYYYMMDD);

             if (isClosed || openTime == null || openTime.isBlank()) {
                 ps.setNull(2, Types.TIME);
             } else {
                 ps.setTime(2, Time.valueOf(openTime + ":00"));
             }

             if (isClosed || closeTime == null || closeTime.isBlank()) {
                 ps.setNull(3, Types.TIME);
             } else {
                 ps.setTime(3, Time.valueOf(closeTime + ":00"));
             }

             ps.setBoolean(4, isClosed);

             return ps.executeUpdate() > 0;
         }

     } catch (Exception e) {
         e.printStackTrace();
         return false;

     } finally {
         pool.releaseConnection(pConn);
     }
 }

 public boolean deleteSpecialOpening(String dateYYYYMMDD) {
	    String sql = "DELETE FROM special_opening_hours WHERE special_date = ?";

	    PooledConnection pConn = null;
	    try {
	        pConn = pool.getConnection();
	        Connection conn = pConn.getConnection();

	        try (PreparedStatement ps = conn.prepareStatement(sql)) {
	            ps.setString(1, dateYYYYMMDD);
	            return ps.executeUpdate() > 0;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        if (pConn != null) pool.releaseConnection(pConn);
	    }
	}


 // ===========================
 // Regular Opening Hours (Weekly)
 // ===========================

 public List<String> getOpeningHours() {
     List<String> result = new ArrayList<>();

     // אנחנו משתמשים בטבלה RestaurantHours: day_of_week, open_time, close_time
     // day_of_week: 1=Mon ... 7=Sun
     String sql = "SELECT day, open_time, close_time FROM opening_hours ORDER BY FIELD(day,'SUNDAY','MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY')";


     PooledConnection pConn = null;
     try {
         pConn = pool.getConnection();
         Connection conn = pConn.getConnection();

         try (PreparedStatement ps = conn.prepareStatement(sql);
              ResultSet rs = ps.executeQuery()) {

             // אם אין כלום בטבלה – נחזיר ברירות מחדל לכל הימים
             boolean any = false;

             while (rs.next()) {
                 any = true;
                 Time open = rs.getTime("open_time");
                 Time close = rs.getTime("close_time");

                 String dayName = rs.getString("day");
                 String openStr = (open == null) ? "--:--" : open.toLocalTime().toString();
                 String closeStr = (close == null) ? "--:--" : close.toLocalTime().toString();

                 result.add(dayName + " | " + openStr + " - " + closeStr);
             }

             if (!any) {
                 // default 10:00-22:00 לכל הימים
                 for (int d = 1; d <= 7; d++) {
                     result.add(dayIntToName(d) + " | 10:00 - 22:00");
                 }
             }
         }

     } catch (Exception e) {
         e.printStackTrace();
         result.clear();
         result.add("❌ DB error: " + e.getMessage());
     } finally {
         if (pConn != null) pool.releaseConnection(pConn);
     }

     return result;
 }

 public boolean updateOpeningHours(String day, String openTime, String closeTime) {
	    String sql =
	        "INSERT INTO opening_hours (day, open_time, close_time) " +
	        "VALUES (?, ?, ?) " +
	        "ON DUPLICATE KEY UPDATE open_time=VALUES(open_time), close_time=VALUES(close_time)";

	    try (Connection conn = pool.getConnection().getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setString(1, day);                     // SUNDAY
	        ps.setTime(2, Time.valueOf(openTime));   // 10:00
	        ps.setTime(3, Time.valueOf(closeTime));  // 22:00

	        return ps.executeUpdate() > 0;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}


 private int dayNameToInt(String dayName) {
     // תומך גם באנגלית וגם אם מגיע "SUNDAY" וכו'
     String d = dayName.trim().toUpperCase();
     return switch (d) {
         case "MONDAY" -> 1;
         case "TUESDAY" -> 2;
         case "WEDNESDAY" -> 3;
         case "THURSDAY" -> 4;
         case "FRIDAY" -> 5;
         case "SATURDAY" -> 6;
         case "SUNDAY" -> 7;
         default -> 7;
     };
 }

 private String dayIntToName(int day) {
     return switch (day) {
         case 1 -> "MONDAY";
         case 2 -> "TUESDAY";
         case 3 -> "WEDNESDAY";
         case 4 -> "THURSDAY";
         case 5 -> "FRIDAY";
         case 6 -> "SATURDAY";
         case 7 -> "SUNDAY";
         default -> "SUNDAY";
     };
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
                        "SELECT status, subscriber_number, email, phone " +
                        "FROM `Reservations` WHERE confirmation_code=? FOR UPDATE";

                String status;
                String subValue;
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
                        subValue = rs.getString("subscriber_number"); // can be "SUB123" or "1" or null


                        gEmail = rs.getString("email");
                        gPhone = rs.getString("phone");
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
				
				    // ✅ ownership by subscriber (supports both old data "1" and new data "SUB123")
				    if (requesterSubscriberId != null && subValue != null && !subValue.isBlank()) {
				
				        // Case 1: old rows stored user_id as string number ("1")
				        if (subValue.trim().equals(String.valueOf(requesterSubscriberId))) {
				            okOwner = true;
				        }
				
				        // Case 2: new rows stored real subscriber_number ("SUB123")
				        if (!okOwner && subValue.toUpperCase().startsWith("SUB")) {
				            String requesterSubNumber = getSubscriberNumberByUserId(conn, requesterSubscriberId);
				            if (requesterSubNumber != null && subValue.equalsIgnoreCase(requesterSubNumber)) {
				                okOwner = true;
				            }
				        }
				    }
				
				    // fallback checks by email / phone
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
                        "UPDATE `Reservations` SET status='CANCELED' " +
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
    
    

    public List<String> getWaitingList() {
        List<String> result = new ArrayList<>();

        String sql =
            "SELECT wl.id, wl.request_time, wl.subscriber_number, u.name, u.phone " +
            "FROM waiting_list wl " +
            "JOIN subscribers s ON wl.subscriber_number = s.subscriber_number " +
            "JOIN users u ON s.user_id = u.id " +
            "ORDER BY wl.request_time";

        try (Connection conn = pool.getConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(
                    "#" + rs.getInt("id") +
                    " | " + rs.getString("subscriber_number") +
                    " | " + rs.getString("name") +
                    " | " + rs.getString("phone") +
                    " | " + rs.getTimestamp("request_time")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.clear();
            result.add("❌ DB error: " + e.getMessage());
        }

        return result;
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

    private boolean isSlotAvailable(Connection conn, LocalDateTime start, int dinners) throws SQLException {
        return findBestAvailableTable(conn, start, dinners) != null;
    }

    private Integer findBestAvailableTable(Connection conn, LocalDateTime start, int dinners) throws SQLException {
        LocalDateTime end = start.plusHours(2);

        String sql =
                "SELECT t.table_number " +
                "FROM `restaurant_tables` t " +
                "WHERE t.capacity >= ? " +
                "AND NOT EXISTS ( " +
                "   SELECT 1 FROM `Reservations` r " +
                "   WHERE r.status='ACTIVE' " +
                "     AND r.table_number = t.table_number " +
                "     AND r.start_time < ? AND r.end_time > ? " +
                ") " +
                "ORDER BY t.capacity ASC, t.table_number ASC " +
                "LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dinners);
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            stmt.setTimestamp(3, Timestamp.valueOf(start));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("table_number");
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
    
    private String getSubscriberNumberByUserId(Connection conn, int userId) throws SQLException {
        String sql = "SELECT subscriber_number FROM subscribers WHERE user_id = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("subscriber_number");
            }
        }
        return null;
    }


    private static class HoursRange {
        final LocalTime open;
        final LocalTime close;

        HoursRange(LocalTime open, LocalTime close) {
            this.open = open;
            this.close = close;
        }
    }
    
    public String payReservation(String confirmationCode) {
        PooledConnection pConn = null;

        try {
            String code = (confirmationCode == null) ? "" : confirmationCode.trim();
            if (code.isEmpty()) return "PAY_FAIL|Invalid confirmation code";

            pConn = pool.getConnection();
            pConn.touch();
            Connection conn = pConn.getConnection();

            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                // lock reservation row
                String selectSql =
                        "SELECT reservation_id, status, subscriber_number, dinners_number, start_time, end_time " +
                        "FROM `Reservations` WHERE confirmation_code=? FOR UPDATE";

                int reservationId;
                String status;
                int diners;
                Timestamp startTs;
                Timestamp endTs;
                String subscriberNumber = null;
                boolean isSubscriber = false;


                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setString(1, code);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return "PAY_FAIL|Confirmation code not found";
                        }

                        reservationId = rs.getInt("reservation_id");
                        status = rs.getString("status");

                        subscriberNumber = rs.getString("subscriber_number"); // "SUB123" or null
                        isSubscriber = (subscriberNumber != null && !subscriberNumber.isBlank());


                        diners = rs.getInt("dinners_number");
                        startTs = rs.getTimestamp("start_time");
                        endTs = rs.getTimestamp("end_time");
                    }
                }

                if (!"ACTIVE".equalsIgnoreCase(status)) {
                    conn.rollback();
                    return "PAY_FAIL|Reservation is not ACTIVE (already paid/canceled)";
                }

                // time window: now between start_time and end_time inclusive
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime start = startTs.toLocalDateTime();
                LocalDateTime end = endTs.toLocalDateTime();

                if (now.isBefore(start)) {
                    conn.rollback();
                    return "PAY_FAIL|Too early to pay (payment allowed from start time)";
                }
                if (now.isAfter(end)) {
                    conn.rollback();
                    return "PAY_FAIL|Payment window expired (more than 2 hours)";
                }

                double total = diners * 100.0;
                double discount = isSubscriber ? total * 0.10 : 0.0;

                double finalTotal = total - discount;

                // insert bill
                String insertBill =
                        "INSERT INTO bills (total_amount, discount_amount, bill_date, reservation_id) " +
                        "VALUES (?, ?, NOW(), ?)";

                int billNumber;
                try (PreparedStatement ps = conn.prepareStatement(insertBill, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setDouble(1, total);
                    ps.setDouble(2, discount);
                    ps.setInt(3, reservationId);

                    int rows = ps.executeUpdate();
                    if (rows <= 0) {
                        conn.rollback();
                        return "PAY_FAIL|Failed to create bill";
                    }

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            conn.rollback();
                            return "PAY_FAIL|Failed to get bill number";
                        }
                        billNumber = keys.getInt(1);
                    }
                }

                // update reservation status -> COMPLETED (table becomes free)
                String updateSql =
                        "UPDATE `Reservations` SET status='COMPLETED' " +
                        "WHERE reservation_id=? AND status='ACTIVE'";

                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, reservationId);
                    int rows = ps.executeUpdate();
                    if (rows <= 0) {
                        conn.rollback();
                        return "PAY_FAIL|Failed to mark reservation as COMPLETED";
                    }
                }

                conn.commit();

                // PAY_OK|billNumber|diners|total|discount|final
                return "PAY_OK|" + billNumber + "|" + diners + "|"
                        + formatMoney(total) + "|" + formatMoney(discount) + "|" + formatMoney(finalTotal);

            } finally {
                conn.setAutoCommit(oldAuto);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "PAY_FAIL|Server error: " + e.getMessage();
        } finally {
            if (pConn != null) pool.releaseConnection(pConn);
        }
    }

    private String formatMoney(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }
    
    public String previewBill(String confirmationCode) {
        PooledConnection pConn = null;

        try {
            String code = (confirmationCode == null) ? "" : confirmationCode.trim();
            if (code.isEmpty()) return "PREVIEW_FAIL|Invalid confirmation code";

            pConn = pool.getConnection();
            pConn.touch();
            Connection conn = pConn.getConnection();

            String sql =
                    "SELECT status, subscriber_number, dinners_number, start_time, end_time " +
                    "FROM `Reservations` WHERE confirmation_code=?";

            String status;
            int diners;
            Timestamp startTs;
            Timestamp endTs;
            String subscriberNumber = null;
            boolean isSubscriber = false;


            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, code);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "PREVIEW_FAIL|Confirmation code not found";
                    }

                    status = rs.getString("status");

                    subscriberNumber = rs.getString("subscriber_number"); // "SUB123" or null
                    isSubscriber = (subscriberNumber != null && !subscriberNumber.isBlank());


                    diners = rs.getInt("dinners_number");
                    startTs = rs.getTimestamp("start_time");
                    endTs = rs.getTimestamp("end_time");
                }
            }

            // must be ACTIVE to pay
            if (!"ACTIVE".equalsIgnoreCase(status)) {
                return "PREVIEW_FAIL|Reservation is not ACTIVE (already paid/canceled)";
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = startTs.toLocalDateTime();
            LocalDateTime end = endTs.toLocalDateTime();

            if (now.isBefore(start)) {
                return "PREVIEW_FAIL|Too early to pay (payment allowed from start time)";
            }
            if (now.isAfter(end)) {
                return "PREVIEW_FAIL|Payment window expired (more than 2 hours)";
            }

            double total = diners * 100.0;
            double discount = isSubscriber ? total * 0.10 : 0.0;

            double finalTotal = total - discount;

            // PREVIEW_OK|diners|total|discount|final
            return "PREVIEW_OK|" + diners + "|" + formatMoney(total) + "|"
                    + formatMoney(discount) + "|" + formatMoney(finalTotal);

        } catch (Exception e) {
            e.printStackTrace();
            return "PREVIEW_FAIL|Server error: " + e.getMessage();
        } finally {
            if (pConn != null) pool.releaseConnection(pConn);
        }
    }
    
    // Subscriber History Section
    public List<ReservationHistoryRow> getSubscriberReservationHistory(int subscriberUserId) {
        List<ReservationHistoryRow> out = new ArrayList<>();

        String sql =
            "SELECT r.start_time, r.end_time, r.dinners_number, r.table_number, " +
            "       r.confirmation_code, r.status, r.created_at " +
            "FROM reservations r " +
            "JOIN subscribers s ON s.subscriber_number = r.subscriber_number " +
            "WHERE s.user_id = ? " +
            "ORDER BY r.start_time DESC";

        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, subscriberUserId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        var start = rs.getTimestamp("start_time").toLocalDateTime();
                        var end = rs.getTimestamp("end_time").toLocalDateTime();
                        int diners = rs.getInt("dinners_number");
                        int table = rs.getInt("table_number");
                        String code = rs.getString("confirmation_code");
                        String status = rs.getString("status");
                        var created = rs.getTimestamp("created_at").toLocalDateTime();

                        out.add(new ReservationHistoryRow(start, end, diners, table, code, status, created));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pConn != null)
            	pool.releaseConnection(pConn);
        }
        return out;
    }
    
    // Subscriber Visit History 
    public int countSubscriberVisits(int subscriberUserId) {
        String sql =
            "SELECT COUNT(*) AS c " +
            "FROM reservations r " +
            "JOIN subscribers s ON s.subscriber_number = r.subscriber_number " +
            "WHERE s.user_id = ? AND r.status = 'COMPLETED'";

        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, subscriberUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("c");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pConn != null)
            	pool.releaseConnection(pConn);
        }
        return 0;
    }

}


