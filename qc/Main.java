// Main.java (Driver Program)
public class Main {
    public static void main(String[] args) throws InterruptedException {
        FlipkartMinutesSystem_a system = new FlipkartMinutesSystem_a();

        System.out.println("\n========== FLIPKART MINUTES DEMO ==========\n");

        // Test Case 1: Onboarding
        System.out.println("--- Test 1: Onboarding ---");
        Customer_a c1 = system.onboardCustomer("Rahul");
        Customer_a c2 = system.onboardCustomer("Priya");
        DeliveryPartner p1 = system.onboardDeliveryPartner("Amit");
        DeliveryPartner p2 = system.onboardDeliveryPartner("Suresh");

        Thread.sleep(500);

        // Test Case 2: Order placement and auto-assignment
        System.out.println("\n--- Test 2: Order Placement ---");
        Order order1 = system.createOrder(c1.getCustomerId(), "Laptop");
        Order order2 = system.createOrder(c2.getCustomerId(), "Mobile");

        Thread.sleep(500);

        // Test Case 3: Check status
        System.out.println("\n--- Test 3: Status Check ---");
        System.out.println(system.getOrderStatus(order1.getOrderId()));
        System.out.println(system.getPartnerStatus(p1.getPartnerId()));

        // Test Case 4: Pickup and delivery
        System.out.println("\n--- Test 4: Pickup & Delivery ---");
        system.pickUpOrder(p1.getPartnerId(), order1.getOrderId());
        system.completeOrder(p1.getPartnerId(), order1.getOrderId(), 5);

        Thread.sleep(500);

        // Test Case 5: Cancel order before pickup
        System.out.println("\n--- Test 5: Cancel Order ---");
        Order order3 = system.createOrder(c1.getCustomerId(), "Headphones");
        Thread.sleep(300);
        system.cancelOrder(order3.getOrderId());

        // Test Case 6: Try to cancel after pickup (should fail)
        System.out.println("\n--- Test 6: Cancel After Pickup (Should Fail) ---");
        system.pickUpOrder(p2.getPartnerId(), order2.getOrderId());
        system.cancelOrder(order2.getOrderId());

        // Complete order 2
        system.completeOrder(p2.getPartnerId(), order2.getOrderId(), 4);

        // Test Case 7: More orders than partners
        System.out.println("\n--- Test 7: Queue Management ---");
        Order order4 = system.createOrder(c1.getCustomerId(), "Book");
        Order order5 = system.createOrder(c2.getCustomerId(), "Watch");
        Order order6 = system.createOrder(c1.getCustomerId(), "Shoes");

        Thread.sleep(1000);

        // Test Case 8: Dashboard
        System.out.println("\n--- Test 8: Dashboard ---");
        system.showTopPartners();

        System.out.println("\n--- Final Status ---");
        System.out.println(system.getOrderStatus(order4.getOrderId()));
        System.out.println(system.getOrderStatus(order5.getOrderId()));

        // Cleanup
        system.shutdown();
        System.out.println("\n========== DEMO COMPLETE ==========");
    }
}