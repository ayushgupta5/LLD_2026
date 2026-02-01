package service;

import models.DeliveryPartner;
import models.Order;
import enums.PartnerStatus;
import enums.OrderStatus;
import repository.InMemoryRepository;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

// Single Responsibility: Manages partner operations
public class PartnerService {
    
    private final InMemoryRepository<DeliveryPartner> partnerRepo;
    private final InMemoryRepository<Order> orderRepo;
    private final AtomicLong partnerIdCounter;
    
    public PartnerService(InMemoryRepository<Order> orderRepo) {
        this.partnerRepo = new InMemoryRepository<>();
        this.orderRepo = orderRepo;
        this.partnerIdCounter = new AtomicLong(1);
    }
    
    public synchronized DeliveryPartner onboardPartner(String name, String phone, String vehicleNumber) {
        long id = partnerIdCounter.getAndIncrement();
        
        DeliveryPartner partner = new DeliveryPartner(id, name);
        partner.setPhone(phone);
        partner.setVehicleNumber(vehicleNumber);
        
        partnerRepo.save(id, partner);
        
        System.out.println("✓ Partner onboarded: " + partner);
        return partner;
    }
    
    public synchronized boolean pickUpOrder(long partnerId, long orderId) {
        DeliveryPartner partner = partnerRepo.findById(partnerId);
        Order order = orderRepo.findById(orderId);
        
        if (partner == null) {
            System.out.println("✗ Partner not found: " + partnerId);
            return false;
        }
        
        if (order == null) {
            System.out.println("✗ Order not found: " + orderId);
            return false;
        }
        
        // Check if order is assigned to this partner
        if (order.getAssignedPartnerId() == null || order.getAssignedPartnerId() != partnerId) {
            System.out.println("✗ Order not assigned to this partner");
            return false;
        }
        
        // Check order status
        if (order.getStatus() != OrderStatus.ASSIGNED) {
            System.out.println("✗ Order cannot be picked up (status: " + order.getStatus() + ")");
            return false;
        }
        
        // Pickup order
        order.setStatus(OrderStatus.PICKED_UP);
        order.setPickedUpAt(LocalDateTime.now());
        orderRepo.save(orderId, order);
        
        System.out.println("✓ Order picked up: " + orderId + " by Partner: " + partnerId);
        return true;
    }
    
    public synchronized boolean completeOrder(long partnerId, long orderId, Integer rating) {
        DeliveryPartner partner = partnerRepo.findById(partnerId);
        Order order = orderRepo.findById(orderId);
        
        if (partner == null || order == null) {
            return false;
        }
        
        // Validate order is picked up by this partner
        if (order.getStatus() != OrderStatus.PICKED_UP || order.getAssignedPartnerId() != partnerId) {
            System.out.println("✗ Cannot complete order (not picked up by this partner)");
            return false;
        }
        
        // Complete delivery
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        orderRepo.save(orderId, order);
        
        // Update partner
        partner.setStatus(PartnerStatus.AVAILABLE);
        partner.setCurrentOrderId(null);
        partner.incrementDeliveries();
        
        if (rating != null && rating >= 1 && rating <= 5) {
            partner.addRating(rating);
            System.out.println("✓ Rating added: " + rating + " stars");
        }
        
        partnerRepo.save(partnerId, partner);
        
        System.out.println("✓ Order delivered: " + orderId + " by Partner: " + partnerId);
        return true;
    }
    
    public DeliveryPartner getPartnerStatus(long partnerId) {
        return partnerRepo.findById(partnerId);
    }
    
    public List<DeliveryPartner> getAvailablePartners() {
        return partnerRepo.findAll().stream()
            .filter(p -> p.getStatus() == PartnerStatus.AVAILABLE)
            .collect(Collectors.toList());
    }
    
    public void showTopPartners(int limit) {
        List<DeliveryPartner> topPartners = partnerRepo.findAll().stream()
            .sorted((p1, p2) -> {
                int deliveryCompare = Integer.compare(p2.getTotalDeliveries(), p1.getTotalDeliveries());
                if (deliveryCompare != 0) return deliveryCompare;
                return Double.compare(p2.getAverageRating(), p1.getAverageRating());
            })
            .limit(limit)
            .collect(Collectors.toList());
        
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("       TOP DELIVERY PARTNERS");
        System.out.println("═══════════════════════════════════════");
        
        if (topPartners.isEmpty()) {
            System.out.println("No partners found");
            return;
        }
        
        System.out.printf("%-5s %-20s %-12s %-10s %-10s%n", "Rank", "Name", "Deliveries", "Rating", "Status");
        System.out.println("───────────────────────────────────────");
        
        int rank = 1;
        for (DeliveryPartner p : topPartners) {
            System.out.printf("%-5d %-20s %-12d %-10.2f %-10s%n",
                rank++, p.getName(), p.getTotalDeliveries(), p.getAverageRating(), p.getStatus());
        }
        System.out.println("═══════════════════════════════════════\n");
    }
    
    // Internal method for OrderService to access partner repo
    InMemoryRepository<DeliveryPartner> getPartnerRepository() {
        return partnerRepo;
    }
}
