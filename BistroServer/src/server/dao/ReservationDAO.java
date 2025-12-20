package server.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;

import entities.Reservation;
import server.MySQLConnectionPool;
import server.PooledConnection;

public class ReservationDAO {

    private final MySQLConnectionPool pool = MySQLConnectionPool.getInstance();

    public void insertReservation(Reservation r) throws SQLException {

        PooledConnection pConn = null;
        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql = """
                INSERT INTO reservations
                (reserve_date, reserve_time, dinners_number,
                 reservation_code, reservation_status,
                 subscriber_number, table_number)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(r.getReserveDate()));
                stmt.setTime(2, Time.valueOf(r.getReserveTime()));
                stmt.setInt(3, r.getDinnersNumber());
                stmt.setString(4, r.getReservationCode());
                stmt.setString(5, r.getReservationStatus());
                stmt.setString(6, r.getSubscriberNumber());
                stmt.setObject(7, r.getTableNumber(), Types.INTEGER);
                stmt.executeUpdate();
            }
        } finally {
            pool.releaseConnection(pConn);
        }
    }

    public Reservation getByCode(String code) throws SQLException {
        PooledConnection pConn = null;

        try {
            pConn = pool.getConnection();
            Connection conn = pConn.getConnection();

            String sql = "SELECT * FROM reservations WHERE reservation_code = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, code);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) return null;

                Reservation r = new Reservation();
                r.setReservationCode(rs.getString("reservation_code"));
                r.setReservationStatus(rs.getString("reservation_status"));
                r.setSubscriberNumber(rs.getString("subscriber_number"));
                return r;
            }
        } finally {
            pool.releaseConnection(pConn);
        }
    }
}
