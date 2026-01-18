package common;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Represents a single entry in the waiting list.
 * <p>
 * This {@link Serializable} DTO is used to transfer waiting list information between
 * system layers (e.g., server to client) and includes basic identifying and contact
 * details along with the original request time.
 * </p>
 */
public class WaitingListEntry implements Serializable {
    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** Unique identifier for the waiting list entry (typically a database ID). */
    private final int id;
    /** Timestamp indicating when the waiting list request was created/recorded. */
    private final Timestamp requestTime;
    /** Subscriber number associated with the request, if applicable. */
    private final String subscriberNumber;
    
    // ✅ NEW FIELDS
    /** Name of the person on the waiting list. */
    private final String name;
    /** Phone number for contacting the person on the waiting list. */
    private final String phone;
    /** Type indicator for the entry (e.g., "Subscriber" or "Guest"). */
    private final String type; // "Subscriber" or "Guest"

    /**
     * Constructs a new waiting list entry with the provided details.
     *
     * @param id the unique identifier for this waiting list entry
     * @param requestTime the timestamp when the request was created
     * @param subscriberNumber the subscriber number associated with the entry (if applicable)
     * @param name the name of the person on the waiting list
     * @param phone the contact phone number
     * @param type the entry type (e.g., "Subscriber" or "Guest")
     */
    // ✅ UPDATED CONSTRUCTOR
    public WaitingListEntry(int id, Timestamp requestTime, String subscriberNumber, String name, String phone, String type) {
        this.id = id;
        this.requestTime = requestTime;
        this.subscriberNumber = subscriberNumber;
        this.name = name;
        this.phone = phone;
        this.type = type;
    }

    // ✅ GETTERS
    /**
     * Returns the unique identifier of this waiting list entry.
     *
     * @return the entry ID
     */
    public int getId() { return id; }

    /**
     * Returns the timestamp when the waiting list request was created.
     *
     * @return the request timestamp
     */
    public Timestamp getRequestTime() { return requestTime; }

    /**
     * Returns the subscriber number associated with this entry.
     *
     * @return the subscriber number, or {@code null} if not applicable
     */
    public String getSubscriberNumber() { return subscriberNumber; }

    /**
     * Returns the name of the person on the waiting list.
     *
     * @return the name
     */
    public String getName() { return name; }

    /**
     * Returns the phone number of the person on the waiting list.
     *
     * @return the phone number
     */
    public String getPhone() { return phone; }

    /**
     * Returns the type of this waiting list entry.
     *
     * @return the entry type (e.g., "Subscriber" or "Guest")
     */
    public String getType() { return type; }
    
    /**
     * Returns a formatted string representation of the {@link #requestTime} suitable for UI display.
     * <p>
     * The returned value is derived from {@link Timestamp#toString()} and truncated to minutes,
     * producing a value such as {@code "2026-01-16 20:00"}.
     * </p>
     *
     * @return a formatted time string for display
     */
    // Helper to make the TableView display a nice String for time
    public String getTimeString() {
        return requestTime.toString().substring(0, 16); // "2026-01-16 20:00"
    }
}
