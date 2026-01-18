package server.dao;

import entities.Bill;
import server.MySQLConnectionPool;
import server.PooledConnection;

import java.sql.*;

public class BillDAO {

    /**
     * Shared MySQL connection pool used to obtain and release database connections.
     */
    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

    /**
     * Inserts a bill record into the {@code bills} table and associates it with a reservation.
     *
     * @param bill the bill entity containing bill number, amounts, and date to persist
     * @param reservationId the reservation ID to associate with the inserted bill
     * @throws SQLException if acquiring a connection or executing the INSERT statement fails
     */
    public void insertBill(Bill bill, int reservationId) throws SQLException {

        PooledConnection pConn = null;
        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql = """
                INSERT INTO bills
                (bill_number, total_amount, discount_amount, bill_date, reservation_id)
                VALUES (?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, bill.getBillNumber());
                stmt.setDouble(2, bill.getTotalAmount());
                stmt.setDouble(3, bill.getDiscountAmount());
                stmt.setDate(4, Date.valueOf(bill.getBillDate()));
                stmt.setInt(5, reservationId);
                stmt.executeUpdate();
            }
        } finally {
            pool.releaseConnection(pConn);
        }
    }
}
