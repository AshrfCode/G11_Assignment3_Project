package entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class Table implements Serializable {

    private static final long serialVersionUID = 1L;

    private int tableNumber;
    private int capacity;
    private String location;
    private String status; // EMPTY / OCCUPIED
    private Timestamp reservedFrom;
    private Timestamp reservedTo;

    public Table() {}

    public int getTableNumber() { return tableNumber; }
    public int getCapacity() { return capacity; }
    public Timestamp getReservedFrom() { return reservedFrom; }
    public Timestamp getReservedTo() { return reservedTo; }
    public String getStatus() { return status; }
}
