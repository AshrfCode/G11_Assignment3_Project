package server.dao;

import server.MySQLConnectionPool;
import server.PooledConnection;

import java.sql.*;

public class WaitingListDAO {

    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

    public void addToWaitingList(String subscriberNumber) throws SQLException {

        PooledConnection pConn = null;
        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql = "INSERT INTO waiting_list (subscriber_number) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, subscriberNumber);
                stmt.executeUpdate();
            }
        } finally {
            pool.releaseConnection(pConn);
        }
    }
}
