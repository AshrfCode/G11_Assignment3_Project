package common;

import java.io.Serializable;

public class ClientRequest implements Serializable {

	
	    // Added reusable command constants for the reservation flow (to avoid typos in command strings).
	    public static final String CMD_GET_AVAILABLE_SLOTS = "GET_AVAILABLE_SLOTS";
	    public static final String CMD_CREATE_RESERVATION  = "CREATE_RESERVATION";
	    public static final String CMD_CANCEL_RESERVATION  = "CANCEL_RESERVATION";
	    public static final String CMD_JOIN_WAITING_LIST = "JOIN_WAITING_LIST";
	    public static final String CMD_JOIN_WAITING_LIST_GUEST = "JOIN_WAITING_LIST_GUEST";
	    public static final String CMD_LEAVE_WAITING_LIST_GUEST = "LEAVE_WAITING_LIST_GUEST";

	    public static final String CMD_GET_WAITING_LIST         = "GET_WAITING_LIST";
	    public static final String CMD_LEAVE_WAITING_LIST = "LEAVE_WAITING_LIST";

	    public static final String CMD_GET_OPENING_HOURS        = "GET_OPENING_HOURS";
	    public static final String CMD_UPDATE_OPENING_HOURS     = "UPDATE_OPENING_HOURS";

	    public static final String CMD_GET_SPECIAL_OPENING      = "GET_SPECIAL_OPENING";
	    public static final String CMD_UPSERT_SPECIAL_OPENING   = "UPSERT_SPECIAL_OPENING";
	    public static final String CMD_DELETE_SPECIAL_OPENING   = "DELETE_SPECIAL_OPENING";

	    public static final String CMD_GET_TODAY_RESERVATIONS   = "GET_TODAY_RESERVATIONS";

	     public static final String CMD_GET_ALL_SUBSCRIBERS = "GET_ALL_SUBSCRIBERS";
	     public static final String CMD_PAY_RESERVATION = "PAY_RESERVATION";
	     public static final String CMD_PREVIEW_BILL = "PREVIEW_BILL";
	     public static final String CMD_GET_SUBSCRIBER_HISTORY = "GET_SUBSCRIBER_HISTORY";
	     public static final String CMD_CHECK_IN = "CHECK_IN";
	     public static final String CMD_FORGOT_CONFIRMATION_CODE = "FORGOT_CONFIRMATION_CODE";
	     public static final String CMD_UPDATE_SUBSCRIBER_DETAILS = "UPDATE_SUBSCRIBER_DETAILS";
	     public static final String CMD_GET_MONTHLY_TIME_REPORT = "GET_MONTHLY_TIME_REPORT";
	     public static final String CMD_GET_SUBSCRIBER_REPORT = "GET_SUBSCRIBER_REPORT";
	     public static final String CMD_GET_ALL_RESERVATIONS = "GET_ALL_RESERVATIONS";


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