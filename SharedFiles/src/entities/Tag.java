package entities;

import java.io.Serializable;

/**
 * Represents a tag entity used for storing or associating activity information.
 * <p>
 * This class is {@link Serializable} to support persistence and/or transfer between
 * application layers. The tag currently encapsulates an activity history representation.
 * </p>
 */
public class Tag implements Serializable {

    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** Activity history associated with this tag (format defined by business logic). */
    private String activityHistory;

    /**
     * Constructs an empty tag instance.
     * <p>
     * Intended for frameworks/serialization tools that require a no-args constructor.
     * </p>
     */
    public Tag() {}

    /**
     * Returns the activity history associated with this tag.
     *
     * @return the activity history
     */
    public String getActivityHistory() {
        return activityHistory;
    }
}
