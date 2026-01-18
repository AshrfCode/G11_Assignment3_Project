package common;

import java.io.Serializable;

import entities.User;

/**
 * Represents the server response for a login attempt.
 * <p>
 * Includes a success flag, a human-readable message, and (when successful) the authenticated user object.
 * This object is serializable for transmission between client and server.
 * </p>
 */
public class LoginResponse implements Serializable {

    /**
     * Serialization version UID for compatibility across different class versions.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Indicates whether the login operation succeeded.
     */
    private final boolean success;

    /**
     * Message describing the result (e.g., error details or success confirmation).
     */
    private final String message;

    /**
     * The authenticated user, or {@code null} when authentication fails.
     */
    private final User user;

    /**
     * Constructs a new login response.
     *
     * @param success {@code true} if login succeeded; {@code false} otherwise
     * @param message a message describing the result
     * @param user the authenticated user if successful; otherwise {@code null}
     */
    public LoginResponse(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    /**
     * Returns whether the login was successful.
     *
     * @return {@code true} if successful; {@code false} otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the response message.
     *
     * @return the message string
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the authenticated user, if any.
     *
     * @return the {@link User} instance on success, or {@code null} on failure
     */
    public User getUser() {
        return user;
    }
}
