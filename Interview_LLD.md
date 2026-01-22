# Interview Questions & Solutions - Complete Guide

---

## 🏗️ Low Level Design (LLD)

### 1. Design LRU Cache

**Solution:**
```
Components:
- HashMap<Key, Node> map
- Doubly Linked List (head, tail)

Node Structure:
- key
- value
- prev pointer
- next pointer

Operations:

GET(key):
1. If key not in map → return -1
2. Get node from map
3. Move node to head (most recently used)
4. Return node.value
Time: O(1)

PUT(key, value):
1. If key exists:
   - Update value
   - Move to head
2. If key doesn't exist:
   - Create new node
   - Add to head
   - Add to map
   - If size > capacity:
     * Remove tail node (LRU)
     * Remove from map
Time: O(1)

Helper Methods:
- addToHead(node)
- removeNode(node)
- moveToHead(node)
- removeTail()
```

---

### 2. Design Cache System with PUT, GET, Eviction

**Solution:**
```
Interface: Cache
Methods:
- put(key, value)
- get(key)
- evict()

Classes:
1. CacheNode
   - key, value, frequency, timestamp

2. LRUCache implements Cache
   - HashMap + Doubly LinkedList
   - O(1) operations

3. LFUCache implements Cache
   - HashMap + MinHeap (by frequency)
   - HashMap for frequency tracking

4. FIFOCache implements Cache
   - HashMap + Queue
   - Remove oldest entry

5. CacheFactory
   - static Cache createCache(CacheType type, int capacity)
   - Returns appropriate cache implementation

Eviction Policies:
- LRU: Remove least recently used
- LFU: Remove least frequently used
- FIFO: Remove first inserted
```

---

### 3. Design Parking Lot System

**Solution:**
```
Classes:

1. Vehicle (Abstract)
   - licensePlate: String
   - type: VehicleType
   
2. Car, Bike, Truck extends Vehicle

3. ParkingSpot
   - spotId: int
   - floor: int
   - type: VehicleType
   - isOccupied: boolean
   - vehicle: Vehicle
   
4. ParkingFloor
   - floorNumber: int
   - spots: List<ParkingSpot>
   - availableSpots: Map<VehicleType, List<ParkingSpot>>
   
5. ParkingLot (Singleton)
   - floors: List<ParkingFloor>
   - parkVehicle(Vehicle): ParkingSpot
   - unparkVehicle(ParkingSpot): void
   - getAvailableSpots(VehicleType): int
   
6. Ticket
   - ticketId: String
   - vehicle: Vehicle
   - spot: ParkingSpot
   - entryTime: DateTime
   - exitTime: DateTime
   
7. PaymentSystem
   - calculateFee(Ticket): double
   - processPayment(Ticket, PaymentMethod): boolean

Key Methods:
- parkVehicle(): Find available spot, assign vehicle, generate ticket
- unparkVehicle(): Calculate fee, free spot, process payment
```

---

### 4. Design Hostel Management System

**Solution:**
```
Classes:

1. Person (Abstract)
   - id, name, phone, email
   
2. Student extends Person
   - rollNumber, department, year
   
3. Warden extends Person
   - employeeId, joiningDate
   
4. Room
   - roomNumber, floor, type (Single/Double/Triple)
   - capacity, currentOccupancy
   - students: List<Student>
   
5. Hostel
   - hostelId, name, gender
   - rooms: List<Room>
   - warden: Warden
   
6. Allocation
   - student: Student
   - room: Room
   - allocationDate, vacateDate
   
7. HostelManagementSystem
   - hostels: List<Hostel>
   - allocateRoom(Student, RoomType): Room
   - vacateRoom(Student): void
   - searchAvailableRooms(HostelId, RoomType): List<Room>
   - transferStudent(Student, Room): void

Methods:
- allocateRoom(): Check availability, assign student, update occupancy
- vacateRoom(): Remove student, update occupancy
- getStudentsByHostel(hostelId): List<Student>
```

---

### 5. Design Chess Game

**Solution:**
```
Classes:

1. Piece (Abstract)
   - color: Color (WHITE/BLACK)
   - position: Position
   - isAlive: boolean
   - canMove(Board, Position): boolean
   
2. King, Queen, Rook, Bishop, Knight, Pawn extends Piece
   - Each implements canMove() with specific logic
   
3. Position
   - row: int (0-7)
   - col: int (0-7)
   
4. Board
   - cells: Piece[8][8]
   - getPiece(Position): Piece
   - setPiece(Position, Piece): void
   
5. Move
   - piece: Piece
   - from: Position
   - to: Position
   - capturedPiece: Piece
   - isValid(Board): boolean
   
6. Player
   - color: Color
   - pieces: List<Piece>
   - makeMove(Move, Board): boolean
   
7. Game
   - board: Board
   - players: Player[2]
   - currentPlayer: Player
   - moveHistory: List<Move>
   - status: GameStatus (ACTIVE/CHECK/CHECKMATE/STALEMATE)
   - makeMove(Move): boolean
   - isCheckmate(): boolean
   - isStalemate(): boolean

Move Validation Logic (in Move.isValid()):
1. Check if piece belongs to current player
2. Call piece.canMove(board, to)
3. Check if move puts own king in check
4. For special moves (castling, en passant)

Example - Rook.canMove():
- Same row OR same column
- No pieces in between
```

---

### 6. Design URL Shortener (LLD)

**Solution:**
```
Classes:

1. URL
   - longUrl: String
   - shortUrl: String
   - createdAt: DateTime
   - expiryDate: DateTime
   - userId: String
   
2. User
   - userId: String
   - name: String
   - urlsCreated: List<URL>
   
3. URLShortener
   - urlDatabase: Map<String, URL>
   - generateShortURL(longUrl, userId): String
   - getLongURL(shortUrl): String
   - deleteURL(shortUrl): boolean
   
4. Base62Encoder
   - encode(number): String
   - decode(string): long
   
5. CounterService
   - getNextId(): long (Thread-safe counter)

Algorithm:
1. generateShortURL(longUrl):
   - Get unique ID from CounterService
   - Convert ID to Base62 (0-9, a-z, A-Z) → 62^6 = 56B combinations
   - Store mapping: shortUrl → longUrl
   - Return shortUrl
   
2. getLongURL(shortUrl):
   - Lookup in database
   - Check expiry
   - Return longUrl

Base62 Encoding:
characters = "0-9a-zA-Z" (62 chars)
For ID = 125:
125 % 62 = 1 → 'b'
125 / 62 = 2 → '2'
Result: "2b"
```

---

### 7. Design Workflow Management Interface

**Solution:**
```
Classes:

1. Task (Abstract)
   - taskId: String
   - status: TaskStatus (PENDING/RUNNING/COMPLETED/FAILED)
   - execute(): void
   
2. SequentialTask extends Task
   - subtasks: List<Task>
   - execute(): Run tasks one by one
   
3. ParallelTask extends Task
   - subtasks: List<Task>
   - execute(): Run tasks concurrently using ThreadPool
   
4. Workflow
   - workflowId: String
   - rootTask: Task
   - dataStore: Map<String, Object> (for data passing)
   - submit(): void
   - getStatus(): WorkflowStatus
   
5. WorkflowEngine
   - workflows: Map<String, Workflow>
   - executor: ExecutorService
   - submitWorkflow(Workflow): String
   - getWorkflowStatus(workflowId): WorkflowStatus
   
6. DataContext
   - data: Map<String, Object>
   - put(key, value): void
   - get(key): Object

Design Patterns:
- Composite Pattern: Task hierarchy
- Strategy Pattern: Sequential vs Parallel execution
- Observer Pattern: Status notifications
```

---

### 8. Implement HashMap from Scratch

**Solution:**
```
Class: MyHashMap<K, V>

Inner Class: Node<K, V>
- key: K
- value: V
- next: Node<K, V> (for chaining)

Fields:
- buckets: Node<K, V>[]
- capacity: int (default 16)
- size: int
- loadFactor: float (default 0.75)

Methods:

1. hash(key):
   hashCode = key.hashCode()
   return (hashCode ^ (hashCode >>> 16)) & (capacity - 1)
   
2. put(key, value):
   - Calculate index = hash(key)
   - If bucket empty: create new node
   - If key exists: update value
   - Else: add to chain (LinkedList)
   - If size/capacity > loadFactor: resize()
   
3. get(key):
   - Calculate index = hash(key)
   - Traverse chain to find key
   - Return value or null
   
4. resize():
   - Create new array (2x capacity)
   - Rehash all nodes
   - Update buckets reference

Collision Handling:
- Separate Chaining using LinkedList
- For better performance: Use TreeMap when chain length > 8
```

---

### 9. Implement String Search with Prefix (Trie-based)

**Solution:**
```
Class: TrieNode
- children: Map<Character, TrieNode>
- words: List<String>
- isEndOfWord: boolean

Class: PrefixSearchSystem

Fields:
- root: TrieNode

Methods:

1. addString(String str): boolean
   Time: O(m) where m = length of string
   
   curr = root
   for each char in str.toLowerCase():
       if char not in curr.children:
           curr.children[char] = new TrieNode()
       curr = curr.children[char]
       curr.words.add(str)
   curr.isEndOfWord = true
   return true

2. searchString(String prefix): List<String>
   Time: O(1) for lookup + O(k) for results
   
   curr = root
   for each char in prefix.toLowerCase():
       if char not in curr.children:
           return empty list
       curr = curr.children[char]
   return curr.words

Example:
add("XX"), add("XXY"), add("XXX"), add("YYX")

Trie Structure:
root
├─ x
│  ├─ x [XX]
│  │  ├─ y [XXY]
│  │  └─ x [XXX]
└─ y
   └─ y
      └─ x [YYX]

search("X") → [XX, XXY, XXX]
search("XX") → [XX, XXY, XXX]
```

---

### 10. Design Data Structure for O(1) Search (95% cases)

**Solution:**
```
Class: OptimizedHashMap<K, V>

Fields:
- buckets: List<TreeMap<K, V>>[]
- capacity: int (start with 100, grow dynamically)
- size: int
- loadFactor: float (0.75)

Methods:

1. hash(key):
   If key is String:
       hash = 0
       for char in key:
           hash = hash * 31 + char
       return Math.abs(hash) % capacity
   Else:
       return Math.abs(key.hashCode()) % capacity

2. put(key, value):
   - index = hash(key)
   - If buckets[index] is null:
       buckets[index] = new ArrayList()
   - If no collision: add directly → O(1)
   - If collision: use TreeMap → O(log n)
   - Check load factor and resize if needed

3. get(key):
   - index = hash(key)
   - If no collision: return directly → O(1) [95% cases]
   - If collision: search in TreeMap → O(log n) [5% cases]

4. resize():
   - When size/capacity > 0.75
   - newCapacity = capacity * 2
   - Rehash all elements
   
Dynamic Growth Strategy:
- Start: capacity = 100
- After 1 year: if size > 10^6, resize to 10^6
- This avoids memory waste initially
```

---

## 🌐 High Level Design (HLD)

### 1. Design URL Shortener (HLD)

**Solution:**
```
Requirements:
- Functional: Shorten URL, Redirect, Custom URLs, Analytics
- Non-Functional: High availability, Low latency, Scalable

Scale Estimation:
- Read:Write = 100:1
- 100 million URLs per month
- Write: 100M / (30 * 24 * 60 * 60) ≈ 40 URLs/sec
- Read: 40 * 100 = 4000 URLs/sec

Storage:
- 100M URLs/month * 12 months * 10 years = 12B URLs
- Each record ≈ 500 bytes
- Total: 12B * 500B = 6TB

Bandwidth:
- Write: 40 URLs/sec * 500B = 20 KB/sec
- Read: 4000 * 500B = 2 MB/sec

Architecture:

[Client] → [Load Balancer] → [API Servers]
                                    ↓
                            [Cache (Redis)]
                                    ↓
                     [Primary DB] ← → [Replica DBs]
                           ↓
                    [Cleanup Service]

Components:

1. API Server:
   - POST /shorten
   - GET /{shortUrl}
   
2. URL Generation Service:
   - Base62 encoding
   - Counter-based (Distributed)
   - Use Zookeeper for unique ID generation
   
3. Database Schema:
   Table: urls
   - id (Primary Key)
   - short_url (Indexed)
   - long_url
   - user_id
   - created_at
   - expiry_date
   - click_count
   
4. Cache (Redis):
   - Store hot URLs (80-20 rule)
   - TTL: 24 hours
   - Eviction: LRU
   
5. Cleanup Service:
   - Cron job (runs daily)
   - Delete expired URLs
   
6. Analytics Service:
   - Click tracking
   - Geo-location
   - Device info
   
High Availability:
- Multi-region deployment
- Database replication (Master-Slave)
- CDN for static content

Scalability:
- Horizontal scaling of API servers
- Database sharding by hash(short_url)
- Read replicas for load distribution
```

---

### 2. Design File Upload & Download System

**Solution:**
```
Requirements:
- Upload files (images, videos, documents)
- Download files
- Authentication & Authorization
- Support large files (chunked upload)

Architecture:

[Client] → [API Gateway] → [Auth Service]
                              ↓
                    [Upload Service] ← → [Object Storage (S3)]
                              ↓
                    [Metadata DB]
                              ↓
                    [CDN] → [Download Service]

Components:

1. Auth Service:
   - JWT-based authentication
   - Role-based access control (RBAC)
   - OAuth 2.0 integration
   
2. Upload Service:
   - Chunked upload for large files
   - Generate unique file ID
   - Store metadata in DB
   - Upload to S3
   - Virus scanning
   
3. Download Service:
   - Verify permissions
   - Generate pre-signed URL (S3)
   - Serve via CDN
   
4. Metadata Database:
   Table: files
   - file_id (UUID)
   - user_id
   - file_name
   - file_size
   - file_type
   - s3_path
   - upload_date
   - permissions
   
Upload Flow:
1. Client requests upload → Get pre-signed URL
2. Client uploads directly to S3
3. Client notifies server
4. Server stores metadata
5. Server runs virus scan
6. Mark file as ready

Download Flow:
1. Client requests file
2. Server validates permissions
3. Generate CDN/S3 URL
4. Return URL to client

Security:
- Encrypt files at rest (S3 encryption)
- Encrypt in transit (HTTPS)
- File type validation
- Size limits
- Rate limiting

Scalability:
- S3 for infinite storage
- CDN for global distribution
- Database sharding by user_id
```

---

### 3. Design Past Purchase Page (E-commerce)

**Solution:**
```
Requirements:
- Show order history (OrderId, Amount, Items)
- Click order → Show price distribution
- Search orders containing specific product
- Filter by date range

Architecture:

[Client] → [API Gateway] → [Order Service]
                              ↓
                    [Search Service (Elasticsearch)]
                              ↓
                    [Order DB] + [Cache]

Database Schema:

Table: orders
- order_id (PK)
- user_id (Indexed)
- total_amount
- order_date (Indexed)
- status

Table: order_items
- item_id (PK)
- order_id (FK)
- product_id
- product_name
- quantity
- price

Table: products
- product_id (PK)
- name
- category

APIs:

1. GET /orders?userId={id}&page={n}
   - Paginated order list
   - Cache: Redis (key: userId, TTL: 10 min)
   
2. GET /orders/{orderId}/items
   - Price distribution of items
   
3. POST /orders/search
   Body: { "productName": "apple", "userId": "123" }
   - Return orderIds containing product
   - Use Elasticsearch for fast text search
   
4. GET /orders?from={date}&to={date}
   - Filter by date range
   - Index on order_date

Search Implementation (Elasticsearch):

Index: orders
Document:
{
  "orderId": "ORD123",
  "userId": "USER456",
  "orderDate": "2025-01-20",
  "items": ["Apple iPhone", "AirPods"],
  "totalAmount": 1200
}

Query:
{
  "query": {
    "bool": {
      "must": [
        { "match": { "userId": "USER456" }},
        { "match": { "items": "apple" }}
      ]
    }
  }
}

Optimization:
- Cache frequently accessed orders
- Denormalize data for read performance
- Partition orders by year
- Archive old orders (>2 years) to cold storage
```

---

### 4. Design Database Synchronization System

**Solution:**
```
Requirements:
- Sync data between Primary and Secondary DB
- High availability
- Minimal data loss
- Handle network partitions

Architecture:

[Primary DB] ← → [Replication Manager] ← → [Secondary DB]
      ↓                                          ↓
[Change Data Capture]                      [Read Replicas]
      ↓
[Message Queue (Kafka)]
      ↓
[Sync Worker Threads]

Components:

1. Change Data Capture (CDC):
   - Monitor Primary DB transaction log
   - Capture INSERT, UPDATE, DELETE
   - Publish to Kafka
   
2. Kafka Topics:
   - db-changes (partitioned by table)
   - Retention: 7 days
   
3. Sync Workers:
   - Consumer group
   - Multi-threaded processing
   - Apply changes to Secondary DB
   - Handle conflicts
   
4. Replication Manager:
   - Health monitoring
   - Failover handling
   - Replication lag tracking
   
Sync Strategies:

1. Synchronous Replication:
   - Primary waits for Secondary ACK
   - Zero data loss
   - Higher latency
   
2. Asynchronous Replication:
   - Primary doesn't wait
   - Lower latency
   - Possible data loss
   
3. Semi-Synchronous:
   - Wait for at least 1 Secondary
   - Balance between latency and durability

Conflict Resolution:
- Timestamp-based (Last Write Wins)
- Version vectors
- Application-level resolution

Failover Process:
1. Detect Primary failure (heartbeat)
2. Promote Secondary to Primary
3. Redirect traffic
4. Old Primary becomes Secondary when recovered

Multitenancy:
- Separate schema per tenant
- Shared schema with tenant_id column
- Separate database per tenant

Monitoring:
- Replication lag
- Throughput (ops/sec)
- Error rate
- Alert on lag > threshold
```

---

### 5. Design Grafana-like Real-time Monitoring System

**Solution:**
```
Requirements:
- Real-time metrics collection
- Visualization dashboards
- Alerting
- Query historical data

Architecture:

[Application Servers] → [Metrics Collector] → [Time-Series DB]
                              ↓                      ↓
                        [Alert Manager]      [Query Service]
                                                     ↓
                                              [Visualization Layer]

Components:

1. Metrics Collection:
   - Push model: Apps send metrics
   - Pull model: Scrape endpoints
   - Protocol: Prometheus format
   
2. Time-Series Database (InfluxDB/Prometheus):
   - Optimized for time-series data
   - Compression
   - Retention policies
   
3. Query Service:
   - PromQL/InfluxQL
   - Aggregation functions
   - Range queries
   
4. Visualization:
   - Real-time charts
   - Dashboards
   - Custom panels

Data Flow:

Application → Agent (Telegraf/Prometheus Exporter)
                ↓
Metrics: cpu_usage{host="server1"} 45.2 timestamp
                ↓
Time-Series DB (stored efficiently)
                ↓
Query: avg(cpu_usage) over last 5 min
                ↓
Visualization

Schema Design:

Measurement: cpu
Tags: host, region (indexed)
Fields: usage, load
Timestamp

Scalability:
- Sharding by metric name or time range
- Read replicas
- Downsampling (1min → 5min → 1hour)
- Data retention (1day high-res, 30days low-res)

Alerting:
- Rule engine
- Threshold-based alerts
- Notification channels (Email, Slack, PagerDuty)
```

---

## ☕ Java / OOPs / Core Java

### 1. Encapsulation

**Solution:**
```
Definition:
Wrapping data (variables) and code (methods) together as a single unit.
Restricting direct access to data using access modifiers.

How Achieved:
1. Declare variables as private
2. Provide public getter/setter methods

Example:
class Employee {
    private String name;
    private int salary;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getSalary() {
        return salary;
    }
    
    public void setSalary(int salary) {
        if (salary > 0) {
            this.salary = salary;
        }
    }
}

Benefits:
- Data hiding
- Flexibility (can change implementation)
- Validation in setters
- Read-only/Write-only properties
```

---

### 2. Polymorphism

**Solution:**
```
Definition:
Ability of an object to take many forms.

Types:

1. Compile-time (Static) Polymorphism - Method Overloading:

class Calculator {
    int add(int a, int b) {
        return a + b;
    }
    
    double add(double a, double b) {
        return a + b;
    }
    
    int add(int a, int b, int c) {
        return a + b + c;
    }
}

Rules:
- Same method name
- Different parameters (number, type, order)
- Return type can be different

2. Runtime (Dynamic) Polymorphism - Method Overriding:

class Animal {
    void sound() {
        System.out.println("Animal makes sound");
    }
}

class Dog extends Animal {
    @Override
    void sound() {
        System.out.println("Dog barks");
    }
}

class Cat extends Animal {
    @Override
    void sound() {
        System.out.println("Cat meows");
    }
}

Usage:
Animal a = new Dog();
a.sound(); // Dog barks (Runtime decision)

Rules:
- Same method signature
- IS-A relationship
- Cannot override static/final/private methods
```

---

### 3. Abstraction

**Solution:**
```
Definition:
Hiding implementation details and showing only functionality.

How Achieved:

1. Abstract Classes (0-100% abstraction):

abstract class Shape {
    abstract double area(); // Abstract method
    
    void display() { // Concrete method
        System.out.println("This is a shape");
    }
}

class Circle extends Shape {
    double radius;
    
    @Override
    double area() {
        return 3.14 * radius * radius;
    }
}

2. Interfaces (100% abstraction):

interface Drawable {
    void draw(); // By default public abstract
}

class Rectangle implements Drawable {
    @Override
    public void draw() {
        System.out.println("Drawing rectangle");
    }
}

Key Points:
- Abstract class can have constructors
- Interface cannot have constructors
- Class can extend 1 class but implement multiple interfaces
- From Java 8: Interfaces can have default and static methods
```

---

### 4. Inheritance

**Solution:**
```
Definition:
Mechanism where a class acquires properties of another class.

Types:

1. Single Inheritance:
class Parent { }
class Child extends Parent { }

2. Multilevel Inheritance:
class GrandParent { }
class Parent extends GrandParent { }
class Child extends Parent { }

3. Hierarchical Inheritance:
class Parent { }
class Child1 extends Parent { }
class Child2 extends Parent { }

4. Multiple Inheritance (via interfaces):
interface A { }
interface B { }
class C implements A, B { }

5. Hybrid Inheritance:
Combination of above types

Example:
class Vehicle {
    void start() {
        System.out.println("Vehicle starts");
    }
}

class Car extends Vehicle {
    void drive() {
        System.out.println("Car drives");
    }
}

Usage:
Car c = new Car();
c.start(); // Inherited method
c.drive(); // Own method

Keywords:
- extends: for classes
- implements: for interfaces
- super: access parent class members
```

---

### 5. Abstraction vs Encapsulation

**Solution:**
```
Abstraction:
- WHAT to do (hiding complexity)
- Design level concept
- Achieved via abstract classes and interfaces
- Focus: Hide implementation details

Example:
interface Payment {
    void pay(double amount); // User knows what, not how
}

Encapsulation:
- HOW to do (data hiding)
- Implementation level concept
- Achieved via access modifiers
- Focus: Protect data

Example:
class Account {
    private double balance; // Hidden
    
    public void deposit(double amt) { // Controlled access
        if (amt > 0) balance += amt;
    }
}

Key Difference:
- Abstraction: TV remote (hide circuit complexity)
- Encapsulation: Capsule (data and methods bundled)
```

---

### 6. Interface vs Abstract Class

**Solution:**
```
Interface:
- 100% abstraction (before Java 8)
- Cannot have instance variables
- All methods public abstract (by default)
- Multiple inheritance supported
- Cannot have constructors
- From Java 8: default and static methods allowed
- From Java 9: private methods allowed

interface Flyable {
    void fly();
    
    default void land() {
        System.out.println("Landing...");
    }
}

Abstract Class:
- 0-100% abstraction
- Can have instance variables
- Can have any access modifier
- Single inheritance only
- Can have constructors
- Can have concrete methods

abstract class Bird {
    String name;
    
    Bird(String name) {
        this.name = name;
    }
    
    abstract void fly();
    
    void eat() {
        System.out.println("Bird eats");
    }
}

When to Use:
- Interface: Define contract (can fly, can swim)
- Abstract class: Share common code (all birds have name)
```

---

### 7. Diamond Problem

**Solution:**
```
Problem:
Class D inherits from B and C, both inherit from A.
If B and C override a method from A, which version does D inherit?

    A
   / \
  B   C
   \ /
    D

Java Solution:
Multiple inheritance NOT allowed for classes.
Allowed for interfaces.

With Interfaces (Java 8+):

interface A {
    default void show() {
        System.out.println("A");
    }
}

interface B extends A {
    default void show() {
        System.out.println("B");
    }
}

interface C extends A {
    default void show() {
        System.out.println("C");
    }
}

class D implements B, C {
    @Override
    public void show() {
        B.super.show(); // Explicitly specify
        // OR
        C.super.show();
        // OR
        // Own implementation
    }
}

Resolution:
Compiler forces you to override and resolve conflict explicitly.
```

---

### 8. How HashMap Works

**Solution:**
```
Internal Structure:
Array of Nodes (buckets)

Node Structure:
class Node<K,V> {
    final int hash;
    final K key;
    V value;
    Node<K,V> next;
}

Working:

1. PUT Operation:
   a. Calculate hash: key.hashCode()
   b. Calculate index: (n-1) & hash
   c. If bucket empty: create new node
   d. If key exists: update value
   e. If collision: add to linked list
   f. If list size > 8: convert to TreeMap (Red-Black Tree)
   g. If size > threshold: resize (rehashing)

2. GET Operation:
   a. Calculate hash and index
   b. Check first node
   c. If collision: traverse list/tree
   d. Return value or null

Initial Capacity: 16
Load Factor: 0.75
Threshold: Capacity * Load Factor

Resize Process:
- When size > threshold
- Create new array (2x size)
- Rehash all entries
- Update reference

Example:
HashMap<String, Integer> map = new HashMap<>();
map.put("A", 1);

Step 1: hash("A") = 65
Step 2: index = 65 & 15 = 1
Step 3: buckets[1] = new Node("A", 1)

Collision Handling:
Before Java 8: Linked List only
From Java 8: Linked List → TreeMap (if size > 8)

Why TreeMap on collision?
Linked List: O(n) lookup
TreeMap: O(log n) lookup
```

---