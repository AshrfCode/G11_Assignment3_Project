package servergui;

/**
 * Holds shared server GUI session configuration values for database connectivity.
 * <p>
 * This class stores the database URL, username, and password as static fields to be accessed
 * across the server GUI components.
 * </p>
 */
public class ServerSession {

    /**
     * JDBC URL for the database connection.
     */
    public static String dbUrl;

    /**
     * Database username.
     */
    public static String dbUser;

    /**
     * Database password.
     */
    public static String dbPass;
}
