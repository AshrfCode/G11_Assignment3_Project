package entities;

import java.io.Serializable;

public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    private String activityHistory;

    public Tag() {}

    public String getActivityHistory() {
        return activityHistory;
    }
}
