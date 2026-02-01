package src.service;
import src.models.DeliveryPartner;
import src.enums.PartnerStatus;
import src.models.Order;
import src.enums.OrderStatus;
import src.repository.PartnerRepository;
import src.repository.OrderRepository;
import src.util.FileUtil;
import java.util.List;
import java.util.Optional;

public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private long partnerIdCounter;

    public PartnerService(PartnerRepository partnerRepository,
                          OrderRepository orderRepository,
                          NotificationService notificationService) {
        this.partnerRepository = partnerRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
        this.partnerIdCounter = loadLastPartnerId();
    }

    private long loadLastPartnerId() {
        return FileUtil.loadCounter("partner_counter.txt", 1L);
    }

    private void savePartnerId(long id) {
        FileUtil.saveCounter("partner_counter.txt", id);
    }

    // Onboard new partner
    public DeliveryPartner onboardPartner(String name) {
        return onboardPartner(name, null, null);
    }

    public synchronized DeliveryPartner onboardPartner(String name, String phone,
                                                       String vehicleNumber) {
        long partnerId = ++partnerIdCounter;
        savePartnerId(partnerId);

        DeliveryPartner partner = new DeliveryPartner(partnerId, name);
        partner.setPhone(phone);
        partner.setVehicleNumber(vehicleNumber);

        partnerRepository.save(partner);

        System.out.println("✓ Partner onboarded: " + partner);
        notificationService.logSystemEvent("New partner onboarded: " + name);

        return partner;
    }

    // Pick up order
    public synchronized boolean pickUpOrder(long partnerId, long orderId) {
        Optional<DeliveryPartner> optPartner =
                partnerRepository.findById(String.valueOf(partnerId));
        Optional<Order> optOrder =
                orderRepository.findById(String.valueOf(orderId));

        if (optPartner.isEmpty()) {
            notificationService.logError("Partner not found: " + partnerId);
            return false;
        }

        if (optOrder.isEmpty()) {
            notificationService.logError("Order not found: " + orderId);
            return false;
        }

        DeliveryPartner partner = optPartner.get();
        Order order = optOrder.get();

        // Validation checks
        if (order.getAssignedPartnerId() == null ||
                order.getAssignedPartnerId() != partnerId) {
            System.out.println("✗ Order not assigned to this partner");
            return false;
        }

        if (order.getStatus() != OrderStatus.ASSIGNED) {
            System.out.println("✗ Order cannot be picked up (status: " +
                    order.getStatus() + ")");
            return false;
        }

        if (partner.getStatus() != PartnerStatus.BUSY) {
            System.out.println("✗ Partner status invalid");
            return false;
        }

        // Pick up order
        order.setStatus(OrderStatus.PICKED_UP);
        order.setPickedUpAt(java.time.LocalDateTime.now());
        orderRepository.save(order);

        System.out.println("✓ Order picked up: " + orderId + " by Partner: " + partnerId);
        notificationService.notifyOrderPickedUp(order.getCustomerId(), orderId, partnerId);

        return true;
    }

    // Complete delivery
    public synchronized boolean completeOrder(long partnerId, long orderId) {
        return completeOrder(partnerId, orderId, null);
    }

    public synchronized boolean completeOrder(long partnerId, long orderId, Integer rating) {
        Optional<DeliveryPartner> optPartner =
                partnerRepository.findById(String.valueOf(partnerId));
        Optional<Order> optOrder =
                orderRepository.findById(String.valueOf(orderId));

        if (optPartner.isEmpty() || optOrder.isEmpty()) {
            return false;
        }

        DeliveryPartner partner = optPartner.get();
        Order order = optOrder.get();

        // Validation
        if (order.getStatus() != OrderStatus.PICKED_UP ||
                order.getAssignedPartnerId() != partnerId) {
            System.out.println("✗ Cannot complete order (not picked up by this partner)");
            return false;
        }

        // Complete order
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(java.time.LocalDateTime.now());
        orderRepository.save(order);

        // Update partner
        partner.setStatus(PartnerStatus.AVAILABLE);
        partner.setCurrentOrderId(null);
        partner.incrementDeliveries();

        if (rating != null && rating >= 1 && rating <= 5) {
            partner.addRating(rating);
            System.out.println("✓ Rating added: " + rating + " stars");
        }

        partnerRepository.save(partner);

        System.out.println("✓ Order delivered: " + orderId + " by Partner: " + partnerId);
        notificationService.notifyOrderDelivered(order.getCustomerId(), orderId);

        return true;
    }

    // Get partner status
    public DeliveryPartner getPartnerStatus(long partnerId) {
        return partnerRepository.findById(String.valueOf(partnerId)).orElse(null);
    }

    // Change partner status (online/offline)
    public synchronized boolean updatePartnerStatus(long partnerId, PartnerStatus status) {
        Optional<DeliveryPartner> optPartner =
                partnerRepository.findById(String.valueOf(partnerId));

        if (optPartner.isEmpty()) {
            return false;
        }

        DeliveryPartner partner = optPartner.get();

        // Cannot go offline if busy
        if (status == PartnerStatus.OFFLINE && partner.getStatus() == PartnerStatus.BUSY) {
            System.out.println("✗ Cannot go offline while delivering order");
            return false;
        }

        partner.setStatus(status);
        partnerRepository.save(partner);

        System.out.println("✓ Partner status updated: " + partnerId + " -> " + status);
        return true;
    }

    // Get all partners
    public List<DeliveryPartner> getAllPartners() {
        return partnerRepository.findAll();
    }

    // Get available partners
    public List<DeliveryPartner> getAvailablePartners() {
        return partnerRepository.findAvailablePartners();
    }

    // Dashboard: Top performers
    public void showTopPartners() {
        showTopPartners(10);
    }

    public void showTopPartners(int limit) {

        List<DeliveryPartner> topPartners = partnerRepository.getTopPartners(limit);

        if (topPartners.isEmpty()) {
            System.out.println("No partners found.");
            return;
        }

        System.out.println(String.format("%-5s %-20s %-12s %-10s %-10s", "Rank", "Name", "Deliveries", "Rating", "Status"));
        System.out.println("───────────────────────────────────────");

        int rank = 1;
        for (DeliveryPartner partner : topPartners) {
            System.out.println(String.format("%-5d %-20s %-12d %-10.2f %-10s",
                    rank++,
                    partner.getName(),
                    partner.getTotalDeliveries(),
                    partner.getAverageRating(),
                    partner.getStatus()
            ));
        }

    }
}