# 🎯 Round 1 Interview Preparation Guide
### Golang · AWS · MySQL · Fintech Lending | 5 YOE

---

## 📋 TABLE OF CONTENTS
1. [Current Role & Self Introduction](#1-current-role--self-introduction)
2. [Microservices Architecture](#2-microservices-architecture)
3. [Current System Design (Whiteboard)](#3-current-system-design-whiteboard)
4. [Database Transactions — Optimistic vs Pessimistic](#4-database-transactions)
5. [Kafka — Scalability & Partition Management](#5-kafka--scalability)
6. [Scalability Strategies](#6-scalability-strategies)
7. [Architectural Patterns — Outbox & Event-Driven](#7-architectural-patterns)
8. [Virtual Machines & OS Layer](#8-virtual-machines--os-layer)
9. [SQL, NoSQL, MongoDB, Redis](#9-sql-nosql-mongodb-redis)
10. [ACID Properties & Atomicity](#10-acid-properties--atomicity)
11. [Load Balancer / Proxy](#11-load-balancer--proxy)
12. [Concurrency Issues & Solutions](#12-concurrency-issues--solutions)
13. [Normal vs Concurrent HashMaps](#13-normal-vs-concurrent-hashmaps)
14. [Garbage Collection](#14-garbage-collection)
15. [PII / Sensitive Data Management](#15-pii--sensitive-data-management)
16. [Test Execution — Parallelism & Integration](#16-test-execution)
17. [QR Code Payment System — HLD](#17-qr-code-payment-system--hld)
18. [Paytm Wallet + Payments — HLD](#18-paytm-wallet--payments--hld)

---

## 1. Current Role & Self Introduction

### 🗣️ Script (Practice out loud)

> *"I have 5 years of experience in backend engineering, currently working on a digital banking and lending platform built on Golang, AWS, and MySQL. I'm part of the Lending team where we handle core operations — disbursements, repayments, auto-repayments, waivers, and write-offs."*

### 📦 Recent Projects

#### Project 1: Portfolio Revenue Management
- Provide excellent offers to good-performing customers
- If customer repays on time → upgrade their **offered_limit** (e.g., 10M → 20M)
- Also modify: interest rate, tenure, expiry date
- Risk team evaluates score → Offer Service updates loan properties
- Audit trail written to separate `audit_log` table

#### Project 2: Platform Fee / Processing Fee
- **Before:** User disburses $100 → full $100 credited to CASA account
- **After:** $97 → CASA account, $3 → Bank Revenue Ledger account
- **Key:** Repayment + interest still calculated on full $100
- Achieved via atomic DB transaction — two ledger writes in one tx
- Fee config is per-product, stored in `fee_config` table

### 🤖 AI in Your Work
- ML scoring models evaluate customer creditworthiness for offer eligibility
- Personal use: AI coding assistants for boilerplate, test case generation, debugging Golang goroutine issues
- Risk team: Anomaly detection for suspicious repayment patterns

---

## 2. Microservices Architecture

### Definition
> Architectural style where a large app is split into small, **independently deployable services**, each owning its own data, communicating via APIs (REST/gRPC) or message queues (Kafka).

### Core Properties
| Property | Meaning |
|---|---|
| Single Responsibility | Each service does ONE thing |
| Independent Deployment | Deploy Repayment without touching Disbursement |
| Decentralized Data | Each service owns its own DB |
| Fault Isolation | Offer Service down ≠ Disbursement down |
| Horizontal Scalability | Scale only high-load services |

### Your Services (Whiteboard — draw boxes with arrows)

```
[API Gateway / ALB]
        |
   ┌────┴────┐──────────────┐──────────────┐
   ▼         ▼              ▼              ▼
[Disbursement] [Repayment] [Offer Svc]  [Waiver/WriteOff]
   |               |
   └──[Kafka]──────┘
          |
   [Notification Svc]
```

| Service | Responsibility | Communicates With |
|---|---|---|
| Disbursement Service | Process disbursal, split fee | Ledger Svc, Kafka |
| Repayment Service | EMI payments, auto-repayment | Disbursement, Notification |
| Offer Service | Evaluate & assign offers | Customer Profile, Lending Core |
| Waiver/WriteOff | Loan waivers, write-off logic | Lending Core, Audit |
| Notification Service | SMS/Email reminders | Kafka Consumer |

---

## 3. Current System Design (Whiteboard)

### 💡 Disbursement Flow with Platform Fee

```
Customer
   |
   ▼
[AWS ALB] ──── HTTPS ────▶ [Disbursement Service (Golang/ECS)]
                                    |
                          ┌─────────┴──────────┐
                          ▼                    ▼
                   [MySQL RDS]          [Redis (ElastiCache)]
                  (loan records,         idempotency key check
                   fee config,           offered_limit cache
                   ledger entries)       distributed lock
                          |
                 BEGIN TRANSACTION
                 ├─ Credit $97 → CASA account
                 └─ Credit $3  → Revenue Ledger
                 COMMIT
                          |
                          ▼
                   [Kafka / MSK]
                  topic: lending.disbursement.completed
                   /              \
          [Repayment Svc]   [Notification Svc]
         (schedule EMI         (SMS/Email
          on full $100)         to customer)
```

### AWS Infrastructure
| Service | Usage |
|---|---|
| AWS ALB | Load balancer, HTTP routing, SSL termination |
| AWS ECS + EC2 | Golang microservices in Docker containers |
| AWS RDS (MySQL) | Loan records, ledger entries, fee config |
| AWS ElastiCache (Redis) | Idempotency keys, offered_limit cache, distributed lock |
| AWS MSK (Managed Kafka) | Event streaming between services |
| AWS S3 | KYC documents, loan agreements |
| AWS CloudWatch | Logs, metrics, alerts |
| AWS KMS | Encryption key management |

### Offer Upgrade Flow (Portfolio Revenue Management)
```
[Cron / Event Trigger]
        |
        ▼
[Offer Evaluation Service]
  - Read repayment_score from DB
  - If score >= threshold:
      UPDATE loans SET offered_limit=20M, interest_rate=X, tenure=Y
  - Write to audit_log
        |
        ▼
[Kafka Event: OFFER_UPGRADED]
        |
        ▼
[Notification Svc] → SMS to Customer
```

---

## 4. Database Transactions

### Pessimistic Locking
> Assumes conflicts WILL happen. Locks row before read.

```sql
BEGIN;
SELECT * FROM loans WHERE loan_id = 123 FOR UPDATE;
-- Row is locked — no other tx can touch it
UPDATE loans SET outstanding_balance = outstanding_balance - 500 WHERE loan_id = 123;
COMMIT;
```

**Your use case:** Auto-repayment deductions — two goroutines cannot deduct simultaneously from same loan.

**Downside:** Can cause deadlocks under high concurrency.

### Optimistic Locking
> Assumes conflicts are RARE. No lock on read — check version at write time.

```sql
-- Read with version
SELECT balance, version FROM accounts WHERE id = 123;
-- version = 5

-- Write with version check
UPDATE accounts SET balance = balance - 100, version = version + 1
WHERE id = 123 AND version = 5;
-- If rows_affected = 0 → conflict → retry!
```

**Your use case:** Offer limit updates — multiple offer jobs race, only one wins.

**In Golang:**
```go
result, err := db.Exec(
    "UPDATE loans SET offered_limit=?, version=version+1 WHERE id=? AND version=?",
    newLimit, loanID, currentVersion,
)
if rows, _ := result.RowsAffected(); rows == 0 {
    return ErrOptimisticLockConflict // retry
}
```

### Concurrency Handling in Golang
```go
// Mutex for critical section
var mu sync.Mutex
mu.Lock()
defer mu.Unlock()
// safe operation

// RWMutex — multiple readers, single writer
var rw sync.RWMutex
rw.RLock() / rw.RUnlock()   // readers
rw.Lock() / rw.Unlock()     // writer

// context timeout — prevent goroutine hanging
ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
defer cancel()
db.QueryContext(ctx, "SELECT ...")
```

---

## 5. Kafka — Scalability

### Your Kafka Topics
| Topic | Producer | Consumers |
|---|---|---|
| `lending.disbursement.completed` | Disbursement Svc | Repayment Svc, Notification Svc |
| `lending.repayment.due` | Scheduler | Auto-Repayment Svc |
| `lending.offer.upgraded` | Offer Svc | Notification Svc |

### Partition Management
- **Partitions = parallelism unit** — more partitions = more consumers in parallel
- **Partition key = customer_id** — all events for one customer → same partition (ordering guaranteed)
- **Rule:** Consumer group size ≤ partition count; extra consumers sit idle
- **Rebalancing:** When consumer joins/leaves, Kafka redistributes partitions (brief pause)

### Retries & Dead Letter Topic
```go
func processWithRetry(msg kafka.Message, maxRetries int) error {
    for i := 0; i < maxRetries; i++ {
        if err := processMessage(msg); err == nil {
            return nil
        }
        time.Sleep(time.Duration(math.Pow(2, float64(i))) * time.Second) // exponential backoff
    }
    return publishToDLT(msg) // Dead Letter Topic after N retries
}
```

### Idempotency (prevent double-processing on retry)
```go
// Check idempotency key in Redis before processing
exists, _ := redis.Exists(ctx, "processed:"+msg.EventID).Result()
if exists > 0 { return nil } // already processed
// process...
redis.Set(ctx, "processed:"+msg.EventID, 1, 24*time.Hour)
```

---

## 6. Scalability Strategies

### CAP Theorem
| Property | Meaning | Your Choice |
|---|---|---|
| Consistency | All nodes see same data | ✅ MySQL = Consistent. Financial txns NEED this. |
| Availability | Always responds | Redis cache for reads. Core DB trades off for consistency. |
| Partition Tolerance | Works even if network splits | Always required. You get CP or AP, not all 3. |

> **Your answer:** *"We choose CP for core transaction flow. In fintech, a customer must never see wrong balance — consistency is non-negotiable."*

### Database Partitioning by Region
```sql
-- MySQL Range Partitioning on customer region
CREATE TABLE loan_transactions (
    id BIGINT,
    customer_id BIGINT,
    region VARCHAR(20),
    amount DECIMAL(18,2),
    created_at DATETIME
) PARTITION BY LIST COLUMNS(region) (
    PARTITION p_north VALUES IN ('IN_NORTH', 'IN_UP'),
    PARTITION p_south VALUES IN ('IN_SOUTH', 'IN_TN'),
    PARTITION p_east  VALUES IN ('IN_EAST', 'IN_WB')
);
```

**Benefits:** Queries hit only relevant partition, not full table scan.
**Risk:** Avoid cross-partition joins — use app-level aggregation.

### CDN Implementation
- Static assets (web app, policy docs) → **AWS CloudFront** (CDN)
- KYC document downloads cached at edge nodes
- API responses → **NOT cached** on CDN (financial data must be fresh)

### Message Queues for Non-Blocking Processing
| Sync (blocking) ❌ | Async via Kafka ✅ |
|---|---|
| Send SMS in disbursement flow | Publish event → Notification Svc consumes |
| Update credit score on repayment | Kafka event → async processing |
| Write audit logs | Batch insert via Kafka consumer |

**Result:** User gets 200 OK in <100ms. Heavy work happens async.

---

## 7. Architectural Patterns

### Outbox Pattern (Critical for Fintech)

**Problem:** After disbursement DB write succeeds, Kafka publish fails → repayment schedule never created!

**Solution:**
```sql
BEGIN;
-- 1. Write to main table
INSERT INTO disbursements (loan_id, amount, fee, status) VALUES (123, 100, 3, 'COMPLETED');

-- 2. Write to outbox IN SAME TRANSACTION
INSERT INTO outbox (event_type, payload, status, created_at)
VALUES ('DISBURSEMENT_COMPLETED', '{"loan_id":123,"amount":100}', 'PENDING', NOW());

COMMIT;
-- Separate poller reads PENDING outbox → publishes to Kafka → marks PROCESSED
```

**Guarantee:** DB write + event publish are atomically linked. No lost events.

### Event-Driven Design
- Repayment Svc does NOT call Disbursement Svc directly — **reacts to Kafka events**
- Loose coupling — add/remove Notification Svc without touching Disbursement Svc
- Saga pattern for distributed transactions across services

---

## 8. Virtual Machines & OS Layer

### VM vs Container
| | EC2 (VM) | ECS/Docker (Container) |
|---|---|---|
| Isolation | Full OS via hypervisor | Namespace + cgroups |
| Startup | Minutes | Seconds |
| Overhead | High (full OS per instance) | Low (shared kernel) |
| Your usage | DB servers, stateful infra | Golang microservices |

### OS Layer Concepts
- **Goroutine vs Thread:** Go runtime maps M goroutines to N OS threads (M:N threading)
- **Context switching:** OS switches processes — expensive. Go scheduler is user-space — cheap.
- **File descriptors:** Each DB/Kafka connection = 1 FD. `ulimit -n` must be configured.
- **Memory:** Virtual address space per process. Go GC manages heap. Stack grows dynamically (starts 8KB).

---

## 9. SQL, NoSQL, MongoDB, Redis

### Why MySQL for Lending
- ACID compliance — money cannot be partially committed
- Strong consistency — loan balance, ledger entries must be exact
- Relational data — loans, customers, transactions are naturally relational

### When to Use NoSQL / MongoDB
| Use Case | Why NoSQL |
|---|---|
| Audit logs | High write, schema-flexible, different event types |
| Customer activity feed | Document model fits nested JSON |
| Config store | Key-value, fast reads |

**MongoDB Internals:**
- BSON storage, B-tree indexes, `_id` auto-indexed
- `db.collection.find({customer_id: X})` — query
- `db.collection.insertOne({...})` — insert
- Replica sets for HA, sharding for horizontal scale

### Redis Usage in Your System
```go
// 1. Idempotency key — prevent duplicate disbursements
redis.SetNX(ctx, "idempotency:"+requestID, 1, 24*time.Hour)

// 2. Distributed lock — prevent concurrent disbursals on same loan
acquired, _ := redis.SetNX(ctx, "lock:loan:"+loanID, 1, 30*time.Second).Result()
if !acquired { return ErrAlreadyProcessing }
defer redis.Del(ctx, "lock:loan:"+loanID)

// 3. Cache offered_limit — avoid DB hit on every eligibility check
limit, err := redis.Get(ctx, "offered_limit:"+customerID).Float64()
if err == redis.Nil { // cache miss — fetch from DB }

// 4. Rate limiting
count, _ := redis.Incr(ctx, "ratelimit:"+customerID).Result()
if count == 1 { redis.Expire(ctx, "ratelimit:"+customerID, time.Minute) }
if count > 10 { return ErrRateLimited }
```

---

## 10. ACID Properties & Atomicity

| Property | Meaning | Your Example |
|---|---|---|
| **Atomicity** | All or nothing | Disbursement: $97 CASA + $3 Revenue BOTH commit or BOTH rollback |
| **Consistency** | DB always valid state | `offered_limit >= 0` enforced by constraints |
| **Isolation** | Concurrent txns don't interfere | Two auto-repayments on same loan → FOR UPDATE prevents double-deduct |
| **Durability** | Committed data survives crash | MySQL InnoDB redo log — committed disbursements survive server crash |

---

## 11. Load Balancer / Proxy

### AWS ALB in Your System
- Layer 7 (HTTP) routing: `/api/disburse` → Disbursement Svc, `/api/repay` → Repayment Svc
- Health checks: Pings `/health` every 30s — removes unhealthy instances automatically
- SSL Termination: HTTPS at ALB, internal traffic HTTP — saves CPU on services
- Sticky sessions: IP hash for session-aware routing (not needed in stateless Golang services)

### Load Balancing Algorithms
| Algorithm | When to Use |
|---|---|
| Round Robin | Default, equal request weight |
| Least Connections | Long-lived or variable-duration requests |
| IP Hash | Session stickiness required |
| Weighted | Canary deployments (10% → new version) |

### Nginx Config Example
```nginx
upstream lending_service {
    least_conn;
    server 10.0.1.1:8080 weight=5;
    server 10.0.1.2:8080 weight=5;
    server 10.0.1.3:8080 backup; # used only if others fail
}
server {
    listen 443 ssl;
    location /api/ { proxy_pass http://lending_service; }
    location /health { return 200 "ok"; }
}
```

---

## 12. Concurrency Issues & Solutions

### Common Problems
| Issue | Description | Solution |
|---|---|---|
| Data Race | Two goroutines write same variable | `sync.Mutex` |
| Deadlock | Two goroutines wait for each other | Lock ordering, timeouts |
| Double Processing | Kafka retry reprocesses event | Idempotency key in Redis |
| Double Spend | Two requests spend same balance | DB-level `FOR UPDATE` lock |

### Solutions in Golang
```go
// 1. Mutex — exclusive access
var mu sync.Mutex
mu.Lock(); defer mu.Unlock()

// 2. Channel — goroutine communication
resultCh := make(chan Result, 1)
go func() { resultCh <- doWork() }()
result := <-resultCh

// 3. WaitGroup — wait for multiple goroutines
var wg sync.WaitGroup
for _, job := range jobs {
    wg.Add(1)
    go func(j Job) { defer wg.Done(); process(j) }(job)
}
wg.Wait()

// 4. errgroup — goroutines with error handling
g, ctx := errgroup.WithContext(context.Background())
g.Go(func() error { return sendSMS(ctx, customer) })
g.Go(func() error { return updateAuditLog(ctx, event) })
if err := g.Wait(); err != nil { ... }
```

---

## 13. Normal vs Concurrent HashMaps

### Regular Map — NOT Thread Safe
```go
// DANGER: concurrent write causes PANIC
m := make(map[string]int)
go func() { m["key"] = 1 }() // goroutine 1
go func() { m["key"] = 2 }() // goroutine 2 — DATA RACE!
```

### Solution 1: sync.Map (built-in)
```go
var m sync.Map
m.Store("offered_limit:C123", 10_000_000)   // write
val, ok := m.Load("offered_limit:C123")      // read
m.Delete("offered_limit:C123")               // delete
m.Range(func(k, v any) bool { ...; return true }) // iterate
```
**Best for:** Read-heavy with infrequent writes (e.g., cache).

### Solution 2: map + RWMutex (more control)
```go
type SafeCache struct {
    mu    sync.RWMutex
    store map[string]float64
}
func (c *SafeCache) Get(key string) (float64, bool) {
    c.mu.RLock(); defer c.mu.RUnlock()
    val, ok := c.store[key]; return val, ok
}
func (c *SafeCache) Set(key string, val float64) {
    c.mu.Lock(); defer c.mu.Unlock()
    c.store[key] = val
}
```
**Best for:** When you need complex operations (check-then-set).

---

## 14. Garbage Collection

### Golang GC
- **Algorithm:** Tri-color mark-and-sweep (concurrent — runs with your program)
- **Trigger:** When heap doubles from last GC (`GOGC=100` default)
- **STW:** Very brief stop-the-world pauses only for marking roots
- **Tuning:** `GOGC=200` → GC less often (more memory, less CPU). `GOGC=50` → more frequent.

```go
// Reduce GC pressure with sync.Pool
var bufPool = sync.Pool{
    New: func() interface{} { return new(bytes.Buffer) },
}
buf := bufPool.Get().(*bytes.Buffer)
buf.Reset()
defer bufPool.Put(buf) // reuse — not GC'd
```

### Java GC (if asked)
| GC | Use Case |
|---|---|
| G1GC (default Java 9+) | Low-latency, predictable pauses |
| ZGC | Ultra-low latency (<10ms) |
| ParallelGC | High throughput, batch jobs |

---

## 15. PII / Sensitive Data Management

### Your Fintech Approach
- **Encryption at rest:** RDS encryption (AES-256), S3 SSE for documents
- **Encryption in transit:** TLS 1.2+ for all APIs and inter-service calls
- **PII masking in logs:** Account numbers, Aadhaar, PAN masked (`****1234`) before logging
- **AWS KMS:** Centralized key management, automatic key rotation
- **Field-level encryption:** `account_no`, `pan_number` stored encrypted in DB
- **RBAC:** IAM roles on ECS tasks — only authorized services access PII
- **Data minimization:** Store only what's needed; purge after retention period (RBI guidelines)
- **Audit trail:** All PII access logged with actor + timestamp

---

## 16. Test Execution

### Golang Parallelism
```go
func TestDisbursementFee(t *testing.T) {
    cases := []struct {
        amount, fee, net float64
    }{
        {100, 3, 97}, {500, 15, 485}, {1000, 30, 970},
    }
    for _, tc := range cases {
        tc := tc // capture loop variable
        t.Run(fmt.Sprintf("amount=%.0f", tc.amount), func(t *testing.T) {
            t.Parallel() // each subtest runs concurrently
            result := CalculatePlatformFee(tc.amount)
            assert.Equal(t, tc.fee, result.Fee)
            assert.Equal(t, tc.net, result.NetCredit)
        })
    }
}
```

### Integration Testing with Testcontainers
```go
// Spin up real MySQL + Redis in Docker for integration tests
mysqlC, _ := testcontainers.GenericContainer(ctx, testcontainers.GenericContainerRequest{
    ContainerRequest: testcontainers.ContainerRequest{
        Image:        "mysql:8.0",
        ExposedPorts: []string{"3306/tcp"},
        Env: map[string]string{"MYSQL_ROOT_PASSWORD": "test"},
        WaitingFor:   wait.ForListeningPort("3306/tcp"),
    },
    Started: true,
})
// Run full integration test against real DB
// Rollback after each test for isolation
```

### CI/CD Strategy
- Unit tests: `t.Parallel()` → run in same job, fast
- Integration tests: Separate job with Docker Compose
- End-to-end: Staging environment, run nightly
- Coverage gate: `go test -coverprofile=coverage.out` → fail if <80%

---

---

# 🔷 HIGH LEVEL DESIGN #1: QR Code Payment System

## Functional Requirements (FR)
- User can generate a QR code (static or dynamic) for receiving payment
- Payer can scan QR code and initiate payment
- System validates payer's balance and processes transfer in real-time
- Payment confirmation sent to both payer and payee (SMS/push notification)
- Support UPI, wallet, debit card as payment sources
- Handle P2P (person-to-person) and P2M (person-to-merchant) payments
- Support QR code expiry (dynamic QR valid for 15 minutes)
- Transaction history and status tracking

## Non-Functional Requirements (NFR)
| NFR | Target |
|---|---|
| Latency | P99 < 2 seconds end-to-end for payment |
| Throughput | 100,000 TPS peak (festival/sale season) |
| Availability | 99.99% uptime (4 nines) — ~52 min downtime/year |
| Consistency | Strong consistency for money movement (CP in CAP) |
| Durability | Zero transaction data loss — replicated DBs, Kafka |
| Security | PCI-DSS compliant, TLS everywhere, tokenization |
| Scalability | Horizontal — add nodes during peak load |
| Idempotency | Duplicate payment requests must not double-charge |

## Capacity Estimation (Back-of-Envelope — BOEE)
```
Daily Active Users (DAU):        50 million
Avg transactions per user/day:   2
Total transactions/day:          100 million
Peak multiplier:                 10x
Peak TPS:                        100M * 10 / 86400 ≈ 11,574 TPS (~12K TPS)

QR Code generation:              10 million/day
QR scan events:                  80 million/day

Storage per transaction record:  500 bytes
Daily storage:                   100M * 500B = 50 GB/day
1 year storage:                  ~18 TB (before compression)

Redis cache entries:             50M active QR codes * 200B = 10 GB
Kafka throughput:                12K events/sec = ~6 MB/sec
```

## High Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                            │
│     [Mobile App]     [Merchant POS]     [Web Browser]           │
└─────────────┬───────────────┬──────────────────┬────────────────┘
              │               │                  │
              └───────────────┴──────────────────┘
                              │ HTTPS/TLS
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        EDGE LAYER                                │
│   [AWS CloudFront CDN]  →  [AWS WAF]  →  [AWS ALB]             │
│   (Static assets,          (DDoS,         (SSL termination,      │
│    SDK downloads)           Bot filter)    Health checks)        │
└─────────────────────────────┬───────────────────────────────────┘
                              │
              ┌───────────────┼───────────────────┐
              ▼               ▼                   ▼
┌─────────────────┐  ┌─────────────────┐  ┌──────────────────┐
│  QR Service     │  │  Payment Service │  │  Notification    │
│  (Golang/ECS)   │  │  (Golang/ECS)   │  │  Service (ECS)   │
│                 │  │                 │  │                  │
│ - generateQR()  │  │ - initiate()    │  │ - SMS via SNS    │
│ - validateQR()  │  │ - validate()    │  │ - Push via FCM   │
│ - expireQR()    │  │ - execute()     │  │ - Email via SES  │
└────────┬────────┘  └────────┬────────┘  └──────────────────┘
         │                    │
         ▼                    ▼
┌─────────────────┐  ┌─────────────────┐
│ Redis Cluster   │  │  MySQL RDS      │
│ (ElastiCache)   │  │  (Primary DB)   │
│                 │  │                 │
│ qr:{token}      │  │ - transactions  │
│ idempotency:{}  │  │ - accounts      │
│ ratelimit:{}    │  │ - qr_codes      │
│ session:{}      │  │ (Multi-AZ,      │
└─────────────────┘  │  Read Replicas) │
                     └────────┬────────┘
                              │
                     ┌────────▼────────┐
                     │  Kafka (MSK)    │
                     │                 │
                     │ payment.initiated│
                     │ payment.executed │
                     │ payment.failed  │
                     └────────┬────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
   [Ledger Service]  [Fraud Detection]  [Analytics/Audit]
   (double-entry      (ML model,         (S3 + Athena,
    bookkeeping)       real-time          compliance
                       scoring)           reporting)
```

## QR Code Payment Flow (Step-by-Step)

### Step 1: QR Code Generation (Payee)
```
1. Merchant/User → POST /qr/generate {amount?, description?, expiry?}
2. QR Service:
   a. Generate unique token: UUID + HMAC signature
   b. For static QR: store in DB, no expiry
   c. For dynamic QR: store in Redis with TTL=900s (15 min)
   d. Encode to QR image (PNG) → return to client
3. Redis key: qr:{token} → {payee_id, amount, status:PENDING}
```

### Step 2: QR Scan & Payment Initiation (Payer)
```
1. Payer scans QR → decode token
2. App → GET /qr/resolve/{token}
   - QR Service checks Redis → returns payee details, amount
   - Validates: not expired, status=PENDING
3. Payer selects payment source (UPI/Wallet/Card)
4. App → POST /payment/initiate {token, payer_id, source, amount}
5. Payment Service:
   a. Check idempotency key (Redis)
   b. Validate payer balance
   c. Lock payer account (Redis distributed lock or DB FOR UPDATE)
   d. Write to outbox table (same tx as account debit)
   e. Publish PAYMENT_INITIATED to Kafka
```

### Step 3: Payment Execution
```
1. Payment Executor (Kafka consumer) picks up PAYMENT_INITIATED
2. Calls bank/UPI switch (NPCI) for UPI payments
3. On success:
   a. BEGIN TRANSACTION
   b. Debit payer account
   c. Credit payee account
   d. Insert transaction record (status=SUCCESS)
   e. Mark QR as USED (prevent reuse)
   f. COMMIT
4. Publish PAYMENT_EXECUTED to Kafka
5. Notification Svc → SMS + Push to both payer and payee
```

## Database Schema

```sql
-- QR Codes Table
CREATE TABLE qr_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(128) UNIQUE NOT NULL,
    payee_id BIGINT NOT NULL,
    amount DECIMAL(18,2),           -- NULL for open-amount QR
    qr_type ENUM('STATIC','DYNAMIC') NOT NULL,
    status ENUM('ACTIVE','USED','EXPIRED') DEFAULT 'ACTIVE',
    expiry_at DATETIME,
    created_at DATETIME DEFAULT NOW(),
    INDEX idx_token (token),
    INDEX idx_payee (payee_id)
);

-- Transactions Table
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    txn_ref VARCHAR(64) UNIQUE NOT NULL,  -- idempotency
    payer_id BIGINT NOT NULL,
    payee_id BIGINT NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    status ENUM('INITIATED','PROCESSING','SUCCESS','FAILED','REVERSED'),
    payment_source ENUM('UPI','WALLET','DEBIT_CARD'),
    qr_token VARCHAR(128),
    created_at DATETIME DEFAULT NOW(),
    updated_at DATETIME ON UPDATE NOW(),
    INDEX idx_payer (payer_id),
    INDEX idx_payee (payee_id),
    INDEX idx_txn_ref (txn_ref)
);

-- Accounts Table
CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    version BIGINT DEFAULT 0,   -- optimistic lock
    status ENUM('ACTIVE','FROZEN','CLOSED'),
    CONSTRAINT chk_balance CHECK (balance >= 0)
);
```

## Key Design Decisions

| Decision | Choice | Reason |
|---|---|---|
| QR token storage | Redis (dynamic) + DB (static) | Fast lookup for active QR, persistent for static |
| Payment execution | Async via Kafka | Non-blocking, retry-safe |
| Duplicate prevention | Idempotency key + DB UNIQUE constraint | Belt-and-suspenders approach |
| Account debit | Pessimistic lock (FOR UPDATE) | Money movement must be exact |
| CAP choice | CP (Consistency + Partition Tolerance) | Finance: consistency non-negotiable |
| Outbox pattern | Yes — for DB + Kafka atomicity | Prevent lost payment events |

## Failure Scenarios & Handling

| Failure | Detection | Recovery |
|---|---|---|
| QR expired | Redis TTL, check expiry_at | Return EXPIRED error to payer |
| Double scan | Redis lock + DB status=USED | Idempotency check blocks second payment |
| Bank API timeout | HTTP timeout 3s + retry 3x | Mark txn PROCESSING, async retry |
| Payment Service crash | Outbox table | Poller picks up PENDING on restart |
| Kafka consumer crash | Consumer group rebalance | Message re-delivered (idempotency handles it) |

---

---

# 🔷 HIGH LEVEL DESIGN #2: Paytm Wallet + Payments System

## Functional Requirements (FR)
- User registration and KYC verification (Min-KYC: ₹10K limit, Full-KYC: ₹1L limit)
- Add money to wallet (Debit card, Net banking, UPI)
- P2P money transfer (Paytm-to-Paytm)
- Merchant payments (QR, Payment link, POS)
- Bank transfer (Paytm Wallet → Bank account)
- Auto-debit / standing instructions
- Cashback and rewards management
- Transaction history and statements
- Multiple payment instruments (Wallet, UPI, Cards, EMI)
- Paytm Postpaid (BNPL — Buy Now Pay Later)

## Non-Functional Requirements (NFR)
| NFR | Target |
|---|---|
| Availability | 99.99% (cannot afford downtime during sales/festivals) |
| Latency | Wallet-to-Wallet: P99 < 500ms; Bank transfer: < 30s |
| Throughput | 500K TPS peak (Diwali, IPL peak loads) |
| Consistency | Strong for money movement; eventual for analytics |
| Data Retention | 7 years (RBI regulation) |
| Compliance | PCI-DSS Level 1, RBI guidelines, NPCI standards |
| Scalability | Multi-region active-active deployment |
| Fraud | Real-time fraud detection < 200ms |

## Capacity Estimation (BOEE)
```
Total registered users:          450 million
DAU:                             80 million
Peak concurrent users:           5 million

Transactions/day (normal):       50 million
Transactions/day (peak):         500 million (10x on Diwali)
Peak TPS:                        500M / 86400 * 3 (peak factor) ≈ 17,361 TPS

Wallet accounts:                 450M * 100B = 45 GB
Transaction records:             500M/day * 200B = 100 GB/day
Transaction history (7 yrs):     100 GB * 365 * 7 ≈ 255 TB

Kafka events/sec (peak):         50,000 events/sec
Redis memory needed:             80M sessions * 500B = 40 GB

Read/Write ratio:                80:20 (most requests are balance checks)
```

## High Level Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                           CLIENT LAYER                                │
│  [Android App]  [iOS App]  [Web App]  [Merchant SDK]  [POS Device]  │
└──────────────────────────────┬───────────────────────────────────────┘
                               │ HTTPS / TLS 1.3
                               ▼
┌──────────────────────────────────────────────────────────────────────┐
│                           EDGE & SECURITY                             │
│  [Cloudflare CDN]  →  [DDoS Protection]  →  [AWS WAF]              │
│  [API Gateway]  →  [Rate Limiter (Redis)]  →  [Auth Service (JWT)]  │
└──────────────────────────────┬───────────────────────────────────────┘
                               │
    ┌──────────────────────────┼──────────────────────────────┐
    ▼              ▼           ▼            ▼                 ▼
┌────────┐  ┌──────────┐ ┌─────────┐ ┌──────────┐  ┌──────────────┐
│  User  │  │  Wallet  │ │Payment  │ │  KYC &   │  │  Merchant    │
│  Svc   │  │  Svc     │ │  Svc    │ │  Auth    │  │  Svc         │
│        │  │          │ │         │ │  Svc     │  │              │
│-signup │  │-addMoney │ │-pay()   │ │-aadhaar  │  │-onboard()    │
│-login  │  │-transfer │ │-refund()│ │-pan      │  │-settlement() │
│-KYC    │  │-balance  │ │-status  │ │-video KYC│  │-qr_gen()     │
└───┬────┘  └────┬─────┘ └────┬────┘ └─────┬────┘  └──────┬───────┘
    │            │             │             │               │
    └────────────┴─────────────┴─────────────┴───────────────┘
                               │
                    ┌──────────▼──────────┐
                    │   Data Layer         │
                    │                      │
                    │  ┌────────────────┐  │
                    │  │ MySQL Cluster  │  │
                    │  │ (Sharded by    │  │
                    │  │  user_id)      │  │
                    │  │                │  │
                    │  │ Shard 0: 0-10M │  │
                    │  │ Shard 1:10-20M │  │
                    │  │ Shard N: ...   │  │
                    │  └────────────────┘  │
                    │                      │
                    │  ┌────────────────┐  │
                    │  │ Redis Cluster  │  │
                    │  │ - wallet cache │  │
                    │  │ - sessions     │  │
                    │  │ - rate limits  │  │
                    │  │ - locks        │  │
                    │  └────────────────┘  │
                    │                      │
                    │  ┌────────────────┐  │
                    │  │ Kafka (MSK)    │  │
                    │  │ - txn events   │  │
                    │  │ - fraud events │  │
                    │  │ - notif events │  │
                    │  └────────────────┘  │
                    └──────────────────────┘
                               │
       ┌───────────────────────┼────────────────────────┐
       ▼                       ▼                        ▼
[Ledger Service]      [Fraud Detection]        [Analytics Platform]
(Double-entry          (ML Model,               (Kafka → S3 →
 bookkeeping,           Real-time scoring        Spark/Athena →
 RBI compliance)        <200ms, Redis            Dashboard)
                        feature store)
       │
       ▼
[External Systems]
 - NPCI/UPI switch
 - Bank APIs (IMPS/NEFT)
 - Card networks (Visa/MC)
 - Aadhaar/PAN verification
```

## Core Microservices Deep Dive

### 1. Wallet Service
```
Responsibilities:
- Maintain wallet balance per user
- Add money, deduct money, reverse transaction
- Enforce KYC limits (Min-KYC: ₹10K/month, Full-KYC: ₹1L/month)
- Cashback credit

Key Operations:
POST /wallet/add      → Add money to wallet
POST /wallet/deduct   → Deduct from wallet
GET  /wallet/balance  → Return balance (Redis cache first)
POST /wallet/reverse  → Reverse a failed transaction

DB: MySQL shard by user_id
Cache: Redis — key: wallet:{user_id}, TTL: 60s
Lock: Redis distributed lock on wallet:{user_id} for mutations
```

### 2. Payment Service
```
Responsibilities:
- Orchestrate payment across instruments (wallet, UPI, card)
- Handle payment states: INITIATED → PROCESSING → SUCCESS/FAILED
- Retry failed payments, handle timeouts
- Integrate with NPCI for UPI, bank APIs for IMPS

State Machine:
INITIATED → PROCESSING → SUCCESS
                       → FAILED → REVERSED (if money was debited)
                       → TIMEOUT → async resolution
```

### 3. Ledger Service (Double-Entry Bookkeeping)
```
Every money movement = 2 ledger entries (debit + credit)

Example: User A pays ₹500 to User B
Entry 1: DEBIT  wallet_A  ₹500
Entry 2: CREDIT wallet_B  ₹500

Entry 3: DEBIT  float_account  ₹500  (RBI float management)
Entry 4: CREDIT settlement_acc ₹500

Schema:
ledger_entries (
  id, txn_id, account_id, entry_type(DEBIT/CREDIT),
  amount, balance_after, created_at
)
-- Immutable! Never UPDATE or DELETE ledger entries
```

### 4. Fraud Detection Service
```
Real-time checks (< 200ms):
- Velocity check: > 5 txns in 1 min → flag
- Amount anomaly: amount > 10x avg → flag
- Device fingerprint mismatch
- Location anomaly (transaction from different city)
- ML model score from feature store (Redis)

Async checks:
- Graph analysis: money mule detection
- Pattern recognition over 30-day history
```

## Add Money Flow (Wallet Top-up)

```
1. User → POST /wallet/add {amount: 1000, source: DEBIT_CARD, card_token: xxx}
2. API Gateway → Auth check → Rate limit check
3. Payment Service:
   a. Generate txn_ref (idempotency key)
   b. Check idempotency key in Redis (prevent duplicate)
   c. Call Fraud Detection → score < threshold → proceed
   d. Call Card Network (Razorpay/Stripe tokenized call)
   e. On card success:
      BEGIN TRANSACTION
        INSERT transactions (status=SUCCESS)
        UPDATE accounts SET balance = balance + 1000
        INSERT outbox (event: MONEY_ADDED)
      COMMIT
4. Outbox poller → publish MONEY_ADDED to Kafka
5. Notification Svc → "₹1000 added to your Paytm wallet"
6. Analytics Svc → Update user activity metrics
```

## P2P Transfer Flow (Paytm-to-Paytm)

```
1. User A → POST /payment/p2p {to: userB, amount: 500}
2. Resolve userB (phone → user_id lookup in Redis/DB)
3. Fraud check (async, < 200ms)
4. BEGIN TRANSACTION (on same DB shard if possible)
   a. SELECT balance FROM accounts WHERE id=A FOR UPDATE  -- lock A
   b. SELECT balance FROM accounts WHERE id=B FOR UPDATE  -- lock B (ORDER by id to prevent deadlock)
   c. Validate A.balance >= 500
   d. UPDATE accounts SET balance=balance-500 WHERE id=A
   e. UPDATE accounts SET balance=balance+500 WHERE id=B
   f. INSERT transactions (txn_ref, payer=A, payee=B, amount=500, status=SUCCESS)
   g. INSERT outbox (P2P_TRANSFER_DONE)
   COMMIT
5. Kafka: P2P_TRANSFER_DONE → Notification Svc, Ledger Svc, Rewards Svc
```

> **Cross-shard P2P (A and B on different shards):** Use Saga pattern — debit A on shard 0, publish event, credit B on shard 1. Compensating transaction if credit fails.

## Database Schema (Key Tables)

```sql
-- Accounts (sharded by user_id % N)
CREATE TABLE accounts (
    id BIGINT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    wallet_balance DECIMAL(18,2) DEFAULT 0.00,
    kyc_type ENUM('NONE','MIN_KYC','FULL_KYC') DEFAULT 'NONE',
    monthly_limit DECIMAL(18,2),
    monthly_spent DECIMAL(18,2) DEFAULT 0.00,
    version BIGINT DEFAULT 0,
    status ENUM('ACTIVE','FROZEN','CLOSED'),
    created_at DATETIME DEFAULT NOW(),
    CONSTRAINT chk_balance CHECK (wallet_balance >= 0)
);

-- Transactions (sharded by payer_id % N, also replicated by payee for reads)
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    txn_ref VARCHAR(64) UNIQUE NOT NULL,
    payer_id BIGINT,
    payee_id BIGINT,
    amount DECIMAL(18,2) NOT NULL,
    txn_type ENUM('P2P','ADD_MONEY','MERCHANT_PAY','BANK_TRANSFER','CASHBACK'),
    payment_instrument ENUM('WALLET','UPI','DEBIT_CARD','CREDIT_CARD','POSTPAID'),
    status ENUM('INITIATED','PROCESSING','SUCCESS','FAILED','REVERSED'),
    failure_reason VARCHAR(256),
    created_at DATETIME DEFAULT NOW(),
    updated_at DATETIME ON UPDATE NOW(),
    INDEX idx_payer_created (payer_id, created_at),
    INDEX idx_payee_created (payee_id, created_at)
) PARTITION BY RANGE (UNIX_TIMESTAMP(created_at)) (
    PARTITION p2024 VALUES LESS THAN (UNIX_TIMESTAMP('2025-01-01')),
    PARTITION p2025 VALUES LESS THAN (UNIX_TIMESTAMP('2026-01-01')),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

## Scalability Design

### DB Sharding Strategy
```
User shard = user_id % 16
Shard 0:  user_id 0–10M
Shard 1:  user_id 10M–20M
...
Shard 15: user_id 140M–150M

Each shard: Primary + 2 Read Replicas (Multi-AZ)
Cross-shard queries: Avoided — use event-driven (Kafka) pattern
Resharding: Consistent hashing to minimize data movement
```

### Caching Strategy (Redis)
```
wallet:{user_id}           → balance (TTL 60s, invalidated on mutation)
session:{token}            → user session (TTL 30min)
ratelimit:{user_id}:{min}  → request count (TTL 60s)
lock:wallet:{user_id}      → distributed lock (TTL 10s)
fraud:features:{user_id}   → ML feature vector (TTL 5min)
phone_to_userid:{phone}    → user_id mapping (TTL 24h)
```

### Kafka Topics
| Topic | Partitions | Consumers |
|---|---|---|
| `payment.initiated` | 64 (by payer_id) | Fraud Svc, Ledger Svc |
| `payment.completed` | 64 | Notification Svc, Rewards Svc |
| `payment.failed` | 32 | Retry Handler, Alert Svc |
| `wallet.topup` | 32 | Analytics, KYC limit updater |
| `fraud.alert` | 16 | Risk team, Account freeze |

## Key Design Decisions

| Decision | Choice | Reason |
|---|---|---|
| DB Sharding | user_id % N (hash) | Even distribution, no hotspots |
| Cross-shard txn | Saga + compensating tx | 2PC too slow and complex at scale |
| Fraud detection | Sync (< 200ms) + Async | Block obvious fraud; deep analysis async |
| Wallet balance cache | Redis (60s TTL) | 80% reads → cache reduces DB load significantly |
| Money movement | Pessimistic lock (FOR UPDATE) | Correctness over performance for money |
| Analytics | Separate read path (S3+Athena) | OLTP DB not suitable for analytics queries |
| KYC limits | DB-enforced + app-level | Double protection for regulatory compliance |

## Failure & Edge Case Handling

| Scenario | Solution |
|---|---|
| Payment Service crashes mid-txn | Outbox pattern — poller retries on restart |
| Bank API timeout | Async polling, txn stays PROCESSING, resolved within 30s |
| Double payment (user clicks twice) | Idempotency key (txn_ref) — DB UNIQUE constraint |
| Wallet overdraft | DB CHECK constraint + app-level validation |
| Cross-shard P2P failure | Saga compensating transaction — reverse debit |
| Redis cache down | Fallback to DB read; write-through on recovery |
| Kafka consumer lag | Increase partitions + consumer instances |
| Fraud spike | Circuit breaker → fallback to rules-based check |

---

## 📌 Interview Power Phrases

> "In our lending platform, we handle this using..."
> "We faced this exact issue in the Platform Fee feature — solved by..."
> "For fintech, we always choose CP over AP — consistency is non-negotiable when moving money."
> "We use the Outbox pattern to solve the dual-write problem between DB and Kafka."
> "Our QR token is validated atomically — Redis SET NX prevents any race condition on concurrent scans."

---

*Prepared for Round 1 Interview | Golang + AWS + MySQL + Fintech | 5 YOE*