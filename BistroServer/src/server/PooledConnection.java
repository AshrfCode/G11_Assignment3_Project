package server;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Wrapper around a JDBC {@link Connection} that tracks when the connection was last used.
 * <p>
 * Used by {@link MySQLConnectionPool} to manage idle-time cleanup and pooling behavior.
 * </p>
 */
public class PooledConnection {

    /**
     * The underlying physical JDBC connection.
     */
    private final Connection conn;

    /**
     * Timestamp (milliseconds since epoch) of the last time this connection was marked as used.
     */
    private long lastUsed;

    /**
     * Creates a new pooled connection wrapper and initializes the last-used timestamp.
     *
     * @param conn the underlying JDBC connection to wrap
     */
    public PooledConnection(Connection conn) {
        this.conn = conn;
        this.lastUsed = System.currentTimeMillis();
    }

    /**
     * Returns the underlying JDBC connection.
     *
     * @return the wrapped {@link Connection}
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * Returns the last-used timestamp for this pooled connection.
     *
     * @return the last-used time in milliseconds since epoch
     */
    public long getLastUsed() {
        return lastUsed;
    }

    /**
     * Refresh timestamp whenever connection is used
     */
    public void touch() {
        lastUsed = System.currentTimeMillis();
    }

    /**
     * Physically close JDBC connection
     */
    public void closePhysical() {
        try {
            conn.close();
        } catch (SQLException ignored) {}
    }
}
