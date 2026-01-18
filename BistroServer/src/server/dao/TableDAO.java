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
       GET TABLE CAPACITY (NEW)
       ========================= */
    public Integer getTableCapacity(int tableNumber) throws SQLException {
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql = "SELECT capacity FROM restaurant_tables WHERE table_number = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tableNumber);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return rs.getInt("capacity");
                    return null;
                }
            }
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /* =========================
       ADD TABLE
       ========================= */
    public void addTable(Table table) throws SQLException {

        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql =
                "INSERT INTO restaurant_tables (table_number, capacity, location, status) " +
                "VALUES (?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, table.getTableNumber());
                stmt.setInt(2, table.getCapacity());
                stmt.setString(3, table.getLocation());
                stmt.setString(4, "EMPTY");
                stmt.executeUpdate();
            }
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    /* =========================
       UPDATE TABLE (capacity + location only)
       ========================= */
    public void updateTable(Table table) throws SQLException {

        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql =
                "UPDATE restaurant_tables " +
                "SET capacity = ?, location = ? " +
                "WHERE table_number = ?";

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
    
    public Integer findEmptyTableForDiners(Connection conn, int diners) throws SQLException
{
    String sql = """
            SELECT table_number
            FROM restaurant_tables
            WHERE UPPER(TRIM(status)) = 'EMPTY' AND capacity >= ?
            ORDER BY capacity ASC, table_number ASC
            LIMIT 1
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, diners);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("table_number");
            return null;
        }
        }
    }

    public int updateTableStatus(int tableNumber, String status) throws SQLException
{
    PooledConnection pConn = null;
        try {
        pConn = pool.getConnection();
        Connection conn = pConn.getConnection();

        System.out.println("DEBUG: updateTableStatus table=" + tableNumber + " status=" + status);
        System.out.println("DEBUG: DB=" + conn.getMetaData().getURL());

        String sql = "UPDATE restaurant_tables SET status = ? WHERE table_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, tableNumber);
            int rows = ps.executeUpdate();
            System.out.println("DEBUG: rowsUpdated=" + rows);

            // בדיקת סטטוס מיד אחרי
            try (PreparedStatement chk =
                         conn.prepareStatement("SELECT status FROM restaurant_tables WHERE table_number=?")) {
                chk.setInt(1, tableNumber);
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next()) System.out.println("DEBUG: statusAfterUpdate=" + rs.getString(1));
                }
                }

                return rows;
            }
            }
            finally
            {
                pool.releaseConnection(pConn);
            }
        }


    public boolean occupyTableIfEmpty(Connection conn, int tableNumber) throws SQLException
{
    String sql = """
            UPDATE restaurant_tables
            SET status = 'OCCUPIED'
            WHERE table_number = ?
              AND UPPER(TRIM(status)) = 'EMPTY'
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, tableNumber);
        return ps.executeUpdate() == 1;
    }
}
}
