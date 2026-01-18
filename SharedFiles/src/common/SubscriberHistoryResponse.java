package common;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a subscriber's reservation history response payload.
 * <p>
 * This {@link Serializable} DTO bundles the subscriber's reservation history rows along with
 * an aggregated visits count, and is typically transferred between server and client.
 * </p>
 */
public class SubscriberHistoryResponse implements Serializable {
    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** List of reservation history entries associated with the subscriber. */
    private final List<ReservationHistoryRow> reservations;
    /** Total number of visits attributed to the subscriber. */
    private final int visitsCount;

    /**
     * Constructs a subscriber history response with reservation entries and a visits count.
     *
     * @param reservations the list of reservation history rows for the subscriber
     * @param visitsCount the total number of visits for the subscriber
     */
    public SubscriberHistoryResponse(List<ReservationHistoryRow> reservations, int visitsCount) {
        this.reservations = reservations;
        this.visitsCount = visitsCount;
    }

    /**
     * Returns the subscriber's reservation history entries.
     *
     * @return the list of reservation history rows
     */
    public List<ReservationHistoryRow> getReservations() { return reservations; }

    /**
     * Returns the total number of visits for the subscriber.
     *
     * @return the visits count
     */
    public int getVisitsCount() { return visitsCount; }
}
