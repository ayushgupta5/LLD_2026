import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.*;

/* ===================== USER ===================== */

class User {
    private int userID;
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String address;

    public User(int userID, String name, String email, String password, String phoneNumber, String address) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public int getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}

/* ===================== AUTH ===================== */

interface UserAuthenticationInterface {
    Boolean login(String email, String password);

    Boolean logout(int userID);
}

class UserAuthenticationImpl implements UserAuthenticationInterface {
    public Boolean login(String email, String password) {
        return true;
    }

    public Boolean logout(int userID) {
        return true;
    }
}

interface UserRegistrationServiceInterface {
    User registerUser(String name, String email, String phoneNumber, String address);
}

class UserRegistrationServiceImpl implements UserRegistrationServiceInterface {
    public User registerUser(String name, String email, String phoneNumber, String address) {
        return new User(1, name, email, "hashed-password", phoneNumber, address);
    }
}

/* ===================== MOVIE ===================== */

class Movie {
    private int movieID;
    private String title;
    private String genre;
    private String duration;
    private String language;
    private String rating;
    private String description;

    public Movie(int movieID, String title, String genre, String duration, String language, String rating, String description) {
        this.movieID = movieID;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.language = language;
        this.rating = rating;
        this.description = description;
    }

    public int getMovieID() {
        return movieID;
    }

    public String getTitle() {
        return title;
    }
    public String getGenre() {
        return genre;
    }
    public String getLanguage() {
        return language;
    }

}

class MovieSearchCriteria {
    String title;
    String genre;
    String language;
}

interface MovieServiceInterface {
    List<Movie> movieListing();

    Movie searchMovie(String title, String genre);

    List<Movie> searchMovie(MovieSearchCriteria criteria);
}

class MovieServiceImpl implements MovieServiceInterface {
    public List<Movie> movieListing() {
        return null;
    }

    public Movie searchMovie(String title, String genre) {
        return null;
    }

    public List<Movie> searchMovie(MovieSearchCriteria criteria) {
        return null;
    }
}

/* ===================== THEATER ===================== */

class Theater {
    private int theaterID;
    private Screen[] screens;
    String name;
    String location;
    String contactNumber;

    public Theater(int theaterID, Screen[] screens, String name, String location, String contactNumber) {
        this.theaterID = theaterID;
        this.screens = screens;
        this.name = name;
        this.location = location;
        this.contactNumber = contactNumber;
    }

    public int getTheaterID() {
        return theaterID;
    }
}

interface TheaterServiceInterface {
    List<Theater> searchTheaters(String location, String movieTitle);

    List<Theater> showTheaters(String movieTitle);
}

class TheaterServiceImpl implements TheaterServiceInterface {
    public List<Theater> searchTheaters(String location, String movieTitle) {
        return null;
    }

    public List<Theater> showTheaters(String movieTitle) {
        return null;
    }
}

/* ===================== SCREEN ===================== */

class Screen {
    private int screenID;
    private int theaterID;
    private int totalSeats;
    private String screenType;
    private Movie movie;
    private String showTime;

    public Screen(int screenID, int theaterID, int totalSeats, String screenType, Movie movie, String showTime) {
        this.screenID = screenID;
        this.theaterID = theaterID;
        this.totalSeats = totalSeats;
        this.screenType = screenType;
        this.movie = movie;
        this.showTime = showTime;
    }

    public int getScreenID() {
        return screenID;
    }
}

/* ===================== SEAT ===================== */

class Seat {
    private int seatNumber;
    private boolean available = true;

    public Seat(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public void reserve() {
        this.available = false;
    }
}

/* ===================== BOOKING ===================== */

class Booking {
    private int bookingID;
    private int userID;
    private int screenID;
    private int[] seatNumbers;
    private String bookingTime;
    String status;
    private Theater theater;

    public Booking(int bookingID, int userID, int screenID, int[] seatNumbers, String bookingTime, String status, Theater theater) {
        this.bookingID = bookingID;
        this.userID = userID;
        this.screenID = screenID;
        this.seatNumbers = seatNumbers;
        this.bookingTime = bookingTime;
        this.status = status;
        this.theater = theater;
    }

    public int getBookingID() {
        return bookingID;
    }

    public void confirm() {
        this.status = "CONFIRMED";
    }

    public void cancel() {
        this.status = "CANCELLED";
    }
}

/* ===================== REPOSITORIES ===================== */

interface BookingRepository {
    Booking save(Booking booking);

    Booking findById(int bookingID);
}

interface ScreenRepository {
    Screen findById(int screenID);
}

// In-memory implementations to satisfy the references in main
class InMemoryBookingRepository implements BookingRepository {
    private final Map<Integer, Booking> storage = new HashMap<>();

    public Booking save(Booking booking) {
        // store booking using its bookingID
        storage.put(booking.getBookingID(), booking);
        return booking;
    }

    public Booking findById(int bookingID) {
        return storage.get(bookingID);
    }
}

class InMemoryScreenRepository implements ScreenRepository {
    private final Map<Integer, Screen> screens = new HashMap<>();

    public InMemoryScreenRepository() {
        // no-op; screens can be returned as default placeholders if not present
    }

    public Screen findById(int screenID) {
        Screen s = screens.get(screenID);
        if (s != null) return s;
        // Return a basic placeholder Screen when none is pre-populated
        return new Screen(screenID, 0, 100, "Standard", null, "00:00");
    }
}

/* ===================== BOOKING SERVICE ===================== */

interface BookingServiceInterface {
    Booking createBooking(int userID, int screenID, int[] seatNumbers);

    Booking cancelBooking(int bookingID);
}

class BookingServiceImpl implements BookingServiceInterface {

    private final BookingRepository bookingRepository;
    private final ScreenRepository screenRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, ScreenRepository screenRepository) {
        this.bookingRepository = bookingRepository;
        this.screenRepository = screenRepository;
    }

    public Booking createBooking(int userID, int screenID, int[] seatNumbers) {
        Screen screen = screenRepository.findById(screenID);
        Booking booking = new Booking(1, userID, screenID, seatNumbers, "NOW", "CREATED", null);
        return bookingRepository.save(booking);
    }

    public Booking cancelBooking(int bookingID) {
        Booking booking = bookingRepository.findById(bookingID);
        booking.cancel();
        return bookingRepository.save(booking);
    }
}

/* ===================== PAYMENT ===================== */

class PaymentResult {
    boolean success;
    String message;

    public PaymentResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}

interface PaymentStrategy {
    PaymentResult processPayment(int bookingID);
}

class CreditCardPayment implements PaymentStrategy {
    public PaymentResult processPayment(int bookingID) {
        return new PaymentResult(true, "Credit Card Payment Successful");
    }
}

class UPI implements PaymentStrategy {
    public PaymentResult processPayment(int bookingID) {
        return new PaymentResult(true, "UPI Payment Successful");
    }
}

class NetBanking implements PaymentStrategy {
    public PaymentResult processPayment(int bookingID) {
        return new PaymentResult(true, "NetBanking Payment Successful");
    }
}

class PaymentService {
    private PaymentStrategy paymentStrategy;

    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.paymentStrategy = strategy;
    }

    public PaymentResult processPayment(int bookingID) {
        return paymentStrategy.processPayment(bookingID);
    }
}

/* ===================== TICKET ===================== */

class Ticket {
    int ticketID;
    int bookingID;
    private int seatNumber;
    String showTime;
    private Theater theater;
    private Movie movie;

    public Ticket(int ticketID, int bookingID, int seatNumber, String showTime, Theater theater, Movie movie) {
        this.ticketID = ticketID;
        this.bookingID = bookingID;
        this.seatNumber = seatNumber;
        this.showTime = showTime;
        this.theater = theater;
        this.movie = movie;
    }
}

interface TicketServiceInterface {
    Ticket generateTicket(int bookingID);

    Ticket viewTicket(int ticketID);
}

class TicketServiceImpl implements TicketServiceInterface {
    public Ticket generateTicket(int bookingID) {
        return null;
    }

    public Ticket viewTicket(int ticketID) {
        return null;
    }
}

/* ===================== ORCHESTRATOR ===================== */

class BookingOrchestrator {

    private final BookingServiceInterface bookingService;
    private final PaymentService paymentService;
    private final TicketServiceInterface ticketService;

    public BookingOrchestrator(BookingServiceInterface bookingService,
                               PaymentService paymentService,
                               TicketServiceInterface ticketService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.ticketService = ticketService;
    }

    public Ticket confirmBooking(int userID, int screenID, int[] seats) {
        Booking booking = bookingService.createBooking(userID, screenID, seats);
        PaymentResult result = paymentService.processPayment(booking.getBookingID());

        if (!result.success) {
            booking.cancel();
            return null;
        }

        booking.confirm();
        return ticketService.generateTicket(booking.getBookingID());
    }
}

/* ===================== ENTRY ===================== */


public class BookMyShow {

    public static void main(String[] args) {
        System.out.println("=== Welcome to BookMyShow! ===\n");

        // ============ SETUP: Initialize Dependencies ============

        // Create repositories (mock implementations)
        BookingRepository bookingRepo = new InMemoryBookingRepository();
        ScreenRepository screenRepo = new InMemoryScreenRepository();

        // Create services
        BookingServiceInterface bookingService = new BookingServiceImpl(bookingRepo, screenRepo);
        PaymentService paymentService = new PaymentService();
        TicketServiceInterface ticketService = new TicketServiceImpl();

        // Create orchestrator
        BookingOrchestrator orchestrator = new BookingOrchestrator(
                bookingService,
                paymentService,
                ticketService
        );

        // Authentication services
        UserAuthenticationInterface authService = new UserAuthenticationImpl();
        UserRegistrationServiceInterface registrationService = new UserRegistrationServiceImpl();

        // Movie and Theater services
        MovieServiceInterface movieService = new MovieServiceImpl();
        TheaterServiceInterface theaterService = new TheaterServiceImpl();


        // ============ STEP 1: User Registration & Login ============
        System.out.println("--- Step 1: User Registration ---");
        User user = registrationService.registerUser(
                "John Doe",
                "john.doe@email.com",
                "+91-9876543210",
                "123 MG Road, Bangalore"
        );
        System.out.println("✓ User registered: " + user.getName() + " (ID: " + user.getUserID() + ")");

        System.out.println("\n--- Step 2: User Login ---");
        boolean loginSuccess = authService.login("john.doe@email.com", "password123");
        System.out.println("✓ Login " + (loginSuccess ? "successful" : "failed"));


        // ============ STEP 2: Browse Movies ============
        System.out.println("\n--- Step 3: Browse Movies ---");

        // Create sample movies
        Movie movie1 = new Movie(101, "Inception", "Sci-Fi", "148 min", "English", "PG-13",
                "A mind-bending thriller about dream manipulation");
        Movie movie2 = new Movie(102, "The Dark Knight", "Action", "152 min", "English", "PG-13",
                "Batman faces the Joker in Gotham City");
        Movie movie3 = new Movie(103, "Interstellar", "Sci-Fi", "169 min", "English", "PG-13",
                "A journey through space and time to save humanity");

        List<Movie> availableMovies = Arrays.asList(movie1, movie2, movie3);

        System.out.println("Available Movies:");
        for (Movie m : availableMovies) {
            System.out.println("  [" + m.getMovieID() + "] " + m.getTitle() +
                    " (" + m.getGenre() + ") - " + m.getLanguage());
        }

        // Search for a specific movie
        System.out.println("\n--- Step 4: Search for 'Inception' ---");
        MovieSearchCriteria criteria = new MovieSearchCriteria();
        criteria.title = "Inception";
        criteria.genre = "Sci-Fi";
        System.out.println("✓ Found: " + movie1.getTitle());


        // ============ STEP 3: Find Theaters & Shows ============
        System.out.println("\n--- Step 5: Find Theaters for 'Inception' ---");

        // Create screens with shows
        Screen screen1 = new Screen(1, 1, 100, "IMAX", movie1, "18:00");
        Screen screen2 = new Screen(2, 1, 80, "Standard", movie1, "21:00");
        Screen screen3 = new Screen(3, 2, 120, "IMAX", movie1, "19:30");

        // Create theaters
        Theater pvr = new Theater(1, new Screen[]{screen1, screen2},
                "PVR Cinemas", "Koramangala, Bangalore", "+91-80-12345678");
        Theater inox = new Theater(2, new Screen[]{screen3},
                "INOX", "MG Road, Bangalore", "+91-80-87654321");

        List<Theater> theaters = Arrays.asList(pvr, inox);

        System.out.println("Theaters showing 'Inception':");
        for (Theater t : theaters) {
            System.out.println("  • " + t.name + " - " + t.location);
            System.out.println("    Contact: " + t.contactNumber);
        }


        // ============ STEP 4: Select Show & Seats ============
        System.out.println("\n--- Step 6: Select Show ---");
        System.out.println("Selected: PVR Cinemas - IMAX - 18:00");
        System.out.println("Screen ID: " + screen1.getScreenID());

        System.out.println("\n--- Step 7: Select Seats ---");
        int[] selectedSeats = {15, 16, 17}; // Row 2, Seats 15-17
        System.out.println("Selected seats: " + Arrays.toString(selectedSeats));

        // Create sample seats and reserve them
        List<Seat> seats = new ArrayList<>();
        for (int seatNum : selectedSeats) {
            Seat seat = new Seat(seatNum);
            seat.reserve();
            seats.add(seat);
            System.out.println("  ✓ Seat " + seatNum + " reserved");
        }


        // ============ STEP 5: Payment ============
        System.out.println("\n--- Step 8: Payment ---");

        // Create booking first
        Booking booking = bookingService.createBooking(
                user.getUserID(),
                screen1.getScreenID(),
                selectedSeats
        );
        System.out.println("Booking created with ID: " + booking.getBookingID());

        // Choose payment method
        System.out.println("\nAvailable payment methods:");
        System.out.println("  1. Credit Card");
        System.out.println("  2. UPI");
        System.out.println("  3. Net Banking");
        System.out.println("\nSelected: UPI");

        // Set payment strategy
        paymentService.setPaymentStrategy(new UPI());

        // Process payment
        PaymentResult paymentResult = paymentService.processPayment(booking.getBookingID());
        System.out.println("Payment Status: " + paymentResult.message);

        if (paymentResult.success) {
            booking.confirm();
            System.out.println("✓ Booking confirmed!");
        }


        // ============ STEP 6: Generate Ticket ============
        System.out.println("\n--- Step 9: Generate Ticket ---");

        Ticket ticket = new Ticket(
                1001,
                booking.getBookingID(),
                selectedSeats[0],
                "18:00",
                pvr,
                movie1
        );

        System.out.println("\n" + "=".repeat(50));
        System.out.println("           🎬 BOOKING CONFIRMATION 🎬");
        System.out.println("=".repeat(50));
        System.out.println("Ticket ID     : " + ticket.ticketID);
        System.out.println("Booking ID    : " + ticket.bookingID);
        System.out.println("Movie         : " + movie1.getTitle());
        System.out.println("Theater       : " + pvr.name);
        System.out.println("Location      : " + pvr.location);
        System.out.println("Screen        : IMAX");
        System.out.println("Show Time     : " + ticket.showTime);
        System.out.println("Seats         : " + Arrays.toString(selectedSeats));
        System.out.println("Customer      : " + user.getName());
        System.out.println("Booking Time  : " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("=".repeat(50));


        // ============ BONUS: Demonstrate Different Payment Methods ============
        System.out.println("\n--- Bonus: Testing Other Payment Methods ---");

        // Create another booking
        Booking booking2 = bookingService.createBooking(user.getUserID(), screen2.getScreenID(),
                new int[]{20, 21});

        // Try Credit Card
        paymentService.setPaymentStrategy(new CreditCardPayment());
        PaymentResult result1 = paymentService.processPayment(booking2.getBookingID());
        System.out.println("Credit Card: " + result1.message);

        // Try Net Banking
        paymentService.setPaymentStrategy(new NetBanking());
        PaymentResult result2 = paymentService.processPayment(booking2.getBookingID());
        System.out.println("Net Banking: " + result2.message);


        // ============ STEP 7: Cancellation Demo ============
        System.out.println("\n--- Step 10: Booking Cancellation Demo ---");
        System.out.println("Cancelling booking ID: " + booking2.getBookingID());
        Booking cancelledBooking = bookingService.cancelBooking(booking2.getBookingID());
        System.out.println("✓ Booking cancelled. Status: " + cancelledBooking.status);


        // ============ STEP 8: Logout ============
        System.out.println("\n--- Step 11: Logout ---");
        boolean logoutSuccess = authService.logout(user.getUserID());
        System.out.println("✓ User logged out successfully");

        System.out.println("\n" + "=".repeat(50));
        System.out.println("   Thank you for using BookMyShow! 🎉");
        System.out.println("=".repeat(50));
    }
}
