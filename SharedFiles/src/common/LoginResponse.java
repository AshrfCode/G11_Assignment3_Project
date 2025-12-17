package common;

import java.io.Serializable;

import entities.User;

public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final User user;

    public LoginResponse(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }
}
