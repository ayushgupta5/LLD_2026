# Parking Lot Low Level Design - Interview Guide

## 📋 Overview

This guide helps you ace a 45-minute Low Level Design (LLD) interview for a Parking Lot System where **runnable code is NOT expected**, but the goal is to demonstrate design thinking, structure, and clarity.

---

## ⏱️ Time Allocation Strategy

| Time | Phase | Focus |
|------|-------|-------|
| 0-5 min | Requirement Clarification | Ask questions, state assumptions |
| 5-15 min | High-Level Design | Class diagram, relationships |
| 15-35 min | Code Key Classes | Write class signatures, key methods |
| 35-40 min | Design Patterns & SOLID | Discuss extensibility |
| 40-45 min | Scenario Walkthrough | Handle questions |

---

## 🎯 Phase 1: Requirement Clarification (5 minutes)

### Questions to Ask

**Functional Requirements:**
- Should we support multiple parking lot locations or just one?
- What vehicle types? Car, Bike, Truck, Electric vehicles?
- How is pricing calculated? Hourly? Per vehicle type? Flat rate?
- Do we need to support reserved/handicapped spots?
- Should the system assign spots automatically or allow customer choice?

**Non-Functional Requirements:**
- Should we handle thread-safety for concurrent entries/exits?
- Should we optimize for spot-finding speed?
- Do we need payment processing or just calculate amount?

### Example Assumptions

```
✓ One parking lot with multiple floors
✓ Support Car, Bike, and Truck
✓ Automatic spot assignment (nearest available)
✓ Hourly pricing with different rates per vehicle type
✓ No payment gateway integration (just calculation)
```

---

## 🏗️ Phase 2: High-Level Design (10 minutes)

### Core Entities

```
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
└─────────────┘
```

### Key Relationships

- **ParkingLot → Floors → Spots** (Composition)
- **Ticket** references Vehicle and ParkingSpot
- **Strategy pattern** for ParkingStrategy and PricingStrategy

---

## 💻 Phase 3: Code Structure (20 minutes)

### 1. Enums (Start Here!)

```java
public enum VehicleType {
    CAR, BIKE, TRUCK, ELECTRIC_CAR
}

public enum SpotType {
    COMPACT,    // For bikes
    REGULAR,    // For cars
    LARGE       // For trucks
}

public enum SpotStatus {
    AVAILABLE, OCCUPIED, RESERVED
}
```

### 2. Vehicle Hierarchy

```java
public abstract class Vehicle {
    private String licensePlate;
    private VehicleType type;
    
    public abstract SpotType getRequiredSpotType();
    
    // Getters, setters
}

public class Car extends Vehicle {
    @Override
    public SpotType getRequiredSpotType() {
        return SpotType.REGULAR;
    }
}

public class Bike extends Vehicle {
    @Override
    public SpotType getRequiredSpotType() {
        return SpotType.COMPACT;
    }
}

public class Truck extends Vehicle {
    @Override
    public SpotType getRequiredSpotType() {
        return SpotType.LARGE;
    }
}
```

### 3. ParkingSpot

```java
public class ParkingSpot {
    private String spotId;
    private SpotType type;
    private SpotStatus status;
    private Vehicle currentVehicle;
    
    public boolean isAvailable() {
        return status == SpotStatus.AVAILABLE;
    }
    
    public boolean canFitVehicle(Vehicle vehicle) {
        return type == vehicle.getRequiredSpotType();
    }
    
    public void assignVehicle(Vehicle vehicle) {
        this.currentVehicle = vehicle;
        this.status = SpotStatus.OCCUPIED;
    }
    
    public void removeVehicle() {
        this.currentVehicle = null;
        this.status = SpotStatus.AVAILABLE;
    }
    
    // Getters, setters
}
```

### 4. Floor

```java
public class Floor {
    private int floorNumber;
    private List<ParkingSpot> spots;
    
    public ParkingSpot findAvailableSpot(Vehicle vehicle) {
        // Returns first available spot of required type
        return spots.stream()
            .filter(spot -> spot.isAvailable() && 
                           spot.canFitVehicle(vehicle))
            .findFirst()
            .orElse(null);
    }
    
    public int getAvailableSpotCount(SpotType type) {
        return (int) spots.stream()
            .filter(spot -> spot.getType() == type && spot.isAvailable())
            .count();
    }
    
    // Getters, setters
}
```

### 5. Ticket

```java
public class Ticket {
    private String ticketId;
    private Vehicle vehicle;
    private ParkingSpot assignedSpot;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    
    // Getters, setters
}
```

### 6. ParkingLot (Singleton Pattern)

```java
public class ParkingLot {
    private static ParkingLot instance;
    private List<Floor> floors;
    private ParkingStrategy parkingStrategy;
    private PricingStrategy pricingStrategy;
    
    private ParkingLot() {
        // Private constructor
        this.floors = new ArrayList<>();
        this.parkingStrategy = new NearestSpotStrategy();
        this.pricingStrategy = new HourlyPricingStrategy();
    }
    
    public static synchronized ParkingLot getInstance() {
        if (instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }
    
    public Ticket issueTicket(Vehicle vehicle) {
        ParkingSpot spot = parkingStrategy.findSpot(vehicle, floors);
        
        if (spot == null) {
            throw new NoSpotAvailableException("No spot available for vehicle type: " 
                                               + vehicle.getType());
        }
        
        spot.assignVehicle(vehicle);
        
        Ticket ticket = new Ticket(
            generateTicketId(),
            vehicle,
            spot,
            LocalDateTime.now()
        );
        
        return ticket;
    }
    
    public Payment processExit(Ticket ticket) {
        ticket.setExitTime(LocalDateTime.now());
        
        double amount = pricingStrategy.calculatePrice(ticket);
        
        ticket.getAssignedSpot().removeVehicle();
        
        return new Payment(ticket, amount);
    }
    
    private String generateTicketId() {
        // In production: UUID.randomUUID().toString()
        return "TKT-" + System.currentTimeMillis();
    }
    
    // Getters, setters
}
```

### 7. Strategy Pattern - Parking Strategy

```java
public interface ParkingStrategy {
    ParkingSpot findSpot(Vehicle vehicle, List<Floor> floors);
}

public class NearestSpotStrategy implements ParkingStrategy {
    @Override
    public ParkingSpot findSpot(Vehicle vehicle, List<Floor> floors) {
        // Find nearest available spot starting from ground floor
        for (Floor floor : floors) {
            ParkingSpot spot = floor.findAvailableSpot(vehicle);
            if (spot != null) {
                return spot;
            }
        }
        return null;
    }
}

public class RandomSpotStrategy implements ParkingStrategy {
    @Override
    public ParkingSpot findSpot(Vehicle vehicle, List<Floor> floors) {
        // Randomly select from available spots
        // Implementation details...
        return null;
    }
}
```

### 8. Strategy Pattern - Pricing Strategy

```java
public interface PricingStrategy {
    double calculatePrice(Ticket ticket);
}

public class HourlyPricingStrategy implements PricingStrategy {
    private Map<VehicleType, Double> hourlyRates;
    
    public HourlyPricingStrategy() {
        hourlyRates = new HashMap<>();
        hourlyRates.put(VehicleType.BIKE, 10.0);
        hourlyRates.put(VehicleType.CAR, 20.0);
        hourlyRates.put(VehicleType.TRUCK, 30.0);
    }
    
    @Override
    public double calculatePrice(Ticket ticket) {
        long hours = calculateHours(ticket.getEntryTime(), 
                                    ticket.getExitTime());
        double rate = hourlyRates.get(ticket.getVehicle().getType());
        return hours * rate;
    }
    
    private long calculateHours(LocalDateTime entry, LocalDateTime exit) {
        long minutes = Duration.between(entry, exit).toMinutes();
        return (minutes / 60) + (minutes % 60 > 0 ? 1 : 0); // Ceiling
    }
}

public class FlatRatePricingStrategy implements PricingStrategy {
    private double flatRate;
    
    @Override
    public double calculatePrice(Ticket ticket) {
        return flatRate;
    }
}
```

### 9. Payment

```java
public class Payment {
    private String paymentId;
    private Ticket ticket;
    private double amount;
    private LocalDateTime paymentTime;
    private PaymentStatus status;
    
    public Payment(Ticket ticket, double amount) {
        this.paymentId = generatePaymentId();
        this.ticket = ticket;
        this.amount = amount;
        this.paymentTime = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
    }
    
    private String generatePaymentId() {
        return "PAY-" + System.currentTimeMillis();
    }
    
    // Getters, setters
}

public enum PaymentStatus {
    PENDING, COMPLETED, FAILED
}
```

---

## ⚠️ What NOT to Write (Time Savers!)

### ❌ Skip These

- Complete implementations of utility methods (calculateHours, generateId)
- Detailed exception handling code
- All constructor parameters
- Database/persistence layer
- Payment gateway integration
- Thread-safety implementation details

### ✅ Instead Say

- "The ID generation would use UUID in production"
- "I'd add proper exception handling with custom exceptions"
- "For thread-safety, I'd use ReentrantLock on spot assignment"
- "In production, this would persist to a database"

---

## 🎨 Phase 4: Design Patterns & SOLID (5 minutes)

### Design Patterns Used

#### 1. Singleton Pattern
**Where:** ParkingLot class
**Why:** Only one instance of parking lot should exist

#### 2. Strategy Pattern
**Where:** ParkingStrategy and PricingStrategy
**Why:**
- Swap between nearest/random/optimized spot assignment
- Different pricing models (hourly/daily/flat)
- Open for extension without modifying existing code

#### 3. Factory Pattern (Bonus)
**Where:** VehicleFactory, SpotFactory
**Why:** Encapsulate object creation logic

#### 4. Observer Pattern (Advanced)
**Where:** DisplayBoard observing spot changes
**Why:** Real-time updates to display boards

### SOLID Principles

#### S - Single Responsibility Principle
✓ Each class has one job
- `ParkingSpot` manages its own state
- `Floor` manages collection of spots
- `Ticket` holds parking session information

#### O - Open/Closed Principle
✓ Open for extension, closed for modification
- Can add new `VehicleType` without changing existing code
- Can add new `PricingStrategy` without modifying `ParkingLot`

#### L - Liskov Substitution Principle
✓ Subtypes can replace base types
- Any `Vehicle` subclass can replace `Vehicle`
- Any `ParkingStrategy` implementation can replace the interface

#### I - Interface Segregation Principle
✓ Clients shouldn't depend on interfaces they don't use
- Separate interfaces for `ParkingStrategy` and `PricingStrategy`
- No fat interfaces with unrelated methods

#### D - Dependency Inversion Principle
✓ Depend on abstractions, not concretions
- `ParkingLot` depends on `ParkingStrategy` interface
- Can inject different implementations at runtime

---

## 🚀 Phase 5: Extensibility Discussion (5 minutes)

### "This design is extensible for..."

#### 1. New Vehicle Types
```java
public class ElectricCar extends Vehicle {
    @Override
    public SpotType getRequiredSpotType() {
        return SpotType.ELECTRIC; // New enum value
    }
}
```
**Impact:** No changes to core parking logic

#### 2. Dynamic Pricing
```java
public class WeekendPricingStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(Ticket ticket) {
        // 1.5x rates on weekends
    }
}
```
**Usage:** Swap strategy at runtime based on day

#### 3. EV Charging Spots
```java
public class ElectricSpot extends ParkingSpot {
    private boolean isCharging;
    private int chargingRate;
    
    public void startCharging() { }
    public void stopCharging() { }
}
```

#### 4. Multiple Parking Lots
```java
public interface IParkingLot {
    Ticket issueTicket(Vehicle vehicle);
    Payment processExit(Ticket ticket);
}

public class ParkingLotManager {
    private Map<String, IParkingLot> parkingLots;
    
    public IParkingLot findNearestLot(Location location) { }
}
```

#### 5. Reservation System
```java
public class ReservationManager {
    private Map<String, Reservation> reservations;
    
    public Reservation reserveSpot(Vehicle vehicle, 
                                   LocalDateTime startTime, 
                                   LocalDateTime endTime) { }
    
    public void cancelReservation(String reservationId) { }
}
```

#### 6. Display Boards (Observer Pattern)
```java
public interface ParkingObserver {
    void update(Floor floor, SpotType type, int availableCount);
}

public class DisplayBoard implements ParkingObserver {
    @Override
    public void update(Floor floor, SpotType type, int availableCount) {
        // Update display
    }
}
```

---

## ⚡ Common Pitfalls to Avoid

### ❌ Don't

- Jump straight to coding without clarifying requirements
- Create god classes that do everything
- Hardcode values (rates, spot counts)
- Forget to handle "spot not available" scenario
- Mix concerns (e.g., Ticket calculating price)
- Write production-ready code with error handling

### ✅ Do

- Ask clarifying questions first
- Draw class diagram before coding
- Use enums for fixed sets of values
- Mention edge cases even if you don't implement
- Keep talking and explaining your thought process
- Focus on design structure over implementation details

---

## 🎬 Sample Interview Flow Script

### Minutes 0-5: Clarification
> "Let me clarify the requirements. Should we support multiple vehicle types?"
>
> [Interviewer responds]
>
> "Great, I'll design for Car, Bike, and Truck. How should pricing work?"
>
> [Discuss]
>
> "Okay, I'll use hourly pricing with different rates per type."

### Minutes 5-10: High-Level Design
> "Let me draw the high-level structure. We'll have a ParkingLot containing Floors, each Floor has multiple ParkingSpots. I'll use the Singleton pattern for ParkingLot since there's only one. The Vehicle will be an abstract class with Car, Bike, Truck subclasses. Does this structure make sense?"

### Minutes 10-30: Code Implementation
> [Write code]
>
> "Starting with enums for type safety... Now the Vehicle hierarchy... ParkingSpot will manage its own occupancy state following Single Responsibility Principle... The ParkingLot will delegate spot-finding to a ParkingStrategy interface - this follows the Strategy pattern and makes it easy to swap algorithms..."

### Minutes 30-40: Design Patterns
> "For extensibility, I've used Strategy pattern for both parking and pricing. If we need to add electric vehicle charging, we can extend SpotType and create ElectricSpot. The design is Open/Closed - open for extension, closed for modification."

### Minutes 40-45: Walkthrough
> "Let me walk through a scenario:
> 1. Vehicle arrives → ParkingLot.issueTicket()
> 2. Strategy finds spot → Spot marks occupied → Return ticket
> 3. On exit → processExit() → Calculate price → Free spot
>
> Any questions on the design?"

---

## 💡 Impression Boosters

### Power Phrases to Use

- "I'm following the Single Responsibility Principle here..."
- "This is the Strategy pattern, which gives us flexibility to..."
- "For thread-safety in production, I'd add..."
- "An alternative approach would be..., but I chose this because..."
- "This design scales well if we need to add..."

### Communication Tips

- ✓ Think aloud constantly
- ✓ Draw diagrams on whiteboard
- ✓ Ask "Does this make sense?" periodically
- ✓ Write clean, formatted code with proper indentation
- ✓ Use meaningful variable names
- ✓ Acknowledge trade-offs

---

## ✅ Pre-Submission Checklist

Before interview ends, ensure:

```
□ Clarified requirements and stated assumptions
□ Drew class diagram showing relationships
□ Implemented core classes with clear responsibilities
□ Used at least one design pattern (Strategy/Singleton)
□ Mentioned SOLID principles application
□ Discussed extensibility/scalability
□ Handled main flow (entry/exit)
□ Addressed edge case (no spots available)
□ Kept code readable and well-structured
□ Asked interviewer for feedback
```

---

## 🎯 Key Takeaway

### The Interviewer is NOT Evaluating:
- Whether your code compiles
- Perfect syntax
- Complete implementation
- All edge cases handled

### The Interviewer IS Evaluating:
- Structured thinking and problem decomposition
- OOP concepts and design patterns knowledge
- Code organization and readability
- Communication and collaboration skills
- Ability to make trade-offs
- Extensibility thinking

### Your Goal:
Show you can design a system that another engineer would want to work with. Clean structure, clear responsibilities, easy to extend.

---

## 📚 Additional Resources

### Other LLD Problems to Practice
- Elevator System
- Library Management System
- Hotel Walmart.LLD.Booking System
- ATM Machine
- Snake and Ladder Game
- Chess Game
- Splitwise / Expense Sharing

### Recommended Study Topics
- SOLID Principles (deep dive)
- Design Patterns (Gang of Four)
- UML Class Diagrams
- Object-Oriented Analysis and Design

---

## 📝 Notes Section

Use this space to add your own observations, interviewer feedback, or variations you encounter:

```
[Your notes here]
```

---

**Good Luck! 🚀**

Remember: The goal is not perfect code, but demonstrating clear thinking, good design principles, and effective communication.