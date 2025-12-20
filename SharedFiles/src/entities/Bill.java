package entities;

import java.io.Serializable;
import java.time.LocalDate;

public class Bill implements Serializable {

    private static final long serialVersionUID = 1L;

    private int billNumber;
    private double totalAmount;
    private double discountAmount;
    private LocalDate billDate;

    public Bill() {}

    public int getBillNumber() { return billNumber; }
    public double getTotalAmount() { return totalAmount; }
    public double getDiscountAmount() { return discountAmount; }
    public LocalDate getBillDate() { return billDate; }
}
