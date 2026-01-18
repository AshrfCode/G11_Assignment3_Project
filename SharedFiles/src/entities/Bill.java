package entities;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a bill entity containing billing details for a transaction.
 * <p>
 * This class is {@link Serializable} to support transferring bill data between
 * application layers or over the network if needed.
 * </p>
 */
public class Bill implements Serializable {

    /** Serialization version UID for compatibility across different runtime versions. */
    private static final long serialVersionUID = 1L;

    /** Unique bill identifier/number. */
    private int billNumber;
    /** Total amount charged before any discounts are applied. */
    private double totalAmount;
    /** Discount amount applied to the bill. */
    private double discountAmount;
    /** Date the bill was issued. */
    private LocalDate billDate;

    /**
     * Constructs an empty bill instance.
     * <p>
     * Intended for frameworks/serialization tools that require a no-args constructor.
     * </p>
     */
    public Bill() {}

    /**
     * Returns the bill number.
     *
     * @return the bill number
     */
    public int getBillNumber() { return billNumber; }

    /**
     * Returns the total amount for the bill (before discounts).
     *
     * @return the total amount
     */
    public double getTotalAmount() { return totalAmount; }

    /**
     * Returns the discount amount applied to the bill.
     *
     * @return the discount amount
     */
    public double getDiscountAmount() { return discountAmount; }

    /**
     * Returns the date the bill was issued.
     *
     * @return the bill date
     */
    public LocalDate getBillDate() { return billDate; }
}
