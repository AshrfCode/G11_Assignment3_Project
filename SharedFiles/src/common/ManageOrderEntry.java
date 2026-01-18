package common;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Represents a single reservation entry for management views (e.g., order/reservation tables in the UI).
 * <p>
 * This object is serializable for transmission between client and server and provides convenient
 * accessors for displaying reservation details such as time, diners count, table number, and status.
 * </p>
 */
public class ManageOrderEntry implements Serializable {
    /**
     * Serialization version UID for compatibility across different class versions.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier of the reservation.
     */
    private final int reservationId;

    /**
     * Name associated with the reservation.
     */
    private final String name;

    /**
     * Phone number associated with the reservation.
     */
    private final String phone;
    
 
    /**
     * Scheduled start time of the reservation.
     */
    private final Timestamp startTime;
    
    /**
     * Number of diners for the reservation.
     */
    private final int diners;       
    
    /**
     * Table number assigned to the reservation.
     */
    private final int tableNumber;

    /**
     * Current reservation status (e.g., ACTIVE, CHECKED_IN, CANCELED).
     */
    private final String status;

    /**
     * Constructs a new management entry for a reservation.
     *
     * @param reservationId the reservation identifier
     * @param name the name associated with the reservation
     * @param phone the phone number associated with the reservation
     * @param startTime the reservation start time
     * @param diners the number of diners
     * @param tableNumber the assigned table number
     * @param status the reservation status
     */
    public ManageOrderEntry(int reservationId, String name, String phone, Timestamp startTime, int diners, int tableNumber, String status) {
        this.reservationId = reservationId;
        this.name = name;
        this.phone = phone;
        this.startTime = startTime;
        this.diners = diners;      
        this.tableNumber = tableNumber;
        this.status = status;
    }

    /**
     * Returns the reservation identifier.
     *
     * @return the reservation ID
     */
    public int getReservationId() { return reservationId; }

    /**
     * Returns the name associated with the reservation.
     *
     * @return the reservation name
     */
    public String getName() { return name; }

    /**
     * Returns the phone number associated with the reservation.
     *
     * @return the phone number
     */
    public String getPhone() { return phone; }

    /**
     * Returns the reservation start time.
     *
     * @return the start time, or {@code null} if not available
     */
    public Timestamp getStartTime() { return startTime; }
    
    /**
     * Returns the number of diners for the reservation.
     * <p>
     * This getter name is used by JavaFX/FXML bindings expecting property {@code diners}.
     * </p>
     *
     * @return the number of diners
     */
    // ✅ Getter must be exactly this spelling for FXML property="diners"
    public int getDiners() { return diners; }  
    
    /**
     * Returns the table number assigned to the reservation.
     *
     * @return the table number
     */
    public int getTableNumber() { return tableNumber; }

    /**
     * Returns the reservation status.
     *
     * @return the status string
     */
    public String getStatus() { return status; }

    /**
     * Returns a formatted time string for display purposes.
     * <p>
     * If {@link #startTime} is {@code null}, an empty string is returned.
     * The returned value is derived from {@link Timestamp#toString()} and truncated for UI display.
     * </p>
     *
     * @return a display-friendly time string, or an empty string if no start time exists
     */
    public String getTimeString() {
        if (startTime == null) return "";
        return startTime.toString().substring(0, 16); 
    }
}
