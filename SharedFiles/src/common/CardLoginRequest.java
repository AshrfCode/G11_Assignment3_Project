package common;

import java.io.Serializable;

/**
 * Represents a login request initiated using a digital card/code (e.g., tag reader input).
 * <p>
 * This object is serializable for transmission between client and server.
 * </p>
 */
public class CardLoginRequest implements Serializable {

    /**
     * Serialization version UID for compatibility across different class versions.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The digital card/code used to identify and authenticate a subscriber.
     */
    private final String digitalCode;

    /**
     * Constructs a new card-based login request.
     *
     * @param digitalCode the digital card/code presented for authentication
     */
    public CardLoginRequest(String digitalCode) {
        this.digitalCode = digitalCode;
    }

    /**
     * Returns the digital card/code provided for authentication.
     *
     * @return the digital code
     */
    public String getDigitalCode() {
        return digitalCode;
    }
}
