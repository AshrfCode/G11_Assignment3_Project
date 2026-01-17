package common;

import java.io.Serializable;

public class CardLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String digitalCode;

    public CardLoginRequest(String digitalCode) {
        this.digitalCode = digitalCode;
    }

    public String getDigitalCode() {
        return digitalCode;
    }
}