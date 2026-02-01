import service.*;
import models.*;

public class Main {
    public static void main(String[] args) throws Exception {
        
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║      FLIPKART MINUTES - QUICK COMMERCE         ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
        
        // Initialize services - Dependency Injection pattern
        CustomerService customerService = new CustomerService();
        PartnerService partnerService = new PartnerService(null);
        OrderService orderService = new OrderService(partnerService);
        
        // Update partner service with order repo reference
        partnerService = new PartnerService(orderService.getOrderRepository());
        orderService = new OrderService(partnerService);
        
        // TEST 1: ONBOARDING
        System.out.println("═══ TEST 1: ONBOARDING ═══");
        Customer c1 = customerService.onboardCustomer("Rahul Kumar", "9876543210");
        Customer c2 = customerService.onboardCustomer("Priya Sharma", "9876543211");
        
        DeliveryPartner p1 = partnerService.onboardPartner("Amit Singh", "9999888877", "DL01AB1234");
        DeliveryPartner p2 = partnerService.onboardPartner("Suresh Patel", "9999888878", "DL01CD5678");
        
        Thread.sleep(500);
        
        // TEST 2: CREATE ORDERS
        System.out.println("\n═══ TEST 2: CREATE ORDERS ═══");
        Order order1 = orderService.createOrder(c1.getCustomerId(), "MacBook Pro");
        Order order2 = orderService.createOrder(c2.getCustomerId(), "iPhone 15");
        
        Thread.sleep(1000);
        
        // TEST 3: CHECK STATUS
        System.out.println("\n═══ TEST 3: ORDER & PARTNER STATUS ═══");
        System.out.println(orderService.getOrderStatus(order1.getOrderId()));
        System.out.println(partnerService.getPartnerStatus(p1.getPartnerId()));
        
        // TEST 4: PICKUP & DELIVERY
        System.out.println("\n═══ TEST 4: PICKUP & DELIVERY ═══");
        partnerService.pickUpOrder(p1.getPartnerId(), order1.getOrderId());
        Thread.sleep(500);
        partnerService.completeOrder(p1.getPartnerId(), order1.getOrderId(), 5);
        
        Thread.sleep(500);
        
        // TEST 5: CANCEL BEFORE PICKUP
        System.out.println("\n═══ TEST 5: ORDER CANCELLATION ═══");
        Order order3 = orderService.createOrder(c1.getCustomerId(), "AirPods Pro");
        Thread.sleep(500);
        orderService.cancelOrder(order3.getOrderId());
        
        // TEST 6: CANCEL AFTER PICKUP (SHOULD FAIL)
        System.out.println("\n═══ TEST 6: CANCEL AFTER PICKUP (SHOULD FAIL) ═══");
        partnerService.pickUpOrder(p2.getPartnerId(), order2.getOrderId());
        orderService.cancelOrder(order2.getOrderId());
        
        partnerService.completeOrder(p2.getPartnerId(), order2.getOrderId(), 4);
        
        // TEST 7: QUEUE MANAGEMENT
        System.out.println("\n═══ TEST 7: QUEUE MANAGEMENT ═══");
        Order order4 = orderService.createOrder(c2.getCustomerId(), "Samsung TV");
        Order order5 = orderService.createOrder(c1.getCustomerId(), "PS5");
        
        Thread.sleep(1000);
        
        partnerService.pickUpOrder(p1.getPartnerId(), order4.getOrderId());
        partnerService.completeOrder(p1.getPartnerId(), order4.getOrderId(), 5);
        
        Thread.sleep(500);
        
        partnerService.pickUpOrder(p2.getPartnerId(), order5.getOrderId());
        partnerService.completeOrder(p2.getPartnerId(), order5.getOrderId(), 3);
        
        // TEST 8: DASHBOARD
        System.out.println("\n═══ TEST 8: DASHBOARD ═══");
        partnerService.showTopPartners(10);
        
        // Cleanup
        orderService.shutdown();
        
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║          DEMO COMPLETED SUCCESSFULLY           ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
    }
}
