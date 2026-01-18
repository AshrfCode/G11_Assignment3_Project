package entities;

import java.io.Serializable;

/**
 * Represents a subscriber user in the system.
 * <p>
 * A {@code Subscriber} extends {@link User} and adds subscriber-specific information such as
 * a subscriber number and a digital card representation. This class is {@link Serializable}
 * to support persistence and/or transfer between application layers.
 * </p>
 */
public class Subscriber extends User implements Serializable {

    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** Unique identifier/number assigned to the subscriber. */
    private String subscriberNumber;
    /** Digital card data associated with the subscriber (e.g., token, encoded value, or reference). */
    private String digitalCard;

    /**
     * Constructs an empty subscriber instance.
     * <p>
     * Intended for frameworks/serialization tools that require a no-args constructor.
     * </p>
     */
    // Empty constructor (required)
    public Subscriber() {
        super();
    }

    /**
     * Constructs a subscriber for creation with the provided user details and subscriber-specific fields.
     * <p>
     * Initializes the base {@link User} fields and sets the role to
     * {@link common.UserRole#SUBSCRIBER}.
     * </p>
     *
     * @param name the subscriber's name
     * @param email the subscriber's email address
     * @param phone the subscriber's phone number
     * @param password the subscriber's password
     * @param subscriberNumber the subscriber's unique identifier/number
     * @param digitalCard the subscriber's digital card data
     */
    // Constructor for creation (without DB id)
    public Subscriber(String name,
                      String email,
                      String phone,
                      String password,
                      String subscriberNumber,
                      String digitalCard) {

        super(name, email, phone, password, common.UserRole.SUBSCRIBER);
        this.subscriberNumber = subscriberNumber;
        this.digitalCard = digitalCard;
    }

    /**
     * Constructs a subscriber instance based on an existing base user and subscriber-specific fields.
     * <p>
     * This constructor copies core user properties from the provided {@code baseUser} and attaches
     * subscriber-specific data, and is typically used when creating objects from database results.
     * </p>
     *
     * @param baseUser the base {@link User} containing common user fields to copy
     * @param subscriberNumber the subscriber's unique identifier/number
     * @param digitalCard the subscriber's digital card data
     */
    // Constructor for DB fetch
    public Subscriber(User baseUser,
                      String subscriberNumber,
                      String digitalCard) {

        // Copy base user data
        this.setId(baseUser.getId());
        this.setName(baseUser.getName());
        this.setEmail(baseUser.getEmail());
        this.setPhone(baseUser.getPhone());
        this.setPassword(baseUser.getPassword());
        this.setRole(baseUser.getRole());
        this.setActive(baseUser.isActive());
        this.setCreatedAt(baseUser.getCreatedAt());

        this.subscriberNumber = subscriberNumber;
        this.digitalCard = digitalCard;
    }

    // -------- Getters & Setters --------

    /**
     * Returns the subscriber number.
     *
     * @return the subscriber number
     */
    public String getSubscriberNumber() {
        return subscriberNumber;
    }

    /**
     * Sets the subscriber number.
     *
     * @param subscriberNumber the subscriber number to set
     */
    public void setSubscriberNumber(String subscriberNumber) {
        this.subscriberNumber = subscriberNumber;
    }

    /**
     * Returns the digital card data associated with the subscriber.
     *
     * @return the digital card data
     */
    public String getDigitalCard() {
        return digitalCard;
    }

    /**
     * Sets the digital card data associated with the subscriber.
     *
     * @param digitalCard the digital card data to set
     */
    public void setDigitalCard(String digitalCard) {
        this.digitalCard = digitalCard;
    }

    // -------- Domain methods (stubs for now) --------

    /**
     * Displays or retrieves the subscriber's reservation/visit history.
     * <p>
     * This method is currently a stub and is intended to be implemented later.
     * </p>
     */
    public void viewHistory() {
        // implemented later
    }

    /**
     * Performs a check-in action using a tag or identifier associated with the subscriber.
     * <p>
     * This method is currently a stub and is intended to be implemented later.
     * </p>
     */
    public void checkInUsingTag() {
        // implemented later
    }
}
