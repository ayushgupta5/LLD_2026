# PhonePe Machine Coding — Journey Framework (Hinglish Notes)

## 1. Project Kya Karta Hai
Ye ek **User Journey / Funnel Engine** hai (PhonePe-style growth campaigns ke liye). System users ko configurable **stages** me aage badhata hai jab unke events (payload) defined **conditions** match karte hain. Har stage transition pe **SMS notification** ja sakti hai. Journey ka apna **lifecycle** (CREATED → ACTIVE → PAUSED/EXPIRED) hota hai aur journey **TIME_BOUND** ya **PERPETUAL** ho sakti hai.

Example: KYC complete karo → KYC_DONE stage. Phir ₹500–₹10000 ka transaction karo → COMPLETED stage (SMS bhi jayega).

---

## 2. Problem Statement
Ek generic framework banao jisme:
- Multiple **journeys** define ho saken with stages + transitions.
- Har **stage** me conditions hon jo decide karen ki user next stage pe jaye ya nahi.
- Stage 3 type ke hon: `ONBOARDING`, `INTERMEDIATE`, `TERMINAL`.
- Journey states manage hon valid transitions ke saath.
- Time-bound journeys auto-expire ho jayen.
- Stage change pe **observers** (e.g. SMS) notify ho.
- Error handling proper error codes ke saath ho.

---

## 3. Architecture & Flow

```
        ┌──────────────┐  evaluate(Payload)   ┌──────────────────┐
 Client │  Driver/Main │ ───────────────────▶ │  JourneyService  │
        └──────────────┘                       └────────┬─────────┘
                                                        │
                            ┌───────────────────────────┼───────────────────────────┐
                            ▼                           ▼                           ▼
                    Loop over Journeys          UserProgress map           Observers (SMS)
                            │                           │                           │
                            ▼                           ▼                           ▼
                  Stage.transitions[]         currentStage update           onStageChange()
                  Condition.evaluate()
```

**Flow:**
1. Client `Stage` + `Transition` + `Condition` graph banata hai.
2. `Journey` create hoti hai aur `JourneyService.createJourney()` me register hoti hai.
3. State `CREATED → ACTIVE` set hoti hai.
4. Event aane par `evaluate(Payload)` call hota hai.
5. Service har active journey ke liye check karti hai:
   - User onboarded nahi → `tryOnboard()`
   - User onboarded hai aur stage TERMINAL nahi → `tryTransition()`
6. Stage change pe `notifyObservers()` SMS bhejta hai.

---

## 4. Important Folders / Files

| Path | Role |
|------|------|
| `Phone_Pay_Machine_Coding/PhonePayMachineCoding.java` | Pura LLD single file me — enums, models, conditions, service, observer, demo `main`. |
| `*.class` | Compiled outputs (har inner/top-level class ka separate `.class`). |

> Sab kuch single file me hai (machine coding round style). Production me alag packages: `model/`, `condition/`, `observer/`, `service/`, `exception/`.

---

## 5. Important Classes & Functions

### Enums
- `JourneyState` — `CREATED, ACTIVE, PAUSED, EXPIRED`
- `StageType` — `ONBOARDING, INTERMEDIATE, TERMINAL`
- `JourneyType` — `TIME_BOUND, PERPETUAL`

### Models
- **`Payload(userId, params)`** — incoming event with key-value attributes.
- **`Stage(name, type)`** — node in journey graph; holds `transitions` list + `smsEnabled` flag.
- **`Transition(condition, nextStage)`** — edge; agar condition true → `nextStage` pe jao.
- **`Journey(id, startStage, type, startDate, endDate)`** — pura graph + lifecycle.
  - `canTransitionTo(newState)` → valid state machine check (CREATED→ACTIVE, ACTIVE↔PAUSED, →EXPIRED).
  - `isExpired()` → TIME_BOUND journey ka endDate cross hua kya.
- **`UserProgress(userId, journeyId, currentStage)`** — per-user pointer in journey.

### Conditions (Strategy)
- **`Condition.evaluate(params)`** — interface.
- **`EqualsCondition`** — `params[key] == expected`.
- **`RangeCondition`** — `min <= params[key] <= max`.

### Observer
- **`JourneyObserver.onStageChange(userId, stage)`** — interface.
- **`SMSObserver`** — agar `stage.smsEnabled` true → SMS print.

### Exception
- **`JourneyException(errorCode, message)`** — custom unchecked exception with codes like `INVALID_INPUT`, `JOURNEY_EXISTS`, `JOURNEY_NOT_FOUND`, `INVALID_TRANSITION`, `INVALID_PAYLOAD`, `USER_NOT_ONBOARDED`.

### Core Service — `JourneyService`
| Method | Kaam |
|--------|------|
| `subscribe(observer)` | Observer register karta hai. |
| `createJourney(j)` | Null + duplicate id validate karke store karta hai. |
| `getJourney(id)` | Lookup with `JOURNEY_NOT_FOUND`. |
| `updateState(id, newState)` | State machine validate karke transition. |
| `evaluate(payload)` | Sabhi ACTIVE journeys ke liye onboard/transition try karta hai; expired auto-mark. |
| `tryOnboard(...)` | startStage ki transitions check karke user ko first real stage pe daalta hai. |
| `tryTransition(...)` | Current stage ki transitions check karke aage badhata hai. |
| `getCurrentStage(userId, journeyId)` | User ka current stage. |
| `isOnboarded(userId, journeyId)` | Onboarded check. |
| `notifyObservers(...)` | Sab observers ko stage change broadcast. |

---

## 6. Design Patterns Used

| Pattern | Kaha | Kyun |
|---------|------|------|
| **Strategy** | `Condition` + `EqualsCondition`, `RangeCondition` | Pluggable rule evaluation. |
| **Observer** | `JourneyObserver`, `SMSObserver`, `JourneyService.subscribe/notifyObservers` | Stage change pe loose-coupled side-effects (SMS, analytics). |
| **State** | `Journey.canTransitionTo()` + `JourneyState` | Lifecycle ko enforce karta hai. |
| **Composite (graph)** | `Stage` → `Transition` → `Stage` | Journey ek directed graph. |
| **Facade** (light) | `JourneyService` | Client ke liye single entry point. |
| **Factory-friendly** | `Condition` interface | Naye conditions easily add. |

> Easy extensions: Composite Condition (AND/OR), `EmailObserver`, `AnalyticsObserver`.

---

## 7. Database & API Flow (Conceptual)

Code in-memory hai (`HashMap`s), but production mapping:

**Tables**
- `journeys(id, type, state, start_date, end_date, start_stage_id)`
- `stages(id, journey_id, name, type, sms_enabled)`
- `transitions(id, from_stage_id, to_stage_id, condition_json)`
- `user_progress(user_id, journey_id, current_stage_id)` — PK `(user_id, journey_id)`

**REST APIs**
| Method | Endpoint | Maps to |
|--------|----------|---------|
| `POST` | `/journeys` | `createJourney()` |
| `PATCH` | `/journeys/{id}/state` | `updateState()` |
| `POST` | `/events` | `evaluate(Payload)` |
| `GET` | `/users/{uid}/journeys/{jid}/stage` | `getCurrentStage()` |

**Event flow:** Client → API Gateway → `JourneyService.evaluate()` → DB update `user_progress` → Kafka publish → SMS/Email consumers (replaces `SMSObserver`).

---

## 8. Security & Performance

**Security**
- Input validation (`INVALID_PAYLOAD`, `INVALID_INPUT` already present).
- AuthN/AuthZ on APIs (only owner can read user progress).
- PII (userId, phone) ko mask/encrypt karein logs me.
- SMS rate-limit + opt-out check.

**Performance**
- `evaluate()` abhi **O(journeys × transitions)** har event pe — bada scale pe heavy.
  - Optimization: index journeys by relevant payload keys.
  - Async processing via Kafka, idempotent consumers.
- `userProgress` `HashMap` thread-safe nahi → `ConcurrentHashMap` use karein.
- TERMINAL users ke liye early-skip already implemented.
- Journey expiry ko background scheduler se mark karein, hot path se hatao.
- Caching: stage graph immutable → safely cache in memory.

---

## 9. Possible Interview Questions + Follow-ups

1. **Q:** `Condition` ko interface kyun banaya? **Follow-up:** AND/OR composite kaise add karoge? → `CompositeCondition implements Condition`.
2. **Q:** Concurrency kaise handle karoge agar same user ke 2 events parallel aayen? **F/U:** Per-user lock / `ConcurrentHashMap.compute()` / optimistic versioning on `user_progress`.
3. **Q:** Observer synchronous hai — production me kaise scale karoge? **F/U:** Async queue / event bus, retry, DLQ.
4. **Q:** Journey state machine kaise enforce kiya? **F/U:** `canTransitionTo` ko table-driven banao, ya State pattern with classes.
5. **Q:** Time-bound expiry abhi lazy hai (`evaluate` me check). Issue? **F/U:** Cron job + index on `end_date`.
6. **Q:** Naya stage type add karna ho (e.g. `BRANCHING`)? **F/U:** Open/Closed: subclass `Stage` ya strategy on transitions.
7. **Q:** Cycle aa gaya transitions me? **F/U:** Validate DAG on `createJourney`, ya max-hops per evaluate.
8. **Q:** Multi-tenant support? **F/U:** `tenantId` har entity me, partition DB.
9. **Q:** `evaluate()` har journey loop karta hai — optimize? **F/U:** Reverse index `payloadKey → journeys`.
10. **Q:** Testing strategy? **F/U:** Unit test conditions, state machine; integration test full journey traversal.
11. **Q:** Error codes string-based hain — better? **F/U:** Enum `ErrorCode`.
12. **Q:** SMS duplicate kaise rokoge agar same event 2x aaye? **F/U:** Idempotency key (userId+stage), dedupe store.

---

## 10. 2-Minute Pitch (Interview Explain)

> "Maine ek **generic User Journey framework** banaya hai jo PhonePe jaisi growth campaigns power kar sake. Core idea ye hai ki har journey ek **directed graph of Stages** hai, jisme edges `Transition` hain aur har transition ek pluggable **`Condition`** (Strategy pattern) pe trigger hoti hai.
>
> Client `JourneyService` ko event bhejta hai as `Payload(userId, params)`. Service har active journey ke liye check karti hai — agar user onboarded nahi to startStage se onboard karta hai, warna current stage ki transitions evaluate karta hai. Stage change hone par **Observer pattern** ke through `SMSObserver` notify ho jata hai — yani notification logic loosely coupled hai.
>
> Journey ka apna **lifecycle** hai — `CREATED → ACTIVE → PAUSED/EXPIRED` — jo `canTransitionTo()` me State pattern jaisa enforce hota hai. TIME_BOUND journeys auto-expire hoti hain `endDate` ke baad. Errors ke liye custom `JourneyException` with error codes hai.
>
> Patterns used: **Strategy** (Condition), **Observer** (notifications), **State** (lifecycle), **Facade** (`JourneyService`). Production me mai isko Kafka-based async, `ConcurrentHashMap`/DB-backed progress, aur reverse-indexed evaluate ke saath scale karunga."

---
