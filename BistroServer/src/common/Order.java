package common;

import java.io.Serializable;
import java.sql.Date;

public class Order implements Serializable {

    private int orderNumber;
    private Date orderDate;
    private int numberOfGuests;
    private int confirmationCode;
    private int subscriberId;
    private Date dateOfPlacingOrder;

    public Order(int orderNumber, Date orderDate, int numberOfGuests,
                 int confirmationCode, int subscriberId, Date dateOfPlacingOrder) {

        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.numberOfGuests = numberOfGuests;
        this.confirmationCode = confirmationCode;
        this.subscriberId = subscriberId;
        this.dateOfPlacingOrder = dateOfPlacingOrder;
    }

    public int getOrderNumber() { return orderNumber; }
    public void setOrderNumber(int orderNumber) { this.orderNumber = orderNumber; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public int getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(int numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    public int getConfirmationCode() { return confirmationCode; }
    public void setConfirmationCode(int confirmationCode) { this.confirmationCode = confirmationCode; }

    public int getSubscriberId() { return subscriberId; }
    public void setSubscriberId(int subscriberId) { this.subscriberId = subscriberId; }

    public Date getDateOfPlacingOrder() { return dateOfPlacingOrder; }
    public void setDateOfPlacingOrder(Date dateOfPlacingOrder) { this.dateOfPlacingOrder = dateOfPlacingOrder; }

    // Helper methods for TableView
    public String getOrderDateString() {
        return orderDate != null ? orderDate.toString() : "";
    }

    public String getDateOfPlacingOrderString() {
        return dateOfPlacingOrder != null ? dateOfPlacingOrder.toString() : "";
    }

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
