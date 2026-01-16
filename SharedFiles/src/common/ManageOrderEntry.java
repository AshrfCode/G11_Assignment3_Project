package common;

import java.io.Serializable;
import java.sql.Timestamp;

public class ManageOrderEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int reservationId;
    private final String name;
    private final String phone;
    
 
    private final Timestamp startTime;
    
    private final int diners;       
    
    private final int tableNumber;
    private final String status;

    public ManageOrderEntry(int reservationId, String name, String phone, Timestamp startTime, int diners, int tableNumber, String status) {
        this.reservationId = reservationId;
        this.name = name;
        this.phone = phone;
        this.startTime = startTime;
        this.diners = diners;      
        this.tableNumber = tableNumber;
        this.status = status;
    }

    public int getReservationId() { return reservationId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public Timestamp getStartTime() { return startTime; }
    
    // ✅ Getter must be exactly this spelling for FXML property="diners"
    public int getDiners() { return diners; }  
    
    public int getTableNumber() { return tableNumber; }
    public String getStatus() { return status; }

    public String getTimeString() {
        if (startTime == null) return "";
        return startTime.toString().substring(0, 16); 
    }
}