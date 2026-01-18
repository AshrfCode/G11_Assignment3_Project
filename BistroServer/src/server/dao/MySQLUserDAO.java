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

/**
 * Data Access Object (DAO) responsible for user-related persistence operations in a MySQL database.
 * <p>
 * Provides authentication, retrieval of subscribers for UI table views, creation of new subscribers,
 * and user contact update operations. Role-specific user data is loaded from dedicated tables
 * (e.g., {@code subscribers}, {@code representatives}) based on the user's role.
 * </p>
 */
public class MySQLUserDAO {

    /**
     * Shared MySQL connection pool used to obtain and release database connections.
     */
    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

    // =================================================
    // LOGIN
    // =================================================

    /**
     * Authenticates an active user by email and password.
     * <p>
     * If the credentials match an active user record, this method loads and returns a role-specific
     * user object (e.g., {@link Subscriber} or {@link Representative}) based on the {@code role} field.
     * </p>
     *
     * @param email the user's email address
     * @param password the user's password
     * @return a role-specific {@link User} instance if authentication succeeds; {@code null} otherwise
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Retrieves all subscribers by joining the {@code users} and {@code subscribers} tables.
     * <p>
     * Intended for populating a UI table view with subscriber-related columns such as
     * subscriber number and digital card.
     * </p>
     *
     * @return a list of {@link User} objects with subscriber-specific fields populated
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Maps the current row in a {@link ResultSet} into a base {@link User} instance using the {@code users} table columns.
     *
     * @param rs the result set positioned at the row to map
     * @return a {@link User} populated with base user data
     * @throws SQLException if reading a column from the {@link ResultSet} fails
     */
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

    /**
     * Authenticates an active subscriber by a digital card code (e.g., scanned by a tag reader).
     * <p>
     * This method joins {@code users} and {@code subscribers} to find the owner of the provided digital card,
     * and returns a fully loaded {@link Subscriber} instance if found.
     * </p>
     *
     * @param digitalCode the digital card code to authenticate by
     * @return a fully loaded {@link Subscriber} if a matching active user is found; {@code null} otherwise
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Loads subscriber-specific data for the provided base user from the {@code subscribers} table.
     *
     * @param conn an open database connection to use for the query
     * @param user the base user to enrich with subscriber-specific data
     * @return a {@link Subscriber} instance containing base user data and subscriber attributes
     * @throws SQLException if subscriber data is missing or a database access error occurs
     */
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
    
    /**
     * Adds a new subscriber by inserting records into both {@code users} and {@code subscribers} tables
     * within a single database transaction.
     * <p>
     * The user is created with the {@code SUBSCRIBER} role, and a subscriber number and digital card
     * are generated using the newly created user ID.
     * </p>
     *
     * @param name the subscriber's name
     * @param email the subscriber's email address
     * @param phone the subscriber's phone number
     * @param password the subscriber's password
     * @param active whether the subscriber account should be active
     * @throws SQLException if a database access error occurs or the transaction fails
     */
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



    /**
     * Loads representative-specific data for the provided base user from the {@code representatives} table.
     * <p>
     * This method is used for both {@code REPRESENTATIVE} and {@code MANAGER} roles.
     * </p>
     *
     * @param conn an open database connection to use for the query
     * @param user the base user to enrich with representative-specific data
     * @return a {@link Representative} instance containing base user data and representative attributes
     * @throws SQLException if representative data is missing or a database access error occurs
     */
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

    /**
     * Checks whether the given email address is already used by a different user (case-insensitive).
     *
     * @param conn an open database connection to use for the query
     * @param email the email address to check for uniqueness
     * @param userId the ID of the current user to exclude from the uniqueness check
     * @return {@code true} if another user already has the given email; {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Updates a user's email and phone number if the user is active.
     * <p>
     * Optionally verifies that the new email is not already used by another user before updating.
     * </p>
     *
     * @param userId the ID of the user to update
     * @param newEmail the new email address to set
     * @param newPhone the new phone number to set
     * @return {@code true} if the update succeeded; {@code false} if the email is already taken or no active user was updated
     * @throws SQLException if a database access error occurs
     */
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
