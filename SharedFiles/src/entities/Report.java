package entities;

import java.io.Serializable;
import java.time.LocalDate;

public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate reportStartDate;
    private LocalDate reportEndDate;

    public Report() {}

    public LocalDate getReportStartDate() { return reportStartDate; }
    public void setReportStartDate(LocalDate reportStartDate) {
        this.reportStartDate = reportStartDate;
    }

    public LocalDate getReportEndDate() { return reportEndDate; }
    public void setReportEndDate(LocalDate reportEndDate) {
        this.reportEndDate = reportEndDate;
    }
}
