package common;

import java.io.Serializable;

/**
 * Represents a reservation creation request payload.
 * <p>
 * This class is a simple {@link Serializable} DTO that holds the minimal information
 * required to request a reservation, and can be sent across the network between
 * client and server.
 * </p>
 */
public class ReservationRequest implements Serializable {
    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** Reservation date-time in the expected format (e.g., "yyyy-mm-dd HH:mm"). */
    private String dateTime; // "yyyy-mm-dd HH:mm"
    /** Number of diners for the requested reservation. */
    private int diners;
    /** Contact phone number associated with the reservation request. */
    private String phone;
    /** Contact email address associated with the reservation request. */
    private String email;

    /**
     * Constructs a new reservation request with the provided details.
     *
     * @param dateTime the reservation date-time in the expected string format
     * @param diners the number of diners for the reservation
     * @param phone the contact phone number
     * @param email the contact email address
     */
    // Added: a simple request class (not mandatory now, but ready if you want typed requests later).
    public ReservationRequest(String dateTime, int diners, String phone, String email) {
        this.dateTime = dateTime;
        this.diners = diners;
        this.phone = phone;
        this.email = email;
    }

    /**
     * Returns the requested reservation date-time string.
     *
     * @return the reservation date-time in string format
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * Returns the number of diners for the reservation.
     *
     * @return the number of diners
     */
    public int getDiners() {
        return diners;
    }

    /**
     * Returns the contact phone number.
     *
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Returns the contact email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }
}
