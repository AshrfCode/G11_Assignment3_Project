package common;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a single row of reservation history data.
 * <p>
 * This DTO is intended for transferring reservation history information between
 * system layers (e.g., server to client) and is {@link Serializable} for network transport.
 * </p>
 */
public class ReservationHistoryRow implements Serializable {
    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** Reservation start date and time. */
    private LocalDateTime start;
    /** Reservation end date and time. */
    private LocalDateTime end;
    /** Number of diners included in the reservation. */
    private int diners;
    /** Reserved table number or identifier. */
    private int table;
    /** Reservation code used to identify the reservation (e.g., confirmation code). */
    private String code;
    /** Reservation status (e.g., confirmed, canceled, completed), as stored in the system. */
    private String status;
    /** Timestamp when the reservation was created in the system. */
    private LocalDateTime created;

    /**
     * Constructs a reservation history row with all relevant reservation details.
     *
     * @param start the reservation start date and time
     * @param end the reservation end date and time
     * @param diners the number of diners for the reservation
     * @param table the reserved table number or identifier
     * @param code the reservation code/confirmation code
     * @param status the reservation status
     * @param created the timestamp when the reservation was created
     */
    public ReservationHistoryRow(LocalDateTime start, LocalDateTime end, int diners, int table,
                                 String code, String status, LocalDateTime created) {
        this.start = start;
        this.end = end;
        this.diners = diners;
        this.table = table;
        this.code = code;
        this.status = status;
        this.created = created;
    }

    /**
     * Returns the reservation start date and time.
     *
     * @return the reservation start date and time
     */
    public LocalDateTime getStart() { return start; }

    /**
     * Returns the reservation end date and time.
     *
     * @return the reservation end date and time
     */
    public LocalDateTime getEnd() { return end; }

    /**
     * Returns the number of diners for the reservation.
     *
     * @return the number of diners
     */
    public int getDiners() { return diners; }

    /**
     * Returns the reserved table number or identifier.
     *
     * @return the table number or identifier
     */
    public int getTable() { return table; }

    /**
     * Returns the reservation code/confirmation code.
     *
     * @return the reservation code
     */
    public String getCode() { return code; }

    /**
     * Returns the reservation status.
     *
     * @return the reservation status
     */
    public String getStatus() { return status; }

    /**
     * Returns the timestamp when the reservation was created.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreated() { return created; }
}
