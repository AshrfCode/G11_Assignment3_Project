package entities;

import java.io.Serializable;
import java.sql.Timestamp;

import common.UserRole;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String email;
    private String phone;
    private String password;
    private UserRole role;
    private boolean active;
    private Timestamp createdAt;
    private String subscriberNumber;
    private String digitalCard;

    // Empty constructor (required)
    public User() {
    }

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    
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

    // ⚠️ Plain password (as per project requirement)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }
    
    public Boolean getActive() {
        return active;
    }


    public void setActive(boolean active) {
        active = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
