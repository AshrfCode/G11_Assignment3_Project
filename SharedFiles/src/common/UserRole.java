package common;

/**
 * Defines the supported user roles within the system.
 * <p>
 * Roles are typically used for authorization and to control access to features and screens.
 * </p>
 */
public enum UserRole {
    /** A subscriber/customer role with access to subscriber features. */
    SUBSCRIBER,
    /** A representative role (e.g., service/restaurant representative) with operational privileges. */
    REPRESENTATIVE,
    /** A manager role with elevated administrative privileges. */
    MANAGER
}
