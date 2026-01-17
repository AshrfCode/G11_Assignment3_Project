package server.dao;

import java.sql.*;

public class WaitingListDAO {

    private final Connection conn;

    public WaitingListDAO(Connection conn) {
        this.conn = conn;
    }    

    // userId -> subscriber_number
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

    private String generateWaitingConfirmationCode() {
        int num = 100000 + new java.security.SecureRandom().nextInt(900000);
        return "WL" + num;
    }

    // ---------- SUBSCRIBER ----------
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