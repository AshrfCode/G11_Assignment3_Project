package common;

import java.io.Serializable;

/**
 * Represents a generic request sent from a client to the server.
 * <p>
 * A request consists of a command string (typically one of the {@code CMD_*} constants)
 * and an optional array of parameters associated with the command.
 * </p>
 */
public class ClientRequest implements Serializable {

	
	    /**
	     * Command to retrieve available reservation slots.
	     */
	    // Added reusable command constants for the reservation flow (to avoid typos in command strings).
	    public static final String CMD_GET_AVAILABLE_SLOTS = "GET_AVAILABLE_SLOTS";

	    /**
	     * Command to create a new reservation.
	     */
	    public static final String CMD_CREATE_RESERVATION  = "CREATE_RESERVATION";

	    /**
	     * Command to cancel an existing reservation.
	     */
	    public static final String CMD_CANCEL_RESERVATION  = "CANCEL_RESERVATION";

	    /**
	     * Command for a subscriber to join the waiting list.
	     */
	    public static final String CMD_JOIN_WAITING_LIST = "JOIN_WAITING_LIST";

	    /**
	     * Command for a guest to join the waiting list.
	     */
	    public static final String CMD_JOIN_WAITING_LIST_GUEST = "JOIN_WAITING_LIST_GUEST";

	    /**
	     * Command for a guest to leave the waiting list.
	     */
	    public static final String CMD_LEAVE_WAITING_LIST_GUEST = "LEAVE_WAITING_LIST_GUEST";

	    /**
	     * Command to retrieve the current waiting list.
	     */
	    public static final String CMD_GET_WAITING_LIST         = "GET_WAITING_LIST";

	    /**
	     * Command for a subscriber to leave the waiting list.
	     */
	    public static final String CMD_LEAVE_WAITING_LIST = "LEAVE_WAITING_LIST";

	    /**
	     * Command to retrieve regular opening hours.
	     */
	    public static final String CMD_GET_OPENING_HOURS        = "GET_OPENING_HOURS";

	    /**
	     * Command to update regular opening hours.
	     */
	    public static final String CMD_UPDATE_OPENING_HOURS     = "UPDATE_OPENING_HOURS";

	    /**
	     * Command to retrieve special opening hours for a specific date.
	     */
	    public static final String CMD_GET_SPECIAL_OPENING      = "GET_SPECIAL_OPENING";

	    /**
	     * Command to insert or update special opening hours for a specific date.
	     */
	    public static final String CMD_UPSERT_SPECIAL_OPENING   = "UPSERT_SPECIAL_OPENING";

	    /**
	     * Command to delete special opening hours for a specific date.
	     */
	    public static final String CMD_DELETE_SPECIAL_OPENING   = "DELETE_SPECIAL_OPENING";

	    /**
	     * Command to retrieve today's reservations.
	     */
	    public static final String CMD_GET_TODAY_RESERVATIONS   = "GET_TODAY_RESERVATIONS";

	    /**
	     * Command to retrieve all subscribers (typically for administrative views).
	     */
	     public static final String CMD_GET_ALL_SUBSCRIBERS = "GET_ALL_SUBSCRIBERS";

	     /**
	      * Command to pay for a reservation.
	      */
	     public static final String CMD_PAY_RESERVATION = "PAY_RESERVATION";

	     /**
	      * Command to preview a bill before payment/commit.
	      */
	     public static final String CMD_PREVIEW_BILL = "PREVIEW_BILL";

	     /**
	      * Command to retrieve a subscriber's reservation/payment history.
	      */
	     public static final String CMD_GET_SUBSCRIBER_HISTORY = "GET_SUBSCRIBER_HISTORY";

	     /**
	      * Command to perform a reservation check-in using a confirmation code.
	      */
	     public static final String CMD_CHECK_IN = "CHECK_IN";

	     /**
	      * Command to request a forgotten reservation confirmation code flow.
	      */
	     public static final String CMD_FORGOT_CONFIRMATION_CODE = "FORGOT_CONFIRMATION_CODE";

	     /**
	      * Command to update subscriber contact/details.
	      */
	     public static final String CMD_UPDATE_SUBSCRIBER_DETAILS = "UPDATE_SUBSCRIBER_DETAILS";

	     /**
	      * Command to retrieve a monthly time report.
	      */
	     public static final String CMD_GET_MONTHLY_TIME_REPORT = "GET_MONTHLY_TIME_REPORT";

	     /**
	      * Command to retrieve a subscriber report.
	      */
	     public static final String CMD_GET_SUBSCRIBER_REPORT = "GET_SUBSCRIBER_REPORT";

	     /**
	      * Command to retrieve all reservations.
	      */
	     public static final String CMD_GET_ALL_RESERVATIONS = "GET_ALL_RESERVATIONS";

	     /**
	      * Command to retrieve subscriber codes (e.g., subscriber number / digital card codes).
	      */
	     public static final String CMD_GET_SUBSCRIBER_CODES = "GET_SUBSCRIBER_CODES";


    /**
     * The command name that identifies the server-side action to execute.
     */
    private String command;

    /**
     * Optional parameters associated with the command.
     */
    private Object[] params;

    /**
     * Creates a new client request.
     *
     * @param command the command to execute (typically one of the {@code CMD_*} constants)
     * @param params the parameters required by the command (may be {@code null} or empty)
     */
    public ClientRequest(String command, Object[] params) {
        this.command = command;
        this.params = params;
    }

    /**
     * Returns the command name for this request.
     *
     * @return the command string
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns the parameter array associated with this request.
     *
     * @return the parameters for the command (may be {@code null})
     */
    public Object[] getParams() {
        return params;
    }
}
