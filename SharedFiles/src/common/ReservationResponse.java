package common;

import java.io.Serializable;

/**
 * Represents a reservation operation response payload.
 * <p>
 * This {@link Serializable} DTO encapsulates the outcome of a reservation-related request,
 * including whether it succeeded, an explanatory message, and an optional confirmation code.
 * </p>
 */
public class ReservationResponse implements Serializable {
    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** Indicates whether the reservation request was processed successfully. */
    private boolean success;
    /** Human-readable message describing the result of the request. */
    private String message;
    /** Confirmation code associated with the reservation, if applicable. */
    private String confirmationCode;

    /**
     * Constructs a new reservation response with the given outcome details.
     *
     * @param success {@code true} if the operation succeeded; {@code false} otherwise
     * @param message a human-readable message describing the outcome
     * @param confirmationCode the reservation confirmation code, or {@code null} if not applicable
     */
    // Added: a small response object (optional) for reservation messages.
    public ReservationResponse(boolean success, String message, String confirmationCode) {
        this.success = success;
        this.message = message;
        this.confirmationCode = confirmationCode;
    }

    /**
     * Returns whether the reservation operation succeeded.
     *
     * @return {@code true} if successful; {@code false} otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the message describing the reservation operation result.
     *
     * @return the result message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the reservation confirmation code, if available.
     *
     * @return the confirmation code, or {@code null} if not applicable
     */
    public String getConfirmationCode() {
        return confirmationCode;
    }
}
