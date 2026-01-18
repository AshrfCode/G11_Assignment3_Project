package entities;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * Represents regular opening hours for a given day.
 * <p>
 * This entity stores the day identifier (e.g., weekday name) along with opening
 * and closing {@link LocalTime} values. It is {@link Serializable} to support
 * persistence and/or transfer between application layers.
 * </p>
 */
public class OpeningHours implements Serializable {

    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** Day associated with these opening hours (e.g., "Monday"). */
    private String day;
    /** Opening time for the specified day. */
    private LocalTime openTime;
    /** Closing time for the specified day. */
    private LocalTime closeTime;

    /**
     * Constructs an empty opening-hours instance.
     * <p>
     * Intended for frameworks/serialization tools that require a no-args constructor.
     * </p>
     */
    public OpeningHours() {}

    /**
     * Constructs opening hours for a specific day.
     *
     * @param day the day identifier (e.g., weekday name)
     * @param openTime the opening time
     * @param closeTime the closing time
     */
    public OpeningHours(String day, LocalTime openTime, LocalTime closeTime) {
        this.day = day;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    /**
     * Returns the day associated with these opening hours.
     *
     * @return the day identifier
     */
    public String getDay() { return day; }

    /**
     * Sets the day associated with these opening hours.
     *
     * @param day the day identifier to set
     */
    public void setDay(String day) { this.day = day; }

    /**
     * Returns the opening time.
     *
     * @return the opening time
     */
    public LocalTime getOpenTime() { return openTime; }

    /**
     * Sets the opening time.
     *
     * @param openTime the opening time to set
     */
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }

    /**
     * Returns the closing time.
     *
     * @return the closing time
     */
    public LocalTime getCloseTime() { return closeTime; }

    /**
     * Sets the closing time.
     *
     * @param closeTime the closing time to set
     */
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }
}
