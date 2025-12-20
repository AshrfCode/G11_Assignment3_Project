package entities;

import java.io.Serializable;
import java.time.LocalTime;

public class OpeningHours implements Serializable {

    private static final long serialVersionUID = 1L;

    private String day;
    private LocalTime openTime;
    private LocalTime closeTime;

    public OpeningHours() {}

    public OpeningHours(String day, LocalTime openTime, LocalTime closeTime) {
        this.day = day;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }

    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }
}
