package common;

import java.io.Serializable;

/**
 * Represents a standard login request containing user credentials.
 * <p>
 * This object is serializable for transmission between client and server.
 * </p>
 */
public class LoginRequest implements Serializable {

    /**
     * Serialization version UID for compatibility across different class versions.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The user's email address.
     */
    private final String email;

    /**
     * The user's password.
     */
    private final String password;

    /**
     * Constructs a new login request with the provided credentials.
     *
     * @param email the user's email address
     * @param password the user's password
     */
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Returns the email address provided for login.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the password provided for login.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }
}
