import java.util.*;
/*
#Functional Requirement of Book My Show
1. User can Search Movie (name, genre, language, releaseDate)
2. User can View Movie Details
3. User can select a Movie and view available showTime and Theater
4. User can book the Ticket
5. User can do the Payment
6. User should Get the Notification
*/

// Classes: User, Movie, Seat, Screen, Ticket, Theater, ShowTime, Payment, Notification

class Movie {
    private int movieID;
    private String name;
    private String genre;
    private String language;
    private String cast;
    private Date releaseDate;

    public int getMovieID() {
        return movieID;
    }

    public void setMovieID(int movieID) {
        this.movieID = movieID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Movie(int movieID, String name, String genre, String language, String cast, Date releaseDate) {
        this.movieID = movieID;
        this.name = name;
        this.genre = genre;
        this.language = language;
        this.cast = cast;
        this.releaseDate = releaseDate;
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
class Ticket {
    private int ticketID;
    private int movieID;
    private int paymentID;
    private int seatID;
    private int theaterID;

    public int getTicketID() {
        return ticketID;
    }

    public void setTicketID(int ticketID) {
        this.ticketID = ticketID;
    }

    public int getMovieID() {
        return movieID;
    }

    public void setMovieID(int movieID) {
        this.movieID = movieID;
    }

    public int getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(int paymentID) {
        this.paymentID = paymentID;
    }

    public int getSeatID() {
        return seatID;
    }

    public void setSeatID(int seatID) {
        this.seatID = seatID;
    }

    public int getTheaterID() {
        return theaterID;
    }

    public void setTheaterID(int theaterID) {
        this.theaterID = theaterID;
    }

    public Ticket(int ticketID, int movieID, int paymentID, int seatID, int theaterID) {
        this.ticketID = ticketID;
        this.movieID = movieID;
        this.paymentID = paymentID;
        this.seatID = seatID;
        this.theaterID = theaterID;
    }
}
class Screen {
    private int screenID;
    private int movieID;
    private List<Seat> seats;

    public int getScreenID() {
        return screenID;
    }

    public void setScreenID(int screenID) {
        this.screenID = screenID;
    }

    public int getMovieID() {
        return movieID;
    }

    public void setMovieID(int movieID) {
        this.movieID = movieID;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public Screen(int screenID, int movieID, List<Seat> seats) {
        this.screenID = screenID;
        this.movieID = movieID;
        this.seats = seats;
    }
}
class Theater {
    private int theaterID;
    private String location;
    private String theaterName;
    private List<Screen> screens;

    public int getTheaterID() {
        return theaterID;
    }

    public void setTheaterID(int theaterID) {
        this.theaterID = theaterID;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTheaterName() {
        return theaterName;
    }

    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
    }

    public List<Screen> getScreens() {
        return screens;
    }

    public void setScreens(List<Screen> screens) {
        this.screens = screens;
    }

    public Theater(int theaterID, String location, String theaterName, List<Screen> screens) {
        this.theaterID = theaterID;
        this.location = location;
        this.theaterName = theaterName;
        this.screens = screens;
    }
}
class ShowTime {
    private int showTimeID;
    private Movie movie;
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

    public ShowTime(int showTimeID, Movie movie, Theater theater, List<Seat> availableSeats) {
        this.showTimeID = showTimeID;
        this.movie = movie;
        this.theater = theater;
        this.availableSeats = availableSeats;
    }
}
class Booking {
    private int bookingID;
    private User user;
    private ShowTime showTime;
    private List<Seat> bookedSeats;
    private double amount;

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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Booking(int bookingID, User user, ShowTime showTime, List<Seat> bookedSeats, double amount) {
        this.bookingID = bookingID;
        this.user = user;
        this.showTime = showTime;
        this.bookedSeats = bookedSeats;
        this.amount = amount;
    }
}
class Payment {
    private int paymentID;
    private double amount;
    private String paymentMethod;
    private Date date;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Payment(int paymentID, double amount, String paymentMethod, Date date) {
        this.paymentID = paymentID;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.date = date;
    }
}
class Notification {
    private int notificationID;
    private String message;
    private User user;
    private String templateID;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTemplateID() {
        return templateID;
    }

    public void setTemplateID(String templateID) {
        this.templateID = templateID;
    }

    public Notification(int notificationID, String message, User user, String templateID) {
        this.notificationID = notificationID;
        this.message = message;
        this.user = user;
        this.templateID = templateID;
    }
}

// Functionalities: searchMovies(), viewMovieDetails(), viewAvailableShowTime(), bookTicket(), payment(), notification()
// ISP Principal
interface ISearchMovies {
    List<Movie> searchMovie(String name, String genre, String language);
}
class SearchMovies implements ISearchMovies {
    List<Movie> movieDB;

    public SearchMovies() {
        this.movieDB = new ArrayList<>();
    }

    @Override
    public List<Movie> searchMovie(String name, String genre, String language) {
        List<Movie> resultMovie = new ArrayList<>();
        for (Movie movie : movieDB) {
            if (movie.getName().equalsIgnoreCase(name) || movie.getGenre().equalsIgnoreCase(genre) || movie.getLanguage().equalsIgnoreCase(language)) {
                resultMovie.add(movie);
            }
        }
        return resultMovie;
    }
}

interface IViewMovieDetails {
    Movie viewMovieDetails(int movieID);
}
class ViewMovieDetails implements IViewMovieDetails {
    List<Movie> movieDB;

    public ViewMovieDetails() {
        this.movieDB = new ArrayList<>();
    }

    @Override
    public Movie viewMovieDetails(int movieID) {
        for(Movie movie: movieDB) {
            if(movie.getMovieID() == movieID) {
                return movie;
            }
        }
        return null;
    }
}

interface IViewAvailableShowTime {
    List<ShowTime> viewAvailableShowTime(int movieID);
}
class ViewAvailableShowTime implements IViewAvailableShowTime {
    List<ShowTime> showTimeDB;

    public ViewAvailableShowTime() {
        this.showTimeDB = new ArrayList<>();
    }

    @Override
    public List<ShowTime> viewAvailableShowTime(int movieID) {
        List<ShowTime> resultShowTime = new ArrayList<>();
        for(ShowTime showTime: showTimeDB) {
            if(showTime.getMovie().getMovieID() == movieID) {
                resultShowTime.add(showTime);
            }
        }
        return resultShowTime;
    }
}

interface IBookTicket {
    Booking bookTicket(int userID, int showTimeID, List<String> seatNumbers);
}
class BookTicket implements IBookTicket {

    @Override
    public Booking bookTicket(int userID, int showTimeID, List<String> seatNumbers) {
        // Lock
        // check Seat is available or not
        // then do the booking
        return null;
    }
}

// Facade Pattern for Book My Show
class BookMyShowFacade {
    private final ISearchMovies searchMovies;
    private final IViewMovieDetails viewMovieDetails;
    private final IViewAvailableShowTime viewAvailableShowTime;
    private final IBookTicket bookTicket;
    public BookMyShowFacade() {
        this.searchMovies = new SearchMovies();
        this.viewMovieDetails = new ViewMovieDetails();
        this.viewAvailableShowTime = new ViewAvailableShowTime();
        this.bookTicket = new BookTicket();
    }

    public List<Movie> searchMovie(String name, String genre, String language) {
        return searchMovies.searchMovie(name, genre, language);
    }

    public Movie viewMovieDetails(int movieID) {
        return viewMovieDetails.viewMovieDetails(movieID);
    }

    public List<ShowTime> viewAvailableShowTime(int movieID) {
        return viewAvailableShowTime.viewAvailableShowTime(movieID);
    }
    public Booking bookTicket(int userID, int showTimeID, List<String> seatNumbers) {
        return bookTicket.bookTicket(userID, showTimeID, seatNumbers);
    }

}

// Strategy Pattern for Payment (Cash, UPI, CreditCard...)
interface PaymentStrategy {
    void pay(double amount);
}
class UPI implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("Payment is in Progress with amount :" + amount + " with UPI");
    }
}
class CASH implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("Payment is in Progress with amount :" + amount + " with CASH");
    }
}
class CreditCard implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("Payment is in Progress with amount :" + amount + " with CreditCard");
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

// Observer Pattern for Notification
interface Observer {
    void update(String message);
}
class User implements Observer{
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
        System.out.println("Sending Notification to user: " + userName + " with given Message : " + message);
    }
}
interface Subject {
    void subscribe(Observer observer);
    void unsubscribe(Observer observer);
    void notify(String message);
}
class NotificationService implements Subject {
    List<Observer> observerList;

    public NotificationService(List<Observer> observerList) {
        this.observerList = observerList;
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