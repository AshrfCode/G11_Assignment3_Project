package entities;

import java.io.Serializable;

public class Representative extends User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String representativeNumber;

    // Empty constructor
    public Representative() {
        super();
    }

    // Constructor for creation
    public Representative(String name,
                          String email,
                          String phone,
                          String password,
                          String representativeNumber) {

        super(name, email, phone, password, common.UserRole.REPRESENTATIVE);
        this.representativeNumber = representativeNumber;
    }

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

    public String getRepresentativeNumber() {
        return representativeNumber;
    }

    public void setRepresentativeNumber(String representativeNumber) {
        this.representativeNumber = representativeNumber;
    }
}
