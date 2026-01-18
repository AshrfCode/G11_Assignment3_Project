package client;

import java.util.function.Consumer;

/**
 * Holds shared client-side session state for the currently active user and UI message handling.
 * <p>
 * This class is a static session container used across screens to:
 * <ul>
 *   <li>Route server messages to the currently active screen via {@link #activeHandler}</li>
 *   <li>Store logged-in user details for convenience (e.g., subscriber auto-fill)</li>
 * </ul>
 * This class is not intended to be instantiated.
 */
public class ClientSession {

    /**
     * The currently active screen-level message handler.
     * <p>
     * When non-null, incoming server messages may be forwarded to this handler first.
     */
    public static Consumer<Object> activeHandler = null;

    /**
     * Logged-in user identifier (used for subscriber auto-fill and authorization).
     * A value of {@code -1} indicates no logged-in user.
     */
    // Logged-in user info (for Subscriber auto-fill)
    public static int userId = -1;

    /**
     * Logged-in user's role (e.g., subscriber, employee, manager).
     */
    public static String userRole = "";

    /**
     * Logged-in user's display/name value.
     */
    public static String userName = "";

    /**
     * Logged-in user's email address.
     */
    public static String userEmail = "";

    /**
     * Logged-in user's phone number.
     */
    public static String userPhone = "";

    /**
     * Clears all session state and resets the active handler and user fields to defaults.
     */
    public static void clear() {
        activeHandler = null;
        userId = -1;
        userRole = "";
        userName = "";
        userEmail = "";
        userPhone = "";
    }

    /**
     * Indicates whether a user is currently considered logged in.
     *
     * @return {@code true} if {@link #userId} is greater than 0; otherwise {@code false}
     */
    public static boolean isLoggedIn() {
        return userId > 0;
    }

    /**
     * Private constructor to prevent instantiation; this is a static utility/session holder.
     */
    private ClientSession() {}
}
