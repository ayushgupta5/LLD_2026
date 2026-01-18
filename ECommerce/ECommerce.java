class User {
    int UserID;
    String UserName;
    String Email;
    String Password;
}

class AuthService {
    public void Login() {
        System.out.println("User is Logging In");
    }

    public void Logout() {
        System.out.println("User is Logging Out");
    }
}

class RegistrationService {
    public void Register() {
        System.out.println("User is Registering");
    }
}

class ProfileService {
    public void UpdateProfile() {
        System.out.println("User is Updating Profile");
    }
}

class Product {
    int ProductID;
    String ProductName;
    String Description;
    double Price;
    String ImageURL;
    String Category;
}

class ProductService {
    public void AddProduct() {
        System.out.println("Adding a new product");
    }

    public void EditProduct() {
        System.out.println("Editing an existing product");
    }

    public void DeleteProduct() {
        System.out.println("Deleting a product");
    }
}

class ProductSerchService {
    public void SearchProduct() {
        System.out.println("Searching for a product");
    }
}

class ShoppingCart {
    int CartID;
    int UserID;
    Product[] Products;

    public void AddToCart() {
        System.out.println("Adding to cart");
    }

    public void RemoveFromCart() {
        System.out.println("Removing from cart");
    }

    public void UpdateQuantity() {
        System.out.println("Updating product quantity in cart");
    }

    public void ViewCart() {
        System.out.println("Viewing cart contents");
    }
}

class PriceManager {
    public void totalPrice() {
        System.out.println("Calculating total price");
    }
}

class Order {
    int OrderID;
    int UserID;
    Product[] Products;
    String ShippingAddress;
    String BillingAddress;
    String PaymentMethod;
}

class OrderService {
    public void PlaceOrder() {
        System.out.println("Placing an order");
    }

    public void CancelOrder() {
        System.out.println("Cancelling an order");
    }
}

class OrderHistoryService {
    public void ViewOrderHistory() {
        System.out.println("Viewing order history");
    }
}

class Payment {
    int PaymentID;
    int OrderID;
    double Amount;
    String PaymentMethod;
}

interface PaymentStrategy {
    public void ProcessPayment();
}

class CreditCardPayment implements PaymentStrategy {
    public void ProcessPayment() {
        System.out.println("Processing credit card payment");
    }
}

class UPI implements PaymentStrategy {
    public void ProcessPayment() {
        System.out.println("Processing UPI payment");
    }
}

class NetBanking implements PaymentStrategy {
    public void ProcessPayment() {
        System.out.println("Processing net banking payment");
    }
}

class PaymentService {
    PaymentStrategy paymentStrategy;

    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public void ProcessPayment() {
        paymentStrategy.ProcessPayment();
    }
}

public class ECommerce {
    public static void main(String[] args) {
        // Example usage
        AuthService authService = new AuthService();
        authService.Login();

        ProductService productService = new ProductService();
        productService.AddProduct();

        ShoppingCart cart = new ShoppingCart();
        cart.AddToCart();

        PaymentService paymentService = new PaymentService();
        paymentService.setPaymentStrategy(new CreditCardPayment());
        paymentService.ProcessPayment();
    }
}
