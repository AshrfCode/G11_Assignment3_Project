package entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class Table implements Serializable {

    private static final long serialVersionUID = 1L;

    private int tableNumber;
    private int capacity;
    private String location;   // Inside / Outside / VIP Room
    private String status;     // EMPTY / OCCUPIED
    private Timestamp reservedFrom;
    private Timestamp reservedTo;

    public Table() {}

    // ✅ בנאי לשימוש ב-GUI
    public Table(int tableNumber, int capacity, String location) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.location = location;
        this.status = "EMPTY"; // תואם ל-DB
    }

    // ---------- Getters ----------
    public int getTableNumber() { return tableNumber; }
    public int getCapacity() { return capacity; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public Timestamp getReservedFrom() { return reservedFrom; }
    public Timestamp getReservedTo() { return reservedTo; }

    // ---------- Setters ----------
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setLocation(String location) { this.location = location; }
    public void setStatus(String status) { this.status = status; }
    public void setReservedFrom(Timestamp reservedFrom) { this.reservedFrom = reservedFrom; }
    public void setReservedTo(Timestamp reservedTo) { this.reservedTo = reservedTo; }
}
