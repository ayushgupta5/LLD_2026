# 🎯 Interview Preparation — STAR Format

> **Goal:** Explain your projects clearly in any interview using the STAR method.
> **STAR = Situation → Task → Action → Result**

---

## 📌 PROJECT 1 — Platform Fee Implementation (Disbursement Flow Redesign)

---

### ⭐ S — Situation

We had a **Loan Disbursement System** at scale:

| Metric | Value |
|---|---|
| Total Users | 10 Million |
| DAU | 3 Million |
| Daily Transactions | ~9 Million |

**Old Flow (Before):**
```
Disbursement: $100  →  User's CASA Account ($100)
1 Transaction = 1 Money Movement
```

**The Problem:** Bank had no mechanism to collect **Platform / Processing / Convenience / Admin Fees** at disbursement time. This caused revenue leakage and reconciliation pain.

---

### ⭐ T — Task

My responsibilities:
1. Re-architect disbursement to support **fee deduction at disbursal**
2. Split: `$100 → $97 (User CASA) + $3 (Bank Revenue GL)`
3. Handle **2× money movement surge** (9M → 18M) without degrading performance
4. Design load testing strategy + safe production rollout

---

### ⭐ A — Action

#### 🔁 Before vs After — Money Movement

| Aspect | Before | After |
|---|---|---|
| Disbursement Split | 100% → CASA | 97% → CASA + 3% → Revenue GL |
| Movements per Txn | **1** | **2** |
| Total Daily Movements | 9 Million | **18 Million** |
| Customer Types | All | NTB (New to Bank) + ETB (Existing — 3%) |

---

#### 🏗️ Disbursement API — Step-by-Step Flow

```
Client → Load Balancer → LoanExp Service ↔ Account-Service

LoanExp:
  Step 1 → Payment1   : Register Intent
  Step 2 → Payment2   : Update Intent
  Step 3 → LoanCore   : Disbursement Request
            LoanCore  → Ledger SVC : Ledger Call
            Ledger SVC → LoanCore  : ACK
            LoanCore  → Transfer Call API
                          ├── Money Movement 1 → $97 to User CASA
                          └── Money Movement 2 → $3 to Revenue GL
  Step 4 ← LoanCore   : ACK back to LoanExp
```

---

#### ⚙️ 5 CRON Jobs Implemented (Batch Processing)

| # | CRON Job | Purpose |
|---|---|---|
| 1 | Disbursement with Platform Fee | Core fee deduction at disbursal |
| 2 | Invoice PDF Generation | Per-customer invoice with fee breakup |
| 3 | Notification Service | Customer alerts post-disbursement |
| 4 | Initial Interest Calculation | Compute interest at disbursal time |
| 5 | Repayment Schedule Preparation | Generate full amortization schedule |

---

#### 🧪 Load Testing Strategy (Hypothesis-Based)

**Hypothesis from historical data:**
> `STG env: X% CPU` → `PRD env: ~3X% CPU`

**Setup:**
- Created a **temp table with 40,000 dummy users** in lower environment
- Triggered all 5 CRON jobs simultaneously
- Measured CPU & time to completion

**Initial Results (Before Optimization):**

| Metric | Observed |
|---|---|
| CPU Utilization | **40%** ❌ |
| Completion Time | **10–11 Hours** ❌ |

**Root Cause:** 2× money movements + 5 CRON jobs + batch size = compute bottleneck

---

#### 📈 Solution — Vertical + Horizontal Scaling

**Vertical Scaling (per pod):**

| Resource | Before | After |
|---|---|---|
| CPU | 500m | **2000m (4×)** |
| RAM | 1 GB | **4 GB (4×)** |

**Horizontal Scaling:**

| Stage | Min Replicas | Max Replicas |
|---|---|---|
| Before | 10 | 15 |
| After | **15** | **20** |

**After Optimization Results:**

| Metric | Before | After | Improvement |
|---|---|---|---|
| CPU Utilization | 40% | **15%** | 62% reduction ✅ |
| Completion Time | 10–11 hrs | **3–3.5 hrs** | 68% faster ✅ |

---

### ⭐ R — Result

- ✅ Fee deduction seamlessly embedded in every disbursement — zero manual ops
- ✅ 18 Million money movements handled efficiently
- ✅ Completion time: 11 hours → 3.5 hours (68% improvement)
- ✅ CPU: 40% → 15% (62% reduction)
- ✅ NTB + ETB customer flows both supported with configurable fee %
- ✅ Invoice, notification, repayment schedule — all auto-generated per disbursement

---

### 💬 Likely Follow-Up Questions

**Q: Why 2 money movements instead of splitting inside 1?**
> "Because $97 goes to the User's CASA account and $3 goes to the Bank's Revenue GL — these are two different ledger accounts in the core banking system. One atomic movement can only target a single destination."

**Q: How did you handle consistency if movement 2 fails after movement 1 succeeds?**
> "We used idempotency keys + rollback at the Ledger SVC layer. If movement 2 fails, movement 1 is reversed via a compensating transaction. The whole flow is logged for audit."

**Q: Why CRON and not event-driven/Kafka?**
> "CRON was chosen because these jobs — invoice generation, repayment schedule, interest calc — don't need real-time execution. Batch gives us controlled execution windows and simpler failure recovery."

---
---

## 📌 PROJECT 2 — Portfolio Management (Loan Parameter Optimization)

---

### ⭐ S — Situation

Bank had millions of **Line of Credit (LOC)** customers. Each loan had multiple parameters:

| Parameter | Example |
|---|---|
| Offered Limit | ₹10,00,000 |
| Interest Rate | 13% |
| Tenor | 24 months |
| Expiry Date | 12 months from issue |

**Problem:** High-quality customers (zero defaults, 700+ score, consistent repayment) were stuck on **default/conservative parameters**. They were not getting proactively upgraded. This caused:
- Customer churn (they switched to other banks offering better deals)
- Missed revenue from customers who could afford higher utilization

---

### ⭐ T — Task

1. **Identify high-value customers** using credit + behavioral signals
2. **Propose upgraded loan parameters** for each eligible customer
3. Build an **automated batch pipeline** from S3 → scoring engine → LMS update
4. Maintain full **audit trail** for all parameter changes (regulatory requirement)

---

### ⭐ A — Action

#### 🏗️ System Architecture — Pipeline Flow

```
┌─────────────┐     ┌────────────┐
│  Ops Team   │────▶│     S3     │  (Campaign config, eligibility rules)
└─────────────┘     └─────┬──────┘
                          │
                          ▼
                   ┌────────────┐
                   │  Fico/CDE  │  (Credit Decision Engine — Scores customers)
                   └─────┬──────┘
                          │
              ┌───────────▼──────────┐
              │       Batchify        │  (Orchestrates batch jobs, handles chunking)
              └──┬──────────────────┬─┘
                 │ ◀─────────────── │
                 ▼                  ▼
          ┌──────────┐       ┌──────────┐
          │  Asset-  │ ◀───▶ │  LMS SVC │  (Loan Management Service)
          │ Offline  │       └────┬─────┘
          └──────────┘            │
                     ┌────────────┼──────────────┐
                     ▼            ▼               ▼
               ┌──────────┐ ┌──────────┐  ┌──────────────┐
               │  Expiry  │ │  Tenor / │  │   Offered    │
               │   Date   │ │ Interest │  │    Limit     │
               └────┬─────┘ └────┬─────┘  └──────┬───────┘
                    └────────────┴────────────────┘
                                 ▼
                          ┌─────────────┐
                          │  Master DB  │  (Audit Trail — All changes logged)
                          └─────────────┘
```

---

#### 🔑 Component Roles

| Component | Role |
|---|---|
| **S3** | Stores historical transactions, repayment history, campaign config from Ops |
| **Fico/CDE** | Credit Decision Engine — scores customers, outputs eligible candidate list |
| **Batchify** | Reads Fico/CDE output, chunks into manageable batches, orchestrates pipeline |
| **Asset-Offline** | Validates parameter changes against business + risk rules before applying |
| **LMS SVC** | Core service — receives validated changes and routes to parameter services |
| **Expiry Date** | Handles loan expiry extension logic |
| **Tenor/Interest** | Handles tenor duration + interest rate adjustments |
| **Offered Limit** | Handles credit limit upgrades/downgrades |
| **Master DB** | Full audit log — who changed what, when, for which customer |

---

#### 📊 Concrete Example — Parameter Upgrade

```
Customer Profile:
  Credit Score        : 785
  Repayment History   : 28 months, 0 defaults
  Utilization Rate    : 65% (healthy — not under, not maxed)
  Tenure with Bank    : 3+ years

Before (Default Parameters):
  Offered Limit  : ₹10,00,000
  Interest Rate  : 13.0%
  Tenor          : 24 months
  Expiry Date    : 6 months remaining

After (Upgraded by Portfolio Mgmt):
  Offered Limit  : ₹20,00,000  (+100%)
  Interest Rate  : 11.5%        (-1.5%)
  Tenor          : 36 months    (+12 months)
  Expiry Date    : Extended by 12 months
```

---

#### 🔄 Why Bidirectional Arrows (Batchify ↔ Asset-Offline ↔ LMS SVC)?

This is a **validation handshake**, not a simple push:

1. Batchify sends parameter change to Asset-Offline
2. Asset-Offline validates (risk checks, rule engine)
3. **If rejected** → Asset-Offline sends rejection back to Batchify → logged for Ops review
4. **If approved** → Asset-Offline forwards to LMS SVC
5. LMS SVC applies change → writes to Master DB → sends ACK back

This ensures **no bad parameter change reaches production** without validation.

---

### ⭐ R — Result

- ✅ Proactive offer delivery — high-value customers auto-upgraded, zero manual ops
- ✅ Customers received up to **2× credit limit** without re-applying
- ✅ Reduced churn — customers with better offers stayed with the bank
- ✅ Full audit trail maintained for regulatory compliance
- ✅ Ops team can change eligibility rules via S3 config — **no code deployment needed**
- ✅ Bidirectional validation ensures bad changes never reach LMS SVC

---

### 💬 Likely Follow-Up Questions

**Q: How do you pick which customers to target?**
> "Combination of FICO score, repayment history from CDE, utilization rate, and tenure with bank. Ops configures the thresholds in S3 — they can run new campaigns without engineer involvement."

**Q: What if LMS SVC fails mid-update — e.g., Offered Limit updated but Tenor fails?**
> "Each parameter service writes with idempotency keys. A failed update leaves a partial state in the audit log — the batch can be re-triggered and it will skip already-completed records using the idempotency key."

**Q: Why batch and not real-time?**
> "Portfolio upgrades are not time-critical to the millisecond — there's no customer waiting for this. Batch gives us cheaper compute, controlled rollout, and easy rollback if a campaign has bad parameters."

---
---

## 🗣️ Opening Lines (Speak These in Interview)

### Project 1:
> *"Maine ek disbursement fee deduction feature implement kiya. Challenge ye tha ki $100 ki jagah $97 user ke CASA mein aur $3 bank ke Revenue GL mein jaana chahiye. Isse ek transaction mein do money movements ho gayi — 9 million transactions ke liye 18 million movements. Maine load testing ki, bottleneck pakdi, aur vertical + horizontal scaling se completion time 11 hours se 3.5 hours tak laaya — 68% improvement."*

### Project 2:
> *"Maine ek Portfolio Management pipeline build ki jisme hum existing customers ko automatically identify karte the jo better loan terms deserve karte hain. S3 se data uthata, Fico/CDE se credit score karta, aur LMS service ke through parameters update karta — fully automated. Ek customer ka credit limit 10 lakh se 20 lakh automatically ho sakta tha bina re-apply kiye."*

---

## 📋 Quick Revision Card

```
PROJECT 1 — Platform Fee
├── Problem  : No fee deduction at disbursement
├── Change   : $100 split → $97 (CASA) + $3 (Revenue GL)
├── Impact   : 9M txns → 18M money movements
├── CRONs    : Fee, Invoice PDF, Notification, Interest, Repayment Schedule
├── Test     : 40K dummy users → 40% CPU, 11 hrs (bad)
├── Fix      : CPU 500m→2000m + Replicas 10-15→15-20
└── Result   : 15% CPU, 3.5 hrs (68% faster) ✅

PROJECT 2 — Portfolio Management
├── Problem  : Good customers stuck on default/bad loan params
├── Pipeline : S3 → Fico/CDE → Batchify → Asset-Offline → LMS SVC
├── Params   : Offered Limit | Interest Rate | Tenor | Expiry Date
├── Example  : 10L → 20L limit, 13% → 11.5% rate, auto-approved
└── Result   : Auto offers, 0 manual ops, full audit, no-code config ✅
```