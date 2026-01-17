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

        // ✅ MODIFIED SQL: Join 'users' with 'subscribers'
        String sql = """
            SELECT u.*, s.subscriber_number, s.digital_card
            FROM users u
            JOIN subscribers s ON u.id = s.user_id
            WHERE u.role = 'SUBSCRIBER'
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // 1. Map the basic User data (using your existing mapper)
                User user = mapUser(rs);

                // 2. ✅ Manually set the extra fields from the JOIN result
                // (Make sure you added these setters to your User class as discussed)
                user.setSubscriberNumber(rs.getString("subscriber_number"));
                user.setDigitalCard(rs.getString("digital_card"));

                list.add(user);
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
    // LOGIN BY CARD (Tag Reader)
    // =================================================

    public User authenticateByCard(String digitalCode) throws SQLException {
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            pConn.touch();
            Connection conn = pConn.getConnection();

            // Join users and subscribers to find the owner of the card
            String sql = """
                SELECT u.*
                FROM users u
                JOIN subscribers s ON u.id = s.user_id
                WHERE s.digital_card = ? AND u.is_active = 1
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, digitalCode);

                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    return null; // Card not found
                }

                User baseUser = mapUser(rs);
                
                // Since they have a digital_card, they are definitely a SUBSCRIBER.
                // We reuse the existing loadSubscriber method to get the full object.
                return loadSubscriber(conn, baseUser);
            }

        } finally {
            pool.releaseConnection(pConn);
        }
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

		PooledConnection pConn = null;
		
		try {
		pConn = pool.getConnection();
		pConn.touch();
		Connection conn = pConn.getConnection();
		
		boolean oldAuto = conn.getAutoCommit();
		conn.setAutoCommit(false);
		
		try {
		// 1) Insert into users and get generated user id
		String userSql = """
		  INSERT INTO users (name, email, phone, password, role, is_active, created_at)
		  VALUES (?, ?, ?, ?, 'SUBSCRIBER', ?, NOW())
		""";
		
		int userId;
		
		try (PreparedStatement stmt = conn.prepareStatement(userSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
		  stmt.setString(1, name);
		  stmt.setString(2, email);
		  stmt.setString(3, phone);
		  stmt.setString(4, password);
		  stmt.setBoolean(5, active);
		
		  int rows = stmt.executeUpdate();
		  if (rows <= 0) throw new SQLException("Failed to insert into users");
		
		  try (ResultSet keys = stmt.getGeneratedKeys()) {
		      if (!keys.next()) throw new SQLException("Failed to get generated user id");
		      userId = keys.getInt(1);
		  }
		}
		
		// 2) Insert into subscribers with SUB prefix
		String subscriberNumber = "SUB" + userId;
		String digitalCard = "CARD" + userId; // you can change this logic if you want
		
		String subSql = """
		  INSERT INTO subscribers (user_id, subscriber_number, digital_card)
		  VALUES (?, ?, ?)
		""";
		
		try (PreparedStatement stmt = conn.prepareStatement(subSql)) {
		  stmt.setInt(1, userId);
		  stmt.setString(2, subscriberNumber);
		  stmt.setString(3, digitalCard);
		  stmt.executeUpdate();
		}
		
		conn.commit();
		
		} catch (SQLException e) {
		conn.rollback();
		throw e;
		} finally {
		conn.setAutoCommit(oldAuto);
		}
		
		} finally {
		if (pConn != null) pool.releaseConnection(pConn);
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
    public boolean isEmailTakenByAnotherUser(Connection conn, String email, int userId) throws SQLException {
        String sql = "SELECT id FROM users WHERE LOWER(email) = LOWER(?) AND id <> ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean updateUserContact(int userId, String newEmail, String newPhone) throws SQLException {
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            pConn.touch();
            Connection conn = pConn.getConnection();

            // optional uniqueness check
            if (isEmailTakenByAnotherUser(conn, newEmail, userId)) {
                return false;
            }

            String sql = "UPDATE users SET email = ?, phone = ? WHERE id = ? AND is_active = 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newEmail);
                stmt.setString(2, newPhone);
                stmt.setInt(3, userId);
                return stmt.executeUpdate() > 0;
            }

        } finally {
            pool.releaseConnection(pConn);
        }
    }

}
