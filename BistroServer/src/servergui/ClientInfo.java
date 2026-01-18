package servergui;

/**
 * Represents basic information about a connected client in the server GUI.
 * <p>
 * Stores network identity (IP/host), connection status, and an internal client identifier.
 * </p>
 */
public class ClientInfo {

    /**
     * The client's IP address.
     */
    private String ip;

    /**
     * The client's host name (or resolved host string).
     */
    private String host;

    /**
     * The client's current connection status (e.g., connected/disconnected).
     */
    private String status;

    /**
     * An internal identifier for the client.
     */
    private int id;

    /**
     * Creates a new {@code ClientInfo} instance.
     *
     * @param ip the client's IP address
     * @param host the client's host name
     * @param status the client's current status
     * @param id the client's internal identifier
     */
    public ClientInfo(String ip, String host, String status, int id) {
        this.ip = ip;
        this.host = host;
        this.status = status;
        this.id = id;
    }

    /**
     * Returns the client's IP address.
     *
     * @return the IP address
     */
    public String getIp() { 
    	return ip; 
    }
    
    /**
     * Returns the client's host name.
     *
     * @return the host name
     */
    public String getHost() { 
    	return host; 
    }
    
    /**
     * Returns the client's current status.
     *
     * @return the status string
     */
    public String getStatus() { 
    	return status; 
    }
    
    /**
     * Returns the client's internal identifier.
     *
     * @return the client ID
     */
    public int getId() { 
    	return id; 
    }

    /**
     * Updates the client's status.
     *
     * @param status the new status string
     */
    public void setStatus(String status) {
    	this.status = status;
    }
}
