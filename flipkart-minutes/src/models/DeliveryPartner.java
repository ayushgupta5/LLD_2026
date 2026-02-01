package src.models;

import src.enums.PartnerStatus;

public class DeliveryPartner {
    private long partnerId;
    private String name;
    private String phone;
    private String vehicleNumber;
    private PartnerStatus status;
    private Long currentOrderId;
    private int totalDeliveries;
    private double totalRating;
    private int ratingCount;

    public DeliveryPartner(long partnerId, String name) {
        this.partnerId = partnerId;
        this.name = name;
        this.status = PartnerStatus.AVAILABLE;
        this.totalDeliveries = 0;
        this.totalRating = 0.0;
        this.ratingCount = 0;
    }

    public long getPartnerId() {
        return partnerId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public PartnerStatus getStatus() {
        return status;
    }

    public void setStatus(PartnerStatus status) {
        this.status = status;
    }

    public Long getCurrentOrderId() {
        return currentOrderId;
    }

    public void setCurrentOrderId(Long orderId) {
        this.currentOrderId = orderId;
    }

    public int getTotalDeliveries() {
        return totalDeliveries;
    }

    public void incrementDeliveries() {
        this.totalDeliveries++;
    }

    public void addRating(int rating) {
        if (rating >= 1 && rating <= 5) {
            this.totalRating += rating;
            this.ratingCount++;
        }
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public double getAverageRating() {
        return ratingCount == 0 ? 0.0 : totalRating / ratingCount;
    }

    @Override
    public String toString() {
        return String.format("Partner[ID=%d, Name=%s, Status=%s, Deliveries=%d, Rating=%.2f]",
                partnerId, name, status, totalDeliveries, getAverageRating());
    }
}