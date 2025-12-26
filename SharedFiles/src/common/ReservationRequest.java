package common;

import java.io.Serializable;

public class ReservationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dateTime; // "yyyy-mm-dd HH:mm"
    private int diners;
    private String phone;
    private String email;

    // Added: a simple request class (not mandatory now, but ready if you want typed requests later).
    public ReservationRequest(String dateTime, int diners, String phone, String email) {
        this.dateTime = dateTime;
        this.diners = diners;
        this.phone = phone;
        this.email = email;
    }

    public String getDateTime() {
        return dateTime;
    }

    public int getDiners() {
        return diners;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}
