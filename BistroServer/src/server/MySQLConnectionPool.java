package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.*;

/**
 * A simple JDBC connection pool for MySQL connections using a fixed-size queue.
 * <p>
 * This pool is implemented as a singleton and supports runtime configuration of database
 * connection parameters via {@link #configure(String, String, String)}.
 * Idle pooled connections are periodically cleaned up by a background scheduled task.
 * </p>
 */
public class MySQLConnectionPool {

    /**
     * Singleton instance of the connection pool.
     */
    private static MySQLConnectionPool instance;

    /**
     * JDBC URL for the database connection, configured at runtime.
     */
    private static String DB_URL;

    /**
     * Database username, configured at runtime.
     */
    private static String DB_USER;

    /**
     * Database password, configured at runtime.
     */
    private static String DB_PASS;

    /**
     * Maximum number of connections allowed in the pool queue.
     */
    private static final int MAX_POOL_SIZE = 10;

    /**
     * Maximum time (in milliseconds) a pooled connection may remain idle before being closed.
     */
    private static final long MAX_IDLE_TIME = 30_000;  // 30 seconds

    /**
     * Queue that stores available pooled connections.
     */
    private final BlockingQueue<PooledConnection> pool;

    /**
     * Scheduled executor responsible for periodically cleaning up idle connections.
     */
    private final ScheduledExecutorService cleaner;

    // ---------------------- SINGLETON -------------------------

    /**
     * Returns the singleton instance of the {@code MySQLConnectionPool}.
     * If it does not exist yet, it will be created.
     *
     * @return the singleton {@code MySQLConnectionPool} instance
     */
    public static synchronized MySQLConnectionPool getInstance() {
        if (instance == null) {
            instance = new MySQLConnectionPool();
        }
        return instance;
    }

    /**
     * Creates a new connection pool and starts the background cleanup task.
     */
    private MySQLConnectionPool() {
        pool = new LinkedBlockingQueue<>(MAX_POOL_SIZE);

        // Background cleanup thread
        cleaner = Executors.newSingleThreadScheduledExecutor();
        cleaner.scheduleAtFixedRate(this::cleanupIdleConnections,
                10, 10, TimeUnit.SECONDS); // Runs every 10 seconds
    }

    // ---------------------- CONFIGURE DB SETTINGS -------------------------

    /**
     * Configures the database connection settings used when creating new physical JDBC connections.
     * <p>
     * This method updates static configuration values that are later used by {@link #getConnection()}.
     * </p>
     *
     * @param url   the JDBC URL
     * @param user  the database username
     * @param pass  the database password
     */
    public static void configure(String url, String user, String pass) {
        DB_URL = url;
        DB_USER = user;
        DB_PASS = pass;
    }

    // ---------------------- GET CONNECTION (Singleton) -------------------------

    /**
     * Obtains a pooled connection from the pool if available; otherwise creates a new physical JDBC connection.
     *
     * @return a {@link PooledConnection} instance wrapping a JDBC {@link Connection}
     * @throws SQLException if a new JDBC connection must be created and the connection attempt fails
     */
    public PooledConnection getConnection() throws SQLException {
        PooledConnection pConn = pool.poll();

        if (pConn == null) {
            // Create new physical JDBC connection
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            return new PooledConnection(conn);
        }
        return pConn;
    }

    // ---------------------- RELEASE CONNECTION -------------------------

    /**
     * Returns a pooled connection back to the pool.
     * <p>
     * If the pool is full, the underlying physical connection is closed instead of being pooled.
     * A {@code null} input is ignored.
     * </p>
     *
     * @param pConn the pooled connection to release back to the pool
     */
    public void releaseConnection(PooledConnection pConn) {
        if (pConn == null) {
            return;
        }

        pConn.touch();

        if (!pool.offer(pConn)) {
            pConn.closePhysical();
        }
    }
    
 // ---------------------- Shutdown All CONNECTIONS -------------------------

    /**
     * Closes all pooled physical connections currently stored in the pool and clears the pool.
     */
    public void shutdown() {

        for (PooledConnection pConn : pool) {
            pConn.closePhysical();
        }

        pool.clear();
    }


    // ---------------------- CLEANUP IDLE CONNECTIONS -------------------------

    /**
     * Removes and closes pooled connections that have been idle longer than {@link #MAX_IDLE_TIME}.
     * <p>
     * This method is intended to be invoked periodically by the scheduled cleaner task.
     * </p>
     */
    private void cleanupIdleConnections() {

        long now = System.currentTimeMillis();

        pool.removeIf(pConn -> {
            boolean idleTooLong = now - pConn.getLastUsed() > MAX_IDLE_TIME;

            if (idleTooLong) {
                pConn.closePhysical();
                return true;
            }
            return false;
        });

    }
}
