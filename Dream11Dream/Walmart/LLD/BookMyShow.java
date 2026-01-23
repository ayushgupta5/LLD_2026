package Walmart.LLD;/*
Functional Requirements: 
1.Users can search for shows based on a movie title and a city. 
2. The system should support multiple cities, cinemas, screens, and shows. 
3. Each screen has a defined layout of seats with different types (e.g., REGULAR, PREMIUM). 
4. A user can book one or more available seats for a specific show. 
 
5.Users can subscribe to movies and receive notifications when booking opens for them. 
bonus: The system must be flexible to support different payment methods.


class Walmart.LLD.User{}
interface Walmart.LLD.ISearchShowsService {

}
1. Search Walmart.LLD.Shows (movieTitle, city)
Walmart.LLD.City --> []Cinemas
Walmart.LLD.Cinema --> []Walmart.LLD.Screen

Walmart.LLD.Screen --> []shows --> []Seats --> Enum (REGULAR, PREMIUM)
Screen1 (totalSeats=100, REGULAR=75, PREMIUM=25)
Walmart.LLD.Shows

Walmart.LLD.BookingManager()
Payment
* */

class User {
    private int userID;
    private String userName;
    private String userEmail;

}
enum SeatType {
    REGULAR, PREMIUM;
}
class Seat {
    private int seatID;
    private SeatType seatType;
    private int seatNumber;
    private boolean isEmpty;

}
class Shows {
    private int showID;
    List<Seat> seats;
    List<User> users;
    private Date showTime;
    private String movieName;

}
class Screen {
    private int screenID;
    List<Show> shows;

}
class Cinema {
    private int cinemaID;
    String cinemaName; //pvr
    List<Screen> screens;
    Private String Address;

}
class City {
    private int cityID;
    private  String cityName;
    List<Cinema> cimnemas;
}
class Booking {
    private int bookingID;
    private String userID;
    private int ShowID;
    private List<seat> seats;
    Ciname cinema;
}

interface ISearchShowsService {
    List<show> SearchShow(String MovieTitle, String City);
    Show SelectShows();

}


interface IBookingManager {
    Booking BookShow(Show, User);
}

class BookingManager implememnts IBookingManager {
    List<Seat> seats = new ArrayList<>();
    seats.add(1);
    seats.add(2);
    seats.add(3);

    HashMap <String, Booking> bookingMap;

    Booking BookShow(List<Seat> seats, Show show) {
        // validate seats
        for(Seat seat: seats) {
            seat.setIsEmpty(false);
        }
        Booking Booking = new (1, 1, 123, seats );
        booking.lock();
        lockKey=createLockKey(uuid, booking)
        redis.store(lockKey);

        bookingMap.put(Booking.bookingID, Booking);
        booking.releaseLock();

    }
}

interface PaymentStrategy {
    void pay();
}
class UPI implements PaymentStrategy {
    void Pay() {
        sop("Walmart.LLD.UPI Payment");
    }
}
class CrediCard implements PaymentStrategy {
    void Pay() {
        sop("Walmart.LLD.UPI Payment");
    }
}
class NetBanking implements PaymentStrategy {
    void Pay() {
        sop("Walmart.LLD.UPI Payment");
    }
}

class PaymentProcessor {
    PaymentStrategy paymentStrategy;
    void setPaymentStrategy(PaymentStrategy paymentStrateg) {
        this.paymentStrategy = paymentStrategy;
    }
    void pay() {
        paymentStrategy.pay();
    }
}

interface NotificationStareggy {
    void pay();
}
class SMS implements NotificationStareggy {
    void Pay() {
        sop("Walmart.LLD.UPI Payment");
    }
}
class Email implements NotificationStareggy {
    void Pay() {
        sop("Walmart.LLD.UPI Payment");
    }
}
class PushInbox implements NotificationStareggy {
    void Pay() {
        sop("Walmart.LLD.UPI Payment");
    }
}


// paramDTo --> NotiFyAPI -->
public class BookMyShow {
    main() {

    }
}
/*

Department
        DdepartmentID;
departmentName;

Employee
        employeeId
DepartmentID
        salary

find max salary for each department

departmentName, Maxsalary

select max(salary) as Maxsalary from emoployee groupby(departmentName)

select departmentName from department where (
)

SELECT
d.departmentName,
MAX(e.salary) AS MaxSalary
FROM Employee e
JOIN Department d
ON e.departmentID = d.departmentID
GROUP BY d.departmentName;


        SELECT
                departmentName,
                MaxSalary
FROM Department d
JOIN (
                SELECT departmentID, MAX(salary) AS MaxSalary
FROM Employee
GROUP BY departmentID
) e
ON d.departmentID = e.departmentID;
*/



















