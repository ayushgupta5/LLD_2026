package src.service;

import src.enums.OrderStatus;
import src.enums.PartnerStatus;
import src.models.*;
import src.repository.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class OrderService {

    private final OrderRepository orderRepository;
    private final PartnerRepository partnerRepository;
    private final NotificationService notificationService;

    private final AtomicLong orderIdCounter;
    private final BlockingQueue<Long> pendingOrders;
    private final ExecutorService assignmentExecutor;

    public OrderService(OrderRepository orderRepository,
                        PartnerRepository partnerRepository,
                        NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.partnerRepository = partnerRepository;
        this.notificationService = notificationService;

        this.orderIdCounter = new AtomicLong(loadLastOrderId());
        this.pendingOrders = new LinkedBlockingQueue<>();
        this.assignmentExecutor = Executors.newSingleThreadExecutor();

        startAutoAssignment();
    }

    private long loadLastOrderId() {
        try {
            String content = Files.readString(Paths.get("data/metadata/order_counter.txt"));
            return Long.parseLong(content.trim());
        } catch (Exception e) {
            return 1000;
        }
    }

    private void saveOrderId(long id) {
        try {
            Files.createDirectories(Paths.get("data/metadata"));
            Files.writeString(Paths.get("data/metadata/order_counter.txt"),
                    String.valueOf(id));
        } catch (IOException e) {

        }
    }

    public Order createOrder(long customerId, String itemName) {
        long orderId = orderIdCounter.incrementAndGet();
        saveOrderId(orderId);

        Order order = new Order(orderId, customerId, itemName);
        orderRepository.save(order);

        pendingOrders.offer(orderId);
        notificationService.notifyCustomer(customerId, "Order created: " + orderId);

        return order;
    }

    public boolean cancelOrder(long orderId) {
        Optional<Order> optOrder = orderRepository.findById(String.valueOf(orderId));

        if (optOrder.isEmpty()) {
            return false;
        }

        Order order = optOrder.get();

        if (order.getStatus() == OrderStatus.PICKED_UP ||
                order.getStatus() == OrderStatus.DELIVERED) {
            return false;
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        if (order.getAssignedPartnerId() != null) {
            partnerRepository.findById(String.valueOf(order.getAssignedPartnerId()))
                    .ifPresent(partner -> {
                        partner.setStatus(PartnerStatus.AVAILABLE);
                        partner.setCurrentOrderId(null);
                        partnerRepository.save(partner);
                    });
        }

        pendingOrders.remove(orderId);
        notificationService.notifyCustomer(order.getCustomerId(),
                "Order cancelled: " + orderId);

        return true;
    }

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
        Optional<Order> optOrder = orderRepository.findById(String.valueOf(orderId));

        if (optOrder.isEmpty() ||
                optOrder.get().getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        Order order = optOrder.get();

        // Find available partner
        Optional<DeliveryPartner> availablePartner = partnerRepository.findAll()
                .stream()
                .filter(p -> p.getStatus() == PartnerStatus.AVAILABLE)
                .findFirst();

        if (availablePartner.isPresent()) {
            DeliveryPartner partner = availablePartner.get();

            order.setStatus(OrderStatus.ASSIGNED);
            order.setAssignedPartnerId(partner.getPartnerId());
            orderRepository.save(order);

            partner.setStatus(PartnerStatus.BUSY);
            partner.setCurrentOrderId(orderId);
            partnerRepository.save(partner);

            notificationService.notifyPartner(partner.getPartnerId(),
                    "New order assigned: " + orderId);
        } else {
            // Put back in queue
            pendingOrders.offer(orderId);
        }
    }

    public Order getOrderStatus(long orderId) {
        return orderRepository.findById(String.valueOf(orderId)).orElse(null);
    }

    public void shutdown() {
        saveOrderId(orderIdCounter.get());
        assignmentExecutor.shutdownNow();
        try {
            if (!assignmentExecutor.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS)) {
                assignmentExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}