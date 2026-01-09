package common;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ReservationHistoryRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDateTime start;
    private LocalDateTime end;
    private int diners;
    private int table;
    private String code;
    private String status;
    private LocalDateTime created;

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

    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public int getDiners() { return diners; }
    public int getTable() { return table; }
    public String getCode() { return code; }
    public String getStatus() { return status; }
    public LocalDateTime getCreated() { return created; }
}
