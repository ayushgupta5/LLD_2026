import java.util.List;

class User {
    int userID;
    String name;
    String email;
    String password;
    String phoneNumber;
    String address;
}

class AuthenticationService {
    boolean authenticate(String email, String password) {
        System.out.println("Authenticating user " + email);
        return true;
    }
}

class SessionService {
    void login(User user) {
        System.out.println("User " + user.name + " logged in.");
    }
    void logout(User user) {
        System.out.println("User " + user.name + " logged out.");
    }
}

class OrderService{
    public void PlaceOrder(int userID, int restaurantID, List<Integer> menuItemIDs) {
        System.out.println("Placing order for user " + userID);
    }
}

class RestaurantService {
    public void SearchRestaurants(String location, String cuisineType) {
        System.out.println("Searching for restaurant " + cuisineType);
    }
}

class MenuService {
    public void ViewMenu(int restaurantID) {
        System.out.println("Viewing menu for restaurant " + restaurantID);
    }
}

class Restaurant {
    int restaurantID;
    String name;
    String location;
    String cuisineType;
    String rating;
    String contactNumber;
}

class Menu {
    int menuID;
    int restaurantID;
    String itemName;
    String description;
    float price;
    String category;
}

class Order {
    int orderID;
    int userID;
    int restaurantID;
    List<Integer> menuItemIDs;
    float totalAmount;
    String orderStatus;
    String orderTime;

}

class OrderTrackService {
    public void TrackOrder(int orderID) {
        System.out.println("Tracking order " + orderID);
    }
}

class OrderPlacementService {
    public void OrderPlacement(int userID, int restaurantID, List<Integer> menuItemIDs) {
        System.out.println("Placing order " + userID + " " + restaurantID + " " + menuItemIDs);
    }
}

class OrderViewService {
    public void ViewOrderHistory(int orderID) {
        System.out.println("View order history " + orderID);
    }
}

class Payment {
    int paymentID;
    int orderID;
    float amount;
    String paymentMethod;
}

interface PaymentStrategy {
    void ProcessPayment(float amount);
}

class CreditCardPayment implements PaymentStrategy {
    public void ProcessPayment(float amount) {
        System.out.println("Credit card payment " + amount);
    }
}

class UPI implements PaymentStrategy {
    public void ProcessPayment(float amount) {
        System.out.println("UPI payment " + amount);
    }
}

class NetBanking implements PaymentStrategy {
    public void ProcessPayment(float amount) {
        System.out.println("Net banking payment " + amount);
    }
}

class PaymentService {
    PaymentStrategy paymentStrategy;
    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.paymentStrategy = strategy;
    }
    public void ProcessPayment(float amount) {
        paymentStrategy.ProcessPayment(amount);
    }
}

public class Swiggy {
    public static void main(String[] args) {
        // Example usage
        User user = new User();
        user.userID = 1;
        user.name = "John Doe";
        user.email = "john@gmail.com";
        user.password = "password";
        user.phoneNumber = "1234567890";
        user.address = "123 Main St";
        AuthenticationService authService = new AuthenticationService();
        if (authService.authenticate(user.email, user.password)) {
            SessionService sessionService = new SessionService();
            sessionService.login(user);
            RestaurantService restaurantService = new RestaurantService();
            restaurantService.SearchRestaurants("Downtown", "Italian");
            MenuService menuService = new MenuService();
            menuService.ViewMenu(101);
            OrderService orderService = new OrderService();
            orderService.PlaceOrder(user.userID, 101, List.of(1, 2, 3));
            PaymentService paymentService = new PaymentService();
            paymentService.setPaymentStrategy(new CreditCardPayment());
            paymentService.ProcessPayment(250.75f);
            sessionService.logout(user);
        }
    }
}
