package common;

import java.io.Serializable;
import java.sql.Timestamp;

public class WaitingListEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final Timestamp requestTime;
    private final String subscriberNumber;

    public WaitingListEntry(int id, Timestamp requestTime, String subscriberNumber) {
        this.id = id;
        this.requestTime = requestTime;
        this.subscriberNumber = subscriberNumber;
    }

    public int getId() { return id; }
    public Timestamp getRequestTime() { return requestTime; }
    public String getSubscriberNumber() { return subscriberNumber; }
}
