package server.dao;

import server.MySQLConnectionPool;
import server.PooledConnection;
import entities.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) responsible for CRUD and status-related operations on restaurant tables.
 * <p>
 * Interacts with the {@code restaurant_tables} table to manage table metadata (capacity, location)
 * and operational status (e.g., EMPTY, OCCUPIED).
 * </p>
 */
public class TableDAO {

    /**
     * Shared MySQL connection pool used to obtain and release database connections.
     */
    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

    /* =========================
       GET ALL TABLES
       ========================= */

    /**
     * Retrieves all restaurant tables from the database.
     *
     * @return a list of {@link Table} entities representing all rows in {@code restaurant_tables}
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Retrieves the capacity of a specific table.
     *
     * @param tableNumber the table number to query
     * @return the table capacity if the table exists; {@code null} otherwise
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Inserts a new table into the {@code restaurant_tables} table.
     * <p>
     * The inserted table status is set to {@code EMPTY} regardless of the provided entity status.
     * </p>
     *
     * @param table the table entity containing number, capacity, and location
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Updates an existing table's capacity and location by table number.
     *
     * @param table the table entity containing the table number and updated capacity/location values
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Deletes a table from the {@code restaurant_tables} table by table number.
     *
     * @param tableNumber the table number to delete
     * @throws SQLException if a database access error occurs
     */
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
    
    /**
     * Finds a single empty table that can accommodate the given number of diners.
     * <p>
     * Chooses the smallest suitable capacity first (ascending), then by table number (ascending).
     * This method uses the provided connection and does not interact with the connection pool.
     * </p>
     *
     * @param conn an open database connection to use for the query
     * @param diners the minimum number of seats required
     * @return the table number of a suitable empty table, or {@code null} if none is available
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Updates the status of a table using a pooled database connection.
     *
     * @param tableNumber the table number to update
     * @param status the new status value to set (e.g., "EMPTY", "OCCUPIED")
     * @return the number of rows updated (0 if table not found, 1 if updated)
     * @throws SQLException if a database access error occurs
     */
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


    /**
     * Attempts to mark a table as {@code OCCUPIED} only if it is currently {@code EMPTY}.
     * <p>
     * This method is safe for concurrent usage at the database level because it performs a conditional
     * update and succeeds only when exactly one row is affected.
     * </p>
     *
     * @param conn an open database connection to use for the update
     * @param tableNumber the table number to occupy
     * @return {@code true} if the table was empty and is now occupied; {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
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
