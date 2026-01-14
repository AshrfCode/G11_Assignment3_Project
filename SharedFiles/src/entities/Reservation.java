package entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class Reservation implements Serializable {

    private static final long serialVersionUID = 1L;

    private int reservationId; 
    private int dinnersNumber;
    private String confirmationCode; // Matches DB 'confirmation_code'
    private String status;           // ACTIVE, CANCELED, CHECKED_IN
    
    // 🔗 Relations
    private String subscriberNumber;
    private int tableNumber;
    
    // 🕒 Time fields (Matching DB 'datetime' columns)
    private Timestamp startTime;
    private Timestamp endTime;
    private Timestamp checkInTime; // New field for the Check-In logic
    
    // Contact info (from your DB screenshots)
    private String phone;
    private String email;

    public Reservation() {}

    // Constructor with main fields
    public Reservation(int reservationId, int dinnersNumber, String confirmationCode, 
                       String status, String subscriberNumber, int tableNumber, 
                       Timestamp startTime, Timestamp endTime) {
        this.reservationId = reservationId;
        this.dinnersNumber = dinnersNumber;
        this.confirmationCode = confirmationCode;
        this.status = status;
        this.subscriberNumber = subscriberNumber;
        this.tableNumber = tableNumber;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // ---------- Getters & Setters ----------

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public int getDinnersNumber() { return dinnersNumber; }
    public void setDinnersNumber(int dinnersNumber) { this.dinnersNumber = dinnersNumber; }

    public String getConfirmationCode() { return confirmationCode; }
    public void setConfirmationCode(String confirmationCode) { this.confirmationCode = confirmationCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubscriberNumber() { return subscriberNumber; }
    public void setSubscriberNumber(String subscriberNumber) { this.subscriberNumber = subscriberNumber; }

    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }

    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

    public Timestamp getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Timestamp checkInTime) { this.checkInTime = checkInTime; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}