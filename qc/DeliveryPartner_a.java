// DeliveryPartner_a.java
import java.util.concurrent.atomic.AtomicLong;

public class DeliveryPartner {
    private static AtomicLong idCounter = new AtomicLong(1);

    private long partnerId;
    private String name;
    private PartnerStatus status;
    private Long currentOrderId;
    private int totalDeliveries;
    private double totalRating;
    private int ratingCount;

    public DeliveryPartner(String name) {
        this.partnerId = idCounter.getAndIncrement();
        this.name = name;
        this.status = PartnerStatus.AVAILABLE;
        this.totalDeliveries = 0;
        this.totalRating = 0.0;
        this.ratingCount = 0;
    }

    // Getters and Setters
    public long getPartnerId() { return partnerId; }
    public String getName() { return name; }
    public PartnerStatus getStatus() { return status; }
    public void setStatus(PartnerStatus status) { this.status = status; }
    public Long getCurrentOrderId() { return currentOrderId; }
    public void setCurrentOrderId(Long orderId) { this.currentOrderId = orderId; }
    public int getTotalDeliveries() { return totalDeliveries; }
    public void incrementDeliveries() { this.totalDeliveries++; }

    public void addRating(int rating) {
        totalRating += rating;
        ratingCount++;
    }

    public double getAverageRating() {
        return ratingCount == 0 ? 0.0 : totalRating / ratingCount;
    }

    @Override
    public String toString() {
        return String.format("Partner[ID=%d, Name=%s, Status=%s, CurrentOrder=%s, Deliveries=%d, Rating=%.2f]",
                partnerId, name, status, currentOrderId, totalDeliveries, getAverageRating());
    }
}

enum PartnerStatus {
    AVAILABLE,
    BUSY
}