package src.service;

import src.repository.CustomerRepository;
import src.repository.PartnerRepository;

import java.time.format.DateTimeFormatter;

import src.models.Customer;
import src.models.DeliveryPartner;

import java.time.LocalDateTime;


public class NotificationService {

    private final CustomerRepository customerRepository;
    private final PartnerRepository partnerRepository;
    private final DateTimeFormatter timeFormatter;

    public NotificationService(CustomerRepository customerRepository,
                               PartnerRepository partnerRepository) {
        this.customerRepository = customerRepository;
        this.partnerRepository = partnerRepository;
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    }

    // Simple constructor for basic usage
    public NotificationService() {
        this.customerRepository = null;
        this.partnerRepository = null;
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    }

    public void notifyCustomer(long customerId, String message) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String customerName = getCustomerName(customerId);

        System.out.println(String.format(
                "[%s] CUSTOMER NOTIFICATION [%s - ID:%d]: %s",
                timestamp, customerName, customerId, message
        ));

        // In real system: send SMS, email, push notification
    }

    public void notifyPartner(long partnerId, String message) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String partnerName = getPartnerName(partnerId);

        System.out.println(String.format(
                "[%s] PARTNER NOTIFICATION [%s - ID:%d]: %s",
                timestamp, partnerName, partnerId, message
        ));

        // In real system: send SMS, app notification
    }

    public void notifyOrderCreated(long customerId, long orderId, String itemName) {
        notifyCustomer(customerId,
                String.format("Order #%d created for %s. Looking for delivery partner...",
                        orderId, itemName));
    }

    public void notifyOrderAssigned(long customerId, long orderId, long partnerId) {
        notifyCustomer(customerId,
                String.format("Order #%d assigned to delivery partner #%d",
                        orderId, partnerId));

        notifyPartner(partnerId,
                String.format("New order #%d assigned to you", orderId));
    }

    public void notifyOrderPickedUp(long customerId, long orderId, long partnerId) {
        notifyCustomer(customerId,
                String.format("Order #%d picked up by delivery partner. On the way!",
                        orderId));
    }

    public void notifyOrderDelivered(long customerId, long orderId) {
        notifyCustomer(customerId,
                String.format("Order #%d delivered successfully! Please rate your experience.",
                        orderId));
    }

    public void notifyOrderCancelled(long customerId, long orderId, String reason) {
        notifyCustomer(customerId,
                String.format("Order #%d has been cancelled. Reason: %s",
                        orderId, reason));
    }

    public void notifyPartnerFreed(long partnerId, long orderId, String reason) {
        notifyPartner(partnerId,
                String.format("Order #%d cancelled. %s. You are now available for new orders.",
                        orderId, reason));
    }

    // Helper methods
    private String getCustomerName(long customerId) {
        if (customerRepository != null) {
            return customerRepository.findById(String.valueOf(customerId))
                    .map(Customer::getName)
                    .orElse("Customer");
        }
        return "Customer";
    }

    private String getPartnerName(long partnerId) {
        if (partnerRepository != null) {
            return partnerRepository.findById(String.valueOf(partnerId))
                    .map(DeliveryPartner::getName)
                    .orElse("Partner");
        }
        return "Partner";
    }

    // System notifications
    public void logSystemEvent(String event) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        System.out.println(String.format("[%s] SYSTEM: %s", timestamp, event));
    }

    public void logError(String error) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        System.err.println(String.format("ERROR: %s", timestamp, error));
    }
}