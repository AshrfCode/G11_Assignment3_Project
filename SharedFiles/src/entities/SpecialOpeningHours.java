package entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents special opening hours for a specific date.
 * <p>
 * This entity is used to override regular opening hours for a given {@link #specialDate},
 * either by marking the day as closed or by defining custom opening and closing times.
 * It is {@link Serializable} to support persistence and/or transfer between application layers.
 * </p>
 */
public class SpecialOpeningHours implements Serializable {

    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** Unique identifier for this special opening hours entry (typically a database ID). */
    private int id;
    /** The date for which these special opening hours apply. */
    private LocalDate specialDate;
    /** Opening time for the special date (may be {@code null} if the day is closed). */
    private LocalTime openTime;
    /** Closing time for the special date (may be {@code null} if the day is closed). */
    private LocalTime closeTime;
    /** Indicates whether the specified date is fully closed. */
    private boolean isClosed;

    /**
     * Constructs an empty special opening hours instance.
     * <p>
     * Intended for frameworks/serialization tools that require a no-args constructor.
     * </p>
     */
    // Required empty constructor
    public SpecialOpeningHours() {}

    /**
     * Constructs a special opening hours entry that marks the given date as closed.
     *
     * @param specialDate the date to mark as closed
     */
    // Constructor for CLOSED day
    public SpecialOpeningHours(LocalDate specialDate) {
        this.specialDate = specialDate;
        this.isClosed = true;
    }

    /**
     * Constructs a special opening hours entry with custom opening and closing times.
     *
     * @param specialDate the date for which these special hours apply
     * @param openTime the opening time for the special date
     * @param closeTime the closing time for the special date
     */
    // Constructor for custom hours
    public SpecialOpeningHours(LocalDate specialDate, LocalTime openTime, LocalTime closeTime) {
        this.specialDate = specialDate;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = false;
    }

    // ---------- Getters & Setters ----------

    /**
     * Returns the unique identifier of this special opening hours entry.
     *
     * @return the entry ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this special opening hours entry.
     *
     * @param id the entry ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the special date to which these hours apply.
     *
     * @return the special date
     */
    public LocalDate getSpecialDate() {
        return specialDate;
    }

    /**
     * Sets the special date to which these hours apply.
     *
     * @param specialDate the special date to set
     */
    public void setSpecialDate(LocalDate specialDate) {
        this.specialDate = specialDate;
    }

    /**
     * Returns the opening time for the special date.
     *
     * @return the opening time, or {@code null} if the day is marked closed
     */
    public LocalTime getOpenTime() {
        return openTime;
    }

    /**
     * Sets the opening time for the special date.
     *
     * @param openTime the opening time to set
     */
    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    /**
     * Returns the closing time for the special date.
     *
     * @return the closing time, or {@code null} if the day is marked closed
     */
    public LocalTime getCloseTime() {
        return closeTime;
    }

    /**
     * Sets the closing time for the special date.
     *
     * @param closeTime the closing time to set
     */
    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    /**
     * Returns whether the special date is marked as closed.
     *
     * @return {@code true} if closed; {@code false} otherwise
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Sets whether the special date is marked as closed.
     *
     * @param closed {@code true} to mark closed; {@code false} otherwise
     */
    public void setClosed(boolean closed) {
        isClosed = closed;
    }
}
