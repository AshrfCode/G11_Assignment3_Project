package entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Reservation implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id; // DB primary key (optional but recommended)

    private LocalDate reserveDate;
    private LocalTime reserveTime;
    private int dinnersNumber;
    private String reservationCode;
    private String reservationStatus; // ACTIVE / INACTIVE

    // 🔗 Relations
    private String subscriberNumber;   // e.g. SUB123
    private Integer tableNumber;       // nullable

    public Reservation() {}

    public Reservation(LocalDate reserveDate, LocalTime reserveTime,
                       int dinnersNumber, String reservationCode,
                       String reservationStatus,
                       String subscriberNumber, Integer tableNumber) {
        this.reserveDate = reserveDate;
        this.reserveTime = reserveTime;
        this.dinnersNumber = dinnersNumber;
        this.reservationCode = reservationCode;
        this.reservationStatus = reservationStatus;
        this.subscriberNumber = subscriberNumber;
        this.tableNumber = tableNumber;
    }

    // ---------- Getters & Setters ----------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(LocalDate reserveDate) {
        this.reserveDate = reserveDate;
    }

    public LocalTime getReserveTime() {
        return reserveTime;
    }

    public void setReserveTime(LocalTime reserveTime) {
        this.reserveTime = reserveTime;
    }

    public int getDinnersNumber() {
        return dinnersNumber;
    }

    public void setDinnersNumber(int dinnersNumber) {
        this.dinnersNumber = dinnersNumber;
    }

    public String getReservationCode() {
        return reservationCode;
    }

    public void setReservationCode(String reservationCode) {
        this.reservationCode = reservationCode;
    }

    public String getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(String reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public String getSubscriberNumber() {
        return subscriberNumber;
    }

    public void setSubscriberNumber(String subscriberNumber) {
        this.subscriberNumber = subscriberNumber;
    }

    public Integer getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }
}
