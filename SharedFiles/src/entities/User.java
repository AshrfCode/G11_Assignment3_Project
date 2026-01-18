package entities;

import java.io.Serializable;
import java.sql.Timestamp;

import common.UserRole;

/**
 * Represents a system user with authentication, contact, and role information.
 * <p>
 * This class serves as the base user entity for different user types (e.g., subscriber,
 * representative, manager). It is {@link Serializable} to support persistence and/or transfer
 * between application layers.
 * </p>
 */
public class User implements Serializable {

    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** Unique identifier for the user (typically a database primary key). */
    private int id;
    /** Full name of the user. */
    private String name;
    /** Email address of the user. */
    private String email;
    /** Phone number of the user. */
    private String phone;
    /** User password (stored in plain text as per project requirement). */
    private String password;
    /** Role of the user used for authorization and feature access control. */
    private UserRole role;
    /** Indicates whether the user account is active/enabled. */
    private boolean active;
    /** Timestamp indicating when the user was created in the system. */
    private Timestamp createdAt;
    /** Subscriber number associated with the user (when applicable). */
    private String subscriberNumber;
    /** Digital card data associated with the user (when applicable). */
    private String digitalCard;

    /**
     * Constructs an empty user instance.
     * <p>
     * Intended for frameworks/serialization tools that require a no-args constructor.
     * </p>
     */
    // Empty constructor (required)
    public User() {
    }

    /**
     * Constructs a user for creation (without an ID).
     * <p>
     * Sets {@code active} to {@code true} by default.
     * </p>
     *
     * @param name the user's name
     * @param email the user's email address
     * @param phone the user's phone number
     * @param password the user's password
     * @param role the user's role
     */
    // Constructor without id (for creation)
    public User(String name, String email, String phone,
                String password, UserRole role) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
        this.active = true;
    }

    /**
     * Constructs a user instance with all core fields (typically used for DB fetch).
     *
     * @param id the user's unique identifier
     * @param name the user's name
     * @param email the user's email address
     * @param phone the user's phone number
     * @param password the user's password
     * @param role the user's role
     * @param isActive whether the user account is active
     * @param createdAt timestamp when the user was created
     */
    // Full constructor (for DB fetch)
    public User(int id, String name, String email, String phone,
                String password, UserRole role,
                boolean isActive, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
        this.active = isActive;
        this.createdAt = createdAt;
    }

    // -------- Getters & Setters --------

    /**
     * Returns the user's unique identifier.
     *
     * @return the user ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the user's unique identifier.
     *
     * @param id the user ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the user's name.
     *
     * @return the user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the user's email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the user's phone number.
     *
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the user's phone number.
     *
     * @param phone the phone number to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    /**
     * Returns the subscriber number associated with this user, if applicable.
     *
     * @return the subscriber number, or {@code null} if not applicable
     */
    public String getSubscriberNumber() {
        return subscriberNumber;
    }

    /**
     * Sets the subscriber number associated with this user.
     *
     * @param subscriberNumber the subscriber number to set
     */
    public void setSubscriberNumber(String subscriberNumber) {
        this.subscriberNumber = subscriberNumber;
    }

    /**
     * Returns the digital card data associated with this user, if applicable.
     *
     * @return the digital card data, or {@code null} if not applicable
     */
    public String getDigitalCard() {
        return digitalCard;
    }

    /**
     * Sets the digital card data associated with this user.
     *
     * @param digitalCard the digital card data to set
     */
    public void setDigitalCard(String digitalCard) {
        this.digitalCard = digitalCard;
    }

    /**
     * Returns the user's password.
     * <p>
     * Note: This returns the plain-text password as required by the project specification.
     * </p>
     *
     * @return the password
     */
    // ⚠️ Plain password (as per project requirement)
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the user's role.
     *
     * @return the user role
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     *
     * @param role the role to set
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Returns whether the user account is active.
     *
     * @return {@code true} if active; {@code false} otherwise
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Returns whether the user account is active as a boxed {@link Boolean}.
     *
     * @return {@code true} if active; {@code false} otherwise
     */
    public Boolean getActive() {
        return active;
    }


    /**
     * Sets whether the user account is active.
     *
     * @param active {@code true} to mark the account as active; {@code false} otherwise
     */
    public void setActive(boolean active) {
        active = active;
    }

    /**
     * Returns the timestamp when the user was created.
     *
     * @return the creation timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the user was created.
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
