# LLD Interview Complete Guide - Requirements & UML Practice

## Challenge 1: Requirements Khud Se Kaise Nikaalein

### The CRAFT Framework (Interview mein ye follow karo)

#### **C - Clarify the Core Use Case**
```
Interviewer: "Design a Parking Lot"

Aap: "Sure! Let me clarify the main functionality:
- Should the system handle vehicle entry and exit?
- Do we need to track payment/billing?
- Should we support different vehicle types?"
```

#### **R - Real-world Analogy**
```
Aap khud se socho: "Real parking lot mein kya hota hai?"
1. Vehicle aati hai → Entry gate
2. Spot milta hai → Assignment
3. Park karti hai → Occupancy
4. Nikalti hai → Exit + Payment
```

#### **A - Actors Identify Karo**
```
Kaun kaun interact karega?
- Customer (vehicle owner)
- Parking Attendant
- System Admin
- Automated System
```

#### **F - Features Priority Wise**
```
Must Have (Core):
- Vehicle entry/exit
- Spot assignment
- Basic billing

Good to Have (Extended):
- Different spot types
- Multiple floors
- Reservation system

Out of Scope (Clarify):
- Mobile app
- Security cameras
- Valet service
```

#### **T - Technical Constraints**
```
Aap poocho:
"Should I consider:
- Concurrency? (Multiple vehicles at once)
- Scalability? (Multi-location support)
- Real-time? (Instant availability updates)"
```

---

## Interview Simulation Example

### Problem: "Design a Library Management System"

**Step 1 - Core Use Case (30 sec):**
```
"I'm assuming:
- Users can borrow and return books
- System tracks book availability
- Basic search functionality
Should I also consider reservations and fines?"
```

**Step 2 - Actors (15 sec):**
```
"Main actors would be:
- Member (borrows books)
- Librarian (manages books)
- System (automated processes)
Correct?"
```

**Step 3 - Requirements List (1 min):**
```
Must Have:
✓ Add/Remove books
✓ Issue/Return books
✓ Search books
✓ Track availability
✓ Member management

Good to Have:
○ Book reservation
○ Fine calculation
○ Multiple copies
```

**Step 4 - Start Design:**
```
"Let me start with the core entities..."
```

---

## Challenge 2: UML Diagram Kaise Practice Karein

### Step-by-Step Approach for Drawing LLD Diagrams

#### **Step 1: Saare Nouns (Entities) Identify Karo**
Problem statement padhke saare important nouns nikaliye:
- Parking Lot
- Floor
- Parking Spot
- Vehicle (Car, Bike, Truck)
- Ticket
- Payment

**Tip:** Ye aapke classes ban jayengi

---

#### **Step 2: Relationships Pehchaniye (4 Main Types)**

##### **A) HAS-A (Composition/Aggregation)** - "contains/has"
```
ParkingLot HAS Floors
Floor HAS ParkingSpots
Ticket HAS parkingSpotID
```

**Kaise draw karein:**
```
┌─────────┐       ┌─────────┐
│ Parent  │◆─────▶│ Child   │
└─────────┘       └─────────┘
```
Filled diamond (◆) = strong ownership

##### **B) IS-A (Inheritance)** - "is a type of"
```
Car IS-A Vehicle
Bike IS-A Vehicle
```

**Kaise draw karein:**
```
      ┌─────────┐
      │ Vehicle │ (parent)
      └────△────┘
           │
    ┏━━━━━━┻━━━━━━┓
    ▽              ▽
┌────────┐    ┌────────┐
│  Car   │    │  Bike  │
└────────┘    └────────┘
```
Triangle (△) parent ki taraf point karta hai

##### **C) USES/DEPENDS ON** - "temporarily uses"
```
ParkingSpot uses Vehicle (assign karte waqt)
Ticket uses Payment (calculate fee)
```

**Kaise draw karein:**
```
┌─────────┐  - - - ->  ┌─────────┐
│  Class1 │            │  Class2 │
└─────────┘            └─────────┘
```
Dotted line = temporary relationship

##### **D) ASSOCIATION** - "knows about"
```
Ticket knows vehicleID
Ticket knows parkingSpotID
```

**Kaise draw karein:**
```
┌─────────┐  ──────>  ┌─────────┐
│ Ticket  │           │ Vehicle │
└─────────┘           └─────────┘
```
Simple arrow

---

#### **Step 3: Cardinality (Numbers) Likho**
```
ParkingLot 1 ──── * Floor (one to many)
Floor 1 ──── * ParkingSpot (one to many)
Vehicle 1 ──── 1 Ticket (one to one)
```

---

#### **Step 4: Important Methods/Attributes**

Har class mein:
- **Top section:** Class name
- **Middle section:** Key attributes (2-3 important ones)
- **Bottom section:** Key methods (2-3 important ones)

```
┌─────────────────────┐
│   ParkingSpot       │
├─────────────────────┤
│ - spotID: int       │
│ - spotType: String  │
│ - isOccupied: bool  │
├─────────────────────┤
│ + assignSpot()      │
│ + vacateSpot()      │
└─────────────────────┘
```

---

## Complete Parking Lot Diagram Example

```
                    ┌──────────────────────────┐
                    │      ParkingLot          │
                    │  (Singleton Pattern)     │
                    ├──────────────────────────┤
                    │ - lotID: int             │
                    │ - location: String       │
                    │ - instance: ParkingLot   │
                    ├──────────────────────────┤
                    │ + getInstance()          │
                    │ + addFloor()             │
                    └─────────────┬────────────┘
                                  │
                                  │ 1
                                  │
                                  │ *
                    ┌─────────────▼────────────┐
                    │        Floor             │
                    ├──────────────────────────┤
                    │ - floorNumber: int       │
                    │ - parkingSpots: List     │
                    ├──────────────────────────┤
                    │ + addParkingSpot()       │
                    │ + removeParkingSpot()    │
                    └─────────────┬────────────┘
                                  │
                                  │ 1
                                  │
                                  │ *
                    ┌─────────────▼────────────┐
                    │     ParkingSpot          │
                    ├──────────────────────────┤
                    │ - spotID: int            │
                    │ - spotType: String       │
                    │ - isOccupied: boolean    │
                    ├──────────────────────────┤
                    │ + assignSpot(Car)        │
                    │ + vacateSpot()           │
                    └──────────────────────────┘


┌──────────────────────────┐
│       Vehicle            │  (Abstract)
├──────────────────────────┤
│ - vehicleID: int         │
│ - licensePlate: String   │
├──────────────────────────┤
│ + getVehicleType()       │
└────────────△─────────────┘
             │
      ┌──────┼──────┐
      │      │      │
      ▽      ▽      ▽
  ┌──────┐ ┌────┐ ┌───────┐
  │ Car  │ │Bike│ │ Truck │
  └──────┘ └────┘ └───────┘


┌──────────────────────────┐       ┌──────────────────────────┐
│       Ticket             │       │       Payment            │
├──────────────────────────┤       ├──────────────────────────┤
│ - ticketID: int          │       │ - paymentID: int         │
│ - entryTime: Date        │◆─────▶│ - amount: double         │
│ - exitTime: Date         │       │ - paymentMethod: String  │
│ - vehicleID: int         │       └──────────────────────────┘
│ - parkingSpotID: int     │
├──────────────────────────┤
│ + calculateDuration()    │
│ + calculateFee()         │
└──────────────────────────┘
```

---

## Interview Mein Kaise Draw Karein (Whiteboard Tips)

### Drawing Sequence:
1. **Pehle main entities draw karo** (boxes)
2. **Phir inheritance relationships** (triangle arrows)
3. **Phir composition** (diamond + arrow)
4. **Last mein attributes/methods add karo**

### Time-Saving Tricks:
- Full UML notation ki zaroorat nahi - simple boxes sufficient hai
- Har class mein **maximum 3-4 attributes** likhiye
- **Most important methods** hi likhiye
- Arrows clearly draw karo with labels

### Common Mistakes - Avoid These:
- ❌ Saare methods/attributes likhne ki koshish mat karo
- ❌ Arrows ka direction galat mat karo
- ❌ Relationships confuse mat karo (has-a vs is-a)
- ✅ Clean, readable diagram > perfectly detailed diagram

---

## Practice Methods

### Method 1: Paper-Pen Daily Practice (15 min/day)

**Week 1 - Basic Shapes:**
- **Day 1:** Sirf boxes draw karo (Classes)
- **Day 2:** Inheritance arrows practice (△)
- **Day 3:** Composition arrows practice (◆)
- **Day 4:** Association arrows practice (→)
- **Day 5:** Mix of all relationships
- **Day 6-7:** Complete small diagrams

**Practice drill:**
```
Timer set karo: 3 minutes
Topic: ATM System
Sirf boxes aur arrows - koi detail nahi
```

---

### Method 2: Reverse Engineering (Best Practice)

**Step 1:** Existing code se diagram banao

```java
class Library {
    List<Book> books;
    void addBook(Book b) {}
}

class Book {
    String title;
    Author author;
}

class Author {
    String name;
}
```

**Step 2:** Iska diagram banao

```
┌─────────┐
│ Library │
└────┬────┘
     │ has
     ▼ *
┌─────────┐      ┌─────────┐
│  Book   │─────▶│ Author  │
└─────────┘      └─────────┘
```

---

### Method 3: 5-Minute Challenges

Har din 5 mini-diagrams banao (no details, only structure):

**Day 1:**
1. Hospital System
2. Restaurant Ordering
3. Movie Ticket Booking
4. Online Shopping Cart
5. Traffic Signal System

**Day 2:**
1. Zoom Meeting
2. Spotify Playlist
3. Twitter Post
4. LinkedIn Connection
5. Food Delivery App

**Example 5-min diagram - Twitter Post System:**
```
    ┌────────┐
    │  User  │
    └───┬────┘
        │ creates
        ▼
    ┌────────┐
    │  Post  │
    └───┬────┘
        │ has
    ┌───┴──┬──────┐
    ▼      ▼      ▼
 ┌────┐ ┌────┐ ┌────┐
 │Like│ │Cmnt│ │Shr │
 └────┘ └────┘ └────┘
```

---

### Method 4: Mental Model Building

**Har system ko 3 layers mein socho:**

```
Layer 1 - Core Entities:
└─ Main nouns (Book, User, Order)

Layer 2 - Relationships:
└─ How they connect (has, is-a, uses)

Layer 3 - Behaviors:
└─ Key methods (create, update, delete)
```

**Practice example: Elevator System**

```
Mental Model (30 sec):

Layer 1: Elevator, Floor, Button, Door

Layer 2: 
  - Building HAS Elevators
  - Elevator HAS Door
  - Floor HAS Buttons

Layer 3:
  - Elevator: moveUp(), moveDown(), openDoor()
  - Button: press()
```

**Diagram (2 min):**
```
┌──────────┐
│ Building │
└─────┬────┘
      │ *
      ▼
┌──────────┐     ┌──────┐
│ Elevator │────▶│ Door │
└─────┬────┘     └──────┘
      │
      │ visits
      ▼ *
┌──────────┐     ┌────────┐
│  Floor   │────▶│ Button │
└──────────┘     └────────┘
```

---

## Practice Tools (No Computer Needed)

### 1. Index Cards Method
```
Card 1: Write "Car"
Card 2: Write "Vehicle"
Card 3: Write "Engine"

Ab physically arrange karo table par
- Car ko Vehicle ke neeche (inheritance)
- Engine ko Car ke side (composition)
```

### 2. Whiteboard/Paper Template

```
┌─────────────────────────────────────┐
│  Problem: ___________________       │
│                                     │
│  Main Entities:                     │
│  1. _______________                 │
│  2. _______________                 │
│  3. _______________                 │
│                                     │
│  Relationships:                     │
│  _____ HAS _____                    │
│  _____ IS-A _____                   │
│                                     │
│  [Diagram Space]                    │
│                                     │
│                                     │
│                                     │
└─────────────────────────────────────┘
```

---

## Common Interview Problems (Practice Order)

### Week 1 - Easy:
1. ✅ Parking Lot
2. Library Management
3. Movie Ticket Booking
4. Restaurant Table Booking

### Week 2 - Medium:
5. Hotel Booking System
6. Splitwise (Bill Splitting)
7. Chess Game
8. Elevator System

### Week 3 - Hard:
9. Amazon/Flipkart (E-commerce)
10. Uber/Ola (Cab Booking)
11. Instagram Feed
12. LinkedIn Job Portal

---

## Pro Tips for Interview

### 1. Start with Boxes, Not Details
```
❌ Wrong: Pehle hi saare attributes likhna shuru kar diya

✅ Right:
  Step 1: Saare classes ke boxes banao
  Step 2: Relationships draw karo
  Step 3: Last mein details add karo
```

### 2. Use Abbreviations
```
Instead of:
┌──────────────────────┐
│ calculateParkingFee()│
└──────────────────────┘

Write:
┌──────────┐
│ calcFee()│
└──────────┘
```

### 3. Think Aloud
```
"I'm drawing User class here..."
"This has-a relationship because..."
"Adding Payment here for billing..."
```

### 4. Keep Eraser Ready
```
First attempt kabhi perfect nahi hota
2-3 baar redraw karna normal hai
Interviewer ko bolo: "Let me reorganize this for clarity"
```

---

## Daily Practice Routine (15 min)

### Monday-Friday:
- **5 min:** One new problem requirements
- **7 min:** Draw diagram on paper
- **3 min:** Compare with online solution

### Saturday:
- Review week's diagrams
- Redraw without looking

### Sunday:
- Mock interview with friend
- Time yourself: 10 min per problem

---

## Practice Strategy Summary

**Koi bhi problem mile:**
1. **15 seconds:** Saare nouns list karo
2. **30 seconds:** Relationships identify karo
3. **2 minutes:** Main diagram draw karo
4. **1 minute:** Details add karo

---

## Next Steps

1. **Aaj ka task:** Library Management System design karo (paper pe, 10 min)
2. **Kal:** Hotel Booking System
3. **Parson:** Review your diagrams

**Remember:**
- Requirements clarify karna = 50% of the battle
- Simple, clean diagrams > Complex, messy ones
- Practice makes perfect - 15 min daily is enough!

---

## Quick Reference: Relationship Symbols

```
◆────▶  Composition (strong has-a)
◇────▶  Aggregation (weak has-a)
────▶   Association (knows about)
- - ->  Dependency (uses temporarily)
────△   Inheritance (is-a)

1      One
*      Many
0..1   Optional (zero or one)
1..*   One or more