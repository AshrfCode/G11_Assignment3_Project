package entities;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a report definition with a date range.
 * <p>
 * This entity holds the start and end dates used to define the reporting period.
 * It is {@link Serializable} to support persistence and/or transfer between
 * application layers.
 * </p>
 */
public class Report implements Serializable {

    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** Start date of the reporting period (inclusive, as defined by business logic). */
    private LocalDate reportStartDate;
    /** End date of the reporting period (inclusive, as defined by business logic). */
    private LocalDate reportEndDate;

    /**
     * Constructs an empty report instance.
     * <p>
     * Intended for frameworks/serialization tools that require a no-args constructor.
     * </p>
     */
    public Report() {}

    /**
     * Returns the start date of the reporting period.
     *
     * @return the report start date
     */
    public LocalDate getReportStartDate() { return reportStartDate; }

    /**
     * Sets the start date of the reporting period.
     *
     * @param reportStartDate the report start date to set
     */
    public void setReportStartDate(LocalDate reportStartDate) {
        this.reportStartDate = reportStartDate;
    }

    /**
     * Returns the end date of the reporting period.
     *
     * @return the report end date
     */
    public LocalDate getReportEndDate() { return reportEndDate; }

    /**
     * Sets the end date of the reporting period.
     *
     * @param reportEndDate the report end date to set
     */
    public void setReportEndDate(LocalDate reportEndDate) {
        this.reportEndDate = reportEndDate;
    }
}
