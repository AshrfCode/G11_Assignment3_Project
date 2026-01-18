package entities;

import java.io.Serializable;

/**
 * Represents a representative user in the system.
 * <p>
 * A {@code Representative} extends {@link User} and adds a representative-specific identifier.
 * This class is {@link Serializable} to support persistence and/or transfer between
 * application layers.
 * </p>
 */
public class Representative extends User implements Serializable {

    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** Unique identifier/number assigned to the representative. */
    private String representativeNumber;

    /**
     * Constructs an empty representative instance.
     * <p>
     * Intended for frameworks/serialization tools that require a no-args constructor.
     * </p>
     */
    // Empty constructor
    public Representative() {
        super();
    }

    /**
     * Constructs a representative for creation with the provided user details and representative number.
     * <p>
     * Initializes the base {@link User} fields and sets the role to
     * {@link common.UserRole#REPRESENTATIVE}.
     * </p>
     *
     * @param name the representative's name
     * @param email the representative's email address
     * @param phone the representative's phone number
     * @param password the representative's password
     * @param representativeNumber the representative's unique identifier/number
     */
    // Constructor for creation
    public Representative(String name,
                          String email,
                          String phone,
                          String password,
                          String representativeNumber) {

        super(name, email, phone, password, common.UserRole.REPRESENTATIVE);
        this.representativeNumber = representativeNumber;
    }

    /**
     * Constructs a representative instance based on an existing base user and a representative number.
     * <p>
     * This constructor copies core user properties from the provided {@code baseUser} and attaches
     * the representative-specific identifier, and is typically used when creating objects from
     * database results.
     * </p>
     *
     * @param baseUser the base {@link User} containing common user fields to copy
     * @param representativeNumber the representative's unique identifier/number
     */
    // Constructor for DB fetch
    public Representative(User baseUser,
                          String representativeNumber) {

        this.setId(baseUser.getId());
        this.setName(baseUser.getName());
        this.setEmail(baseUser.getEmail());
        this.setPhone(baseUser.getPhone());
        this.setPassword(baseUser.getPassword());
        this.setRole(baseUser.getRole());
        this.setActive(baseUser.isActive());
        this.setCreatedAt(baseUser.getCreatedAt());

        this.representativeNumber = representativeNumber;
    }

    // -------- Getters & Setters --------

    /**
     * Returns the representative number.
     *
     * @return the representative number
     */
    public String getRepresentativeNumber() {
        return representativeNumber;
    }

    /**
     * Sets the representative number.
     *
     * @param representativeNumber the representative number to set
     */
    public void setRepresentativeNumber(String representativeNumber) {
        this.representativeNumber = representativeNumber;
    }
}
