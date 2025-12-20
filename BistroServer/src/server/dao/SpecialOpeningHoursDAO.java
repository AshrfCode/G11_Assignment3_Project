package server.dao;

import entities.SpecialOpeningHours;
import server.MySQLConnectionPool;
import server.PooledConnection;

import java.sql.*;
import java.time.LocalDate;

public class SpecialOpeningHoursDAO {

    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

    public SpecialOpeningHours getByDate(LocalDate date) throws SQLException {

        PooledConnection pConn = null;
        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql = "SELECT * FROM special_opening_hours WHERE special_date = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(date));
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) return null;

                SpecialOpeningHours s = new SpecialOpeningHours();
                s.setId(rs.getInt("id"));
                s.setSpecialDate(rs.getDate("special_date").toLocalDate());
                s.setOpenTime(rs.getTime("open_time") != null ?
                        rs.getTime("open_time").toLocalTime() : null);
                s.setCloseTime(rs.getTime("close_time") != null ?
                        rs.getTime("close_time").toLocalTime() : null);
                s.setClosed(rs.getBoolean("is_closed"));
                return s;
            }
        } finally {
            pool.releaseConnection(pConn);
        }
    }
}
