package common;

import java.io.Serializable;
import java.util.List;

public class SubscriberHistoryResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<ReservationHistoryRow> reservations;
    private final int visitsCount;

    public SubscriberHistoryResponse(List<ReservationHistoryRow> reservations, int visitsCount) {
        this.reservations = reservations;
        this.visitsCount = visitsCount;
    }

    public List<ReservationHistoryRow> getReservations() { return reservations; }
    public int getVisitsCount() { return visitsCount; }
}
