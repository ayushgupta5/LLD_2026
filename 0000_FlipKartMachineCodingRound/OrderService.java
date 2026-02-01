package service;

import models.Order;
import models.DeliveryPartner;
import enums.OrderStatus;
import enums.PartnerStatus;
import repository.InMemoryRepository;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDateTime;
import java.time.Duration;

// Single Responsibility: Manages order lifecycle and assignment
public class OrderService {
    
    private final InMemoryRepository<Order> orderRepo;
    private final service.PartnerService partnerService;
    private final AtomicLong orderIdCounter;
    
    // Queue for pending orders
    private final BlockingQueue<Long> pendingOrders;
    
    // Background threads
    private final ExecutorService assignmentExecutor;
    private final ScheduledExecutorService autoCancelExecutor;
    
    public OrderService(service.PartnerService partnerService) {
        this.orderRepo = new InMemoryRepository<>();
        this.partnerService = partnerService;
        this.orderIdCounter = new AtomicLong(1000);
        this.pendingOrders = new LinkedBlockingQueue<>();
        this.assignmentExecutor = Executors.newSingleThreadExecutor();
        this.autoCancelExecutor = Executors.newScheduledThreadPool(1);
        
        startAutoAssignment();
    }
    
    public Order createOrder(long customerId, String itemName) {
        long orderId = orderIdCounter.getAndIncrement();
        
        Order order = new Order(orderId, customerId, itemName);
        orderRepo.save(orderId, order);
        
        System.out.println("✓ Order created: " + order);
        
        // Add to queue for auto-assignment
        pendingOrders.offer(orderId);
        
        // Schedule auto-cancel after 30 minutes
        scheduleAutoCancel(orderId);
        
        return order;
    }
    
    public synchronized boolean cancelOrder(long orderId) {
        Order order = orderRepo.findById(orderId);
        
        if (order == null) {
            System.out.println("✗ Order not found");
            return false;
        }
        
        // Cannot cancel if picked up or delivered
        if (order.getStatus() == OrderStatus.PICKED_UP || order.getStatus() == OrderStatus.DELIVERED) {
            System.out.println("✗ Cannot cancel - already " + order.getStatus());
            return false;
        }
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            System.out.println("✗ Already cancelled");
            return false;
        }
        
        // Cancel order
        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(orderId, order);
        
        // Free partner if assigned
        if (order.getAssignedPartnerId() != null) {
            DeliveryPartner partner = partnerService.getPartnerStatus(order.getAssignedPartnerId());
            if (partner != null) {
                partner.setStatus(PartnerStatus.AVAILABLE);
                partner.setCurrentOrderId(null);
                partnerService.getPartnerRepository().save(partner.getPartnerId(), partner);
            }
        }
        
        // Remove from queue
        pendingOrders.remove(orderId);
        
        System.out.println("✓ Order cancelled: " + orderId);
        return true;
    }
    
    public Order getOrderStatus(long orderId) {
        return orderRepo.findById(orderId);
    }
    
    // Auto-assignment background thread
    private void startAutoAssignment() {
        assignmentExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Long orderId = pendingOrders.take();
                    assignOrderToPartner(orderId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    private synchronized void assignOrderToPartner(long orderId) {
        Order order = orderRepo.findById(orderId);
        
        // Check if order still exists and is pending
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
            return;
        }
        
        // Find available partner
        DeliveryPartner availablePartner = partnerService.getAvailablePartners().stream()
            .findFirst()
            .orElse(null);
        
        if (availablePartner != null) {
            // Assign order
            order.setStatus(OrderStatus.ASSIGNED);
            order.setAssignedPartnerId(availablePartner.getPartnerId());
            orderRepo.save(orderId, order);
            
            // Update partner
            availablePartner.setStatus(PartnerStatus.BUSY);
            availablePartner.setCurrentOrderId(orderId);
            partnerService.getPartnerRepository().save(availablePartner.getPartnerId(), availablePartner);
            
            System.out.println("✓ Order assigned: " + orderId + " -> Partner: " + availablePartner.getPartnerId());
        } else {
            // No partner available, put back in queue
            pendingOrders.offer(orderId);
        }
    }
    
    // Auto-cancel after 30 minutes
    private void scheduleAutoCancel(long orderId) {
        autoCancelExecutor.schedule(() -> {
            synchronized (this) {
                Order order = orderRepo.findById(orderId);
                if (order != null && 
                    order.getStatus() != OrderStatus.PICKED_UP && 
                    order.getStatus() != OrderStatus.DELIVERED &&
                    order.getStatus() != OrderStatus.CANCELLED) {
                    
                    Duration timeSinceCreation = Duration.between(order.getCreatedAt(), LocalDateTime.now());
                    if (timeSinceCreation.toMinutes() >= 30) {
                        System.out.println("⚠ Auto-cancelling order " + orderId + " (30 min timeout)");
                        cancelOrder(orderId);
                    }
                }
            }
        }, 30, TimeUnit.MINUTES);
    }
    
    public void shutdown() {
        assignmentExecutor.shutdownNow();
        autoCancelExecutor.shutdownNow();
        try {
            assignmentExecutor.awaitTermination(2, TimeUnit.SECONDS);
            autoCancelExecutor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // Internal method for PartnerService to access order repo
    InMemoryRepository<Order> getOrderRepository() {
        return orderRepo;
    }
}
