package server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.UserRole;
import entities.User;
import entities.Subscriber;
import entities.Representative;
import server.MySQLConnectionPool;
import server.PooledConnection;

public class MySQLUserDAO {

    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

    public User authenticate(String email, String password) throws SQLException {
    	
        PooledConnection pConn = null;
        Connection conn = null;
       


        try {
            // 1️⃣ Get connection from pool
            pConn = pool.getConnection();
            pConn.touch();
            conn = pConn.getConnection();

            // 2️⃣ Query base user
            String userSql = """
                SELECT *
                FROM users
                WHERE email = ? AND password = ? AND is_active = 1
            """;

            try (PreparedStatement stmt = conn.prepareStatement(userSql)) {

                stmt.setString(1, email);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    return null; // login failed
                }

                User user = new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("password"),
                    UserRole.valueOf(rs.getString("role")),
                    rs.getBoolean("is_active"),
                    rs.getTimestamp("created_at")
                );

                // 3️⃣ Load role-specific data
                return switch (user.getRole()) {

                    case SUBSCRIBER -> loadSubscriber(conn, user);

                    case REPRESENTATIVE -> loadRepresentative(conn, user);

                    case MANAGER -> loadRepresentative(conn, user);
                };
            }

        } finally {
            // 4️⃣ Always release connection back to pool
            pool.releaseConnection(pConn);
        }
    }

    // ----------------------------------------------------

    private Subscriber loadSubscriber(Connection conn, User user) throws SQLException {

        String sql = """
            SELECT subscriber_number, digital_card
            FROM subscribers
            WHERE user_id = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Subscriber data missing for user_id " + user.getId());
            }

            return new Subscriber(
                user,
                rs.getString("subscriber_number"),
                rs.getString("digital_card")
            );
        }
    }

    private Representative loadRepresentative(Connection conn, User user) throws SQLException {

        String sql = """
            SELECT representative_number
            FROM representatives
            WHERE user_id = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Representative data missing for user_id " + user.getId());
            }

            return new Representative(
                user,
                rs.getString("representative_number")
            );
        }
    }
    
}
