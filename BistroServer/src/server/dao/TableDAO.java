package server.dao;

import server.MySQLConnectionPool;
import server.PooledConnection;
import entities.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {

    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

    /* =========================
       GET ALL TABLES
       ========================= */
    public List<Table> getAllTables() throws SQLException {

        List<Table> tables = new ArrayList<>();
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql = "SELECT * FROM restaurant_tables";

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Table t = new Table();
                    t.setTableNumber(rs.getInt("table_number"));
                    t.setCapacity(rs.getInt("capacity"));
                    t.setLocation(rs.getString("location"));
                    t.setStatus(rs.getString("status"));

                    tables.add(t);
                }
            }
        } finally {
            pool.releaseConnection(pConn);
        }

        return tables;
    }

    /* =========================
       ADD TABLE
       ========================= */
    public void addTable(Table table) throws SQLException {

        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql = """
                INSERT INTO restaurant_tables
                (table_number, capacity, location, status)
                VALUES (?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, table.getTableNumber());
                stmt.setInt(2, table.getCapacity());
                stmt.setString(3, table.getLocation());
                stmt.setString(4, "EMPTY"); // תואם DB
                stmt.executeUpdate();
            }
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /* =========================
       UPDATE TABLE (capacity + location בלבד)
       ========================= */
    public void updateTable(Table table) throws SQLException {

        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql = """
                UPDATE restaurant_tables
                SET capacity = ?, location = ?
                WHERE table_number = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, table.getCapacity());
                stmt.setString(2, table.getLocation());
                stmt.setInt(3, table.getTableNumber());
                stmt.executeUpdate();
            }
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /* =========================
       DELETE TABLE
       ========================= */
    public void deleteTable(int tableNumber) throws SQLException {

        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql = "DELETE FROM restaurant_tables WHERE table_number = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tableNumber);
                stmt.executeUpdate();
            }
        } finally {
            pool.releaseConnection(pConn);
        }
    }
}