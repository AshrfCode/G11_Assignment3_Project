package server.dao;

import server.MySQLConnectionPool;
import server.PooledConnection;

import java.sql.*;

public class TableDAO {

    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

    public void updateTableStatus(int tableNumber, String status,
                                  Timestamp from, Timestamp to) throws SQLException {

        PooledConnection pConn = null;
        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql = """
                UPDATE restaurant_tables
                SET status = ?, reserved_from = ?, reserved_to = ?
                WHERE table_number = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setTimestamp(2, from);
                stmt.setTimestamp(3, to);
                stmt.setInt(4, tableNumber);
                stmt.executeUpdate();
            }
        } finally {
            pool.releaseConnection(pConn);
        }
    }
}
