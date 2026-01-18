package server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import server.MySQLConnectionPool;
import server.PooledConnection;

/**
 * Data Access Object (DAO) responsible for reservation-related database operations.
 * <p>
 * This class currently focuses on the customer check-in flow, validating an active reservation
 * by confirmation code and updating reservation status accordingly.
 * </p>
 */
public class ReservationDAO { 

    /**
     * Shared MySQL connection pool used to obtain and release database connections.
     */
    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

    /**
     * Handles the customer Check-In process.
     * 1. Checks if reservation exists and is ACTIVE.
     * 2. Checks if customer is late (>15 mins).
     * 3. If OK, updates status to CHECKED_IN and sets check_in_time.
     *
     * @param confirmationCode The code entered by the user.
     * @return Integer (Table Number) if success, or String (Error Message) if failed.
     */
    public Object checkInCustomer(String confirmationCode) {
        PooledConnection pConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String query =
                    "SELECT reservation_id, start_time, table_number " +
                    "FROM reservations " +
                    "WHERE confirmation_code = ? AND status = 'ACTIVE'";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, confirmationCode);
            rs = stmt.executeQuery();

            if (!rs.next()) {
                return "Error: Invalid code or reservation not active.";
            }

            int reservationId = rs.getInt("reservation_id");
            Timestamp startTime = rs.getTimestamp("start_time");
            int tableNumber = rs.getInt("table_number");

            long nowMillis = System.currentTimeMillis();
            long startMillis = startTime.getTime();
            long fifteenMin = 15L * 60 * 1000;

            // Late => cancel immediately (also protects if scheduler didn't run yet)
            if (nowMillis > (startMillis + fifteenMin)) {
                updateReservationStatus(conn, reservationId, "CANCELED");
                return "Error: Reservation expired. You arrived more than 15 minutes late.";
            }

            // Too early => block
            if (nowMillis < startMillis) {
                return "Error: It is too early to check in. Please wait for your reservation time.";
            }

            // Success
            updateReservationToCheckedIn(conn, reservationId);
            return tableNumber;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Database connection failed.";
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (pConn != null) pool.releaseConnection(pConn);
        }
    }

    /**
     * Updates the specified reservation to {@code CHECKED_IN} and sets {@code check_in_time} to the current time.
     *
     * @param conn the active database connection to use
     * @param reservationId the reservation identifier to update
     * @throws SQLException if a database access error occurs while updating the reservation
     */
    private void updateReservationToCheckedIn(Connection conn, int reservationId) throws SQLException {
        String update =
                "UPDATE reservations " +
                "SET status = 'CHECKED_IN', check_in_time = NOW() " +
                "WHERE reservation_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(update)) {
            stmt.setInt(1, reservationId);
            stmt.executeUpdate();
        }
    }

    /**
     * Updates the status of the specified reservation.
     *
     * @param conn the active database connection to use
     * @param reservationId the reservation identifier to update
     * @param status the new status value to set
     * @throws SQLException if a database access error occurs while updating the reservation status
     */
    private void updateReservationStatus(Connection conn, int reservationId, String status) throws SQLException {
        String update = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(update)) {
            stmt.setString(1, status);
            stmt.setInt(2, reservationId);
            stmt.executeUpdate();
        }
    }
}
