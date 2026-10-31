/*
Design Book My Show LLD

#Functional Requirement
1. User Registration and Authentication
2. Movie Listing and Search
3. Showtime and Seat Selection
4. Booking and Payment Processing
5. Booking History and Ticket Management
#Non-Functional Requirements:
        1. Performance: The system should handle high traffic during peak hours with minimal latency.
        2. Scalability: The system should be able to scale horizontally to accommodate a growing user base and increasing number of movies and showtimes.
3. Security: Ensure secure handling of user data and payment information.
4. Usability: The user interface should be intuitive and easy to navigate.
*/

import java.util.List;

class User{}
class Movie{}
class Seat{}
class Payment{}
class Booking{}

interface IRegister{
    void Register();
}
interface IAuthentication{
    void Login();
    void LogOut();
}
interface ISearchMovie{
    List<Movie>  SearchMovie();
    Movie ListMovie(int movieID);
}
interface IShowTime{
    List<String> ShowTime(int movieID, String movieName);
}
interface ISeatSelector {
    List<Seat> SeatSelection();
}

interface IBookingHistory {
    List<Booking> BookingHistory();
}

interface ITicketManagement {
    String PrintTicket();
}

interface PaymentStrategy {
    void pay(int amount);
}
class CreditCard implements PaymentStrategy {

    @Override
    public void pay(int amount) {

    }
}

class UPI implements PaymentStrategy {

    @Override
    public void pay(int amount) {

    }
}

class NetBanking implements PaymentStrategy {

    @Override
    public void pay(int amount) {

    }
}

class PaymentService {
    PaymentStrategy paymentStrategy;
    void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
    void pay(int amount) {
        paymentStrategy.pay(amount);
    }
}

public class Book {
    public static void main(String[] args) {
        PaymentService paymentService = new PaymentService();
        int amount = 500;
        PaymentStrategy paymentStrategy=new UPI();

    }
}
