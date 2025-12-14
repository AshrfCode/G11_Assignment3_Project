package server;

import java.sql.Connection;
import java.sql.SQLException;

public class PooledConnection {

    private final Connection conn;
    private long lastUsed;

    public PooledConnection(Connection conn) {
        this.conn = conn;
        this.lastUsed = System.currentTimeMillis();
    }

    public Connection getConnection() {
        return conn;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    /** Refresh timestamp whenever connection is used */
    public void touch() {
        lastUsed = System.currentTimeMillis();
    }

    /** Physically close JDBC connection */
    public void closePhysical() {
        try {
            conn.close();
        } catch (SQLException ignored) {}
    }
}
