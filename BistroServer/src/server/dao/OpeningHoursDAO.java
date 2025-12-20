package server.dao;

import entities.OpeningHours;
import server.MySQLConnectionPool;
import server.PooledConnection;

import java.sql.*;

public class OpeningHoursDAO {

    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

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
