# Expedia Interview Preparation Guide
*Last-minute revision for DSA, LLD, HLD, and Behavioral rounds*

---

## 📋 Interview Structure
- **Round 1-2**: DSA (2 rounds, 45-60 min each)
- **Round 3**: LLD/HLD (60 min, often combined)
- **Round 4**: Hiring Manager (Behavioral + Technical discussion)

---

## 1️⃣ DSA Round

### Most Frequently Asked Patterns
1. **Sliding Window** (Very Common)
2. **Two Pointers**
3. **Hashing**
4. **Binary Search variants**
5. **DP (House Robber variants)**
6. **Graph/Tree (BFS/DFS)**

---

### 🔥 High-Priority Problems

#### **Sliding Window**

**1. Longest Substring with K Distinct Characters**
```cpp
int lengthOfLongestSubstringKDistinct(string s, int k) {
    unordered_map<char, int> freq;
    int left = 0, maxLen = 0;
    
    for (int right = 0; right < s.length(); right++) {
        freq[s[right]]++;
        
        while (freq.size() > k) {
            freq[s[left]]--;
            if (freq[s[left]] == 0) freq.erase(s[left]);
            left++;
        }
        maxLen = max(maxLen, right - left + 1);
    }
    return maxLen;
}
```
**Interview Tip**: Explain how you maintain the window invariant - "I keep expanding right, and shrink left when constraint breaks"

**2. Max Consecutive 1s After Flipping K Zeros**
```cpp
int longestOnes(vector<int>& nums, int k) {
    int left = 0, zeros = 0, maxLen = 0;
    
    for (int right = 0; right < nums.size(); right++) {
        if (nums[right] == 0) zeros++;
        
        while (zeros > k) {
            if (nums[left] == 0) zeros--;
            left++;
        }
        maxLen = max(maxLen, right - left + 1);
    }
    return maxLen;
}
```

---

#### **Two Pointers**

**3. Reorder Linked List**
```cpp
void reorderList(ListNode* head) {
    if (!head || !head->next) return;
    
    // Find middle
    ListNode *slow = head, *fast = head;
    while (fast->next && fast->next->next) {
        slow = slow->next;
        fast = fast->next->next;
    }
    
    // Reverse second half
    ListNode *prev = nullptr, *curr = slow->next;
    slow->next = nullptr;
    while (curr) {
        ListNode* next = curr->next;
        curr->next = prev;
        prev = curr;
        curr = next;
    }
    
    // Merge
    ListNode *first = head, *second = prev;
    while (second) {
        ListNode *tmp1 = first->next, *tmp2 = second->next;
        first->next = second;
        second->next = tmp1;
        first = tmp1;
        second = tmp2;
    }
}
```
**Interview Tip**: "I'll break this into 3 steps: find middle, reverse second half, merge alternately"

---

#### **Hashing**

**4. Longest Consecutive Sequence**
```cpp
int longestConsecutive(vector<int>& nums) {
    unordered_set<int> numSet(nums.begin(), nums.end());
    int maxLen = 0;
    
    for (int num : numSet) {
        if (numSet.find(num - 1) == numSet.end()) {
            int curr = num, len = 1;
            while (numSet.find(curr + 1) != numSet.end()) {
                curr++;
                len++;
            }
            maxLen = max(maxLen, len);
        }
    }
    return maxLen;
}
```
**Interview Tip**: "I only start counting from sequence starts to avoid duplicates - O(n) time"

---

#### **Binary Search**

**5. Find Minimum in Rotated Sorted Array**
```cpp
int findMin(vector<int>& nums) {
    int left = 0, right = nums.size() - 1;
    
    while (left < right) {
        int mid = left + (right - left) / 2;
        
        if (nums[mid] > nums[right]) {
            left = mid + 1;
        } else {
            right = mid;
        }
    }
    return nums[left];
}
```
**Interview Tip**: "I compare mid with right to determine which half is sorted. The min is in the unsorted half"

---

#### **Dynamic Programming**

**6. House Robber II (Circular)**
```cpp
int rob(vector<int>& nums) {
    if (nums.size() == 1) return nums[0];
    
    auto robLinear = [](vector<int>& arr, int start, int end) {
        int prev2 = 0, prev1 = 0;
        for (int i = start; i <= end; i++) {
            int curr = max(prev1, prev2 + arr[i]);
            prev2 = prev1;
            prev1 = curr;
        }
        return prev1;
    };
    
    return max(robLinear(nums, 0, nums.size() - 2),
               robLinear(nums, 1, nums.size() - 1));
}
```
**Interview Tip**: "Since it's circular, I can't rob both first and last. So I solve twice: excluding first, and excluding last"

---

#### **Graph/Tree**

**7. Rotting Oranges (BFS)**
```cpp
int orangesRotting(vector<vector<int>>& grid) {
    queue<pair<int,int>> q;
    int fresh = 0, minutes = 0;
    int m = grid.size(), n = grid[0].size();
    
    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            if (grid[i][j] == 2) q.push({i, j});
            if (grid[i][j] == 1) fresh++;
        }
    }
    
    int dirs[4][2] = {{0,1},{1,0},{0,-1},{-1,0}};
    while (!q.empty() && fresh > 0) {
        int size = q.size();
        for (int i = 0; i < size; i++) {
            auto [x, y] = q.front(); q.pop();
            for (auto& d : dirs) {
                int nx = x + d[0], ny = y + d[1];
                if (nx >= 0 && nx < m && ny >= 0 && ny < n && grid[nx][ny] == 1) {
                    grid[nx][ny] = 2;
                    fresh--;
                    q.push({nx, ny});
                }
            }
        }
        minutes++;
    }
    return fresh == 0 ? minutes : -1;
}
```

---

#### **String Manipulation**

**8. Palindromic Substrings**
```cpp
int countSubstrings(string s) {
    int count = 0, n = s.length();
    
    auto expand = [&](int l, int r) {
        while (l >= 0 && r < n && s[l] == s[r]) {
            count++;
            l--; r++;
        }
    };
    
    for (int i = 0; i < n; i++) {
        expand(i, i);      // odd length
        expand(i, i + 1);  // even length
    }
    return count;
}
```
**Interview Tip**: "I expand around each center - handles both odd and even length palindromes"

---

#### **Intervals**

**9. Merge Intervals**
```cpp
vector<vector<int>> merge(vector<vector<int>>& intervals) {
    sort(intervals.begin(), intervals.end());
    vector<vector<int>> result;
    
    for (auto& interval : intervals) {
        if (result.empty() || result.back()[1] < interval[0]) {
            result.push_back(interval);
        } else {
            result.back()[1] = max(result.back()[1], interval[1]);
        }
    }
    return result;
}
```

---

### 🎯 Quick Tips for DSA Round
- **Always clarify constraints**: array size, value ranges, duplicates allowed?
- **Start with brute force**: "Naive approach would be O(n²), let me optimize"
- **Think aloud**: "I'm thinking sliding window because we need contiguous..."
- **Test with examples**: Walk through 1-2 test cases before coding
- **Edge cases to mention**: empty input, single element, all same elements
- **Time/Space complexity**: State upfront and after solution

---

## 2️⃣ LLD Round

### Core Focus Areas
1. **SOLID Principles** (especially Single Responsibility, Open/Closed)
2. **Design Patterns** (Strategy, Factory, Observer, Builder)
3. **Class Design** (models, services, interfaces)
4. **Extensibility & Scalability**

---

### 🏗️ Common LLD Problems

#### **1. LRU Cache**

```java
class LRUCache {
    class Node {
        int key, value;
        Node prev, next;
        Node(int k, int v) { key = k; value = v; }
    }
    
    private Map<Integer, Node> cache;
    private int capacity;
    private Node head, tail;
    
    public LRUCache(int capacity) {
        this.capacity = capacity;
        cache = new HashMap<>();
        head = new Node(0, 0);
        tail = new Node(0, 0);
        head.next = tail;
        tail.prev = head;
    }
    
    public int get(int key) {
        if (!cache.containsKey(key)) return -1;
        Node node = cache.get(key);
        remove(node);
        insert(node);
        return node.value;
    }
    
    public void put(int key, int value) {
        if (cache.containsKey(key)) {
            remove(cache.get(key));
        }
        if (cache.size() == capacity) {
            remove(tail.prev);
        }
        insert(new Node(key, value));
    }
    
    private void remove(Node node) {
        cache.remove(node.key);
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
    
    private void insert(Node node) {
        cache.put(node.key, node);
        node.next = head.next;
        node.next.prev = node;
        head.next = node;
        node.prev = head;
    }
}
```

**Interview Explanation**:
- "I use HashMap for O(1) lookup and Doubly LinkedList for O(1) insertion/deletion"
- "Most recent is at head, least recent at tail"
- "On access, I move to head. On capacity full, I remove from tail"

---

#### **2. Design Parking Lot**

```java
// Enums
enum VehicleType { CAR, BIKE, TRUCK }
enum SpotSize { COMPACT, MEDIUM, LARGE }

// Models
class Vehicle {
    private String licensePlate;
    private VehicleType type;
    
    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }
    // getters
}

class ParkingSpot {
    private int spotId;
    private SpotSize size;
    private boolean isOccupied;
    private Vehicle vehicle;
    
    public boolean canFit(Vehicle vehicle) {
        // Logic to check if vehicle fits
        return !isOccupied && sizeCompatible(vehicle);
    }
    
    public void park(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.isOccupied = true;
    }
    
    public void unpark() {
        this.vehicle = null;
        this.isOccupied = false;
    }
}

class ParkingLevel {
    private int levelId;
    private List<ParkingSpot> spots;
    
    public ParkingSpot findSpot(Vehicle vehicle) {
        return spots.stream()
            .filter(spot -> spot.canFit(vehicle))
            .findFirst()
            .orElse(null);
    }
}

// Service Layer
class ParkingLot {
    private List<ParkingLevel> levels;
    private Map<String, ParkingSpot> vehicleSpotMap;
    
    public boolean parkVehicle(Vehicle vehicle) {
        for (ParkingLevel level : levels) {
            ParkingSpot spot = level.findSpot(vehicle);
            if (spot != null) {
                spot.park(vehicle);
                vehicleSpotMap.put(vehicle.getLicensePlate(), spot);
                return true;
            }
        }
        return false;
    }
    
    public boolean unparkVehicle(String licensePlate) {
        ParkingSpot spot = vehicleSpotMap.get(licensePlate);
        if (spot != null) {
            spot.unpark();
            vehicleSpotMap.remove(licensePlate);
            return true;
        }
        return false;
    }
}
```

**Interview Explanation**:
- "I separate concerns: Vehicle is model, ParkingSpot handles occupancy, ParkingLot orchestrates"
- "Strategy Pattern for different vehicle types"
- "Easy to extend: add new vehicle types without changing existing code (Open/Closed)"

---

#### **3. Design Invoice Generation System**

```java
// Models
class Item {
    private String name;
    private double price;
    private String category;
    
    public double getPriceWithTax(TaxStrategy taxStrategy) {
        return price + taxStrategy.calculateTax(this);
    }
}

// Strategy Pattern for Tax Calculation
interface TaxStrategy {
    double calculateTax(Item item);
}

class StandardTaxStrategy implements TaxStrategy {
    public double calculateTax(Item item) {
        return item.getPrice() * 0.18; // 18% GST
    }
}

class LuxuryTaxStrategy implements TaxStrategy {
    public double calculateTax(Item item) {
        return item.getPrice() * 0.28; // 28% for luxury
    }
}

// Service
class InvoiceService {
    private TaxStrategy taxStrategy;
    
    public Invoice generateInvoice(List<Item> items) {
        double totalAmount = 0, totalTax = 0;
        List<LineItem> lineItems = new ArrayList<>();
        
        for (Item item : items) {
            double tax = taxStrategy.calculateTax(item);
            double priceWithTax = item.getPrice() + tax;
            
            lineItems.add(new LineItem(item, tax, priceWithTax));
            totalAmount += priceWithTax;
            totalTax += tax;
        }
        
        return new Invoice(lineItems, totalAmount, totalTax);
    }
}

class Invoice {
    private List<LineItem> items;
    private double totalAmount;
    private double totalTax;
    private LocalDateTime generatedAt;
    
    // Constructor and getters
}
```

**Interview Explanation**:
- "Strategy Pattern allows different tax calculations without changing Invoice logic"
- "Easy to add new tax rules (seasonal discounts, region-specific taxes)"
- "Single Responsibility: Item knows its data, TaxStrategy calculates tax, Invoice aggregates"

---

### 🎯 LLD Interview Strategy
1. **Clarify Requirements** (5 min): "Should we handle concurrent bookings? Multiple payment methods?"
2. **List Core Entities** (5 min): User, Product, Order, Payment
3. **Define Relationships** (5 min): Draw simple class diagram
4. **Write Key Classes** (20 min): Focus on 3-4 main classes with proper methods
5. **Discuss Patterns** (10 min): Which patterns used and why
6. **Extensibility** (5 min): "To add new feature X, I'd extend Y interface"

**What Interviewers Look For**:
- Clean separation of concerns
- Proper use of interfaces
- Naming conventions (meaningful names)
- Handling edge cases in methods
- Discussion of trade-offs

---

## 3️⃣ HLD/System Design Round

### Most Asked Systems
1. **Hotel/Flight Booking System** (Most Common)
2. **Food Delivery App**
3. **Distributed Cache**
4. **URL Shortener**
5. **API Rate Limiter**

---

### 🏛️ Template Approach (Use for ANY System)

#### **Step 1: Requirements (5-7 min)**

**Functional:**
- Core features only (3-4 max)
- Example: "Search hotels, Book room, View bookings, Cancel booking"

**Non-Functional:**
- Availability > Consistency (booking systems)
- Low latency (< 200ms for search)
- Scale: 1M DAU, 10K requests/sec

**Interview Tip**: "I'm assuming eventual consistency is acceptable since we can handle double bookings with compensation"

---

#### **Step 2: Back-of-envelope Estimation (3 min)**

```
Users: 10M daily
Bookings: 5% conversion = 500K bookings/day
Read:Write = 100:1
Storage: 500KB per booking × 500K × 365 = ~100TB/year
```

---

#### **Step 3: High-Level Architecture (10 min)**

```
[Client] → [CDN] → [Load Balancer] → [API Gateway]
                                           ↓
                    [Booking Service] [Search Service] [Payment Service]
                           ↓                 ↓                ↓
                    [MySQL Master]    [Elasticsearch]  [Payment DB]
                           ↓
                    [MySQL Replicas]
                           ↓
                    [Redis Cache]
```

**Core Components**:
1. **API Gateway**: Rate limiting, auth, routing
2. **Microservices**: Booking, Search, User, Notification
3. **Databases**: 
   - MySQL (transactional data)
   - Elasticsearch (search)
   - Redis (cache, session)
4. **Message Queue**: Kafka for async tasks (emails, analytics)
5. **CDN**: Static content

---

#### **Step 4: Database Design (7 min)**

```sql
-- Keep it simple, show only key tables

Table: Hotels
- hotel_id (PK)
- name, location, rating
- total_rooms

Table: Rooms
- room_id (PK)
- hotel_id (FK)
- room_type, price

Table: Bookings
- booking_id (PK)
- user_id (FK)
- room_id (FK)
- check_in, check_out
- status (confirmed, cancelled)
- created_at

Table: Inventory
- room_id (PK)
- date (PK)
- available_count
```

**Interview Tip**: "I'm using a separate Inventory table with date-wise availability for fast search queries"

---

#### **Step 5: Deep Dive (20 min)**

**Pick 2-3 critical flows to elaborate:**

##### **Flow 1: Hotel Search**
```
User → API Gateway → Search Service
                          ↓
                   Check Redis (cache)
                          ↓ (miss)
                   Query Elasticsearch
                          ↓
                   Rank by price/rating/location
                          ↓
                   Cache results (TTL: 5 min)
                          ↓
                   Return top 20 results
```

**Optimizations**:
- Cache popular searches (city + dates)
- Use Elasticsearch for fuzzy search, filters
- Pagination to reduce payload

---

##### **Flow 2: Room Booking (Critical)**
```
User selects room → Booking Service
                          ↓
                   BEGIN TRANSACTION
                          ↓
                   1. Check inventory (SELECT FOR UPDATE)
                   2. Decrement available_count
                   3. Create booking record
                   4. Call Payment Service
                          ↓
                   COMMIT if payment succeeds
                   ROLLBACK if fails
                          ↓
                   Publish event to Kafka
                   (send confirmation email)
```

**Handling Race Conditions**:
- Use `SELECT FOR UPDATE` (pessimistic locking)
- Or optimistic locking with version number
- Idempotency key to prevent duplicate bookings

**Interview Tip**: "I'm using database transactions for atomicity. If payment fails, entire booking rolls back"

---

##### **Flow 3: Handling High Traffic (Scalability)**

**Problem**: 10K concurrent requests for same room

**Solutions**:
1. **Database Sharding**: 
   - Shard by `hotel_id % 10`
   - Hot shard issue? Add consistent hashing
   
2. **Caching Strategy**:
   - Cache available rooms (invalidate on booking)
   - Use Redis with TTL
   
3. **Rate Limiting**:
   - Token bucket per user (10 requests/min)
   - Prevents DDoS

4. **Async Processing**:
   - Waitlist for sold-out rooms
   - Process via Kafka queue

---

### 🔥 Key Concepts to Mention

#### **1. Database Choices**
| Database | Use Case |
|----------|----------|
| MySQL/PostgreSQL | Transactional data (bookings, payments) |
| Elasticsearch | Full-text search, filters |
| Redis | Cache, session, rate limiting |
| Cassandra | Time-series data (logs, analytics) |
| MongoDB | Flexible schema (user preferences) |

---

#### **2. Caching Strategies**
- **Cache-Aside**: Read from cache, if miss then DB
- **Write-Through**: Write to cache + DB synchronously
- **Write-Behind**: Write to cache, async to DB (faster, risk of loss)

**For Booking System**: Cache-Aside for search, invalidate on booking

---

#### **3. Consistency Patterns**
- **Strong Consistency**: Read after write sees latest (MySQL transactions)
- **Eventual Consistency**: Replicas sync over time (user profile updates)

**For Booking System**: Strong consistency for inventory, eventual for user reviews

---

#### **4. Scaling Techniques**
- **Vertical Scaling**: Bigger machines (limited)
- **Horizontal Scaling**: More machines + Load Balancer
- **Database Replication**: Master (writes) + Replicas (reads)
- **Sharding**: Split data across multiple DBs

---

### 🎯 HLD Interview Strategy

**Time Management**:
- Requirements: 5 min
- High-level design: 10 min
- Database schema: 5 min
- Deep dive: 25 min
- Bottlenecks & scale: 10 min
- Questions: 5 min

**What NOT to Do**:
- ❌ Jump to implementation details too early
- ❌ Spend too much time on one component
- ❌ Forget to discuss trade-offs
- ❌ Ignore non-functional requirements

**What TO Do**:
- ✅ Ask clarifying questions
- ✅ State assumptions clearly
- ✅ Draw diagrams (boxes and arrows)
- ✅ Discuss trade-offs (CAP theorem)
- ✅ Mention monitoring & alerts

---

## 4️⃣ Hiring Manager Round

### Common Questions & STAR Answers

#### **1. Tell me about a challenging project**

**Structure (STAR)**:
- **Situation**: "We had to migrate 50+ microservices to Kubernetes with zero downtime"
- **Task**: "I was responsible for containerization and rollout strategy"
- **Action**: "I created Docker images, set up CI/CD pipelines, and used blue-green deployment"
- **Result**: "Achieved 99.9% uptime during migration, reduced deployment time from 2 hours to 15 minutes"

---

#### **2. Conflict with team member**

**Good Answer**:
"During API design, a colleague preferred REST while I suggested GraphQL. Instead of arguing, I created a POC for both, documented pros/cons (REST: simpler, GraphQL: flexible queries), and we discussed in team meeting. We chose REST for MVP speed, with option to add GraphQL later. This taught me to validate opinions with data."

---

#### **3. How do you handle tight deadlines?**

**Good Answer**:
"I break work into MVPs. For a payment integration under deadline, I:
1. Implemented core flow first (happy path)
2. Added error handling incrementally
3. Wrote tests in parallel
4. Communicated daily progress to stakeholders
Result: Delivered MVP on time, polished features in next sprint"

---

#### **4. Describe a technical decision you made**

**Good Answer**:
"Chose PostgreSQL over MongoDB for booking system because:
- Need ACID transactions (booking atomicity)
- Structured data (hotel, rooms, bookings)
- Complex joins for analytics
Trade-off: Less flexible schema, but data integrity was priority"

---

### Technical Questions in HM Round

#### **1. Design Patterns You've Used**

**Prepare 2-3 with examples**:
- **Strategy Pattern**: "Used in payment processing - different gateways (Stripe, PayPal) implement PaymentStrategy interface"
- **Factory Pattern**: "Vehicle factory creates Car/Bike objects based on type"
- **Observer Pattern**: "Notification system - when order status changes, multiple observers (email, SMS, push) get notified"

---

#### **2. SOLID Principles**

**Quick Examples**:
- **S**: UserService handles only user logic, not payments
- **O**: Add new payment method without modifying existing code
- **L**: Square can replace Rectangle without breaking code
- **I**: Don't force clients to depend on unused methods
- **D**: Depend on interfaces (PaymentGateway), not concrete classes (StripeGateway)

---

#### **3. Java 8 Features**

- **Lambda**: `list.forEach(item -> System.out.println(item))`
- **Stream API**: `list.stream().filter(x -> x > 10).collect(Collectors.toList())`
- **Optional**: Avoid NullPointerException
- **CompletableFuture**: Async programming

---

#### **4. Microservices Concepts**

- **Circuit Breaker**: Stop calling failing service (Hystrix, Resilience4j)
- **Service Discovery**: Eureka, Consul
- **API Gateway**: Single entry point, handles auth, rate limiting
- **Event-Driven**: Kafka for async communication

---

### 🎯 Questions to Ask Interviewer

1. "What does a typical sprint look like for your team?"
2. "What's the biggest technical challenge your team is solving?"
3. "How do you handle on-call and production issues?"
4. "What technologies are you exploring for future projects?"
5. "How do you measure success for a software engineer here?"

---

## ⚡ Quick Revision Checklist

### DSA (Must Know)
- [ ] Sliding Window (3 variations)
- [ ] Two Pointers (array, linked list)
- [ ] Hashing (frequency, anagrams)
- [ ] Binary Search (rotated array, search space)
- [ ] DP (House Robber, Unique Paths)
- [ ] BFS/DFS (graph, tree traversal)
- [ ] Merge Intervals

### LLD (Must Know)
- [ ] LRU Cache (HashMap + DLL)
- [ ] Parking Lot (models, services, enums)
- [ ] SOLID Principles (with examples)
- [ ] Design Patterns (Strategy, Factory, Observer)
- [ ] Clean code practices

### HLD (Must Know)
- [ ] Booking System architecture
- [ ] Database sharding & replication
- [ ] Caching strategies (Redis)
- [ ] Load balancing
- [ ] Message queues (Kafka)
- [ ] CAP theorem

### Behavioral (Must Prepare)
- [ ] 3 STAR stories (challenge, conflict, decision)
- [ ] 5 questions to ask interviewer
- [ ] Why Expedia? (mention travel passion)

---

## 🚀 Final Tips

### During DSA:
- **Clarify before coding**: "Should I handle negative numbers?"
- **Think aloud**: "I'm using a map because we need O(1) lookup"
- **Test with examples**: Walk through one example before submitting
- **Complexity analysis**: State time/space upfront and at end

### During LLD:
- **Start with requirements**: "Let me list the core features first"
- **Draw class diagram**: Visual helps interviewer understand
- **Explain design choices**: "I'm using Strategy pattern for extensibility"
- **Production-ready code**: Proper naming, error handling, edge cases

### During HLD:
- **Clarify scale**: "How many users? Read/write ratio?"
- **Draw first**: Architecture diagram with boxes and arrows
- **Discuss trade-offs**: "SQL gives consistency, NoSQL gives scale"
- **Focus on bottlenecks**: "This will be the bottleneck at 10K RPS, here's how we scale"

### During HM:
- **STAR format**: Structure answers clearly
- **Be honest**: Don't exaggerate, interviewers can tell
- **Show enthusiasm**: Mention passion for travel (aligns with Expedia)
- **Ask thoughtful questions**: Shows genuine interest

---

**Good luck! You've got this! 🎯**