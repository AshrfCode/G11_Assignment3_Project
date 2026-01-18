package common;

import java.io.Serializable;
import java.sql.Date;

/**
 * Represents an order/reservation record that can be transferred between client and server.
 * <p>
 * Contains basic order details such as order number, reservation/order dates, guest count,
 * confirmation code, and subscriber association. Includes helper methods for UI table display.
 * </p>
 */
public class Order implements Serializable {

    /**
     * Unique identifier for the order.
     */
    private int orderNumber;

    /**
     * The date associated with the order (e.g., reservation date).
     */
    private Date orderDate;

    /**
     * The number of guests for the order.
     */
    private int numberOfGuests;

    /**
     * Confirmation code associated with the order.
     */
    private int confirmationCode;

    /**
     * Identifier of the subscriber associated with this order.
     */
    private int subscriberId;

    /**
     * The date on which the order was placed/created.
     */
    private Date dateOfPlacingOrder;

    /**
     * Constructs a new {@code Order} instance.
     *
     * @param orderNumber the order number
     * @param orderDate the order date (e.g., reservation date)
     * @param numberOfGuests the number of guests for the order
     * @param confirmationCode the confirmation code for the order
     * @param subscriberId the subscriber identifier associated with the order
     * @param dateOfPlacingOrder the date the order was placed
     */
    public Order(int orderNumber, Date orderDate, int numberOfGuests,
                 int confirmationCode, int subscriberId, Date dateOfPlacingOrder) {

        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.numberOfGuests = numberOfGuests;
        this.confirmationCode = confirmationCode;
        this.subscriberId = subscriberId;
        this.dateOfPlacingOrder = dateOfPlacingOrder;
    }

    /**
     * Returns the order number.
     *
     * @return the order number
     */
    public int getOrderNumber() { return orderNumber; }

    /**
     * Sets the order number.
     *
     * @param orderNumber the new order number
     */
    public void setOrderNumber(int orderNumber) { this.orderNumber = orderNumber; }

    /**
     * Returns the order date.
     *
     * @return the order date, or {@code null} if not set
     */
    public Date getOrderDate() { return orderDate; }

    /**
     * Sets the order date.
     *
     * @param orderDate the new order date
     */
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    /**
     * Returns the number of guests.
     *
     * @return the guest count
     */
    public int getNumberOfGuests() { return numberOfGuests; }

    /**
     * Sets the number of guests.
     *
     * @param numberOfGuests the new guest count
     */
    public void setNumberOfGuests(int numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    /**
     * Returns the confirmation code.
     *
     * @return the confirmation code
     */
    public int getConfirmationCode() { return confirmationCode; }

    /**
     * Sets the confirmation code.
     *
     * @param confirmationCode the new confirmation code
     */
    public void setConfirmationCode(int confirmationCode) { this.confirmationCode = confirmationCode; }

    /**
     * Returns the subscriber identifier.
     *
     * @return the subscriber ID
     */
    public int getSubscriberId() { return subscriberId; }

    /**
     * Sets the subscriber identifier.
     *
     * @param subscriberId the new subscriber ID
     */
    public void setSubscriberId(int subscriberId) { this.subscriberId = subscriberId; }

    /**
     * Returns the date the order was placed.
     *
     * @return the placing date, or {@code null} if not set
     */
    public Date getDateOfPlacingOrder() { return dateOfPlacingOrder; }

    /**
     * Sets the date the order was placed.
     *
     * @param dateOfPlacingOrder the new placing date
     */
    public void setDateOfPlacingOrder(Date dateOfPlacingOrder) { this.dateOfPlacingOrder = dateOfPlacingOrder; }

    // Helper methods for TableView

    /**
     * Returns the order date as a string for UI display.
     *
     * @return the order date string, or an empty string if {@code orderDate} is {@code null}
     */
    public String getOrderDateString() {
        return orderDate != null ? orderDate.toString() : "";
    }

    /**
     * Returns the placing date as a string for UI display.
     *
     * @return the placing date string, or an empty string if {@code dateOfPlacingOrder} is {@code null}
     */
    public String getDateOfPlacingOrderString() {
        return dateOfPlacingOrder != null ? dateOfPlacingOrder.toString() : "";
    }

    /**
     * Returns a string representation of this order for debugging/logging.
     *
     * @return a string describing the order fields
     */
    @Override
    public String toString() {
        return "Order {" +
                "orderNumber=" + orderNumber +
                ", orderDate=" + orderDate +
                ", numberOfGuests=" + numberOfGuests +
                ", confirmationCode=" + confirmationCode +
                ", subscriberId=" + subscriberId +
                ", dateOfPlacingOrder=" + dateOfPlacingOrder +
                '}';
    }
}
