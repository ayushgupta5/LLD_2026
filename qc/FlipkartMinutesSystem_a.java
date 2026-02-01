// FlipkartMinutesSystem.java
import java.util.*;
import java.util.concurrent.*;
import java.time.*;

public class FlipkartMinutesSystem_a {

    // Thread-safe collections
    private final Map<Long, Customer_a> customers = new ConcurrentHashMap<>();
    private final Map<Long, DeliveryPartner> partners = new ConcurrentHashMap<>();
    private final Map<Long, Order> orders = new ConcurrentHashMap<>();

    // Queue for pending orders
    private final BlockingQueue<Long> pendingOrders = new LinkedBlockingQueue<>();

    // Thread pool for auto-assignment
    private final ExecutorService assignmentExecutor = Executors.newSingleThreadExecutor();

    // Thread pool for auto-cancel (bonus feature)
    private final ScheduledExecutorService autoCancelExecutor = Executors.newScheduledThreadPool(2);

    public FlipkartMinutesSystem_a() {
        // Start auto-assignment thread
        startAutoAssignment();
    }

    // 1. ONBOARDING

    public synchronized Customer_a onboardCustomer(String name) {
        Customer_a customer = new Customer_a(name);
        customers.put(customer.getCustomerId(), customer);
        System.out.println("✓ Customer onboarded: " + customer);
        return customer;
    }

    public synchronized DeliveryPartner onboardDeliveryPartner(String name) {
        DeliveryPartner partner = new DeliveryPartner(name);
        partners.put(partner.getPartnerId(), partner);
        System.out.println("✓ Delivery Partner onboarded: " + partner);
        return partner;
    }

    // 2. ORDER PLACEMENT & CANCELLATION

    public Order createOrder(long customerId, String itemName) {
        if (!customers.containsKey(customerId)) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }

        Order order = new Order(customerId, itemName);
        orders.put(order.getOrderId(), order);

        // Add to pending queue
        pendingOrders.offer(order.getOrderId());

        System.out.println("✓ Order created: " + order);

        // Schedule auto-cancel after 30 minutes (bonus feature)
        scheduleAutoCancel(order.getOrderId());

        return order;
    }

    public synchronized boolean cancelOrder(long orderId) {
        Order order = orders.get(orderId);

        if (order == null) {
            System.out.println("✗ Order not found: " + orderId);
            return false;
        }

        // Cannot cancel if already picked up or delivered
        if (order.getStatus() == OrderStatus.PICKED_UP ||
                order.getStatus() == OrderStatus.DELIVERED) {
            System.out.println("✗ Cannot cancel order (already picked up/delivered): " + orderId);
            return false;
        }

        // Cannot cancel if already cancelled
        if (order.getStatus() == OrderStatus.CANCELLED) {
            System.out.println("✗ Order already cancelled: " + orderId);
            return false;
        }

        // Cancel the order
        order.setStatus(OrderStatus.CANCELLED);

        // If assigned to a partner, free the partner
        if (order.getAssignedPartnerId() != null) {
            DeliveryPartner partner = partners.get(order.getAssignedPartnerId());
            if (partner != null) {
                partner.setStatus(PartnerStatus.AVAILABLE);
                partner.setCurrentOrderId(null);
                System.out.println("✓ Partner freed: " + partner.getPartnerId());
            }
        }

        // Remove from pending queue if present
        pendingOrders.remove(orderId);

        System.out.println("✓ Order cancelled: " + orderId);
        notifyCustomer(order.getCustomerId(), "Order " + orderId + " has been cancelled");

        return true;
    }

    // 3. ORDER ASSIGNMENT & FULFILLMENT

    private void startAutoAssignment() {
        assignmentExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Take order from queue (blocking)
                    Long orderId = pendingOrders.take();

                    // Try to assign
                    assignOrderToPartner(orderId);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private synchronized void assignOrderToPartner(long orderId) {
        Order order = orders.get(orderId);

        // Skip if order is cancelled
        if (order == null || order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        // Find available partner
        DeliveryPartner availablePartner = null;
        for (DeliveryPartner partner : partners.values()) {
            if (partner.getStatus() == PartnerStatus.AVAILABLE) {
                availablePartner = partner;
                break;
            }
        }

        if (availablePartner != null) {
            // Assign order
            order.setStatus(OrderStatus.ASSIGNED);
            order.setAssignedPartnerId(availablePartner.getPartnerId());

            availablePartner.setStatus(PartnerStatus.BUSY);
            availablePartner.setCurrentOrderId(orderId);

            System.out.println("✓ Order assigned: " + orderId + " -> Partner: " + availablePartner.getPartnerId());
            notifyPartner(availablePartner.getPartnerId(), "New order assigned: " + orderId);
            notifyCustomer(order.getCustomerId(), "Order " + orderId + " assigned to partner");
        } else {
            // No partner available, put back in queue
            pendingOrders.offer(orderId);
        }
    }

    public synchronized boolean pickUpOrder(long partnerId, long orderId) {
        DeliveryPartner partner = partners.get(partnerId);
        Order order = orders.get(orderId);

        if (partner == null) {
            System.out.println("✗ Partner not found: " + partnerId);
            return false;
        }

        if (order == null) {
            System.out.println("✗ Order not found: " + orderId);
            return false;
        }

        // Check if order is assigned to this partner
        if (order.getAssignedPartnerId() == null ||
                order.getAssignedPartnerId() != partnerId) {
            System.out.println("✗ Order not assigned to this partner");
            return false;
        }

        // Check if order is in correct status
        if (order.getStatus() != OrderStatus.ASSIGNED) {
            System.out.println("✗ Order cannot be picked up (status: " + order.getStatus() + ")");
            return false;
        }

        // Pick up order
        order.setStatus(OrderStatus.PICKED_UP);
        order.setPickedUpAt(LocalDateTime.now());

        System.out.println("✓ Order picked up: " + orderId + " by Partner: " + partnerId);
        notifyCustomer(order.getCustomerId(), "Order " + orderId + " picked up by delivery partner");

        return true;
    }

    public synchronized boolean completeOrder(long partnerId, long orderId, Integer rating) {
        DeliveryPartner partner = partners.get(partnerId);
        Order order = orders.get(orderId);

        if (partner == null) {
            System.out.println("✗ Partner not found: " + partnerId);
            return false;
        }

        if (order == null) {
            System.out.println("✗ Order not found: " + orderId);
            return false;
        }

        // Check if order is picked up by this partner
        if (order.getStatus() != OrderStatus.PICKED_UP ||
                order.getAssignedPartnerId() != partnerId) {
            System.out.println("✗ Cannot complete order (not picked up by this partner)");
            return false;
        }

        // Complete order
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());

        // Free partner
        partner.setStatus(PartnerStatus.AVAILABLE);
        partner.setCurrentOrderId(null);
        partner.incrementDeliveries();

        // Add rating if provided (bonus feature)
        if (rating != null && rating >= 1 && rating <= 5) {
            partner.addRating(rating);
            System.out.println("✓ Rating added: " + rating + " stars");
        }

        System.out.println("✓ Order delivered: " + orderId + " by Partner: " + partnerId);
        notifyCustomer(order.getCustomerId(), "Order " + orderId + " delivered successfully");

        return true;
    }

    // 4. STATUS TRACKING

    public String getOrderStatus(long orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            return "Order not found: " + orderId;
        }
        return order.toString();
    }

    public String getPartnerStatus(long partnerId) {
        DeliveryPartner partner = partners.get(partnerId);
        if (partner == null) {
            return "Partner not found: " + partnerId;
        }
        return partner.toString();
    }

    // BONUS FEATURES

    // Notifications
    private void notifyCustomer(long customerId, String message) {
        System.out.println("📱 [NOTIFY Customer " + customerId + "]: " + message);
    }

    private void notifyPartner(long partnerId, String message) {
        System.out.println("📱 [NOTIFY Partner " + partnerId + "]: " + message);
    }

    // Auto-cancel after 30 minutes
    private void scheduleAutoCancel(long orderId) {
        autoCancelExecutor.schedule(() -> {
            Order order = orders.get(orderId);
            if (order != null && order.getStatus() != OrderStatus.PICKED_UP &&
                    order.getStatus() != OrderStatus.DELIVERED &&
                    order.getStatus() != OrderStatus.CANCELLED) {

                System.out.println("⏰ Auto-cancelling order (30 min timeout): " + orderId);
                cancelOrder(orderId);
            }
        }, 30, TimeUnit.MINUTES);
    }

    // Dashboard - Top partners
    public void showTopPartners() {
        System.out.println("\n═══ TOP DELIVERY PARTNERS ═══");

        List<DeliveryPartner> sortedPartners = new ArrayList<>(partners.values());
        sortedPartners.sort((p1, p2) -> {
            // Sort by deliveries, then by rating
            int deliveryCompare = Integer.compare(p2.getTotalDeliveries(), p1.getTotalDeliveries());
            if (deliveryCompare != 0) return deliveryCompare;
            return Double.compare(p2.getAverageRating(), p1.getAverageRating());
        });

        int rank = 1;
        for (DeliveryPartner partner : sortedPartners) {
            System.out.printf("%d. %s - %d deliveries, %.2f★\n",
                    rank++, partner.getName(), partner.getTotalDeliveries(), partner.getAverageRating());
        }
    }

    // Shutdown
    public void shutdown() {
        assignmentExecutor.shutdownNow();
        autoCancelExecutor.shutdownNow();
    }
}