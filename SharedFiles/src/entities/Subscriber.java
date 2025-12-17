package entities;

import java.io.Serializable;

public class Subscriber extends User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String subscriberNumber;
    private String digitalCard;

    // Empty constructor (required)
    public Subscriber() {
        super();
    }

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

    public String getSubscriberNumber() {
        return subscriberNumber;
    }

    public void setSubscriberNumber(String subscriberNumber) {
        this.subscriberNumber = subscriberNumber;
    }

    public String getDigitalCard() {
        return digitalCard;
    }

    public void setDigitalCard(String digitalCard) {
        this.digitalCard = digitalCard;
    }

    // -------- Domain methods (stubs for now) --------

    public void viewHistory() {
        // implemented later
    }

    public void checkInUsingTag() {
        // implemented later
    }
}
