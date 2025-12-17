package common;

import java.io.Serializable;

public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String email;
    private final String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
