import java.util.List;

/*
    * Restaurant Management System
    * Version: 1.0
    #Functional Requirements
    * Customer Can view Menu
    * Customer can Place order
    * Staff can Manage orders
    * Staff can Manage Menu
    * Billing and Payment processing
    #Non-Functional Requirements
    * System should be user-friendly
    * System should be reliable and available
    * System should ensure data security
    #Classes and Relationships
    * Restaurant (Main class)
    * Menu (Contains list of MenuItems)
    * MenuItem (Details of each item)
    * Order (Contains list of MenuItems ordered by Customer)
    * Customer (Places orders)
    * Staff (Manages orders and Menu)
    * Bill (Generates bill for orders)
    * Payment (Processes payment for bills)
*/

interface PaymentProcessor {
    void processPayment();
}
class CashPaymentProcessor implements PaymentProcessor {
    @Override
    public void processPayment() {
        System.out.println("- Cash Payment: \n");
    }
}
class UPIPaymentProcessor implements PaymentProcessor {
    @Override
    public void processPayment() {
        System.out.println("- UPI Payment: \n");
    }
}
class CreditCardPaymentProcessor implements PaymentProcessor {
    @Override
    public void processPayment() {
        System.out.println("- Credit Card Payment: \n");
    }
}
class Payment {
    Bill bill;
    PaymentProcessor PaymentProcessor;
    public Payment(Bill bill, PaymentProcessor paymentProcessor) {
        this.bill = bill;
        this.PaymentProcessor = paymentProcessor;
    }
    public void processPayment() {
        PaymentProcessor.processPayment();
    }
    public void setPaymentProcessor(PaymentProcessor paymentProcessor) {
        this.PaymentProcessor = paymentProcessor;
    }
}
class Bill {
    Order order;
    double totalAmount;
    public Bill(Order order, double totalAmount) {
        this.order = order;
        this.totalAmount = totalAmount;
    }
    public void generateBill() {
        System.out.println("- Bill: \n");
    }
}
class ManageOrder {
    public void updateOrderStatus(Order order, String status) {
       System.out.println("- Order Status: " + status + "\n");
    }
}
class ManageMenu {
    public void addMenuItem(Menu menu, MenuItems item) {
        menu.items.add(item);
    }
    public void removeMenuItem(Menu menu, MenuItems item) {
        menu.items.remove(item);
    }
}
class Staff {
    String name;
    String employeeID;
    ManageOrder manageOrder;
    ManageMenu manageMenu;

    public Staff(String name, String employeeID) {
        this.name = name;
        this.employeeID = employeeID;
        this.manageOrder = new ManageOrder();
        this.manageMenu = new ManageMenu();
    }
}
class Customer {
    String name;
    String contactInfo;
    public Customer(String name, String contactInfo) {
        this.name = name;
        this.contactInfo = contactInfo;
    }
    public void orderPlace(Order order) {
        System.out.println("- Order Place: \n");
    }
}
class Order {
    List<MenuItems> menuItems;
    public Order(List<MenuItems> menuItems) {
        this.menuItems = menuItems;
    }
}
class MenuItems {
    String name;
    String description;
    double price;

    public MenuItems(String name, String description, double price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }
}
class Menu {
    List<MenuItems> items;
    public Menu(List<MenuItems> items) {
        this.items = items;
    }
}

public class Restaurant {
    public static void main(String[] args) {
        System.out.println("Welcome to the Restaurant Management System!");
        Customer customer = new Customer("John Doe", "1234567890");
        Order order = new Order(List.of(
            new MenuItems("Pizza", "Delicious cheese pizza", 8.99),
            new MenuItems("Pasta", "Creamy Alfredo pasta", 7.99)
        ));
        customer.orderPlace(order);
        Bill bill = new Bill(order, 16.98);
        Payment payment = new Payment(bill, new CashPaymentProcessor());
        payment.processPayment();
        payment.setPaymentProcessor(new CreditCardPaymentProcessor());
        payment.processPayment();
        payment.setPaymentProcessor(new UPIPaymentProcessor());
        payment.processPayment();
    }
}
