# PayPay-Style QR Code Payment System Design
## Complete Interview Preparation Guide

---

## TABLE OF CONTENTS

1. [Clarifying Questions (ALWAYS START HERE)](#1-clarifying-questions)
2. [Functional Requirements](#2-functional-requirements)
3. [Non-Functional Requirements (NFRs) — CRITICAL](#3-non-functional-requirements)
4. [Capacity Estimation](#4-capacity-estimation)
5. [High-Level Architecture](#5-high-level-architecture)
6. [Service Decomposition (Microservices)](#6-service-decomposition)
7. [Wallet System Design (Multi-Type, Priority-Based)](#7-wallet-system-design)
8. [QR Code Payment Flow](#8-qr-code-payment-flow)
9. [Database Design](#9-database-design)
10. [Distributed Transactions & SAGA Pattern](#10-distributed-transactions--saga-pattern)
11. [Concurrency: Pessimistic vs Optimistic Locking](#11-concurrency-pessimistic-vs-optimistic-locking)
12. [Security](#12-security)
13. [Handling Peak Load & TPS Control](#13-handling-peak-load--tps-control)
14. [Idempotency](#14-idempotency)
15. [Coding Round: Wallet Deduction Logic](#15-coding-round-wallet-deduction-logic)
16. [Coding Round: Binary Tree Level-Order Traversal](#16-coding-round-binary-tree-level-order-traversal)
17. [Why This Design? (Justification Framework)](#17-why-this-design-justification-framework)

---

## 1. Clarifying Questions

> **Pro tip:** Always clarify before designing. Interviewers expect this. It shows you think about business impact, not just code.

Ask these questions and note down the answers:

**Scale:**
- How many registered users? (e.g., 50M users)
- Expected peak TPS? (Given: 1500+ TPS)
- Read/Write ratio? (Given: 50/50)

**Functional scope:**
- Does the merchant generate the QR code, or the user? (Given: Merchant generates, user scans)
- Is the QR code static (always same) or dynamic (per-transaction)?
- What wallet types exist? (Points, balance, credit card, cashback)
- What is the priority order for deduction? (e.g., expiring cashback → points → balance → credit card)
- Does cashback expire? How? (Given: Yes, expiring cashback)
- Do we need real-time balance updates or eventual consistency is ok?

**Business rules:**
- What happens on payment timeout? Rollback or retry?
- Is partial deduction allowed? (e.g., points cover ¥200, balance covers remaining ¥800)
- Maximum transaction amount?

**Geo/Compliance:**
- Data must stay in Japan? (Given: Yes)
- PCI DSS compliance needed for credit card? (Yes, always assume)

---

## 2. Functional Requirements

### Minimum (Core)
| Feature | Description |
|---|---|
| Top-up | User loads money from bank/credit card to PayPay wallet |
| Wallet ownership | Both users and merchants have wallets with balances and history |
| QR Payment | Merchant shows QR code → User scans → Payment deducted |
| Payment history | User and merchant can view transaction history |
| Multi-type wallet | Points, balance, credit card, expiring cashback |
| Priority deduction | Deduct from wallet types in priority order |

### Additional
| Feature | Description |
|---|---|
| User authentication | OAuth2, MFA, device fingerprinting |
| Merchant payout | Transfer accumulated balance to merchant's bank account |
| Notifications | Real-time push/SMS/email on transaction events |
| Refunds | Reverse a transaction, restore wallet balances |

---

## 3. Non-Functional Requirements

> **This section FAILED the candidate in the feedback. Always address NFRs proactively and specifically.**

### 3.1 Performance
- **TPS:** System must handle **1500+ TPS** at peak (e.g., lunch hours, holidays like paypay campaigns)
- **Latency:** Payment confirmation < **2 seconds** end-to-end (p99)
- **Read latency:** Balance check < **100ms** (p99)
- **QR code generation:** < **500ms**

### 3.2 Availability (SLA)
- **Payment service:** 99.99% uptime = ~52 minutes downtime/year
- **Read services (history, balance):** 99.9% uptime acceptable
- **Strategy:** Active-active multi-AZ deployment within Japan region

### 3.3 Consistency
- **Payments (money movement):** **Strong consistency** — money must never be double-spent or lost
- **History / notifications:** **Eventual consistency** is acceptable
- **Balance reads:** Strongly consistent at point of payment; stale reads OK for display

### 3.4 Durability
- **Zero data loss** for payment records (financial audit trail)
- Write-ahead log (WAL) + synchronous replication
- Backup: Daily full + continuous WAL archiving to separate AZ

### 3.5 Security
- **Encryption in transit:** TLS 1.3 for all APIs
- **Encryption at rest:** AES-256 for all PII and financial data
- **Authentication:** JWT with short expiry (15 min) + refresh tokens
- **Authorization:** RBAC — users can only access own wallets
- **PCI DSS:** Credit card data handled via certified vault (e.g., tokenization); raw card numbers never stored in our DB
- **Rate limiting:** Per-user, per-device transaction rate limits (fraud prevention)
- **Fraud detection:** ML-based anomaly detection on transaction patterns
- **QR code security:** Time-bound, signed tokens (HMAC-SHA256) in QR codes; expire in 5 minutes

### 3.6 Data Residency
- All data stored in Japan (Tokyo region: AWS ap-northeast-1 or equivalent)
- No cross-border data transfer except for card network calls (Visa/Mastercard), which is exempt

### 3.7 Scalability
- Horizontally scalable stateless services
- Database: vertical scaling + read replicas + eventual sharding by user_id

### 3.8 Compliance
- Financial Services Agency (FSA) Japan regulations
- AML (Anti Money Laundering) checks
- KYC (Know Your Customer) for registration

---

## 4. Capacity Estimation

> Show your math. Interviewers love this.

**Assumptions:**
- 50M registered users, 10M Daily Active Users (DAU)
- Average 3 transactions/user/day on peak days
- Merchant QR scans: 10M transactions/day on normal days

**TPS Calculation:**
```
Normal TPS:  10,000,000 / 86,400 ≈ 115 TPS
Peak TPS:    ~10x normal during PayPay campaigns = ~1,500 TPS  ✓ (matches requirement)
```

**Storage Estimation:**
```
Per transaction record: ~500 bytes
Daily transactions:     10M
Daily storage:          10M × 500B = 5 GB/day
Yearly (hot):           5GB × 365 = ~1.8 TB/year
```

**QPS for balance reads:**
```
50/50 read-write ratio at 1500 TPS → 750 read QPS + 750 write QPS
```

**Bandwidth:**
```
Avg API payload: 2 KB
At 1500 TPS:     1500 × 2KB = 3 MB/s — easily manageable
```

---

## 5. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                                  │
│          [User App (iOS/Android)]   [Merchant App/Terminal]          │
└──────────────────────┬──────────────────────┬───────────────────────┘
                       │ HTTPS/TLS             │ HTTPS/TLS
                       ▼                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     API GATEWAY / LOAD BALANCER                      │
│     - Auth (JWT verification)  - Rate Limiting  - SSL Termination    │
│     - Request routing          - DDoS protection                     │
└────┬─────────────┬────────────────┬────────────┬─────────────────────┘
     │             │                │            │
     ▼             ▼                ▼            ▼
┌────────┐  ┌──────────┐  ┌──────────────┐  ┌──────────────┐
│  User  │  │  Wallet  │  │   Payment    │  │     QR       │
│Service │  │  Service │  │   Service    │  │   Service    │
└────────┘  └──────────┘  └──────┬───────┘  └──────────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    ▼             ▼              ▼
             ┌───────────┐ ┌──────────┐ ┌─────────────┐
             │  Message  │ │ Ledger   │ │Notification │
             │  Broker   │ │ Service  │ │  Service    │
             │ (Kafka)   │ └──────────┘ └─────────────┘
             └───────────┘
```

---

## 6. Service Decomposition

### 6.1 User Service
**Responsibility:** Registration, authentication, KYC, profile management

**APIs:**
- `POST /users/register`
- `POST /auth/login` → returns JWT + refresh token
- `GET /users/{userId}/profile`

**DB:** PostgreSQL (relational, ACID, user data is critical)

---

### 6.2 Wallet Service
**Responsibility:** Manage wallet balances per type, enforce priority deduction

**APIs:**
- `GET /wallets/{userId}` — returns all wallet types and balances
- `POST /wallets/{userId}/topup` — add funds from bank/card
- `POST /wallets/{userId}/deduct` — deduct by priority (internal, called by Payment Service)
- `GET /wallets/{userId}/history`

**Key design decisions:**
- Each wallet type is a separate row in `wallet_balance` table
- Priority is configurable per user or system-wide
- Uses **optimistic locking** for concurrent balance updates (see Section 11)

---

### 6.3 Payment Service
**Responsibility:** Orchestrate the payment flow, ensure atomicity, handle failures

**APIs:**
- `POST /payments/initiate` — user scans QR, starts payment
- `GET /payments/{paymentId}/status`
- `POST /payments/{paymentId}/confirm`
- `POST /payments/{paymentId}/refund`

**Key design decisions:**
- Acts as the SAGA orchestrator
- Generates idempotency keys
- Manages distributed transaction state machine

---

### 6.4 QR Service
**Responsibility:** Generate and validate QR codes for merchants

**APIs:**
- `POST /qr/generate` — merchant requests QR for an amount
- `POST /qr/validate` — payment service validates scanned QR

**QR code structure (payload):**
```json
{
  "merchant_id": "M123",
  "amount": 1500,
  "currency": "JPY",
  "expires_at": "2024-01-01T12:05:00Z",
  "nonce": "abc123xyz",
  "signature": "HMAC-SHA256(merchant_id+amount+expires_at+nonce, secret)"
}
```

**Security:** Time-bound (5 min TTL), HMAC-signed, single-use nonce stored in Redis.

---

### 6.5 Ledger Service
**Responsibility:** Immutable double-entry bookkeeping for all financial movements

Every transaction creates two entries: debit from user, credit to merchant. This is the financial source of truth.

---

### 6.6 Notification Service
**Responsibility:** Push/SMS/email notifications on transaction events

Consumes Kafka events asynchronously. Does not block payment flow.

---

### 6.7 Merchant Payout Service
**Responsibility:** Batch transfer of accumulated merchant balance to bank

Runs scheduled jobs (e.g., nightly), uses bank API integration.

---

## 7. Wallet System Design

### 7.1 Wallet Types and Priority

| Priority | Wallet Type | Notes |
|---|---|---|
| 1 (highest) | Expiring Cashback | Use first — expires soonest |
| 2 | Points | Use before real money |
| 3 | PayPay Balance | Main wallet |
| 4 | Linked Bank | ACH/bank direct debit |
| 5 (lowest) | Credit Card | Last resort, incurs fees |

> **Why this order?** Expiring cashback loses value if unused. Points are sunk cost. Credit card incurs interchange fees for the merchant.

### 7.2 Priority Deduction Algorithm

```java
// Wallet type entity
public class WalletBalance {
    String walletType;       // "CASHBACK", "POINTS", "BALANCE", "CREDIT_CARD"
    BigDecimal available;
    int priority;            // lower = deduct first
    LocalDateTime expiresAt; // null if no expiry
}

// Priority deduction logic
public List<DeductionEntry> deduct(List<WalletBalance> wallets, BigDecimal totalAmount) {
    // Sort by priority, then by expiry date (soonest-expiring first within same priority)
    wallets.sort(Comparator
        .comparingInt(WalletBalance::getPriority)
        .thenComparing(w -> w.getExpiresAt() == null ? LocalDateTime.MAX : w.getExpiresAt())
    );

    BigDecimal remaining = totalAmount;
    List<DeductionEntry> deductions = new ArrayList<>();

    for (WalletBalance wallet : wallets) {
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
        if (wallet.getAvailable().compareTo(BigDecimal.ZERO) <= 0) continue;

        BigDecimal deductAmount = remaining.min(wallet.getAvailable());
        deductions.add(new DeductionEntry(wallet.getWalletType(), deductAmount));
        remaining = remaining.subtract(deductAmount);
    }

    if (remaining.compareTo(BigDecimal.ZERO) > 0) {
        throw new InsufficientBalanceException("Total balance insufficient. Short by: " + remaining);
    }

    return deductions;
}
```

**Example:**
```
Payment: ¥1,000
Wallets:
  - Expiring Cashback: ¥200  → deduct ¥200
  - Points:            ¥150  → deduct ¥150
  - Balance:           ¥800  → deduct ¥650 (remainder)
  - Credit Card:       ¥∞    → deduct ¥0 (not needed)
Total deducted: ¥1,000 ✓
```

---

## 8. QR Code Payment Flow

### 8.1 Happy Path Sequence

```
User App          API Gateway       Payment Svc      Wallet Svc      Merchant DB
   │                   │                │                │               │
   │── Scan QR ──────►│                │                │               │
   │                   │── Validate ──►│                │               │
   │                   │   QR token    │── Validate ──►│               │
   │                   │               │   QR (Redis)   │               │
   │                   │               │── Check ───────────────────────►│
   │                   │               │   merchant                     │
   │                   │               │◄───────────────────────────────│
   │                   │               │── Get wallet ─►│               │
   │                   │               │   balances     │               │
   │                   │               │◄───────────────│               │
   │                   │               │                │               │
   │◄── Show preview ──│               │                │               │
   │    (amount, from) │               │                │               │
   │── Confirm ───────►│               │                │               │
   │                   │── Execute ───►│                │               │
   │                   │   payment     │── SAGA ───────►│               │
   │                   │               │   Deduct       │               │
   │                   │               │◄───────────────│               │
   │                   │               │── Credit ──────────────────────►│
   │                   │               │   merchant                     │
   │                   │               │── Emit event to Kafka          │
   │◄── Success ───────│◄──────────────│                │               │
   │                   │               │                │               │
```

### 8.2 QR Code Types

**Dynamic QR (recommended):** Merchant's terminal generates a new QR per transaction with amount embedded. Expires in 5 minutes.

**Static QR:** Merchant has a fixed QR (no amount). User enters amount manually. Simpler but less secure.

> **Design choice:** Dynamic QR preferred because it is more secure (signed, time-bound), better UX (no manual entry), and enables merchant receipt matching.

---

## 9. Database Design

### 9.1 Database Choices & Justification

| Service | DB Choice | Justification |
|---|---|---|
| User Service | PostgreSQL | Relational, ACID, good for KYC/user data |
| Wallet Service | PostgreSQL | Financial data needs ACID transactions |
| Payment Service | PostgreSQL | Strong consistency for payment state |
| Ledger Service | PostgreSQL (append-only) | Immutable audit trail |
| QR Nonce store | Redis | Fast TTL-based lookup, single-use validation |
| Transaction history | PostgreSQL + read replicas | Frequent reads, eventual replica OK |
| Notification queue | Kafka | High-throughput event streaming |

> **Why NOT NoSQL (MongoDB, DynamoDB) for financial data?**
> NoSQL databases are often eventually consistent. For money movement, you need ACID guarantees — you can't afford a scenario where a read sees stale data and allows a double-spend. PostgreSQL gives strong ACID and is battle-tested for financial systems.

---

### 9.2 Schema Design

```sql
-- Users
CREATE TABLE users (
    user_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number  VARCHAR(20) UNIQUE NOT NULL,
    email         VARCHAR(255) UNIQUE,
    kyc_status    VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, VERIFIED, REJECTED
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Wallet balances (one row per user per wallet type)
CREATE TABLE wallet_balance (
    wallet_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(user_id),
    wallet_type   VARCHAR(30) NOT NULL,  -- BALANCE, POINTS, CASHBACK, CREDIT_CARD
    balance       NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
    priority      INT NOT NULL,           -- Lower = higher priority
    expires_at    TIMESTAMPTZ,            -- NULL = no expiry (for cashback)
    version       BIGINT NOT NULL DEFAULT 0, -- For optimistic locking
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, wallet_type)
);

-- Payments
CREATE TABLE payments (
    payment_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key   VARCHAR(64) UNIQUE NOT NULL,
    user_id           UUID NOT NULL REFERENCES users(user_id),
    merchant_id       UUID NOT NULL,
    amount            NUMERIC(15, 2) NOT NULL,
    currency          CHAR(3) NOT NULL DEFAULT 'JPY',
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- PENDING → PROCESSING → COMPLETED | FAILED | REFUNDED
    qr_code_id        UUID,
    failure_reason    TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_merchant_id ON payments(merchant_id);
CREATE INDEX idx_payments_status ON payments(status) WHERE status = 'PENDING';

-- Deduction entries (which wallet type paid how much in each payment)
CREATE TABLE payment_deductions (
    deduction_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id    UUID NOT NULL REFERENCES payments(payment_id),
    wallet_type   VARCHAR(30) NOT NULL,
    amount        NUMERIC(15, 2) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Ledger (immutable double-entry)
CREATE TABLE ledger_entries (
    entry_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id    UUID NOT NULL REFERENCES payments(payment_id),
    account_id    UUID NOT NULL,   -- user or merchant wallet
    account_type  VARCHAR(20) NOT NULL, -- USER | MERCHANT
    entry_type    VARCHAR(10) NOT NULL, -- DEBIT | CREDIT
    amount        NUMERIC(15, 2) NOT NULL,
    wallet_type   VARCHAR(30) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
    -- NO UPDATE OR DELETE EVER — audit trail is immutable
);

-- QR Codes
CREATE TABLE qr_codes (
    qr_code_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id   UUID NOT NULL,
    amount        NUMERIC(15, 2) NOT NULL,
    nonce         VARCHAR(64) UNIQUE NOT NULL,
    signature     VARCHAR(128) NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | USED | EXPIRED
    expires_at    TIMESTAMPTZ NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_qr_nonce ON qr_codes(nonce) WHERE status = 'ACTIVE';

-- Merchants
CREATE TABLE merchants (
    merchant_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    wallet_id     UUID NOT NULL REFERENCES wallet_balance(wallet_id),
    bank_account  VARCHAR(255), -- encrypted/tokenized
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

---

## 10. Distributed Transactions & SAGA Pattern

> **This was the #1 failure point in the interview feedback. Master this section.**

### 10.1 The Problem

A payment involves multiple services:
1. Validate QR (QR Service)
2. Deduct from user wallet (Wallet Service)
3. Credit merchant wallet (Wallet Service / Merchant Service)
4. Record in ledger (Ledger Service)
5. Send notification (Notification Service)

If step 3 fails after step 2 succeeded → **user lost money, merchant didn't get paid** → data inconsistency.

You **cannot** use a single database transaction across services. So we need **distributed transaction patterns.**

---

### 10.2 SAGA Pattern (Choreography vs Orchestration)

**SAGA:** Break a distributed transaction into a sequence of local transactions, each with a **compensating transaction** that undoes it on failure.

#### Option A: Choreography SAGA (Event-driven)
Each service listens to events and reacts. No central coordinator.

```
Payment Initiated
      │
      ▼
[QR Service] → QR_VALIDATED event
      │
      ▼
[Wallet Service] → DEDUCTION_COMPLETED event
      │
      ▼
[Merchant Service] → CREDIT_COMPLETED event
      │
      ▼
[Ledger Service] → LEDGER_RECORDED event
      │
      ▼
Payment COMPLETED
```

**On failure:** Each service listens for FAILED events and emits compensating events.

**Problem:** Hard to debug, hard to track overall saga state.

---

#### Option B: Orchestration SAGA (Recommended for PayPay)

**Payment Service acts as orchestrator.** It explicitly calls each step and manages failure/compensation.

```
Payment Service (Orchestrator)
    │
    ├─[Step 1]─► QR Service.validate(qr_id)
    │               ✓ OK
    ├─[Step 2]─► Wallet Service.deduct(userId, amount, wallets)
    │               ✓ OK → save deduction_id for rollback
    ├─[Step 3]─► Merchant Service.credit(merchantId, amount)
    │               ✗ FAIL!
    │
    │   ── COMPENSATION (reverse order) ──
    ├─[Comp 2]─► Wallet Service.refund(deduction_id)   ← rollback step 2
    ├─[Comp 1]─► QR Service.markFailed(qr_id)          ← rollback step 1
    │
    └─► Payment.status = FAILED
```

**Why orchestration?** Single place to trace payment state. Easier to debug, monitor, replay.

---

### 10.3 SAGA State Machine

```
INITIATED
   │
   ├──────► QR_VALIDATED
   │              │
   │              ├──────► DEDUCTION_IN_PROGRESS
   │              │               │
   │              │               ├──────► CREDIT_IN_PROGRESS
   │              │               │               │
   │              │               │               ├──────► COMPLETED ✓
   │              │               │               │
   │              │               │               └──────► COMPENSATING → FAILED ✗
   │              │               │
   │              │               └──────► COMPENSATING → FAILED ✗
   │              │
   │              └──────► FAILED ✗
   │
   └──────► FAILED ✗ (QR invalid/expired)
```

---

### 10.4 Handling Network Timeouts

**Scenario:** Payment Service called Wallet Service to deduct, but got no response (timeout). Did it deduct or not?

**Solution: Idempotency + Status polling**

1. Payment Service generates `idempotency_key` before calling Wallet Service
2. On timeout → Payment Service polls `GET /deductions?idempotency_key=xyz`
3. If COMPLETED → continue to next step
4. If NOT FOUND → safe to retry
5. If FAILED → compensate

---

## 11. Concurrency: Pessimistic vs Optimistic Locking

> **Explicitly asked in the feedback. Know both, know when to use each.**

### 11.1 The Race Condition Problem

```
User has ¥1,000 balance.
Thread A: read balance = ¥1,000
Thread B: read balance = ¥1,000
Thread A: deduct ¥1,000, write balance = ¥0
Thread B: deduct ¥1,000, write balance = ¥0  ← DOUBLE SPEND! Lost ¥1,000.
```

---

### 11.2 Pessimistic Locking

**Lock the row before reading.** No other transaction can modify it until you release the lock.

```sql
BEGIN;

-- Lock the row for update (blocks other transactions)
SELECT balance, version
FROM wallet_balance
WHERE user_id = $1 AND wallet_type = $2
FOR UPDATE;  -- ← pessimistic lock

-- Do your logic
UPDATE wallet_balance
SET balance = balance - $3,
    updated_at = now()
WHERE user_id = $1 AND wallet_type = $2;

COMMIT;
```

**✅ Use when:** High contention (many threads writing to same row), e.g., a popular merchant's balance being credited by 100 simultaneous users.

**❌ Downside:** Blocks other transactions → lower throughput. Risk of deadlocks if locks are acquired in inconsistent order.

**Deadlock prevention:** Always acquire locks in consistent order (e.g., always lock user wallet before merchant wallet, ordered by wallet_id).

---

### 11.3 Optimistic Locking

**Don't lock. Read with version number. On write, check version hasn't changed.**

```sql
-- Read
SELECT balance, version FROM wallet_balance
WHERE user_id = $1 AND wallet_type = $2;
-- Got: balance=1000, version=5

-- Application logic: compute new balance

-- Write with version check
UPDATE wallet_balance
SET balance = $new_balance,
    version = version + 1,
    updated_at = now()
WHERE user_id = $1
  AND wallet_type = $2
  AND version = 5;  -- ← version check

-- If 0 rows updated → someone else modified it → retry
```

**✅ Use when:** Low contention (rare concurrent writes to same wallet), e.g., a regular user's wallet.

**❌ Downside:** On high contention, many retries → wasted work.

---

### 11.4 Which to Use for PayPay?

| Scenario | Lock Type | Reason |
|---|---|---|
| User wallet deduction (payment) | **Pessimistic** | Payment is time-critical, must not fail on retry |
| Merchant wallet credit | **Pessimistic** | High contention during campaigns |
| User balance top-up | **Optimistic** | Low contention, user-initiated |
| Cashback award (batch) | **Optimistic** | Low conflict, retry acceptable |

---

## 12. Security

> **Never skip this section in a fintech interview.**

### 12.1 Authentication & Authorization
- **JWT (RS256):** Asymmetric signing, short expiry (15 min)
- **Refresh tokens:** Stored in HttpOnly cookie or secure device storage; rotate on use
- **MFA:** OTP via SMS for login and payments > ¥10,000
- **Device binding:** Payment allowed only from registered devices

### 12.2 API Security
- **Rate limiting:** 
  - Login: 5 attempts/minute/IP
  - Payment: 10 payments/minute/user
- **HTTPS/TLS 1.3** everywhere
- **Input validation:** Reject malformed requests at gateway
- **CORS:** Strict origin policy

### 12.3 Financial Security
- **Idempotency keys:** Prevent double-charging from retries
- **QR code signing:** HMAC-SHA256 with server-side secret
- **QR code TTL:** 5-minute expiry, single-use nonce
- **Transaction limits:** Per-transaction and daily limits per user
- **Fraud detection:** Real-time ML scoring on each transaction

### 12.4 Data Security
- **PII encryption:** Phone numbers, addresses AES-256 encrypted at rest
- **Card data tokenization:** Raw card numbers never stored (use Stripe/Adyen vault or equivalent)
- **Audit logging:** All admin actions and payment state changes logged immutably
- **Secrets management:** AWS Secrets Manager / Vault; no secrets in code

### 12.5 Compliance
- **PCI DSS Level 1** for credit card processing
- **FSA Japan** regulations for e-money operators
- **AML:** Flag transactions > ¥1M, suspicious patterns reported
- **GDPR / Personal Information Protection Act (Japan)** for data handling

---

## 13. Handling Peak Load & TPS Control

> **Interviewers want to hear: caching, queues, rate limiting, circuit breakers.**

### 13.1 API Gateway Layer
- **Rate limiting per user/merchant:** Sliding window counters in Redis
- **DDoS protection:** Cloudflare or AWS Shield at edge
- **Connection pooling:** PgBouncer in front of PostgreSQL

### 13.2 Application Layer
- **Horizontal scaling:** Kubernetes, auto-scale pods on CPU/RPS metrics
- **Circuit breaker:** Hystrix/Resilience4J — if Wallet Service is slow, fail fast and queue the request
- **Bulkhead pattern:** Isolate payment threads from notification threads so notification slowness doesn't block payments

### 13.3 Queue-Based Load Leveling
For non-critical async work (notifications, ledger recording after payment), push to Kafka:
```
Payment confirmed → emit PaymentCompleted event to Kafka
Kafka → Notification Service (processes at its own pace)
Kafka → Ledger Service (processes at its own pace)
```
This decouples payment speed from notification/ledger speed.

### 13.4 Caching
- **Balance cache (Redis):** Cache wallet balance with 1-second TTL
  - On payment: invalidate cache for involved wallets
  - This handles the 50% read load without hitting DB every time
- **Merchant info cache:** Cache merchant details (static, 5 min TTL)
- **QR nonce store (Redis):** Fast TTL-based single-use nonce validation

### 13.5 Database
- **Read replicas:** Route balance reads and history queries to read replicas
- **Connection pooling:** PgBouncer; max 100 connections per DB instance
- **Sharding (future):** Shard wallet_balance and payments by user_id (consistent hash) when single DB can't handle volume
- **Partitioning:** Partition payments table by month for efficient historical queries

### 13.6 Campaign Spike Handling (PayPay bonus events)
During campaigns (e.g., 20% cashback):
- Pre-scale Kubernetes pods using scheduled HPA
- Enable aggressive caching
- Increase Kafka consumer group size
- Activate read-only mode for non-critical features (e.g., history) to protect core payment flow

---

## 14. Idempotency

> **Critical for financial systems. Prevents double-charging on retries.**

### 14.1 How It Works

**Client:** Generate a unique `Idempotency-Key` header (UUID) before initiating payment.

**Server:**
1. Check Redis: `GET idempotency:{key}`
2. If found → return cached response (same as original)
3. If not found → process, then `SET idempotency:{key} {response} EX 86400` (24h TTL)

```
POST /payments
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000

Client retries (network timeout)? Same key → same response. No double charge.
```

### 14.2 Implementation

```java
@PostMapping("/payments")
public ResponseEntity<PaymentResponse> initiatePayment(
    @RequestHeader("Idempotency-Key") String idempotencyKey,
    @RequestBody PaymentRequest request) {

    // Check cache
    String cached = redis.get("idempotency:" + idempotencyKey);
    if (cached != null) {
        return ResponseEntity.ok(deserialize(cached)); // Return same result
    }

    // Process payment
    PaymentResponse response = paymentService.process(request);

    // Cache result for 24h
    redis.setex("idempotency:" + idempotencyKey, 86400, serialize(response));

    return ResponseEntity.ok(response);
}
```

### 14.3 Duplicate Payment Detection
Beyond API-level idempotency, detect at business level:
- Same user + same merchant + same amount within 30 seconds → flag as potential duplicate
- Show user confirmation: "You already paid this merchant ¥1,500 30 seconds ago. Pay again?"

---

## 15. Coding Round: Wallet Deduction Logic

### Problem: Transfer money between users when buying at a store

```java
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

// ===== DOMAIN MODELS =====

enum WalletType {
    EXPIRING_CASHBACK(1),
    POINTS(2),
    BALANCE(3),
    BANK_ACCOUNT(4),
    CREDIT_CARD(5);

    final int defaultPriority;
    WalletType(int p) { this.defaultPriority = p; }
}

class WalletBalance {
    WalletType type;
    BigDecimal available;
    int priority;
    LocalDateTime expiresAt; // null = no expiry

    WalletBalance(WalletType type, BigDecimal available, LocalDateTime expiresAt) {
        this.type = type;
        this.available = available;
        this.priority = type.defaultPriority;
        this.expiresAt = expiresAt;
    }
}

class DeductionEntry {
    WalletType walletType;
    BigDecimal amount;

    DeductionEntry(WalletType walletType, BigDecimal amount) {
        this.walletType = walletType;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return walletType + ": ¥" + amount;
    }
}

class PaymentResult {
    boolean success;
    List<DeductionEntry> deductions;
    String message;

    PaymentResult(List<DeductionEntry> deductions) {
        this.success = true;
        this.deductions = deductions;
    }

    PaymentResult(String error) {
        this.success = false;
        this.message = error;
    }
}

// ===== WALLET SERVICE =====

class WalletService {

    /**
     * Deducts totalAmount from wallets in priority order.
     * Partial deduction from multiple wallets is allowed.
     */
    public PaymentResult deductByPriority(List<WalletBalance> wallets, BigDecimal totalAmount) {
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return new PaymentResult("Invalid amount: must be positive");
        }

        // Sort by priority, then by expiry (soonest first within same priority)
        List<WalletBalance> sorted = new ArrayList<>(wallets);
        sorted.sort(Comparator
            .comparingInt((WalletBalance w) -> w.priority)
            .thenComparing(w -> w.expiresAt == null ? LocalDateTime.MAX : w.expiresAt)
        );

        // Filter out empty wallets
        BigDecimal totalAvailable = sorted.stream()
            .map(w -> w.available)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAvailable.compareTo(totalAmount) < 0) {
            return new PaymentResult(
                "Insufficient balance. Available: ¥" + totalAvailable + ", Required: ¥" + totalAmount
            );
        }

        BigDecimal remaining = totalAmount;
        List<DeductionEntry> deductions = new ArrayList<>();

        for (WalletBalance wallet : sorted) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            if (wallet.available.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal deductAmount = remaining.min(wallet.available);
            deductions.add(new DeductionEntry(wallet.type, deductAmount));
            remaining = remaining.subtract(deductAmount);
        }

        return new PaymentResult(deductions);
    }
}

// ===== PAYMENT SERVICE =====

class PaymentService {

    private final WalletService walletService = new WalletService();

    /**
     * Transfer money from buyer to merchant when buying at a store.
     * Returns the payment result with deduction breakdown.
     */
    public PaymentResult processStorePayment(
            String buyerUserId,
            String merchantId,
            BigDecimal amount,
            List<WalletBalance> buyerWallets) {

        System.out.println("Processing payment: User=" + buyerUserId
            + " → Merchant=" + merchantId + " Amount=¥" + amount);

        // Step 1: Calculate deductions
        PaymentResult result = walletService.deductByPriority(buyerWallets, amount);

        if (!result.success) {
            System.out.println("Payment FAILED: " + result.message);
            return result;
        }

        // Step 2: Print deduction breakdown
        System.out.println("Payment SUCCEEDED. Deductions:");
        for (DeductionEntry entry : result.deductions) {
            System.out.println("  → " + entry);
        }

        // (In real code: persist to DB, credit merchant, write ledger, emit Kafka event)

        return result;
    }
}

// ===== DEMO =====

public class Main {
    public static void main(String[] args) {
        List<WalletBalance> wallets = Arrays.asList(
            new WalletBalance(WalletType.EXPIRING_CASHBACK, new BigDecimal("200"),
                LocalDateTime.now().plusDays(1)),
            new WalletBalance(WalletType.POINTS, new BigDecimal("150"), null),
            new WalletBalance(WalletType.BALANCE, new BigDecimal("800"), null),
            new WalletBalance(WalletType.CREDIT_CARD, new BigDecimal("99999"), null)
        );

        PaymentService svc = new PaymentService();
        svc.processStorePayment("user-123", "merchant-456", new BigDecimal("1000"), wallets);
    }
}

/*
Output:
Processing payment: User=user-123 → Merchant=merchant-456 Amount=¥1000
Payment SUCCEEDED. Deductions:
  → EXPIRING_CASHBACK: ¥200
  → POINTS: ¥150
  → BALANCE: ¥650
*/
```

---

## 16. Coding Round: Binary Tree Level-Order Traversal

### Problem: Print each node with its level

```java
import java.util.*;

class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
}

class BinaryTreeTraversal {

    /**
     * Level-order traversal (BFS), printing each node with its level.
     * Time: O(n), Space: O(n) — queue holds at most one full level
     */
    public void printWithLevels(TreeNode root) {
        if (root == null) return;

        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        int level = 0;

        while (!queue.isEmpty()) {
            int size = queue.size(); // Number of nodes at current level
            System.out.print("Level " + level + ": ");

            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();
                System.out.print(node.val);
                if (i < size - 1) System.out.print(", ");

                if (node.left != null)  queue.offer(node.left);
                if (node.right != null) queue.offer(node.right);
            }

            System.out.println();
            level++;
        }
    }

    public static void main(String[] args) {
        /*
         *         1
         *        / \
         *       2   3
         *      / \   \
         *     4   5   6
         */
        TreeNode root = new TreeNode(1);
        root.left  = new TreeNode(2);
        root.right = new TreeNode(3);
        root.left.left   = new TreeNode(4);
        root.left.right  = new TreeNode(5);
        root.right.right = new TreeNode(6);

        new BinaryTreeTraversal().printWithLevels(root);
    }
}

/*
Output:
Level 0: 1
Level 1: 2, 3
Level 2: 4, 5, 6
*/
```

**Key insight to mention:**
- Use a **queue (BFS)**, not recursion (which is DFS)
- Snapshot `queue.size()` at the start of each iteration to know exactly how many nodes are at the current level
- Time complexity: O(n), Space: O(n) worst case (complete binary tree, bottom level has n/2 nodes)

---

## 17. Why This Design? (Justification Framework)

Use this framework when asked **"Why did you choose X?"**

> **Structure:** Problem → Options → Trade-offs → Decision → Future migration path

### Why PostgreSQL over Cassandra/MongoDB for wallets?
- **Problem:** Need ACID transactions for money movement. A partial deduction (deduct user but fail to credit merchant) must be atomically rolled back.
- **Options:** PostgreSQL (ACID), MongoDB (flexible schema, eventually consistent), Cassandra (high write throughput, eventual consistency)
- **Trade-off:** Cassandra gives better write throughput but at the cost of strong consistency — not acceptable for money.
- **Decision:** PostgreSQL. Financial data demands ACID. We can scale reads via replicas.
- **Future:** If write throughput becomes the bottleneck, shard by user_id. 1500 TPS is well within PostgreSQL's capabilities with proper indexing and connection pooling.

### Why SAGA Orchestration over 2PC (Two-Phase Commit)?
- **Problem:** Distributed transaction across 3+ services.
- **2PC downside:** Blocking protocol. If coordinator fails, all participants block indefinitely. Not suitable for microservices.
- **SAGA advantage:** Non-blocking. Each step is a local transaction. Failure triggers compensation. Services remain independently deployable.
- **Trade-off:** Eventual consistency (brief window where deduction is done but credit is pending). Acceptable for our use case since the window is milliseconds.

### Why optimistic locking for top-ups but pessimistic for payments?
- **Payments:** Multiple users might pay a popular merchant concurrently → high contention on merchant wallet → pessimistic lock avoids repeated retry storms.
- **Top-ups:** User tops up their own wallet; very low chance of concurrent write → optimistic lock is faster and avoids unnecessary blocking.

### Why Redis for QR nonce store?
- **Problem:** Need to validate that a QR nonce has never been used before (prevent QR replay attacks), with 5-minute TTL.
- **Redis features used:** `SET nonce used EX 300 NX` — atomic set-if-not-exists with TTL.
- **Alternative:** Store in PostgreSQL. But this is a hot-path (every payment scans QR). Redis gives sub-millisecond lookup.

---

## Quick Reference Cheat Sheet

| Topic | Key Points |
|---|---|
| NFRs | TPS, latency, SLA, security, consistency, durability, data residency |
| Wallet priority | Expiring cashback → Points → Balance → Bank → Credit Card |
| Locking | Pessimistic = high contention (payments); Optimistic = low contention (top-ups) |
| SAGA | Orchestration for PayPay; each step has a compensating transaction |
| Idempotency | Idempotency-Key header → Redis cache → prevents double-charge |
| QR security | HMAC-signed, 5min TTL, single-use nonce in Redis |
| DB choice | PostgreSQL for financial data (ACID); Redis for caching/nonces; Kafka for events |
| Peak load | Rate limiting + caching + Kafka queuing + horizontal scaling + pre-scaling |
| Failure | Timeout → poll status → idempotent retry or compensate |
| Double-entry | Every payment = debit user + credit merchant in ledger (immutable) |

---

*Prepared for PayPay / Fintech System Design Interview — L2/L3 Level*