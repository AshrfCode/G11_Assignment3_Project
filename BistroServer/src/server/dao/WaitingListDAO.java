package server.dao;

import java.sql.*;

/**
 * Data Access Object (DAO) responsible for managing entries in the restaurant waiting list.
 * <p>
 * Supports both subscribers (identified by {@code subscriber_number}) and guests (no subscriber number),
 * generating unique confirmation codes for each waiting list request.
 * </p>
 */
public class WaitingListDAO {

    /**
     * Database connection used for all waiting list operations.
     */
    private final Connection conn;

    /**
     * Constructs a new {@code WaitingListDAO} using the provided database connection.
     *
     * @param conn an open JDBC connection to use for waiting list operations
     */
    public WaitingListDAO(Connection conn) {
        this.conn = conn;
    }    

    // userId -> subscriber_number
    /**
     * Retrieves a subscriber number associated with a given user ID.
     *
     * @param userId the user ID to look up
     * @return the subscriber number if found; {@code null} otherwise
     * @throws SQLException if a database access error occurs
     */
    private String getSubscriberNumberByUserId(int userId) throws SQLException {
        String sql = "SELECT subscriber_number FROM subscribers WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("subscriber_number");
                return null;
            }
        }
    }

    /**
     * Generates a waiting list confirmation code using a random 6-digit number with a {@code WL} prefix.
     *
     * @return a newly generated confirmation code (e.g., {@code WL123456})
     */
    private String generateWaitingConfirmationCode() {
        int num = 100000 + new java.security.SecureRandom().nextInt(900000);
        return "WL" + num;
    }

    // ---------- SUBSCRIBER ----------
    /**
     * Adds (or replaces) a waiting list entry for a subscriber identified by user ID.
     * <p>
     * If the subscriber already has an entry in the waiting list, it is deleted before inserting the new one.
     * The method attempts to generate a unique confirmation code, retrying on collisions.
     * </p>
     *
     * @param userId the subscriber's user ID
     * @param diners the number of diners for the request
     * @param phone the guest phone number (may be {@code null})
     * @param email the guest email address (may be {@code null})
     * @return the generated confirmation code if insertion succeeds; {@code null} if subscriber not found or insertion fails
     * @throws SQLException if a database access error occurs
     */
    public String joinAsSubscriber(int userId, int diners, String phone, String email) throws SQLException {
        String subNumber = getSubscriberNumberByUserId(userId);
        if (subNumber == null) return null;

        String safePhone = (phone == null) ? "" : phone.trim();
        String safeEmail = (email == null) ? "" : email.trim();

        // מוחקים כניסה קודמת
        try (PreparedStatement del = conn.prepareStatement(
                "DELETE FROM waiting_list WHERE subscriber_number = ?")) {
            del.setString(1, subNumber);
            del.executeUpdate();
        }

        String sql =
            "INSERT INTO waiting_list (subscriber_number, diners_number, request_time, guest_phone, guest_email, confirmation_code) " +
            "VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, ?)";

        for (int attempt = 0; attempt < 10; attempt++) {
            String code = generateWaitingConfirmationCode();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, subNumber);
                ps.setInt(2, diners);
                ps.setString(3, safePhone);
                ps.setString(4, safeEmail);
                ps.setString(5, code);

                int rows = ps.executeUpdate();
                if (rows == 1) return code;
            } catch (SQLIntegrityConstraintViolationException dup) {
                // code collision -> retry
            }
        }

        return null;
    }

    /**
     * Removes a subscriber's waiting list entry based on user ID.
     *
     * @param userId the subscriber's user ID
     * @return {@code true} if an entry was deleted; {@code false} if subscriber not found or no entry existed
     * @throws SQLException if a database access error occurs
     */
    public boolean leaveAsSubscriber(int userId) throws SQLException {
        String subNumber = getSubscriberNumberByUserId(userId);
        if (subNumber == null) return false;

        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM waiting_list WHERE subscriber_number = ?")) {
            ps.setString(1, subNumber);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- GUEST ----------
    /**
     * Adds a waiting list entry for a guest (non-subscriber).
     * <p>
     * The guest is inserted with a {@code NULL} subscriber number. The method attempts to generate a unique
     * confirmation code, retrying on collisions.
     * </p>
     *
     * @param diners the number of diners for the request
     * @param phone the guest phone number (may be {@code null})
     * @param email the guest email address (may be {@code null})
     * @return the generated confirmation code if insertion succeeds; {@code null} if insertion fails
     * @throws SQLException if a database access error occurs
     */
    public String joinAsGuest(int diners, String phone, String email) throws SQLException {
        String safePhone = (phone == null) ? "" : phone.trim();
        String safeEmail = (email == null) ? "" : email.trim();

        String sql =
            "INSERT INTO waiting_list (subscriber_number, diners_number, request_time, guest_phone, guest_email, confirmation_code) " +
            "VALUES (NULL, ?, CURRENT_TIMESTAMP, ?, ?, ?)";

        for (int attempt = 0; attempt < 10; attempt++) {
            String code = generateWaitingConfirmationCode();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, diners);
                ps.setString(2, safePhone);
                ps.setString(3, safeEmail);
                ps.setString(4, code);

                int rows = ps.executeUpdate();
                if (rows == 1) return code;

            } catch (SQLIntegrityConstraintViolationException dup) {
                // collision -> try again
            }
        }
        return null;
    }

    /**
     * Removes a guest waiting list entry by confirmation code.
     * <p>
     * This method deletes only guest entries (where {@code subscriber_number IS NULL}).
     * </p>
     *
     * @param confirmationCode the guest confirmation code
     * @return {@code true} if an entry was deleted; {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean leaveAsGuest(String confirmationCode) throws SQLException {
        String code = (confirmationCode == null) ? "" : confirmationCode.trim();
        if (code.isEmpty()) return false;

        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM waiting_list WHERE confirmation_code = ? AND subscriber_number IS NULL")) {
            ps.setString(1, code);
            return ps.executeUpdate() > 0;
        }
    }


}
