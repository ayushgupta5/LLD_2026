import java.util.*;
import java.time.LocalDateTime;

enum Category {
    Fiction,
    NonFiction,
    Science,
    History,
    Biography
}

class Book {
    int bookID;
    String title;
    String author;
    String ISBN;
    Category category;
    double price;
    boolean isAvailable;
    int stockQuantity;

    public Book(int id, String t, String a, String isbn, Category cat, double p, int stock) {
        this.bookID = id;
        this.title = t;
        this.author = a;
        this.ISBN = isbn;
        this.category = cat;
        this.price = p;
        this.stockQuantity = stock;
        this.isAvailable = (stock > 0);
    }

    void updateStock(int quantity) {
        stockQuantity += quantity;
        isAvailable = (stockQuantity > 0);
    }

    boolean checkAvailability() {
        return isAvailable && stockQuantity > 0;
    }
}

class ShoppingCart {
    int cartID;
    int userID;
    List<Book> books;

    public ShoppingCart(int cid, int uid) {
        this.cartID = cid;
        this.userID = uid;
        this.books = new ArrayList<>();
    }

    void addBook(Book book) {
        if (book.checkAvailability()) {
            books.add(book);
            System.out.println("Book added to cart: " + book.title);
        } else {
            System.out.println("Book is not available!");
        }
    }

    void removeBook(Book book) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).bookID == book.bookID) {
                books.remove(i);
                System.out.println("Book removed from cart: " + book.title);
                return;
            }
        }
        System.out.println("Book not found in cart!");
    }

    double calculateTotal() {
        double total = 0.0;
        for (Book book : books) {
            total += book.price;
        }
        return total;
    }
}

class Payment {
    int paymentID;
    int orderID;
    double amount;
    LocalDateTime paymentDate;
    String paymentMethod;
    String paymentStatus;

    public Payment() {
        this.paymentID = 0;
        this.orderID = 0;
        this.amount = 0.0;
        this.paymentStatus = "Pending";
        this.paymentDate = LocalDateTime.now();
    }

    public Payment(int i, double fee, Date date, String card) {
        this.paymentID = i;
        this.amount = fee;
        this.paymentDate = LocalDateTime.now();
        this.paymentMethod = card;
        this.paymentStatus = "Pending";
    }

    void processPayment(int userID, double amt, String method) {
        this.amount = amt;
        this.paymentMethod = method;

        System.out.println("Processing payment of $" + String.format("%.2f", amount)
                + " via " + method + "...");

        // Simulate payment processing
        this.paymentStatus = "Completed";
        this.paymentDate = LocalDateTime.now();
        System.out.println("Payment completed successfully!");
    }
}

class Order {
    int orderID;
    int userID;
    List<Book> books;
    double totalAmount;
    LocalDateTime orderDate;
    String orderStatus;

    public Order() {
        this.orderID = 0;
        this.userID = 0;
        this.books = new ArrayList<>();
        this.totalAmount = 0.0;
        this.orderStatus = "Processing";
        this.orderDate = LocalDateTime.now();
    }

    void placeOrder(int uid, List<Book> bks, double total) {
        this.userID = uid;
        this.books = new ArrayList<>(bks);
        this.totalAmount = total;
        this.orderDate = LocalDateTime.now();
        this.orderStatus = "Processing";

        System.out.println("\n--- Order Placed ---");
        System.out.println("Order ID: " + orderID);
        System.out.println("User ID: " + userID);
        System.out.println("Total Amount: $" + String.format("%.2f", totalAmount));
        System.out.println("Status: " + orderStatus);
        System.out.println("Books ordered:");
        for (Book book : books) {
            System.out.println("  - " + book.title + " by " + book.author);
        }
    }
}

class User {
    int userID;
    String userName;
    String userEmail;
    String password;
    String userAddress;

    public User() {
        this.userID = 0;
    }

    void login(String email, String pass) {
        if (userEmail != null && userEmail.equals(email) && password.equals(pass)) {
            System.out.println("Login successful! Welcome, " + userName + "!");
        } else {
            System.out.println("Invalid credentials!");
        }
    }

    void signup(String name, String email, String pass, String address) {
        this.userName = name;
        this.userEmail = email;
        this.password = pass;
        this.userAddress = address;
        this.userID = new Random().nextInt(10000) + 1;
        System.out.println("Signup successful! User ID: " + userID);
    }
}

public class BookstoreSystem {
    public static void main(String[] args) {
        // Demo usage
        User user = new User();
        user.signup("John Doe", "john@example.com", "password123", "123 Main St");
        user.login("john@example.com", "password123");

        Book book1 = new Book(1, "The Great Gatsby", "F. Scott Fitzgerald",
                "978-0743273565", Category.Fiction, 12.99, 10);
        Book book2 = new Book(2, "Sapiens", "Yuval Noah Harari",
                "978-0062316097", Category.History, 18.99, 5);



        ShoppingCart cart = new ShoppingCart(1, user.userID);
        cart.addBook(book1);
        cart.addBook(book2);

        cart.removeBook(book1);

        double total = cart.calculateTotal();
        System.out.println("\nCart Total: $" + String.format("%.2f", total));

        Payment payment = new Payment();
        payment.orderID = 1001;
        payment.paymentID = 5001;
        payment.processPayment(user.userID, total, "Credit Card");

        Order order = new Order();
        order.orderID = 1001;
        order.placeOrder(user.userID, cart.books, total);

        book1.updateStock(-1);

        book1.updateStock(10);
    }
}