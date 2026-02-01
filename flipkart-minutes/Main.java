import src.repository.*;
import src.service.*;
import src.models.*;

public class Main {
    public static void main(String[] args) throws Exception {
        CustomerRepository customerRepo = new CustomerRepository();
        PartnerRepository partnerRepo = new PartnerRepository();
        OrderRepository orderRepo = new OrderRepository();

        NotificationService notificationService = new NotificationService();

        CustomerService customerService = new CustomerService(customerRepo, notificationService);

        PartnerService partnerService = new PartnerService(partnerRepo, orderRepo, notificationService);

        OrderService orderService = new OrderService(orderRepo, partnerRepo, notificationService);

        Customer c1 = customerService.onboardCustomer("Customer c1", "9876543210");
        Customer c2 = customerService.onboardCustomer("Customer c2", "9876543211");

        DeliveryPartner p1 = partnerService.onboardPartner("DeliveryPartner p1", "9999888877", "123");
        DeliveryPartner p2 = partnerService.onboardPartner("DeliveryPartner p2", "9999888878", "456");



        Order order1 = orderService.createOrder(c1.getCustomerId(), "MacBook Pro");
        Order order2 = orderService.createOrder(c2.getCustomerId(), "iPhone 15");

        System.out.println(orderService.getOrderStatus(order1.getOrderId()));
        System.out.println(partnerService.getPartnerStatus(p1.getPartnerId()));

        partnerService.pickUpOrder(p1.getPartnerId(), order1.getOrderId());
        partnerService.completeOrder(p1.getPartnerId(), order1.getOrderId(), 5);


        Order order3 = orderService.createOrder(c1.getCustomerId(), "AirPods Pro");
        orderService.cancelOrder(order3.getOrderId());

        partnerService.pickUpOrder(p2.getPartnerId(), order2.getOrderId());
        orderService.cancelOrder(order2.getOrderId());


        partnerService.completeOrder(p2.getPartnerId(), order2.getOrderId(), 4);

        Order order4 = orderService.createOrder(c2.getCustomerId(), "Samsung TV");
        Order order5 = orderService.createOrder(c1.getCustomerId(), "PS5");

        partnerService.pickUpOrder(p1.getPartnerId(), order4.getOrderId());
        partnerService.completeOrder(p1.getPartnerId(), order4.getOrderId(), 5);

        partnerService.pickUpOrder(p2.getPartnerId(), order5.getOrderId());
        partnerService.completeOrder(p2.getPartnerId(), order5.getOrderId(), 3);


        partnerService.showTopPartners();

        // Cleanup
        orderService.shutdown();
    }
}
