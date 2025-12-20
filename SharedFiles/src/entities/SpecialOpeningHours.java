package entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class SpecialOpeningHours implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private LocalDate specialDate;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean isClosed;

    // Required empty constructor
    public SpecialOpeningHours() {}

    // Constructor for CLOSED day
    public SpecialOpeningHours(LocalDate specialDate) {
        this.specialDate = specialDate;
        this.isClosed = true;
    }

    // Constructor for custom hours
    public SpecialOpeningHours(LocalDate specialDate, LocalTime openTime, LocalTime closeTime) {
        this.specialDate = specialDate;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = false;
    }

    // ---------- Getters & Setters ----------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getSpecialDate() {
        return specialDate;
    }

    public void setSpecialDate(LocalDate specialDate) {
        this.specialDate = specialDate;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }
}
