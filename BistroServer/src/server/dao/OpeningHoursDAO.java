package server.dao;

import entities.OpeningHours;
import server.MySQLConnectionPool;
import server.PooledConnection;

import java.sql.*;

public class OpeningHoursDAO {

    /**
     * Shared MySQL connection pool used to obtain and release database connections.
     */
    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

    /**
     * Retrieves opening hours for a specific day from the {@code opening_hours} table.
     *
     * @param day the day identifier to query (e.g., "MONDAY", "Sunday", depending on DB convention)
     * @return an {@link OpeningHours} object if a matching row exists; {@code null} otherwise
     * @throws SQLException if a database access error occurs
     */
    public OpeningHours getByDay(String day) throws SQLException {

        PooledConnection pConn = null;
        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql = "SELECT * FROM opening_hours WHERE day = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, day);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) return null;

                OpeningHours oh = new OpeningHours();
                oh.setDay(rs.getString("day"));
                oh.setOpenTime(rs.getTime("open_time").toLocalTime());
                oh.setCloseTime(rs.getTime("close_time").toLocalTime());
                return oh;
            }
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /**
     * Updates the opening and closing times for a specific day in the {@code opening_hours} table.
     *
     * @param day the day identifier whose hours should be updated
     * @param open the new opening time
     * @param close the new closing time
     * @throws SQLException if a database access error occurs
     */
    public void updateHours(String day, Time open, Time close) throws SQLException {
        PooledConnection pConn = null;
        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql = """
                UPDATE opening_hours
                SET open_time = ?, close_time = ?
                WHERE day = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setTime(1, open);
                stmt.setTime(2, close);
                stmt.setString(3, day);
                stmt.executeUpdate();
            }
        } finally {
            pool.releaseConnection(pConn);
        }
    }
}
