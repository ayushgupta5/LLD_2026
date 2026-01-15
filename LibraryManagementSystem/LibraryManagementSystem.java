import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

class User {
    int userID;
    String userName;
    String userEmail;
    String password;
    String userAddress;
    List<Borrowing> borrowingHistory;
    double totalFines;

    public User() {
        borrowingHistory = new ArrayList<>();
        totalFines = 0.0;
    }

    public User(int userID, String userName, String userEmail, String password, String userAddress) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
        this.password = password;
        this.userAddress = userAddress;
        this.borrowingHistory = new ArrayList<>();
        this.totalFines = 0.0;
    }

    void login(String userEmail, String password) {
        if (this.userEmail.equals(userEmail) && this.password.equals(password)) {
            System.out.println("Login successful! Welcome " + userName);
        } else {
            System.out.println("Invalid email or password!");
        }
    }

    void signup(String userName, String userEmail, String password, String userAddress) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.password = password;
        this.userAddress = userAddress;
        System.out.println("Signup successful! Welcome " + userName);
    }

    void searchBook(String title, String author, String ISBN) {
        System.out.println("Searching for book...");
        System.out.println("Title: " + title);
        System.out.println("Author: " + author);
        System.out.println("ISBN: " + ISBN);
    }

    void borrowBook(int bookID) {
        System.out.println("Borrowing book with ID: " + bookID);
        Borrowing newBorrowing = new Borrowing();
        newBorrowing.borrowingID = borrowingHistory.size() + 1;
        newBorrowing.userID = this.userID;
        newBorrowing.bookID = bookID;
        newBorrowing.borrowDate = new Date();

        // Set due date to 14 days from now
        long dueTime = newBorrowing.borrowDate.getTime() + (14L * 24 * 60 * 60 * 1000);
        newBorrowing.dueDate = new Date(dueTime);

        borrowingHistory.add(newBorrowing);
        System.out.println("Book borrowed successfully! Due date: " + newBorrowing.dueDate);
    }

    void returnBook(int bookID) {
        System.out.println("Returning book with ID: " + bookID);
        for (Borrowing borrowing : borrowingHistory) {
            if (borrowing.bookID == bookID && borrowing.returnDate == null) {
                borrowing.returnDate = new Date();
                double fine = borrowing.calculateFine();
                if (fine > 0) {
                    totalFines += fine;
                    System.out.println("Book returned with a fine of $" + fine);
                } else {
                    System.out.println("Book returned successfully with no fine!");
                }
                return;
            }
        }
        System.out.println("No active borrowing found for this book!");
    }

    void viewBorrowedHistory() {
        System.out.println("\n=== Borrowing History for " + userName + " ===");
        if (borrowingHistory.isEmpty()) {
            System.out.println("No borrowing history available.");
            return;
        }

        for (Borrowing borrowing : borrowingHistory) {
            System.out.println("\nBorrowing ID: " + borrowing.borrowingID);
            System.out.println("Book ID: " + borrowing.bookID);
            System.out.println("Borrow Date: " + borrowing.borrowDate);
            System.out.println("Due Date: " + borrowing.dueDate);
            System.out.println("Return Date: " + (borrowing.returnDate != null ? borrowing.returnDate : "Not returned"));
            System.out.println("Fine: $" + borrowing.calculateFine());
        }
        System.out.println("\nTotal Outstanding Fines: $" + totalFines);
    }

    void payFine(double amount) {
        if (amount <= 0) {
            System.out.println("Invalid payment amount!");
            return;
        }

        if (amount > totalFines) {
            System.out.println("Payment amount exceeds total fines!");
            return;
        }

        totalFines -= amount;
        System.out.println("Payment of $" + amount + " processed successfully!");
        System.out.println("Remaining fines: $" + totalFines);

        Payment payment = new Payment();
        payment.paymentID = (int)(Math.random() * 10000);
        payment.userID = this.userID;
        payment.amount = amount;
        payment.paymentDate = new Date();
        payment.paymentMethod = "Credit Card";
        payment.paymentStatus = "Completed";
    }
}

class Librarian {
    int librarianID;
    String librarianName;
    String librarianEmail;
    String password;
    List<Book> bookInventory;
    List<User> userList;

    public Librarian() {
        bookInventory = new ArrayList<>();
        userList = new ArrayList<>();
    }

    public Librarian(int librarianID, String librarianName, String librarianEmail, String password) {
        this.librarianID = librarianID;
        this.librarianName = librarianName;
        this.librarianEmail = librarianEmail;
        this.password = password;
        this.bookInventory = new ArrayList<>();
        this.userList = new ArrayList<>();
    }

    void login(String librarianEmail, String password) {
        if (this.librarianEmail.equals(librarianEmail) && this.password.equals(password)) {
            System.out.println("Librarian login successful! Welcome " + librarianName);
        } else {
            System.out.println("Invalid email or password!");
        }
    }

    void addBook(Book book) {
        bookInventory.add(book);
        System.out.println("Book added successfully!");
        System.out.println("Title: " + book.title);
        System.out.println("Author: " + book.author);
        System.out.println("ISBN: " + book.ISBN);
        System.out.println("Stock Quantity: " + book.stockQuantity);
    }

    void removeBook(int bookID) {
        Book bookToRemove = null;
        for (Book book : bookInventory) {
            if (book.bookID == bookID) {
                bookToRemove = book;
                break;
            }
        }

        if (bookToRemove != null) {
            bookInventory.remove(bookToRemove);
            System.out.println("Book removed successfully: " + bookToRemove.title);
        } else {
            System.out.println("Book not found with ID: " + bookID);
        }
    }

    void manageInventory(int bookID, int quantity) {
        for (Book book : bookInventory) {
            if (book.bookID == bookID) {
                book.updateStock(quantity);
                System.out.println("Inventory updated for: " + book.title);
                System.out.println("New stock quantity: " + book.stockQuantity);
                return;
            }
        }
        System.out.println("Book not found with ID: " + bookID);
    }

    void viewUserBorrowingHistory(int userID) {
        User targetUser = null;
        for (User user : userList) {
            if (user.userID == userID) {
                targetUser = user;
                break;
            }
        }

        if (targetUser != null) {
            System.out.println("\n=== Borrowing History for User: " + targetUser.userName + " ===");
            targetUser.viewBorrowedHistory();
        } else {
            System.out.println("User not found with ID: " + userID);
        }
    }

    void generateReport(String reportType) {
        Report report = new Report();
        report.reportID = (int)(Math.random() * 10000);
        report.reportType = reportType;
        report.generatedDate = new Date();

        System.out.println("\n=== Generating " + reportType + " Report ===");
        System.out.println("Report ID: " + report.reportID);
        System.out.println("Generated Date: " + report.generatedDate);

        if (reportType.equals("Book Usage")) {
            System.out.println("\nTotal Books in Inventory: " + bookInventory.size());
            int availableBooks = 0;
            for (Book book : bookInventory) {
                if (book.isAvailable) availableBooks++;
            }
            System.out.println("Available Books: " + availableBooks);
            System.out.println("Borrowed Books: " + (bookInventory.size() - availableBooks));
        } else if (reportType.equals("User Activity")) {
            System.out.println("\nTotal Users: " + userList.size());
            int activeUsers = 0;
            for (User user : userList) {
                if (!user.borrowingHistory.isEmpty()) activeUsers++;
            }
            System.out.println("Active Users: " + activeUsers);
        }
    }

    void addUser(User user) {
        userList.add(user);
    }
}

class Book {
    int bookID;
    String title;
    String author;
    String ISBN;
    String category;
    double price;
    boolean isAvailable;
    int stockQuantity;

    public Book() {
        this.isAvailable = true;
        this.stockQuantity = 0;
    }

    public Book(int bookID, String title, String author, String ISBN, String category, double price, int stockQuantity) {
        this.bookID = bookID;
        this.title = title;
        this.author = author;
        this.ISBN = ISBN;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.isAvailable = stockQuantity > 0;
    }

    void updateStock(int quantity) {
        this.stockQuantity += quantity;
        this.isAvailable = this.stockQuantity > 0;
        System.out.println("Stock updated. New quantity: " + this.stockQuantity);
    }

    boolean checkAvailability() {
        this.isAvailable = this.stockQuantity > 0;
        if (this.isAvailable) {
            System.out.println("Book is available. Stock: " + this.stockQuantity);
        } else {
            System.out.println("Book is not available.");
        }
        return this.isAvailable;
    }
}

class Borrowing {
    int borrowingID;
    int userID;
    int bookID;
    Date borrowDate;
    Date dueDate;
    Date returnDate;

    public Borrowing() {
        this.returnDate = null;
    }

    public Borrowing(int borrowingID, int userID, int bookID, Date borrowDate, Date dueDate) {
        this.borrowingID = borrowingID;
        this.userID = userID;
        this.bookID = bookID;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = null;
    }

    double calculateFine() {
        if (returnDate == null) {
            // Book not yet returned, check if overdue
            Date currentDate = new Date();
            if (currentDate.after(dueDate)) {
                long overdueDays = (currentDate.getTime() - dueDate.getTime()) / (1000 * 60 * 60 * 24);
                return overdueDays * 1.0; // $1 per day fine
            }
            return 0.0;
        } else {
            // Book already returned
            if (returnDate.after(dueDate)) {
                long overdueDays = (returnDate.getTime() - dueDate.getTime()) / (1000 * 60 * 60 * 24);
                return overdueDays * 1.0; // $1 per day fine
            }
            return 0.0;
        }
    }
}

class Payment {
    int paymentID;
    int userID;
    double amount;
    Date paymentDate;
    String paymentMethod;
    String paymentStatus;

    public Payment() {
        this.paymentStatus = "Pending";
    }

    public Payment(int paymentID, int userID, double amount, String paymentMethod) {
        this.paymentID = paymentID;
        this.userID = userID;
        this.amount = amount;
        this.paymentDate = new Date();
        this.paymentMethod = paymentMethod;
        this.paymentStatus = "Pending";
    }

    public Payment(int paymentID, double fee, Date date, String card) {
        this.paymentID = paymentID;
        this.amount = fee;
        this.paymentDate = date;
        this.paymentMethod = card;
        this.paymentStatus = "Pending";
    }

    void processPayment(int userID, double amount, String paymentMethod) {
        this.userID = userID;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = new Date();

        // Simulate payment processing
        System.out.println("Processing payment...");
        System.out.println("User ID: " + userID);
        System.out.println("Amount: $" + amount);
        System.out.println("Payment Method: " + paymentMethod);

        // Simulate success
        this.paymentStatus = "Completed";
        System.out.println("Payment Status: " + this.paymentStatus);
        System.out.println("Payment processed successfully on " + this.paymentDate);
    }
}

class Notification {
    int notificationID;
    int userID;
    String message;
    Date notificationDate;

    public Notification() {
        this.notificationDate = new Date();
    }

    public Notification(int notificationID, int userID, String message) {
        this.notificationID = notificationID;
        this.userID = userID;
        this.message = message;
        this.notificationDate = new Date();
    }

    void sendNotification(int userID, String message) {
        this.userID = userID;
        this.message = message;
        this.notificationDate = new Date();

        System.out.println("\n=== NEW NOTIFICATION ===");
        System.out.println("Notification ID: " + this.notificationID);
        System.out.println("User ID: " + this.userID);
        System.out.println("Message: " + this.message);
        System.out.println("Date: " + this.notificationDate);
        System.out.println("========================\n");
    }
}

class Report {
    int reportID;
    String reportType;
    Date generatedDate;

    public Report() {
        this.generatedDate = new Date();
    }

    public Report(int reportID, String reportType) {
        this.reportID = reportID;
        this.reportType = reportType;
        this.generatedDate = new Date();
    }

    void generateReport(String reportType) {
        this.reportType = reportType;
        this.generatedDate = new Date();

        System.out.println("\n========== REPORT ==========");
        System.out.println("Report ID: " + this.reportID);
        System.out.println("Report Type: " + this.reportType);
        System.out.println("Generated Date: " + this.generatedDate);
        System.out.println("============================\n");
    }
}

public class LibraryManagementSystem {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   LIBRARY MANAGEMENT SYSTEM");
        System.out.println("========================================\n");

        // Create librarian
        Librarian librarian = new Librarian(1, "John Smith", "john@library.com", "admin123");

        // Create some books
        Book book1 = new Book(101, "The Great Gatsby", "F. Scott Fitzgerald", "978-0743273565", "Fiction", 15.99, 5);
        Book book2 = new Book(102, "To Kill a Mockingbird", "Harper Lee", "978-0061120084", "Fiction", 18.99, 3);
        Book book3 = new Book(103, "1984", "George Orwell", "978-0451524935", "Fiction", 14.99, 4);

        // Librarian adds books
        System.out.println(">>> Librarian Adding Books <<<\n");
        librarian.addBook(book1);
        librarian.addBook(book2);
        librarian.addBook(book3);

        // Create users
        User user1 = new User(1001, "Alice Johnson", "alice@email.com", "pass123", "123 Main St");
        User user2 = new User(1002, "Bob Williams", "bob@email.com", "pass456", "456 Oak Ave");

        librarian.addUser(user1);
        librarian.addUser(user2);

        System.out.println("\n>>> User Login <<<\n");
        user1.login("alice@email.com", "pass123");

        System.out.println("\n>>> User Borrowing Books <<<\n");
        user1.borrowBook(101);
        user1.borrowBook(102);

        System.out.println("\n>>> Checking Book Availability <<<\n");
        book1.checkAvailability();

        System.out.println("\n>>> User Returning Book <<<\n");
        user1.returnBook(101);

        System.out.println("\n>>> Viewing Borrowing History <<<");
        user1.viewBorrowedHistory();

        System.out.println("\n>>> Managing Inventory <<<\n");
        librarian.manageInventory(103, 10);

        System.out.println("\n>>> Sending Notification <<<");
        Notification notification = new Notification(5001, 1001, "Your book 'To Kill a Mockingbird' is due tomorrow!");
        notification.sendNotification(1001, "Your book 'To Kill a Mockingbird' is due tomorrow!");

        System.out.println("\n>>> Generating Reports <<<");
        librarian.generateReport("Book Usage");
        librarian.generateReport("User Activity");

        System.out.println("\n>>> Processing Payment <<<\n");
        Payment payment = new Payment(7001, 1001, 25.50, "Credit Card");
        payment.processPayment(1001, 25.50, "Credit Card");

        System.out.println("\n========================================");
        System.out.println("   DEMO COMPLETED SUCCESSFULLY");
        System.out.println("========================================");
    }
}