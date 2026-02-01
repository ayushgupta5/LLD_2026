# Agoda Interview - Complete Revision Guide

## 🎯 Interview Process Overview

### Typical Round Structure (1 Week Duration)
1. **Recruiter Screening** (30 mins)
   - Introduction and background discussion
   - Salary expectations
   - Relocation willingness (Bangkok)
   - Why Agoda?

2. **Online Assessment** (HackerRank/CodeSignal)
   - 1 DSA problem (can be LeetCode Hard level)
   - 1 API design problem
   - Must pass majority of test cases live

3. **DSA/Coding Round** (1-1.5 hours)
   - 1-2 coding problems (Easy to Medium, sometimes Hard)
   - Multiple follow-ups on optimizations
   - Complexity analysis required
   - Full executable code expected

4. **Platform Round** (1 hour)
   - System design review of existing architecture
   - REST API design and improvements
   - Code review with pattern suggestions
   - Connectivity management and authentication

5. **HLD/System Design** (1 hour each, can be 2 rounds)
   - Design booking systems, streaming platforms
   - Trade-offs discussion (critical!)
   - Scalability and fault tolerance
   - Database and caching strategies

6. **Architecture Round** (1 hour)
   - Deep technical discussion
   - Bottlenecks identification
   - Metrics and telemetry
   - "What if..." scenario questions

7. **Culture Fit/Values Round** (1-1.5 hours) ⚠️ **LONGEST**
   - Mentorship experiences
   - Mistakes and learnings
   - Conflict resolution
   - Working under pressure
   - **Can reject even after strong technical performance**

8. **Hiring Manager Round** (1 hour)
   - Mix of behavioral and technical
   - Compensation discussion
   - Team fit assessment

---

## 📊 DSA - Data Structures & Algorithms

### Stack-Based Problems

#### 1. Min Stack
**Problem**: Design a stack supporting push, pop, top, and getMin in O(1)

**Approach**: Use two stacks
```java
class MinStack {
    Stack<Integer> stack = new Stack<>();
    Stack<Integer> minStack = new Stack<>();
    
    void push(int x) {
        stack.push(x);
        if (minStack.isEmpty() || x <= minStack.peek()) {
            minStack.push(x);
        }
    }
    
    void pop() {
        if (stack.pop().equals(minStack.peek())) {
            minStack.pop();
        }
    }
    
    int top() { return stack.peek(); }
    int getMin() { return minStack.peek(); }
}
```
**Complexity**: O(1) for all operations, O(n) space

#### 2. Next Greater Element
**Problem**: For each element, find the next greater element to its right

**Approach**: Monotonic decreasing stack
```java
int[] nextGreaterElement(int[] arr) {
    int n = arr.length;
    int[] result = new int[n];
    Stack<Integer> stack = new Stack<>();
    
    // Traverse from right to left
    for (int i = n - 1; i >= 0; i--) {
        // Pop smaller elements
        while (!stack.isEmpty() && stack.peek() <= arr[i]) {
            stack.pop();
        }
        result[i] = stack.isEmpty() ? -1 : stack.peek();
        stack.push(arr[i]);
    }
    return result;
}
```
**Follow-up (Circular Array)**: Iterate array twice using `i % n`

**Complexity**: O(n) time, O(n) space

#### 3. Chemical Formula Weight Calculator
**Problem**: Calculate weight of chemical formula (C=12, H=1, O=8)
- Example: CH4 = 16, H(CH4)2 = 33

**Approach**: Stack for nested parentheses
```java
int calculateWeight(String formula) {
#include <bits/stdc++.h>
using namespace std;

int calculateWeight(const string& formula) {
    stack<int> st;
    unordered_map<char, int> weights = {
        {'C', 12},
        {'H', 1},
        {'O', 16}   // Oxygen is 16, not 8 😉
    };

    st.push(0); // Base level

    for (int i = 0; i < formula.length(); i++) {
        char c = formula[i];

        if (c == '(') {
            st.push(0); // New level
        } 
        else if (c == ')') {
            int temp = st.top();
            st.pop();

            i++; // Multiplier after ')'
            int mult = formula[i] - '0';

            int prev = st.top();
            st.pop();
            st.push(prev + temp * mult);
        } 
        else {
            // Atom
            int count = 1;
            if (i + 1 < formula.length() && isdigit(formula[i + 1])) {
                count = formula[++i] - '0';
            }

            int prev = st.top();
            st.pop();
            st.push(prev + weights[c] * count);
        }
    }

    return st.top();
}

```

### Binary Search Problems

#### 1. Koko Eating Bananas ⭐
**Problem**: Minimum eating speed k to finish all piles in h hours

**Approach**: Binary search on speed
```java
boolean canEat(int[] piles, int k, int h) {
    long hours = 0;
    for (int pile : piles) {
        hours += (pile + k - 1) / k; // Ceiling division
    }
    return hours <= h;
}

int minEatingSpeed(int[] piles, int h) {
    int left = 1, right = Arrays.stream(piles).max().getAsInt();
    
    while (left < right) {
        int mid = left + (right - left) / 2;
        if (canEat(piles, mid, h)) {
            right = mid; // Try slower
        } else {
            left = mid + 1; // Need faster
        }
    }
    return left;
}
```
**Complexity**: O(n log m) where m = max(piles)

### Array & String Problems

#### 1. Flower Bed Placement
**Problem**: Array of 0s and 1s, can you place k flowers with no adjacent flowers?

**Approach**: Greedy with neighbor checking
```java
boolean canPlaceFlowers(int[] flowerbed, int k) {
    int count = 0;
    for (int i = 0; i < flowerbed.length; i++) {
        if (flowerbed[i] == 0) {
            boolean leftEmpty = (i == 0) || (flowerbed[i-1] == 0);
            boolean rightEmpty = (i == flowerbed.length-1) || (flowerbed[i+1] == 0);
            
            if (leftEmpty && rightEmpty) {
                flowerbed[i] = 1;
                count++;
                if (count >= k) return true; // Early termination
            }
        }
    }
    return count >= k;
}
```
**Complexity**: O(n) time, O(1) space

#### 2. Reverse Each Word in String
**Problem**: "Hello Bangkok:)" → "olleh :)kokgnaB"

**Approach**: Two pointers or stack
```java
String reverseWords(String s) {
    char[] arr = s.toCharArray();
    int start = 0;
    
    for (int i = 0; i <= arr.length; i++) {
        if (i == arr.length || arr[i] == ' ') {
            reverse(arr, start, i - 1);
            start = i + 1;
        }
    }
    return new String(arr);
}

void reverse(char[] arr, int left, int right) {
    while (left < right) {
        char temp = arr[left];
        arr[left++] = arr[right];
        arr[right--] = temp;
    }
}
```

#### 3. String Comparison with Backspace
**Problem**: Compare strings S1 and S2 where '#' means backspace

**Approach**: Two pointers from end
```java
boolean backspaceCompare(String s1, String s2) {
    int i = s1.length() - 1, j = s2.length() - 1;
    
    while (i >= 0 || j >= 0) {
        i = getNextValidChar(s1, i);
        j = getNextValidChar(s2, j);
        
        if (i >= 0 && j >= 0 && s1.charAt(i) != s2.charAt(j)) return false;
        if ((i >= 0) != (j >= 0)) return false;
        i--; j--;
    }
    return true;
}

int getNextValidChar(String s, int index) {
    int backspace = 0;
    while (index >= 0) {
        if (s.charAt(index) == '#') {
            backspace++;
        } else if (backspace > 0) {
            backspace--;
        } else {
            break;
        }
        index--;
    }
    return index;
}
```
**Complexity**: O(n+m) time, O(1) space

### Dynamic Programming

#### 1. Robot Path Counting
**Problem**: Robot at (0,0), reach (n-1,m-1) with obstacles, only right/down moves

**Approach**: 2D DP
```java
int uniquePathsWithObstacles(int[][] grid) {
    int m = grid.length, n = grid[0].length;
    if (grid[0][0] == 1 || grid[m-1][n-1] == 1) return 0;
    
    int[][] dp = new int[m][n];
    dp[0][0] = 1;
    
    // First column
    for (int i = 1; i < m; i++) {
        dp[i][0] = (grid[i][0] == 1) ? 0 : dp[i-1][0];
    }
    
    // First row
    for (int j = 1; j < n; j++) {
        dp[0][j] = (grid[0][j] == 1) ? 0 : dp[0][j-1];
    }
    
    // Fill rest
    for (int i = 1; i < m; i++) {
        for (int j = 1; j < n; j++) {
            if (grid[i][j] == 1) {
                dp[i][j] = 0;
            } else {
                dp[i][j] = dp[i-1][j] + dp[i][j-1];
            }
        }
    }
    return dp[m-1][n-1];
}
```
**Optimization**: Use 1D array for O(n) space
**Complexity**: O(m*n) time, O(m*n) space

#### 2. Count Palindromic Substrings
**Problem**: Count all palindromic substrings

**Approach**: Expand around center
```java
int countSubstrings(String s) {
    int count = 0;
    for (int i = 0; i < s.length(); i++) {
        count += expandAroundCenter(s, i, i);     // Odd length
        count += expandAroundCenter(s, i, i + 1); // Even length
    }
    return count;
}

int expandAroundCenter(String s, int left, int right) {
    int count = 0;
    while (left >= 0 && right < s.length() && 
           s.charAt(left) == s.charAt(right)) {
        count++;
        left--;
        right++;
    }
    return count;
}
```
**Complexity**: O(n²) time, O(1) space

### Graph Problems

#### Course Schedule (Topological Sort) ⭐
**Problem**: Given prerequisites, can you finish all courses?

**Approach 1: DFS with Cycle Detection**
```java
boolean canFinish(int numCourses, int[][] prerequisites) {
    List<List<Integer>> graph = new ArrayList<>();
    for (int i = 0; i < numCourses; i++) graph.add(new ArrayList<>());
    
    for (int[] pre : prerequisites) {
        graph.get(pre[1]).add(pre[0]);
    }
    
    int[] state = new int[numCourses]; // 0=white, 1=gray, 2=black
    
    for (int i = 0; i < numCourses; i++) {
        if (state[i] == 0 && hasCycle(graph, state, i)) {
            return false;
        }
    }
    return true;
}

boolean hasCycle(List<List<Integer>> graph, int[] state, int node) {
    if (state[node] == 1) return true;  // Back edge (cycle)
    if (state[node] == 2) return false; // Already processed
    
    state[node] = 1; // Mark as visiting
    for (int neighbor : graph.get(node)) {
        if (hasCycle(graph, state, neighbor)) return true;
    }
    state[node] = 2; // Mark as visited
    return false;
}
```
**Complexity**: O(V + E) time, O(V) space

**Approach 2: BFS (Kahn's Algorithm)**
```java
boolean canFinish(int numCourses, int[][] prerequisites) {
    int[] indegree = new int[numCourses];
    List<List<Integer>> graph = new ArrayList<>();
    for (int i = 0; i < numCourses; i++) graph.add(new ArrayList<>());
    
    for (int[] pre : prerequisites) {
        graph.get(pre[1]).add(pre[0]);
        indegree[pre[0]]++;
    }
    
    Queue<Integer> queue = new LinkedList<>();
    for (int i = 0; i < numCourses; i++) {
        if (indegree[i] == 0) queue.offer(i);
    }
    
    int count = 0;
    while (!queue.isEmpty()) {
        int course = queue.poll();
        count++;
        for (int next : graph.get(course)) {
            if (--indegree[next] == 0) {
                queue.offer(next);
            }
        }
    }
    return count == numCourses;
}
```
**Complexity**: O(V + E) time, O(V) space

### Greedy Problems

#### Eliminate Maximum Monsters (Airplane Landing)
**Problem**: Planes descending at different speeds, shoot one per second, maximize saves

**Approach**: Sort by arrival time, greedy selection
```java
int eliminateMaximum(int[] dist, int[] speed) {
    int n = dist.length;
    double[] arrival = new double[n];
    
    for (int i = 0; i < n; i++) {
        arrival[i] = (double) dist[i] / speed[i];
    }
    
    Arrays.sort(arrival);
    
    for (int i = 0; i < n; i++) {
        if (arrival[i] <= i) { // Plane lands before we can shoot
            return i;
        }
    }
    return n;
}
```
**Key Insight**: Always shoot the plane that will arrive soonest
**Complexity**: O(n log n) time, O(n) space

---

## 🏗️ Low Level Design (LLD)

### Common Systems & Patterns

#### 1. Banking System
**Requirements**: Accounts, transactions, transfers, balance inquiry

**Core Classes**:
```java
// Account types using Strategy Pattern
interface AccountType {
    double calculateInterest(double balance);
    boolean canWithdraw(double amount, double balance);
}

class SavingsAccount implements AccountType {
    public double calculateInterest(double balance) {
        return balance * 0.04; // 4% interest
    }
    public boolean canWithdraw(double amount, double balance) {
        return balance >= amount;
    }
}

class CurrentAccount implements AccountType {
    private double overdraftLimit = 10000;
    public double calculateInterest(double balance) { return 0; }
    public boolean canWithdraw(double amount, double balance) {
        return (balance + overdraftLimit) >= amount;
    }
}

class Account {
    private String accountId;
    private double balance;
    private AccountType type;
    
    public synchronized boolean withdraw(double amount) {
        if (type.canWithdraw(amount, balance)) {
            balance -= amount;
            return true;
        }
        return false;
    }
    
    public synchronized void deposit(double amount) {
        balance += amount;
    }
}

class Transaction {
    private String txnId;
    private Account from, to;
    private double amount;
    private LocalDateTime timestamp;
    private TransactionStatus status;
}
```

**Key Points**:
- Use **Strategy Pattern** for different account types
- **Synchronized methods** for thread safety
- Transaction history with **audit trail**
- Exception handling for insufficient funds

#### 2. Elevator System
**Requirements**: Multiple elevators, floor requests, efficient scheduling

**Core Design**:
```java
enum Direction { UP, DOWN, IDLE }
enum ElevatorState { MOVING, STOPPED, MAINTENANCE }

class Elevator {
    private int currentFloor;
    private Direction direction;
    private ElevatorState state;
    private Set<Integer> requests = new TreeSet<>();
    
    public void addRequest(int floor) {
        requests.add(floor);
        if (state == ElevatorState.STOPPED) {
            move();
        }
    }
    
    private void move() {
        while (!requests.isEmpty()) {
            int target = getNextFloor();
            moveToFloor(target);
            requests.remove(target);
            openDoors();
        }
        state = ElevatorState.STOPPED;
    }
    
    private int getNextFloor() {
        // SCAN algorithm: continue in same direction
        if (direction == Direction.UP) {
            return requests.stream()
                .filter(f -> f >= currentFloor)
                .findFirst()
                .orElse(Collections.max(requests));
        } else {
            return requests.stream()
                .filter(f -> f <= currentFloor)
                .findFirst()
                .orElse(Collections.min(requests));
        }
    }
}

class ElevatorController {
    private List<Elevator> elevators;
    
    public void requestElevator(int floor, Direction direction) {
        Elevator best = findBestElevator(floor, direction);
        best.addRequest(floor);
    }
    
    private Elevator findBestElevator(int floor, Direction dir) {
        // Find closest idle or moving in same direction
        return elevators.stream()
            .min((e1, e2) -> calculateScore(e1, floor) - calculateScore(e2, floor))
            .orElse(elevators.get(0));
    }
}
```

**Key Concepts**:
- **State Pattern** for elevator states
- **SCAN algorithm** for efficient scheduling
- Priority queue for requests
- Thread safety for concurrent requests

#### 3. HashMap with Concurrency
**Requirements**: Thread-safe hash map implementation

**Design**:
```java
class ConcurrentHashMap<K, V> {
    private static final int DEFAULT_CAPACITY = 16;
    private static final int SEGMENT_COUNT = 16;
    
    private Segment<K, V>[] segments;
    
    static class Segment<K, V> {
        private Node<K, V>[] table;
        private final ReentrantLock lock = new ReentrantLock();
        
        V put(K key, V value) {
            lock.lock();
            try {
                int hash = hash(key);
                int index = hash % table.length;
                
                Node<K, V> node = table[index];
                while (node != null) {
                    if (node.key.equals(key)) {
                        V old = node.value;
                        node.value = value;
                        return old;
                    }
                    node = node.next;
                }
                
                // Add new node
                Node<K, V> newNode = new Node<>(key, value);
                newNode.next = table[index];
                table[index] = newNode;
                return null;
            } finally {
                lock.unlock();
            }
        }
        
        V get(K key) {
            // No lock needed for reads
            int hash = hash(key);
            int index = hash % table.length;
            Node<K, V> node = table[index];
            
            while (node != null) {
                if (node.key.equals(key)) return node.value;
                node = node.next;
            }
            return null;
        }
    }
    
    public V put(K key, V value) {
        int segmentIndex = hash(key) % SEGMENT_COUNT;
        return segments[segmentIndex].put(key, value);
    }
}
```

**Key Points**:
- **Segment locking** reduces contention
- Lock-free reads when possible
- Use **AtomicInteger** for size counting
- Handle rehashing with proper synchronization

#### 4. Flashcard System
**Requirements**: Spaced repetition, card states, user progress

**Core Design**:
```java
enum CardState { NEW, LEARNING, REVIEWING, RELEARNING }
enum Difficulty { EASY, MEDIUM, HARD }

class Flashcard {
    private String question;
    private String answer;
    private CardState state;
    private int repetitions;
    private double easeFactor = 2.5;
    private LocalDateTime nextReview;
    
    public void updateAfterReview(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                easeFactor += 0.15;
                repetitions++;
                nextReview = calculateNextReview(repetitions, easeFactor);
                state = CardState.REVIEWING;
                break;
            case MEDIUM:
                repetitions++;
                nextReview = calculateNextReview(repetitions, easeFactor);
                break;
            case HARD:
                easeFactor -= 0.20;
                repetitions = 0;
                nextReview = LocalDateTime.now().plusMinutes(10);
                state = CardState.RELEARNING;
                break;
        }
    }
    
    private LocalDateTime calculateNextReview(int n, double ef) {
        // SM-2 algorithm
        int interval = (n == 1) ? 1 : (int)(interval * ef);
        return LocalDateTime.now().plusDays(interval);
    }
}

class Deck {
    private String name;
    private List<Flashcard> cards;
    
    public List<Flashcard> getDueCards() {
        return cards.stream()
            .filter(c -> c.getNextReview().isBefore(LocalDateTime.now()))
            .collect(Collectors.toList());
    }
}
```

### Design Patterns Summary

**Strategy Pattern**: Multiple algorithms/behaviors
- Payment methods (Credit, Debit, UPI)
- Account types (Savings, Current)
- Sorting strategies

**State Pattern**: Object behavior changes based on state
- Elevator (Moving, Stopped, Maintenance)
- Order (Pending, Confirmed, Shipped, Delivered)
- Flashcard states

**Singleton Pattern**: Single instance
```java
class Database {
    private static volatile Database instance;
    
    private Database() {}
    
    public static Database getInstance() {
        if (instance == null) {
            synchronized (Database.class) {
                if (instance == null) {
                    instance = new Database();
                }
            }
        }
        return instance;
    }
}
```

**Factory Pattern**: Object creation abstraction
```java
interface Vehicle {
    void drive();
}

class VehicleFactory {
    public static Vehicle createVehicle(String type) {
        switch (type) {
            case "car": return new Car();
            case "bike": return new Bike();
            default: throw new IllegalArgumentException();
        }
    }
}
```

### Thread Safety Best Practices
- Use **synchronized** for critical sections
- **AtomicInteger/AtomicLong** for counters
- **ConcurrentHashMap** over synchronized HashMap
- **ReentrantLock** for complex locking scenarios
- Avoid nested locks (deadlock risk)

---

## 🌐 High Level Design (HLD)

### Hotel/Flight Booking System ⭐⭐⭐

**Most frequently asked - prepare this thoroughly!**

#### Requirements
- Search hotels/flights by location, dates
- View availability and pricing
- Book rooms/seats
- Prevent double booking
- Handle concurrent requests
- Real-time price updates from 3rd parties
- Proximity-based search

#### Architecture

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
┌──────▼──────────┐
│  API Gateway    │ ← Rate Limiting, Auth
│  (Load Balancer)│
└──────┬──────────┘
       │
   ┌───┴────┬──────────┬───────────┐
   │        │          │           │
┌──▼──┐ ┌──▼──┐   ┌───▼────┐  ┌──▼───┐
│Search│ │Book │   │Payment │  │User  │
│Service│ │Service│   │Service │  │Service│
└──┬──┘ └──┬──┘   └───┬────┘  └──┬───┘
   │       │          │          │
┌──▼───────▼──────────▼──────────▼──┐
│         Redis Cache                │
└──────────┬────────────────────────┘
           │
   ┌───────┴────────┬──────────┐
┌──▼──────┐  ┌──────▼─┐  ┌─────▼───┐
│ Primary │  │ Read   │  │External │
│   DB    │  │Replica │  │ APIs    │
└─────────┘  └────────┘  └─────────┘
```

#### Core Components

**1. Search Service**
- Geospatial indexing (PostGIS/Elasticsearch)
- Filter by price, rating, amenities
- Cache popular searches

**2. Inventory Management**
- Track available rooms/seats
- Real-time updates from suppliers
- Version control for optimistic locking

**3. Booking Service**
- Handle reservation requests
- Distributed locking (Redis/Zookeeper)
- Transaction management

**4. Payment Service**
- Process payments via gateway
- Handle refunds
- Exactly-once processing (Kafka)

#### Preventing Double Booking

**Approach 1: Pessimistic Locking**
```sql
BEGIN TRANSACTION;
SELECT * FROM rooms 
WHERE room_id = 101 
FOR UPDATE; -- Row lock

-- Check availability
IF available THEN
    UPDATE rooms SET status = 'booked' WHERE room_id = 101;
    INSERT INTO bookings (...);
END IF;

COMMIT;
```
**Pros**: Strong consistency
**Cons**: Lower throughput, deadlock risk

**Approach 2: Optimistic Locking**
```sql
-- Add version column
SELECT room_id, available_count, version FROM inventory WHERE room_id = 101;

-- Try to book
UPDATE inventory 
SET available_count = available_count - 1, 
    version = version + 1
WHERE room_id = 101 AND version = @original_version;

-- If 0 rows updated, retry (someone else booked)
```
**Pros**: Better throughput
**Cons**: Retry logic needed

**Approach 3: Distributed Lock (Redis)**
```java
String lockKey = "room:101:lock";
boolean acquired = redis.setnx(lockKey, "locked", 30); // 30 sec TTL

if (acquired) {
    try {
        // Check and book
        if (isAvailable(roomId)) {
            bookRoom(roomId);
        }
    } finally {
        redis.del(lockKey);
    }
} else {
    // Retry or fail
}
```

#### Proximity-Based Search

**Geohashing**:
- Divide world into grid
- Each location has geohash (e.g., "9q5")
- Nearby locations share prefix
- Query: Find all hotels where geohash starts with "9q5"

**Implementation**:
```sql
CREATE INDEX idx_geohash ON hotels(geohash);

SELECT * FROM hotels 
WHERE geohash LIKE '9q5%' 
  AND check_in = '2025-02-01'
ORDER BY distance(lat, lon, @user_lat, @user_lon)
LIMIT 20;
```

**Alternative: PostGIS**:
```sql
SELECT * FROM hotels
WHERE ST_DWithin(
    location::geography,
    ST_MakePoint(@user_lon, @user_lat)::geography,
    50000 -- 50km radius
);
```

#### Caching Strategy

**Cache Layers**:
1. **CDN**: Static content (images, CSS)
2. **Redis**: Search results, inventory
3. **Application Cache**: Session data

**What to Cache**:
- Popular search queries (TTL: 5 mins)
- Hotel/Flight details (TTL: 1 hour)
- Availability (TTL: 30 secs) ← Short due to real-time updates
- User sessions

**Cache Invalidation**:
```java
// On booking
redis.del("hotel:101:availability");
redis.del("search:london:2025-02-01");

// Update cache
redis.setex("hotel:101:availability", 30, newCount);
```

#### Flight Price Management

**Challenge**: Multiple suppliers, different API limits, costs per call

**Solution**:
```
┌────────────────────────────────────┐
│     API Gateway / Rate Limiter      │
├────────────────────────────────────┤
│  Supplier A: 100 req/min           │
│  Supplier B: 50 req/min            │
│  Supplier C: 200 req/min           │
└──────────┬─────────────────────────┘
           │
   ┌───────▼────────┐
   │ Price Aggregator│
   └───────┬────────┘
           │
   ┌───────▼────────┐
   │  Redis Cache   │ ← Cache prices (TTL: 2-5 mins)
   └───────┬────────┘
           │
   ┌───────▼────────┐
   │   Database     │ ← Historical prices
   └────────────────┘
```

**Rate Limiting**:
```java
class RateLimiter {
    // Token bucket per supplier
    Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    boolean allowRequest(String supplier) {
        Bucket bucket = buckets.get(supplier);
        return bucket.tryConsume(1);
    }
}

// Before calling supplier API
if (!rateLimiter.allowRequest("SupplierA")) {
    return getCachedPrice(); // Fallback to cache
}
```

**Showing Cheapest Option**:
```java
List<Price> prices = getAllSupplierPrices(flightId);
Price cheapest = prices.stream()
    .min(Comparator.comparing(Price::getAmount))
    .orElse(null);

// Store supplier info for booking
redis.setex("flight:101:best_price", 120, 
    cheapest.getSupplier() + ":" + cheapest.getAmount());
```

#### Scalability

**Horizontal Scaling**:
- Stateless services → Auto-scaling groups
- Database: Read replicas for queries
- Sharding: By geography (US, EU, APAC)

**Load Balancing**:
- Round-robin for stateless services
- Consistent hashing for cache servers

**Database Scaling**