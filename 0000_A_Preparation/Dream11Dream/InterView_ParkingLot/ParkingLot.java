/*
1. User can Register
2. User can Login/logout
3.

class :-
User --> UserID, UserName, Email, phoneNumber
Vehicle (Bike, Car) --> VehicleID, Name, Model, Type(car, Bike etc, etc)
ParkingSpot --> id, spotNumber isEmpty, floorID
Floor --> FloorID, parkingSpotID, FloorNumber
Payment --> PaymentID, PaymentMethod, Time, Amount, UserID, Status
Ticket --> TicketID, FloorID, ParkingSpotID, UserID, PaymentID

Floor-->ParkingSpot-->
Vehicle(Car, Bike)
Payment(UPI | CreditCard | NetBanking)
Ticket(TicketID, FloorNo, ParkingSpotID, UserID, PaymentID)

IsEmptySlot()
GetEmptyParkingSlotID();
BookParkingSpot();
Payment()
GenerateTicket();
ReleseParkingSpot();
 */


class User {
    private String userID;
    private String userName;
    private String UserEmail;
    private String PhoneNumber;
}

enum VehicleType {
    CAR, BIKE
}

class Vehicle {
    private String VehicleID;
    private String model;
    private String vehiclePlateNumber;
    private VehicleType VehicleType;
}

interface PaymentStrategy {
    void Pay(int amount);
}
class CreditCard implements PaymentStrategy {

    @Override
    public void Pay(int amount) {

    }
}
class UPI implements PaymentStrategy {

    @Override
    public void Pay(int amount) {

    }
}
class NetBanking implements PaymentStrategy {

    @Override
    public void Pay(int amount) {

    }
}
class PaymentProcessor {
    PaymentStrategy paymentStrategy;
    void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
    void pay(int amount) {
        paymentStrategy.Pay(amount);
    }
}
public class ParkingLot {
}
