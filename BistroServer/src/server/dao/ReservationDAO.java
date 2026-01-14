package server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import entities.Reservation;
import server.MySQLConnectionPool;
import server.PooledConnection;

public class ReservationDAO {

    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

    /**
     * Handles the customer Check-In process.
     * 1. Checks if reservation exists and is ACTIVE.
     * 2. Checks if customer is late (>15 mins).
     * 3. If OK, updates status to CHECKED_IN and returns Table Number.
     * * @param confirmationCode The code entered by the user.
     * @return Integer (Table Number) if success, or String (Error Message) if failed.
     */
    public Object checkInCustomer(String confirmationCode) {
        PooledConnection pConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            // 1. Get the reservation details (Only ACTIVE reservations)
            String query = "SELECT reservation_id, start_time, table_number FROM reservations WHERE confirmation_code = ? AND status = 'ACTIVE'";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, confirmationCode);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                Timestamp startTime = rs.getTimestamp("start_time");
                int tableNumber = rs.getInt("table_number");

                // --- 15 Minute Validation Logic ---
                long currentTimeMillis = System.currentTimeMillis();
                long reservationTimeMillis = startTime.getTime();
                long fifteenMinutesInMillis = 15 * 60 * 1000; 

                // Check if Late: Current Time > Reservation Time + 15 mins
                if (currentTimeMillis > (reservationTimeMillis + fifteenMinutesInMillis)) {
                    // Update DB to cancel the reservation
                    updateReservationStatus(conn, reservationId, "CANCELED");
                    return "Error: Reservation expired. You arrived more than 15 minutes late.";
                }

                // Check if Too Early (STRICT MODE: Must be at or after reservation time)
                if (currentTimeMillis < reservationTimeMillis) {
                     return "Error: It is too early to check in. Please wait for your reservation time.";
                }

                // --- Success: Perform Check In ---
                updateReservationToCheckedIn(conn, reservationId);
                
                // Return the table number so the Client can tell the user where to sit
                return tableNumber; 
            } else {
                return "Error: Invalid code or reservation not active.";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Database connection failed.";
        } finally {
            // Important: Close resources and release connection back to pool
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (pConn != null) pool.releaseConnection(pConn);
        }
    }

    /**
     * Updates the reservation to CHECKED_IN and sets the actual check_in_time.
     */
    private void updateReservationToCheckedIn(Connection conn, int reservationId) throws SQLException {
        String update = "UPDATE reservations SET status = 'CHECKED_IN', check_in_time = NOW() WHERE reservation_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(update)) {
            stmt.setInt(1, reservationId);
            stmt.executeUpdate();
        }
    }

    /**
     * Updates the status of a reservation (e.g., to CANCELED).
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