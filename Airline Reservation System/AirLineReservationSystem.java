import java.util.Date;
import java.util.List;
class User {
    private int userID;
    private String name;
    private String email;
    private String passwordHash;
    private String phoneNumber;
    private String address;
    private String role; // e.g., "admin", "customer"
    private Boolean active;

    public User(int userID, String name, String email, String passwordHash, String phoneNumber, String address, String role, Boolean active) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
class Flight {
    private int flightID;
    private String flightNumber;
    private String origin;
    private String destination;
    private Date departureTime;
    private Date arrivalTime;
    private double price;
    private int totalSeats;
    private int availableSeats;

    public Flight(int flightID, String flightNumber, String origin, String destination, Date departureTime, Date arrivalTime, double price, int totalSeats, int availableSeats) {
        this.flightID = flightID;
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
    }

    public int getFlightID() {
        return flightID;
    }

    public void setFlightID(int flightID) {
        this.flightID = flightID;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
}
class Passenger {
    private int passengerID;
    private String name;
    private int age;
    private String gender;
    private String passportNumber;
    private String seatNumber;
    private int seatID;

    public Passenger(int passengerID, String name, int age, String gender, String passportNumber, String seatNumber, int seatID) {
        this.passengerID = passengerID;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.passportNumber = passportNumber;
        this.seatNumber = seatNumber;
        this.seatID = seatID;
    }

    public int getPassengerID() {
        return passengerID;
    }

    public void setPassengerID(int passengerID) {
        this.passengerID = passengerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public int getSeatID() {
        return seatID;
    }

    public void setSeatID(int seatID) {
        this.seatID = seatID;
    }
}
class Booking {
    private int bookingID;
    private int userID;
    private int flightID;
    private List<Passenger> passengers;
    private Date bookingDate;
    private double totalAmount;

    public Booking(int bookingID, int userID, int flightID, List<Passenger> passengers, Date bookingDate, double totalAmount) {
        this.bookingID = bookingID;
        this.userID = userID;
        this.flightID = flightID;
        this.passengers = passengers;
        this.bookingDate = bookingDate;
        this.totalAmount = totalAmount;
    }

    public int getBookingID() {
        return bookingID;
    }

    public void setBookingID(int bookingID) {
        this.bookingID = bookingID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getFlightID() {
        return flightID;
    }

    public void setFlightID(int flightID) {
        this.flightID = flightID;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
class Seat {
    private int seatID;
    private boolean isAvailable;
    public Seat(int seatID, boolean isAvailable) {
        this.seatID = seatID;
        this.isAvailable = isAvailable;
    }
    public int getSeatID() {
        return seatID;
    }
    public void setSeatID(int seatID) {
        this.seatID = seatID;
    }
    public boolean isAvailable() {
        return isAvailable;
    }
    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
class Review {
    private int reviewID;
    private int userID;
    private int flightID;
    private int rating; // e.g., 1 to 5
    private String comments;
    public Review(int reviewID, int userID, int flightID, int rating, String comments) {
        this.reviewID = reviewID;
        this.userID = userID;
        this.flightID = flightID;
        this.rating = rating;
        this.comments = comments;
    }

    public int getReviewID() {
        return reviewID;
    }

    public void setReviewID(int reviewID) {
        this.reviewID = reviewID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getFlightID() {
        return flightID;
    }

    public void setFlightID(int flightID) {
        this.flightID = flightID;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}

interface IUserAuthentication {
    boolean login();
    boolean logout();
}
class UserAuthentication implements IUserAuthentication {
    public boolean login() {
        // Implement login logic
        return true;
    }
    public boolean logout() {
        // Implement logout logic
        return true;
    }
}
interface IUserRegistration {
    boolean registration();
}
class  UserRegistration implements IUserRegistration {
    public boolean registration() {
        // Implement registration logic
        return true;
    }
}
interface IFlightManagement {
    boolean addFlight(Flight flight);
    boolean updateFlight(int flightID, Flight updatedFlight);
    boolean removeFlight(int flightID);
}
class FlightManagement implements IFlightManagement {
    public boolean addFlight(Flight flight) {
        // Implement add flight logic
        return true;
    }
    public boolean updateFlight(int flightID, Flight updatedFlight) {
        // Implement update flight logic
        return true;
    }
    public boolean removeFlight(int flightID) {
        // Implement remove flight logic
        return true;
    }
}
interface ISelectAndSearchFlight {
    List<Flight> searchFlight(String origin, String destination, Date date, int passengers);
    Flight selectFlight(int flightID, List<Flight> availableFlights);
}
class SelectAndSearchFlight implements ISelectAndSearchFlight {
    public List<Flight> searchFlight(String origin, String destination, Date date, int passengers) {
        // Implement flight search logic
        return null;
    }
    public Flight selectFlight(int flightID, List<Flight> availableFlights) {
        // Implement flight selection logic
        return null;
    }
}
interface ISeatSelection {
    List<Seat> getAvailableSeats(int flightID);
    boolean selectSeat(int seatID, int bookingID);
}
class SeatSelection implements ISeatSelection {
    public List<Seat> getAvailableSeats(int flightID) {
        // Implement logic to get available seats
        return null;
    }
    public boolean selectSeat(int seatID, int bookingID) {
        // Implement seat selection logic
        return true;
    }
}
interface IBookingService {
    boolean createBooking(int userID, int flightID, List<Passenger> passengers, double totalAmount);
    boolean cancelBooking(int bookingID);
    boolean modifyBooking(int bookingID, List<Passenger> updatedPassengers);
}
class BookingService implements IBookingService {
    public boolean createBooking(int userID, int flightID, List<Passenger> passengers, double totalAmount) {
        // Implement booking creation logic
        return true;
    }
    public boolean cancelBooking(int bookingID) {
        // Implement booking cancellation logic
        return true;
    }
    public boolean modifyBooking(int bookingID, List<Passenger> updatedPassengers) {
        // Implement booking modification logic
        return true;
    }
}
interface IReminderService {
    void sendReminder(int bookingId);
}
class ReminderService implements IReminderService {
    public void sendReminder(int bookingId) {
        // Implement reminder sending logic
    }
}
interface IGenerateReport {
    void generateReport(String reportType);
}
class GenerateReport implements IGenerateReport {
    public void generateReport(String reportType) {
        // Implement report generation logic
    }
}
interface IBookingHistory {
    List<Booking> viewBookingHistory(int userID);
    List<Booking> viewAllBookingHistory();
}
class BookingHistory implements IBookingHistory {
    public List<Booking> viewBookingHistory(int userID) {
        // Implement logic to view user's booking history
        return null;
    }
    public List<Booking> viewAllBookingHistory() {
        // Implement logic to view all booking history
        return null;
    }
}
interface IReviewService{
    boolean addReview(int userID, int flightID, int rating, String comments);
    List<Review> viewReviews(int flightID);
}
class ReviewService implements IReviewService {
    public boolean addReview(int userID, int flightID, int rating, String comments) {
        // Implement logic to add review
        return true;
    }
    public List<Review> viewReviews(int flightID) {
        // Implement logic to view reviews for a flight
        return null;
    }
}
interface IManageUsers {
    List<User> viewAllUsers();
    boolean updateUserRole(int userID, String newRole);
    boolean deactivateUser(int userID);
}
class ManageUsers implements IManageUsers {
    public List<User> viewAllUsers() {
        // Implement logic to view all users
        return null;
    }
    public boolean updateUserRole(int userID, String newRole) {
        // Implement logic to update user role
        return true;
    }
    public boolean deactivateUser(int userID) {
        // Implement logic to deactivate user
        return true;
    }
}

// Strategy Pattern for Payment Processing
interface PaymentStrategy {
    boolean processPayment(double amount);
}
class CreditCardPayment implements PaymentStrategy {
    public boolean processPayment(double amount) {
        // Implement credit card payment processing logic
        return true;
    }
}
class UPI implements PaymentStrategy {
    public boolean processPayment(double amount) {
        // Implement PayPal payment processing logic
        return true;
    }
}
class NetBanking implements PaymentStrategy {
    public boolean processPayment(double amount) {
        // Implement PayPal payment processing logic
        return true;
    }
}
class PaymentProcessor {
    private  PaymentStrategy paymentStrategy;
    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
    public boolean executePayment(double amount) {
        return paymentStrategy.processPayment(amount);
    }
}
// Factory Pattern for Email + PushInbox + SMS Notification
interface Notification {
    void sendNotification(String message, String recipient);
}
class EmailNotification implements Notification {
    public void sendNotification(String message, String recipient) {
        // Implement email sending logic
    }
}
class SMSNotification implements Notification {
    public void sendNotification(String message, String recipient) {
        // Implement SMS sending logic
    }
}
class PushInboxNotification implements Notification {
    public void sendNotification(String message, String recipient) {
        // Implement push inbox sending logic
    }
}
enum NotificationType {
    EMAIL,
    SMS,
    PUSH_INBOX
}
class NotificationFactory {
    public static Notification createNotification(NotificationType notificationType) {
        switch (notificationType) {
            case NotificationType.EMAIL:
                return new EmailNotification();
            case NotificationType.SMS:
                return new SMSNotification();
            case NotificationType.PUSH_INBOX:
                return new PushInboxNotification();
            default:
                throw new IllegalArgumentException("Unknown notification type: " + notificationType);
        }
    }
}

class AirlineReservationFacade {
    private IUserAuthentication userAuthentication;
    private IUserRegistration userRegistration;
    private IFlightManagement flightManagement;
    private ISelectAndSearchFlight selectAndSearchFlight;
    private ISeatSelection seatSelection;
    private IBookingService bookingService;
    private IReminderService reminderService;
    private IGenerateReport generateReport;
    private IBookingHistory bookingHistory;
    private IReviewService reviewService;
    private IManageUsers manageUsers;
    private PaymentProcessor paymentProcessor;

    public AirlineReservationFacade() {
        this.userAuthentication = new UserAuthentication();
        this.userRegistration = new UserRegistration();
        this.flightManagement = new FlightManagement();
        this.selectAndSearchFlight = new SelectAndSearchFlight();
        this.seatSelection = new SeatSelection();
        this.bookingService = new BookingService();
        this.reminderService = new ReminderService();
        this.generateReport = new GenerateReport();
        this.bookingHistory = new BookingHistory();
        this.reviewService = new ReviewService();
        this.manageUsers = new ManageUsers();
        this.paymentProcessor = new PaymentProcessor();
    }

    public boolean loginUser() {
        return userAuthentication.login();
    }
    public boolean logoutUser() {
        return userAuthentication.logout();
    }
    public boolean registerUser() {
        return userRegistration.registration();
    }
    public boolean addFlight(Flight flight) {
        return flightManagement.addFlight(flight);
    }
    public List<Flight> searchFlights(String origin, String destination, Date date, int passengers) {
        return selectAndSearchFlight.searchFlight(origin, destination, date, passengers);
    }
    public boolean bookFlight(int userID, int flightID, List<Passenger> passengers, double totalAmount) {
        return bookingService.createBooking(userID, flightID, passengers, totalAmount);
    }
    public Boolean cancelBooking(int bookingID) {
        return bookingService.cancelBooking(bookingID);
    }
    public Boolean modifyBooking(int bookingID, List<Passenger> updatedPassengers) {
        return bookingService.modifyBooking(bookingID, updatedPassengers);
    }
    public void sendReminder(int bookingId) {
        reminderService.sendReminder(bookingId);
    }
    public void generateReport(String reportType) {
        generateReport.generateReport(reportType);
    }
    public List<Booking> viewBookingHistory(int userID) {
        return bookingHistory.viewBookingHistory(userID);
    }
    public List<Booking> viewAllBookingHistory() {
        return bookingHistory.viewAllBookingHistory();
    }
    public Boolean addReview(int userID, int flightID, int rating, String comments) {
        return reviewService.addReview(userID, flightID, rating, comments);
    }
    public List<Review> viewReviews(int flightID) {
        return reviewService.viewReviews(flightID);
    }
    public List<User> viewAllUsers() {
        return manageUsers.viewAllUsers();
    }
    public Boolean updateUserRole(int userID, String newRole) {
        return manageUsers.updateUserRole(userID, newRole);
    }
    public Boolean deactivateUser(int userID) {
        return manageUsers.deactivateUser(userID);
    }
    public boolean processPayment(PaymentStrategy paymentStrategy, double amount) {
        this.paymentProcessor = new PaymentProcessor();
        this.paymentProcessor.setPaymentStrategy(paymentStrategy);
        return this.paymentProcessor.executePayment(amount);
    }
}

public class AirLineReservationSystem {
    public static void main(String[] args) {
        // Implement Facade pattern to simplify interactions
        AirlineReservationFacade airlineReservationFacade = new AirlineReservationFacade();
        airlineReservationFacade.registerUser();
        airlineReservationFacade.loginUser();
        // Further operations like booking, seat selection, payment, etc.
        airlineReservationFacade.addFlight(new Flight(1, "AA101", "NYC", "LAX", new Date(), new Date(), 300.0, 150, 150));
        airlineReservationFacade.searchFlights("NYC", "LAX", new Date(), 2);
        Passenger passenger = new Passenger(1, "John Doe", 30, "Male", "P123456", "12A", 1);
        airlineReservationFacade.bookFlight(1, 101, List.of(passenger), 500.0);
        PaymentStrategy paymentStrategy = new CreditCardPayment();
        airlineReservationFacade.processPayment(paymentStrategy, 500.0);
        airlineReservationFacade.sendReminder(1);
        airlineReservationFacade.generateReport("PDF");

        airlineReservationFacade.logoutUser();

    }
}
