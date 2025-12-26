package common;

import java.io.Serializable;

public class ReservationResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private String confirmationCode;

    // Added: a small response object (optional) for reservation messages.
    public ReservationResponse(boolean success, String message, String confirmationCode) {
        this.success = success;
        this.message = message;
        this.confirmationCode = confirmationCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }
}
