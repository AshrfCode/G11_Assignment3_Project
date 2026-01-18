package entities;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Represents a restaurant table entity.
 * <p>
 * A table includes identifying information (table number), capacity, location, and current status.
 * It may also include a reserved time range to indicate when it is allocated for a reservation.
 * This class is {@link Serializable} to support persistence and/or transfer between application layers.
 * </p>
 */
public class Table implements Serializable {

    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** Unique identifier/number of the table. */
    private int tableNumber;
    /** Maximum number of diners the table can accommodate. */
    private int capacity;
    /** Physical location designation for the table (e.g., Inside / Outside / VIP Room). */
    private String location;   // Inside / Outside / VIP Room
    /** Current table status (e.g., EMPTY / OCCUPIED). */
    private String status;     // EMPTY / OCCUPIED
    /** Timestamp indicating when the table is reserved from (if reserved). */
    private Timestamp reservedFrom;
    /** Timestamp indicating when the table is reserved until (if reserved). */
    private Timestamp reservedTo;

    /**
     * Constructs an empty table instance.
     * <p>
     * Intended for frameworks/serialization tools that require a no-args constructor.
     * </p>
     */
    public Table() {}

    /**
     * Constructs a table instance for GUI usage with default status set to {@code "EMPTY"}.
     *
     * @param tableNumber the table number/identifier
     * @param capacity the maximum number of diners the table can accommodate
     * @param location the location designation for the table (e.g., Inside / Outside / VIP Room)
     */
    // ✅ בנאי לשימוש ב-GUI
    public Table(int tableNumber, int capacity, String location) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.location = location;
        this.status = "EMPTY"; // תואם ל-DB
    }

    // ---------- Getters ----------
    /**
     * Returns the table number/identifier.
     *
     * @return the table number
     */
    public int getTableNumber() { return tableNumber; }

    /**
     * Returns the table capacity.
     *
     * @return the maximum number of diners the table can accommodate
     */
    public int getCapacity() { return capacity; }

    /**
     * Returns the table location designation.
     *
     * @return the location (e.g., Inside / Outside / VIP Room)
     */
    public String getLocation() { return location; }

    /**
     * Returns the current table status.
     *
     * @return the table status (e.g., EMPTY / OCCUPIED)
     */
    public String getStatus() { return status; }

    /**
     * Returns the timestamp from which the table is reserved.
     *
     * @return the reserved-from timestamp, or {@code null} if not reserved
     */
    public Timestamp getReservedFrom() { return reservedFrom; }

    /**
     * Returns the timestamp until which the table is reserved.
     *
     * @return the reserved-to timestamp, or {@code null} if not reserved
     */
    public Timestamp getReservedTo() { return reservedTo; }

    // ---------- Setters ----------
    /**
     * Sets the table number/identifier.
     *
     * @param tableNumber the table number to set
     */
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }

    /**
     * Sets the table capacity.
     *
     * @param capacity the capacity to set
     */
    public void setCapacity(int capacity) { this.capacity = capacity; }

    /**
     * Sets the table location designation.
     *
     * @param location the location to set
     */
    public void setLocation(String location) { this.location = location; }

    /**
     * Sets the current table status.
     *
     * @param status the status to set (e.g., EMPTY / OCCUPIED)
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Sets the timestamp from which the table is reserved.
     *
     * @param reservedFrom the reserved-from timestamp to set
     */
    public void setReservedFrom(Timestamp reservedFrom) { this.reservedFrom = reservedFrom; }

    /**
     * Sets the timestamp until which the table is reserved.
     *
     * @param reservedTo the reserved-to timestamp to set
     */
    public void setReservedTo(Timestamp reservedTo) { this.reservedTo = reservedTo; }
}
