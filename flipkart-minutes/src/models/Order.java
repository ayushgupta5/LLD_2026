package src.models;

import src.enums.OrderStatus;

import java.time.LocalDateTime;

public class Order {
    private long orderId;
    private long customerId;
    private String itemName;
    private OrderStatus status;
    private Long assignedPartnerId;
    private LocalDateTime createdAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;

    public Order(long orderId, long customerId, String itemName) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.itemName = itemName;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public long getOrderId() {
        return orderId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public String getItemName() {
        return itemName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Long getAssignedPartnerId() {
        return assignedPartnerId;
    }

    public void setAssignedPartnerId(Long partnerId) {
        this.assignedPartnerId = partnerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPickedUpAt() {
        return pickedUpAt;
    }

    public void setPickedUpAt(LocalDateTime time) {
        this.pickedUpAt = time;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime time) {
        this.deliveredAt = time;
    }

    @Override
    public String toString() {
        return String.format("Order[ID=%d, Customer=%d, Item=%s, Status=%s, Partner=%s]",
                orderId, customerId, itemName, status, assignedPartnerId);
    }
}