package server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.UserRole;
import entities.User;
import entities.Subscriber;
import entities.Representative;
import server.MySQLConnectionPool;
import server.PooledConnection;

public class MySQLUserDAO {

    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

    // =================================================
    // LOGIN
    // =================================================

    public User authenticate(String email, String password) throws SQLException {

        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            pConn.touch();
            Connection conn = pConn.getConnection();

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
                    return null;
                }

                User baseUser = mapUser(rs);

                return switch (baseUser.getRole()) {
                    case SUBSCRIBER -> loadSubscriber(conn, baseUser);
                    case REPRESENTATIVE, MANAGER -> loadRepresentative(conn, baseUser);
                };
            }

        } finally {
            pool.releaseConnection(pConn);
        }
    }

    // =================================================
    // GET ALL SUBSCRIBERS (FOR TABLE VIEW)
    // =================================================

    public List<User> getAllSubscribers() throws SQLException {

        List<User> list = new ArrayList<>();
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql = "SELECT * FROM users WHERE role = 'SUBSCRIBER'";

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    list.add(mapUser(rs));
                }
            }
            return list;

        } finally {
            pool.releaseConnection(pConn);
        }
    }

    // =================================================
    // MAPPERS
    // =================================================

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("password"),
            UserRole.valueOf(rs.getString("role")),
            rs.getBoolean("is_active"),
            rs.getTimestamp("created_at")
        );
    }

    // =================================================
    // ROLE-SPECIFIC LOADERS
    // =================================================

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
    
    public void addSubscriber(String name, String email, String phone,
            String password, boolean active) throws SQLException {

String sql = """
INSERT INTO users
(name, email, phone, password, role, is_active)
VALUES (?, ?, ?, ?, 'SUBSCRIBER', ?)
""";

try (Connection conn = pool.getConnection().getConnection();
PreparedStatement stmt = conn.prepareStatement(sql)) {

stmt.setString(1, name);
stmt.setString(2, email);
stmt.setString(3, phone);
stmt.setString(4, password);
stmt.setBoolean(5, active);

stmt.executeUpdate();
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
