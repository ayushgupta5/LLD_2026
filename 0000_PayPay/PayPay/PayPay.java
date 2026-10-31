import java.util.*;


/*
# Functional Requirements:-
*/

// functionalities for Book My Show
// Search Movie
interface ISearchMovie{
    List<Movie> searchMovies(String name, String genre, String language, Date releaseDate);
}
class SearchMovie implements ISearchMovie {

    @Override
    public List<Movie> searchMovies(String name, String genre, String language, Date releaseDate) {
        return List.of();
    }
}
// Movie Details
interface IMovieDetails {
    Movie viewMovieDetails(int movieID);
}
class MovieDetails implements IMovieDetails {

    @Override
    public Movie viewMovieDetails(int movieID) {
        return null;
    }
}
// Select Show Time
interface ISelectShowTime {
    List<ShowTime> selectShowTime(int movieID);
}
class SelectShowTime implements ISelectShowTime {

    @Override
    public List<ShowTime> selectShowTime(int movieID) {
        return List.of();
    }
}
// Book Tickets
interface IBookTickets {
    Ticket bookTickets(int showTimeID, List<Seat> seats);
}
class BookTickets implements IBookTickets {

    @Override
    public Ticket bookTickets(int showTimeID, List<Seat> seats) {
        return null;
    }
}

// Facade pattern for Book My Show LLD
class BookMyShowFacade {
    private ISearchMovie searchMovie;
    private IMovieDetails movieDetails;
    private ISelectShowTime selectShowTime;
    private IBookTickets bookTickets;
    public BookMyShowFacade() {
        this.searchMovie = new SearchMovie();
        this.movieDetails = new MovieDetails();
        this.selectShowTime = new SelectShowTime();
        this.bookTickets = new BookTickets();
    }

    public List<Movie> searchMovies(String name, String genre, String language, Date releaseDate) {
        return searchMovie.searchMovies(name, genre, language, releaseDate);
    }
    public Movie viewMovieDetails(int movieID) {
        return movieDetails.viewMovieDetails(movieID);
    }
    public List<ShowTime> selectShowTime(int movieID) {
        return selectShowTime.selectShowTime(movieID);
    }
    public Ticket bookTickets(int showTimeID, List<Seat> seats) {
        return bookTickets.bookTickets(showTimeID, seats);
    }
}

// classes for Book My Show, Seat, Movie, ShowTime, Theater, Ticket, Booking
class Movie {
    private int movieID;
    private String movieName;
    private String genre;
    private String releaseDate;
    private String cast;
    private String crew;

    public int getMovieID() {
        return movieID;
    }

    public void setMovieID(int movieID) {
        this.movieID = movieID;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public String getCrew() {
        return crew;
    }

    public void setCrew(String crew) {
        this.crew = crew;
    }

    public Movie(int movieID, String movieName, String genre, String releaseDate, String cast, String crew) {
        this.movieID = movieID;
        this.movieName = movieName;
        this.genre = genre;
        this.releaseDate = releaseDate;
        this.cast = cast;
        this.crew = crew;
    }
}
class Seat {
    private int seatID;
    private String seatNumber;
    private boolean isAvailable;

    public int getSeatID() {
        return seatID;
    }

    public void setSeatID(int seatID) {
        this.seatID = seatID;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public Seat(int seatID, String seatNumber, boolean isAvailable) {
        this.seatID = seatID;
        this.seatNumber = seatNumber;
        this.isAvailable = isAvailable;
    }
}
class Theater {
    private int theaterID;
    private String name;
    private String location;
    private List<ShowTime> showTimes;

    public int getTheaterID() {
        return theaterID;
    }

    public void setTheaterID(int theaterID) {
        this.theaterID = theaterID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<ShowTime> getShowTimes() {
        return showTimes;
    }

    public void setShowTimes(List<ShowTime> showTimes) {
        this.showTimes = showTimes;
    }

    public Theater(int theaterID, String name, String location, List<ShowTime> showTimes) {
        this.theaterID = theaterID;
        this.name = name;
        this.location = location;
        this.showTimes = showTimes;
    }
}
class ShowTime {
    private int showTimeID;
    private Movie movie;
    private Date showTime;
    private Theater theater;
    private List<Seat> availableSeats;

    public int getShowTimeID() {
        return showTimeID;
    }

    public void setShowTimeID(int showTimeID) {
        this.showTimeID = showTimeID;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Date getShowTime() {
        return showTime;
    }

    public void setShowTime(Date showTime) {
        this.showTime = showTime;
    }

    public Theater getTheater() {
        return theater;
    }

    public void setTheater(Theater theater) {
        this.theater = theater;
    }

    public List<Seat> getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(List<Seat> availableSeats) {
        this.availableSeats = availableSeats;
    }

    public ShowTime(int showTimeID, Movie movie, Date showTime, Theater theater, List<Seat> availableSeats) {
        this.showTimeID = showTimeID;
        this.movie = movie;
        this.showTime = showTime;
        this.theater = theater;
        this.availableSeats = availableSeats;
    }
}
class Ticket {
    private int ticketID;
    private ShowTime showTime;
    private List<Seat> bookedSeats;
    private double price;

    public int getTicketID() {
        return ticketID;
    }

    public void setTicketID(int ticketID) {
        this.ticketID = ticketID;
    }

    public ShowTime getShowTime() {
        return showTime;
    }

    public void setShowTime(ShowTime showTime) {
        this.showTime = showTime;
    }

    public List<Seat> getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(List<Seat> bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Ticket(int ticketID, ShowTime showTime, List<Seat> bookedSeats, double price) {
        this.ticketID = ticketID;
        this.showTime = showTime;
        this.bookedSeats = bookedSeats;
        this.price = price;
    }
}
class Booking {
    private int bookingID;
    private User user;
    private Ticket ticket;
    private Payment payment;

    public int getBookingID() {
        return bookingID;
    }

    public void setBookingID(int bookingID) {
        this.bookingID = bookingID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Booking(int bookingID, User user, Ticket ticket, Payment payment) {
        this.bookingID = bookingID;
        this.user = user;
        this.ticket = ticket;
        this.payment = payment;
    }
}

// Strategy Pattern for Payment
class Payment {
    private int paymentID;
    private double amount;
    private String paymentMethod;

    public int getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(int paymentID) {
        this.paymentID = paymentID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Payment(int paymentID, double amount, String paymentMethod) {
        this.paymentID = paymentID;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
}
interface PaymentStrategy{
    void pay(double amount);
}
class UPI implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Payment with UPI is in Progress");
    }
}
class CASH implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Payment with CASH is in Progress");
    }
}
class CreditCard implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Payment with CreditCard is in Progress");
    }
}
class PaymentProcessor {
    private PaymentStrategy paymentStrategy;
    void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
    void pay(double amount) {
        paymentStrategy.pay(amount);
    }
}

// Observer Pattern for Notification for Book My Show
interface Observer {
    void update(String message);
}
class User implements Observer {
    private int userID;
    private String userName;
    private String userEmail;
    private String userPhone;

    public User(int userID, String userName, String userEmail, String userPhone) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    @Override
    public void update(String message) {
        System.out.println("Notification for " + userName + ": " + message);
    }
}
interface Subject {
    void subscribe(Observer observer);
    void unsubscribe(Observer observer);
    void notify(String message);
}
class NotificationService implements Subject {
    List<Observer> observerList;

    public NotificationService() {
        this.observerList = new ArrayList<>();
    }

    @Override
    public void subscribe(Observer observer) {
        observerList.add(observer);
    }

    @Override
    public void unsubscribe(Observer observer) {
        observerList.remove(observer);
    }

    @Override
    public void notify(String message) {
        for(Observer observer: observerList) {
            observer.update(message);
        }
    }
}
class Notification {
    private int notificationID;
    private String message;
    private String templateID;

    public Notification(int notificationID, String message, String templateID) {
        this.notificationID = notificationID;
        this.message = message;
        this.templateID = templateID;
    }

    public int getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(int notificationID) {
        this.notificationID = notificationID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTemplateID() {
        return templateID;
    }

    public void setTemplateID(String templateID) {
        this.templateID = templateID;
    }
}

public class PayPay {
}
