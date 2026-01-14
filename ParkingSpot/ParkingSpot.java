import java.util.Date;
import java.util.List;
import java.util.ArrayList;

// ParkingSpot.java - simplified, self-contained LLD sample classes

/*

┌─────────────┐
│ ParkingLot  │ (Singleton)
└──────┬──────┘
       │ has
       ▼
┌─────────────┐
│   Floor     │ (1 to many)
└──────┬──────┘
       │ has
       ▼
┌─────────────┐
│ ParkingSpot │ (many per floor)
└─────────────┘

┌─────────────┐
│   Vehicle   │ (Abstract/Interface)
└──────┬──────┘
       │
   ┌───┴───┬───────┐
   ▼       ▼       ▼
  Car    Bike   Truck

┌─────────────┐
│   Ticket    │ (issued on entry)
└─────────────┘

┌─────────────┐
│   Payment   │ (calculates amount)

 */

public class ParkingSpot {
    int spotID;
    String spotType; // e.g., Compact, Large, Handicapped
    boolean isOccupied;

    public ParkingSpot() {}

    public ParkingSpot(int spotID, String spotType) {
        this.spotID = spotID;
        this.spotType = spotType;
        this.isOccupied = false;
    }

    void assignSpot(Car vehicle) {
        this.isOccupied = true;
        // In a fuller design we would keep a reference to the vehicle
    }

    void vacateSpot() {
        this.isOccupied = false;
    }

    // Main method to demonstrate the system
    public static void main(String[] args) {
        System.out.println("=== Parking Lot System Demo ===\n");

        // Create parking lot (singleton)
        ParkingLot parkingLot = ParkingLot.getInstance();
        parkingLot.lotID = 1;
        parkingLot.location = "Downtown";
        System.out.println("Created Parking Lot at: " + parkingLot.location);

        // Create floor
        Floor floor1 = new Floor(1);
        System.out.println("Created Floor: " + floor1.floorNumber);

        // Create parking spots
        ParkingSpot spot1 = new ParkingSpot(101, "Compact");
        ParkingSpot spot2 = new ParkingSpot(102, "Large");
        ParkingSpot spot3 = new ParkingSpot(103, "Handicapped");

        floor1.addParkingSpot(spot1);
        floor1.addParkingSpot(spot2);
        floor1.addParkingSpot(spot3);
        parkingLot.addFloor(floor1);
        System.out.println("Added 3 parking spots to floor 1\n");

        // Create vehicles
        Car car = new Car(1, "ABC123", "Red", "Toyota Camry");
        Bike bike = new Bike(2, "XYZ789", "Mountain");
        Truck truck = new Truck(3, "TRK456", 5000.0);

        System.out.println("Vehicles created:");
        System.out.println("- Car: " + car.licensePlate + " (" + car.model + ")");
        System.out.println("- Bike: " + bike.licensePlate + " (" + bike.type + ")");
        System.out.println("- Truck: " + truck.licensePlate + " (Load: " + truck.loadCapacity + " kg)\n");

        // Assign parking spots
        spot1.assignSpot(car);
        System.out.println("Spot " + spot1.spotID + " assigned to car " + car.licensePlate);
        System.out.println("Spot occupied: " + spot1.isOccupied + "\n");

        // Create ticket
        Date entryTime = new Date();
        Ticket ticket = new Ticket(1001, entryTime, spot1.spotID, car.vehicleID);
        System.out.println("Ticket created:");
        System.out.println("- Ticket ID: " + ticket.ticketID);
        System.out.println("- Entry Time: " + ticket.entryTime);
        System.out.println("- Spot ID: " + ticket.parkingSpotID + "\n");

        // Simulate some parking time (2 hours)
        try {
            Thread.sleep(100); // Small delay for demo
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ticket.exitTime = new Date(entryTime.getTime() + (2 * 60 * 60 * 1000)); // 2 hours later

        double duration = ticket.calculateParkingDuration();
        double fee = ticket.calculateParkingFee();

        System.out.println("Vehicle Exit:");
        System.out.println("- Exit Time: " + ticket.exitTime);
        System.out.println("- Parking Duration: " + String.format("%.2f", duration) + " hours");
        System.out.println("- Parking Fee: $" + String.format("%.2f", fee) + "\n");

        // Process payment
        Payment payment = new Payment(5001, fee, new Date(), "Card");
        System.out.println("Payment processed:");
        System.out.println("- Payment ID: " + payment.paymentID);
        System.out.println("- Amount: $" + String.format("%.2f", payment.amount));
        System.out.println("- Method: " + payment.paymentMethod + "\n");

        // Vacate spot
        spot1.vacateSpot();
        System.out.println("Spot " + spot1.spotID + " vacated");
        System.out.println("Spot occupied: " + spot1.isOccupied);

        System.out.println("\n=== Demo Complete ===");
    }
}
class Ticket{
    int ticketID;
    Date entryTime;
    Date exitTime;
    int parkingSpotID;
    int vehicleID;

    public Ticket() {}

    public Ticket(int ticketID, Date entryTime, int parkingSpotID, int vehicleID) {
        this.ticketID = ticketID;
        this.entryTime = entryTime;
        this.parkingSpotID = parkingSpotID;
        this.vehicleID = vehicleID;
    }

    /**
     * Calculates parking duration in hours (fractional).
     * If exitTime is null, returns 0.
     */
    public double calculateParkingDuration() {
        if (entryTime == null || exitTime == null) return 0.0;
        long diffMillis = exitTime.getTime() - entryTime.getTime();
        return diffMillis / (1000.0 * 60.0 * 60.0);
    }

    /**
     * Simple fee calculation: baseRatePerHour * hours (rounded up to nearest 0.5h for demonstration).
     */
    public double calculateParkingFee() {
        double hours = calculateParkingDuration();
        if (hours <= 0) return 0.0;
        double baseRatePerHour = 10.0; // example rate
        return baseRatePerHour * hours;
    }
}

class Car extends  Vehicle {
    int vehicleID;
    String licensePlate;
    String color;
    String model;

    public Car() {}

    public Car(int vehicleID, String licensePlate, String color, String model) {
        this.vehicleID = vehicleID;
        this.licensePlate = licensePlate;
        this.color = color;
        this.model = model;
    }
}

class Bike extends Vehicle {
    int vehicleID;
    String licensePlate;
    String type; // e.g., Mountain, Road

    public Bike() {}

    public Bike(int vehicleID, String licensePlate, String type) {
        this.vehicleID = vehicleID;
        this.licensePlate = licensePlate;
        this.type = type;
    }
}

class Truck extends Vehicle {
    int vehicleID;
    String licensePlate;
    double loadCapacity;

    public Truck() {}

    public Truck(int vehicleID, String licensePlate, double loadCapacity) {
        this.vehicleID = vehicleID;
        this.licensePlate = licensePlate;
        this.loadCapacity = loadCapacity;
    }
}

abstract class Vehicle{
    int vehicleID;
    String licensePlate;

    public Vehicle() {}

    public Vehicle(int vehicleID, String licensePlate) {
        this.vehicleID = vehicleID;
        this.licensePlate = licensePlate;
    }

    public String getVehicleType() {
        return "GenericVehicle";
    }
}

class Floor{
    int floorNumber;
    List<ParkingSpot> parkingSpots;

    public Floor() {
        this.parkingSpots = new ArrayList<>();
    }

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.parkingSpots = new ArrayList<>();
    }

    public void addParkingSpot(ParkingSpot spot) {
        this.parkingSpots.add(spot);
    }

    public void removeParkingSpot(int spotID) {
        this.parkingSpots.removeIf(s -> s.spotID == spotID);
    }
}

class ParkingLot{
    int lotID;
    String location;
    List<Floor> floors;
    static ParkingLot instance;

    public ParkingLot() {
        this.floors = new ArrayList<>();
    }

    public static synchronized ParkingLot getInstance() {
        if (instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }

    public void addFloor(Floor floor) {
        this.floors.add(floor);
    }
}