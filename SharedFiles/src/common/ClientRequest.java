package common;

import java.io.Serializable;

public class ClientRequest implements Serializable {
	
	 private static final long serialVersionUID = 1L;

	    // Added reusable command constants for the reservation flow (to avoid typos in command strings).
	    public static final String CMD_GET_AVAILABLE_SLOTS = "GET_AVAILABLE_SLOTS";
	    public static final String CMD_CREATE_RESERVATION  = "CREATE_RESERVATION";
	    public static final String CMD_CANCEL_RESERVATION  = "CANCEL_RESERVATION";
    private String command;
    private Object[] params;

    public ClientRequest(String command, Object[] params) {
        this.command = command;
        this.params = params;
    }

    public String getCommand() {
        return command;
    }

    public Object[] getParams() {
        return params;
    }
}


