package entities;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Represents a reservation entity within the system.
 * <p>
 * A reservation includes identifying information, status, relationships to a subscriber and table,
 * scheduled time range, optional check-in time, and contact details. This class is
 * {@link Serializable} to support persistence and/or transfer between application layers.
 * </p>
 */
public class Reservation implements Serializable {

    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** Unique identifier for the reservation (typically a database primary key). */
    private int reservationId; 
    /** Number of diners associated with the reservation. */
    private int dinnersNumber;
    /** Confirmation code identifying the reservation (matches DB column {@code confirmation_code}). */
    private String confirmationCode; // Matches DB 'confirmation_code'
    /** Reservation status (e.g., ACTIVE, CANCELED, CHECKED_IN). */
    private String status;           // ACTIVE, CANCELED, CHECKED_IN
    
    // 🔗 Relations
    /** Subscriber number associated with the reservation (if applicable). */
    private String subscriberNumber;
    /** Table number assigned to the reservation. */
    private int tableNumber;
    
    // 🕒 Time fields (Matching DB 'datetime' columns)
    /** Scheduled start timestamp for the reservation. */
    private Timestamp startTime;
    /** Scheduled end timestamp for the reservation. */
    private Timestamp endTime;
    /** Timestamp when the guest checked in, if check-in was performed. */
    private Timestamp checkInTime; // New field for the Check-In logic
    
    // Contact info (from your DB screenshots)
    /** Contact phone number associated with the reservation. */
    private String phone;
    /** Contact email address associated with the reservation. */
    private String email;

    /**
     * Constructs an empty reservation instance.
     * <p>
     * Intended for frameworks/serialization tools that require a no-args constructor.
     * </p>
     */
    public Reservation() {}

    /**
     * Constructs a reservation with the primary reservation fields.
     *
     * @param reservationId the unique reservation identifier
     * @param dinnersNumber the number of diners for the reservation
     * @param confirmationCode the reservation confirmation code
     * @param status the reservation status (e.g., ACTIVE, CANCELED, CHECKED_IN)
     * @param subscriberNumber the subscriber number associated with the reservation
     * @param tableNumber the table number assigned to the reservation
     * @param startTime the scheduled reservation start time
     * @param endTime the scheduled reservation end time
     */
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

    /**
     * Returns the reservation ID.
     *
     * @return the reservation ID
     */
    public int getReservationId() { return reservationId; }

    /**
     * Sets the reservation ID.
     *
     * @param reservationId the reservation ID to set
     */
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    /**
     * Returns the number of diners for the reservation.
     *
     * @return the number of diners
     */
    public int getDinnersNumber() { return dinnersNumber; }

    /**
     * Sets the number of diners for the reservation.
     *
     * @param dinnersNumber the number of diners to set
     */
    public void setDinnersNumber(int dinnersNumber) { this.dinnersNumber = dinnersNumber; }

    /**
     * Returns the reservation confirmation code.
     *
     * @return the confirmation code
     */
    public String getConfirmationCode() { return confirmationCode; }

    /**
     * Sets the reservation confirmation code.
     *
     * @param confirmationCode the confirmation code to set
     */
    public void setConfirmationCode(String confirmationCode) { this.confirmationCode = confirmationCode; }

    /**
     * Returns the current reservation status.
     *
     * @return the reservation status
     */
    public String getStatus() { return status; }

    /**
     * Sets the reservation status.
     *
     * @param status the status to set
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Returns the subscriber number associated with the reservation.
     *
     * @return the subscriber number, or {@code null} if not applicable
     */
    public String getSubscriberNumber() { return subscriberNumber; }

    /**
     * Sets the subscriber number associated with the reservation.
     *
     * @param subscriberNumber the subscriber number to set
     */
    public void setSubscriberNumber(String subscriberNumber) { this.subscriberNumber = subscriberNumber; }

    /**
     * Returns the assigned table number.
     *
     * @return the table number
     */
    public int getTableNumber() { return tableNumber; }

    /**
     * Sets the assigned table number.
     *
     * @param tableNumber the table number to set
     */
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }

    /**
     * Returns the scheduled start time of the reservation.
     *
     * @return the start time
     */
    public Timestamp getStartTime() { return startTime; }

    /**
     * Sets the scheduled start time of the reservation.
     *
     * @param startTime the start time to set
     */
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

    /**
     * Returns the scheduled end time of the reservation.
     *
     * @return the end time
     */
    public Timestamp getEndTime() { return endTime; }

    /**
     * Sets the scheduled end time of the reservation.
     *
     * @param endTime the end time to set
     */
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

    /**
     * Returns the check-in time for the reservation, if available.
     *
     * @return the check-in time, or {@code null} if not checked in
     */
    public Timestamp getCheckInTime() { return checkInTime; }

    /**
     * Sets the check-in time for the reservation.
     *
     * @param checkInTime the check-in time to set
     */
    public void setCheckInTime(Timestamp checkInTime) { this.checkInTime = checkInTime; }

    /**
     * Returns the contact phone number associated with the reservation.
     *
     * @return the phone number
     */
    public String getPhone() { return phone; }

    /**
     * Sets the contact phone number associated with the reservation.
     *
     * @param phone the phone number to set
     */
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Returns the contact email address associated with the reservation.
     *
     * @return the email address
     */
    public String getEmail() { return email; }

    /**
     * Sets the contact email address associated with the reservation.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) { this.email = email; }
}
