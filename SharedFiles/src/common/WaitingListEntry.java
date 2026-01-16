package common;

import java.io.Serializable;
import java.sql.Timestamp;

public class WaitingListEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final Timestamp requestTime;
    private final String subscriberNumber;
    
    // ✅ NEW FIELDS
    private final String name;
    private final String phone;
    private final String type; // "Subscriber" or "Guest"

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
    public int getId() { return id; }
    public Timestamp getRequestTime() { return requestTime; }
    public String getSubscriberNumber() { return subscriberNumber; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getType() { return type; }
    
    // Helper to make the TableView display a nice String for time
    public String getTimeString() {
        return requestTime.toString().substring(0, 16); // "2026-01-16 20:00"
    }
}