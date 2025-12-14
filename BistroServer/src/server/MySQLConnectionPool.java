package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.*;

public class MySQLConnectionPool {

    private static MySQLConnectionPool instance;

    // Dynamic DB configuration (updated at runtime)
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASS;

    // Pool configuration
    private static final int MAX_POOL_SIZE = 10;
    private static final long MAX_IDLE_TIME = 30_000;  // 30 seconds

    private final BlockingQueue<PooledConnection> pool;
    private final ScheduledExecutorService cleaner;

    // ---------------------- SINGLETON -------------------------
    public static synchronized MySQLConnectionPool getInstance() {
        if (instance == null) {
            instance = new MySQLConnectionPool();
        }
        return instance;
    }

    private MySQLConnectionPool() {
        pool = new LinkedBlockingQueue<>(MAX_POOL_SIZE);

        // Background cleanup thread
        cleaner = Executors.newSingleThreadScheduledExecutor();
        cleaner.scheduleAtFixedRate(this::cleanupIdleConnections,
                10, 10, TimeUnit.SECONDS); // Runs every 10 seconds
    }

    // ---------------------- CONFIGURE DB SETTINGS -------------------------
    public static void configure(String url, String user, String pass) {
        DB_URL = url;
        DB_USER = user;
        DB_PASS = pass;
    }

    // ---------------------- GET CONNECTION (Singleton) -------------------------
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
    public void shutdown() {

        for (PooledConnection pConn : pool) {
            pConn.closePhysical();
        }

        pool.clear();
    }


    // ---------------------- CLEANUP IDLE CONNECTIONS -------------------------
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
