# Walmart Global Tech — Interview Preparation Guide
**Resume + Interview Experience Se Tayyar Kiye Gaye Questions & Answers (Hinglish)**
**Candidate:** Ayush Kumar Gupta | **Role:** Software Engineer | **Company:** Grab → Walmart

---

## ROUND 1 — Hiring Manager Round (Technical + Project Deep Dive)

Yeh round approx 60 min ka tha. Pehle 30 min project discussion, last 30 min mein Low-Level Design (Chess Game). Positive feedback mila.

---

### 1A. Project Deep Dive — Grab Experience Se Questions

**Q: Aapne revenue-generation platform banaya jo 5000+ TPS handle karta hai. Isme microservices architecture kyun choose kiya, monolith kyun nahi?**

A: Monolith mein ek service ka failure poora system down kar sakta hai. Hamare platform pe 15+ external APIs integrate thi aur har service ka load alag-alag tha. Microservices se hum independently scale kar sakte the — jaise fee calculation service ko horizontally scale kiya bina notification service ko touch kiye. Aur 99.9% uptime achieve karna tha, toh fault isolation critical tha.

---

**Q: Redis mein 800K+ ops/sec achieve kiya. Yeh kaise possible hua? Kya challenges aaye?**

A: Redis single-threaded hone ke bawajood in-memory operations extremely fast hain. Humne pipeline batching use ki taaki multiple commands ek roundtrip mein execute hon. Consistent hashing se data evenly distribute kiya. Challenge yeh tha ki Redis cluster mein cross-slot operations nahi hoti thi, toh sharding logic carefully design karte the. TTL tuning bhi important tha — stale data avoid karna tha par cache miss bhi nahi chahiye tha.

---

**Q: Kafka mein 10M+ events/day process ho rahe hain. Consumer lag kaise handle kiya?**

A: Pehle Prometheus + Grafana se consumer lag monitor karna set up kiya. Jab lag badhta tha, EKS pe horizontal autoscaling trigger hota tha — naye consumer pods add ho jaate the. Partition count bhi optimize ki thi taaki parallelism increase ho. Ek issue aaya tha ki ek consumer group mein ek pod slow tha — distributed tracing se identify kiya aur unhealthy pod replace kiya. MTTR 2 hours ke andar raha.

---

**Q: MySQL sharding kiya — sharding strategy kya thi? Resharding kaise handle kiya?**

A: Range-based sharding avoid ki kyunki hotspots create hote hain. Hash-based sharding use ki user_id par. Resharding ke liye consistent hashing use ki — nayi shard add karne pe sirf 1/n data move karna padta hai, poora data nahi. Double-write strategy use ki migration ke dauran: naye aur purane dono shards pe likhte the, phir verification ke baad old shard off kiya.

---

**Q: SQS retry logic mein circuit breaker pattern kaise implement kiya?**

A: Circuit breaker ke teen states the: Closed (normal flow), Open (failures threshold cross hone pe — directly fail karo, queue mat karo), Half-Open (thodi requests allow karo test ke liye). Failure rate track karne ke liye sliding window counter use kiya. Jab downstream service down hoti thi, circuit open ho jaata tha aur SQS mein messages accumulate hone se bachte the. Isse 95% delivery reliability achieve hui.

---

**Q: API optimization mein 500 RPS se 5000+ RPS tak kaise gaye?**

A: Pehle profiling ki — bottleneck tha N+1 query problem aur offset-based pagination. Cursor-based pagination implement ki jo last seen ID use karti thi — database ko skip karna nahi padta tha. Covering indexes add kiye frequently queried columns par. Batch processing ko parallel goroutines mein split kiya Golang mein — 15 min se 6 min ho gaya. Connection pooling bhi tune ki.

---

**Q: P0 incident mein 2hr MTTR kaise maintain kiya? Ek real example batao.**

A: Ek baar production mein fee calculation wrong ho rahi thi kuch users ke liye. Distributed tracing (Jaeger) se dekha ki ek specific microservice mein config mismatch tha after a recent deploy. Rollback immediately kiya Jenkins se — yeh 15 min mein ho gaya. Phir hotfix prepare ki, staging pe test ki, aur peak traffic ke baad deploy ki. Postmortem likha aur automated config validation add ki pipeline mein.

---

### 1B. Low-Level Design — Chess Game

**Q: Chess game ka class diagram kaise design karoge?**

A: Main classes: `Game` (orchestrator), `Board` (8x8 grid), `Square` (position), `Piece` (abstract base — King, Queen, Rook, Bishop, Knight, Pawn as subclasses), `Player`, `Move`, `MoveValidator`, `GameState`. Piece class mein abstract method `getValidMoves(Board)` hoga jo har piece apne rules ke according override karega.

---

**Q: API call aaye 'move Rook from A1 to A5' — server side validation kahan hogi?**

A: Request pehle `MoveController` ko jaayegi jo parse karega. Phir `MoveValidator.validate(move, board, player)` call hoga. Rook ke case mein `RookValidator` check karega: same row ya column hai, path mein koi piece toh nahi, destination pe apni piece toh nahi. Agar valid hai toh `Board.applyMove()` call hoga aur new board state return hoga. Check/checkmate detection bhi yahi hoga.

---

**Q: Valid moves kaise show karoge jab user koi piece select kare?**

A: Client se request aayegi: `getValidMoves(pieceId, boardState)`. Server `Piece.getValidMoves(board)` call karega. Ye ek list of valid Square positions return karega. Frontend pe yeh squares highlight ho jaayenge. Caching bhi kar sakte hain current board state ke liye — agar board nahi badla toh recalculate mat karo.

---

## ROUND 2 — Technical Round (Project + System Design)

Pehle 30 min project deep dive, last 30 min mein System Design: "Past Purchase Page for E-commerce". Time thoda kam tha — frustrating tha, par confident rahna zaroori hai.

---

### 2A. System Design — Past Purchase Page

**Q: Past Purchase Page design karo — features: Order list (OrderId, Amount, Items), Order detail on click, Search by product name, Filter by time.**

A: Feature 5 jo yaad nahi aaya woh probably tha: Pagination/Infinite Scroll ya Order Status (delivered/returned/cancelled).

**Design approach:**
- **Frontend components:** OrderList, OrderDetailModal, SearchBar, DateRangePicker
- **API:** `GET /orders?userId=&search=&from=&to=&page=&limit=`
- **Database:**
  - `Orders` table: orderId, userId, amount, createdAt
  - `OrderItems` table: itemId, orderId, itemName, price
  - Index on (userId, createdAt) for time filter
- **Search:** ElasticSearch on itemName
- **Cache:** Redis for recent orders per user

---

**Q: Search feature — 'apple' type karo toh OrderIds milein. Kaise implement karoge?**

A: ElasticSearch index maintain karo OrderItems par. Jab order create ho, async Kafka event se ES mein index karo. Search query: ES se itemName match karo, OrderIds return karo, phir MySQL se order details fetch karo. Yeh full-text search support karega ('apple', 'Apple iPhone', sab match hoga). Alternatively MySQL FULLTEXT index bhi chhote scale ke liye kaam karega.

---

**Q: Price distribution kaise dikhaaoge jab OrderId click karein?**

A: `GET /orders/{orderId}/items` API banao. Response mein har item ka name aur price aayega. Frontend pe pie chart ya bar chart render karo (item-wise breakdown). Percentage bhi calculate karo: `item_price / order_total * 100`. Yeh data cached ho sakta hai kyunki past orders change nahi hote.

---

**Q: Time filter kaise efficient banaaoge large scale pe?**

A: Composite index on `(userId, createdAt)` — range queries fast banata hai. Partition by date bhi kar sakte hain MySQL mein large datasets ke liye. Cache layer: Redis mein user ke last 3 months ke orders cache karo — majority of access yahi hogi. Cache invalidation: naya order aane par user ki cache invalidate karo.

---

### 2B. Technical Concept Questions

**Q: Caching strategies kya kyon alag hoti hain? LRU, LFU, FIFO explain karo.**

A:
- **LRU (Least Recently Used):** Recently use ki gayi item evict nahi hoti — good for temporal locality
- **LFU (Least Frequently Used):** Kam use hone wali items evict hoti hain — good for repeated access patterns
- **FIFO:** Pehle aaya pehle gaya — simple par not smart

Hamare Grab system mein LRU use karte the Redis pe kyunki user sessions temporally localized hote hain.

---

**Q: TTL cache mein kaise implement karoge?**

A: Har cache entry ke saath expiry timestamp store karo. Access ke waqt check karo: `if (now > entry.expiry)` then evict and return miss. Background thread ya lazy eviction dono approaches kaam karti hain. Redis mein natively supported hai: `SET key value EX 3600`. Custom implementation mein min-heap use karo expiry times ke liye — efficient eviction.

---

**Q: Kafka vs SQS — kab kya use karoge?**

A:
- **Kafka:** High throughput (10M+ events/day), message replay chahiye, multiple consumers same message process karein, ordering matters
- **SQS:** Simpler setup, at-least-once delivery, retry with DLQ out of the box, AWS managed

Humne Grab mein dono use kiye — Kafka portfolio events ke liye (replay critical tha), SQS notifications ke liye (simplicity + DLQ).

---

**Q: CAP theorem aur distributed cache pe kaise apply hota hai?**

A: CAP: Consistency, Availability, Partition Tolerance — teen mein se do hi guarantee ho sakti hain. Redis Cluster CP hai — partition pe kuch nodes unavailable ho jaate hain consistency ke liye. Eventual consistency chahiye thi humhe caching mein, toh hum slightly stale data accept karte the (high availability). Consensus algorithm (Raft) use kiya leader election ke liye distributed cache mein.

---

## ROUND 3 — Behavioral + Technical Blend Round

Yeh round mein previous work experience + behavioral + technical concepts (Kafka, Caching, API Design) dono the. 60 min ka blend round tha.

---

### 3A. Behavioral Questions

**Q: Cross-team conflict kaise handle kiya? Ek example do.**

A: Grab mein ek baar fee calculation team aur loan origination team ke beech conflict tha — dono apna API contract change karna chahte the aur koi ready nahi tha accommodate karne ko. Maine beech mein mediate kiya: dono teams ke pain points document kiye, common ground dhundha (versioned API — v1 aur v2 parallel chalao), aur timeline agree karaya. Result: zero downtime migration, dono teams happy.

---

**Q: Deadline tight thi aur kaam zyada tha — kya kiya?**

A: HashedIn mein ek Fortune 500 client ke liye 15 APIs ki deadline 2 weeks thi. Maine pehle prioritization ki — kaunsi APIs client ke critical path pe hain. Phir parallel development set up kiya — 3 engineers, 5-5 APIs each. Daily sync raka taaki blockers turant resolve hon. Kuch non-critical features deferred kiye stakeholder ke agreement se. Deadline meet ki with all critical APIs.

---

**Q: Kya aapne kisi junior ko mentor kiya hai?**

A: Haan, Grab mein ek junior engineer ko onboard kiya tha. Pehle hafte mein codebase walkthrough, architecture overview, aur small tasks assign kiye. Weekly 1:1 raka. Jab unhe Kafka consumer lag issue solve karna tha, maine pair programming kiya — sirf answer nahi diya, thought process dikhaya. 3 months mein woh independently features deliver karne lage.

---

**Q: Agile practices kaise follow karte ho apne current org mein?**

A: Grab mein 2-week sprints follow karte hain. Sprint planning mein user stories estimate karte hain story points mein. Daily standups mein blockers share karte hain. Retrospectives mein honestly discuss karte hain kya kaam kiya, kya nahi. Maine specifically sprint review improve kiya tha — demo-first approach adopt ki jisme actual working feature dikhate the, slides nahi.

---

### 3B. Technical Concept Questions (Blend Round)

**Q: Messaging queue ka high-level diagram explain karo.**

A: Producer messages publish karta hai Queue mein. Queue durably store karti hai (disk pe). Consumer messages poll ya subscribe karke consume karta hai. Acknowledgment: Consumer process karne ke baad ACK bhejta hai — tab message delete hota hai. DLQ (Dead Letter Queue): Agar repeatedly fail ho toh message DLQ mein jaata hai manual inspection ke liye. Yeh pattern humne notification system mein use kiya tha.

---

**Q: API design mein versioning kaise karte ho?**

A: URL versioning: `/v1/orders`, `/v2/orders` — simple aur explicit. Header versioning: `Accept: application/vnd.api+json;version=2`. Humne URL versioning prefer ki kyunki debugging aur caching easy hoti hai. Breaking changes pe naya version, backward compatible changes existing version mein. Old versions deprecate karne ka process: 6 month notice + sunset header.

---

**Q: Production mein bug aaya — kaise handle karte ho?**

A: Pehle severity assess karo — P0/P1/P2. P0 ke liye immediately incident channel create karo, stakeholders notify karo. Rollback option evaluate karo — agar quick fix nahi hai toh rollback. Distributed tracing se root cause identify karo. Fix karo, staging pe test karo. Phir production deploy karo with monitoring. Postmortem likho — blameless, focus on process improvement.

---

## ROUND 4 — HR Round (Short: 10-15 min)

Yeh round very conversational tha. Recruiter ya HR conduct karta hai. Answers confident aur genuine hone chahiye.

---

**Q: Apne aap ke baare mein batao (Introduce yourself).**

A: Main Ayush Kumar Gupta hoon, currently Grab mein Software Engineer hoon — 3+ saal se. Maine revenue-generation platforms, event-driven systems aur high-throughput APIs build ki hain jo lakhs of users use karte hain daily. Mujhe distributed systems aur scalability problems genuinely interesting lagte hain. Isse pehle HashedIn by Deloitte mein Fortune 500 clients ke liye enterprise APIs banai thi. LeetCode pe 2200+ problems solve kiye hain — problem solving meri core strength hai.

---

**Q: Walmart join kyun karna chahte ho?**

A: Walmart ka scale genuinely intimidating hai — 230M+ customers weekly, massive supply chain, real-world complexity. Engineering problems yahan truly hard hain. Grab pe high-scale systems build kiye, par Walmart pe global impact hoga. Aur Walmart Global Tech India ka work culture aur growth trajectory mujhe genuinely attract karta hai.

---

**Q: Higher studies pursue kyun nahi kiye?**

A: Mera belief hai ki real-world engineering experience often better teacher hota hai. Grab pe jo distributed systems problems solve kiye hain — woh classroom mein nahi milti. Aur main continuously learning kar raha hoon — system design, new tech, competitive programming. Agar future mein higher studies make sense karein toh consider karunga, par abhi industry mein contribute karna zyada meaningful lagta hai.

---

**Q: Job aur career mein kya difference hai aur tumhe kya chahiye?**

A: Job sirf paycheck ke liye hoti hai — uthao, karo, ghar jao. Career mein growth hoti hai, ownership hoti hai, impact hota hai. Main career chahta hoon. Grab mein maine sirf feature nahi banaya — architecture design kiya, incidents own kiye, junior mentor kiye. Walmart mein bhi yahi chahta hoon — ek aisa place jahan main engineer se tech lead tak grow kar sakoon.

---

**Q: 5 saal mein khud ko kahan dekhte ho?**

A: 5 saal mein main ek strong Senior/Staff Engineer banna chahta hoon jo end-to-end system design kar sake, cross-functional teams lead kar sake aur technically young engineers ko grow karne mein help kare. Walmart ka scale aur complexity mujhe yeh journey fast karne ka mauka dega.

---

**Q: Resume mein jo project mention kiya hai — brief batao.**

A: Grab pe maine Intelligent Portfolio Management Platform banaya. Yeh ek event-driven system hai jo 10M+ Kafka events process karta hai daily. Real-time risk scoring hai under 100ms mein. AWS Lambda se automated credit adjustments hoti hain aur audit trails partitioned MySQL mein store hote hain. Iska business impact yeh tha ki manual interventions 60% reduce hue — 200+ hours/month bachaye.

---

## ROUND 5 — Product Manager Round (45 min, Zoom)

Yeh round ek senior PM ne liya. Conversational tha — apni personality aur problem-solving dikhane ka mauka tha.

---

**Q: Apne aap ko introduce karo (PM round style — hobbies bhi batao).**

A: Main Ayush hoon, Software Engineer at Grab. Technically mujhe distributed systems aur event-driven architecture pasand hai — genuinely enjoy karta hoon scalability puzzles. Personal life mein cooking mera passion hai — naye recipes experiment karna aur competitive programming. Dono mein ek common theme hai — systematic problem solving!

---

**Q: Situational: Team ka ek feature delay ho raha hai aur stakeholder unhappy hai — kya karoge?**

A: Pehle internally assess karo: delay ka root cause kya hai, kitna aur time lagega, kya scope reduce ho sakta hai. Phir stakeholder ke saath transparent conversation: current status, revised timeline, aur agar possible ho toh partial delivery option. Blame mat karo — solution focus raho. Hamare Grab pe ek baar aisa hua tha, humne MVP first ship kiya aur remaining features next sprint mein — stakeholder happy tha partial value milne se.

---

**Q: PM ne pucha: Competitive programming kya hota hai?**

A: Competitive programming mein time-limited contests hote hain jisme algorithmic problems solve karne hote hain. LeetCode, CodeChef, Codeforces popular platforms hain. Mera CodeChef rating 4-star hai, global rank 511 rha hai. Yeh skill real engineering mein bhi help karti hai — edge cases sochna, optimal solutions dhundhna, pressure mein perform karna.

---

**Q: Walmart mein growth opportunities ke baare mein kya poochha?**

A: Main pooch sakta hoon: *"As a Software Engineer at Walmart Global Tech, what does the typical growth path look like — from SDE2 to Senior to Staff? Are there opportunities to work on cross-functional or global projects? How does the team support engineers who want to move into technical leadership roles?"*

---

## ROUND 6 — Senior Technical Round (Caching + CAP + Distributed Systems)

Deep technical round tha — caching deep dive, pseudo-code, distributed systems concepts.

---

**Q: LRU Cache ka pseudo-code likho with get/put operations.**

A: Data structures: `HashMap (key -> Node)` + `Doubly Linked List` (for order).

```
get(key):
  if key not in map → return -1
  move node to head
  return node.value

put(key, value):
  if key in map → update value, move to head
  else:
    create new node, add to head, add to map
    if size > capacity:
      remove tail node from list and map
```

Time complexity: O(1) for both get and put.

---

**Q: LFU eviction policy implement karo.**

A: Data structures: `HashMap<key, {value, freq}>` + `HashMap<freq, LinkedHashSet<keys>>` + `minFreq` variable.

```
get(key):
  increment freq of key
  remove from freq bucket, add to freq+1 bucket
  update minFreq if old bucket empty
  return value

put(key, value):
  if full:
    evict first key from minFreq bucket
    remove from key map
  add new key with freq=1
  minFreq = 1
```

Time complexity: O(1) with this approach.

---

**Q: Distributed cache multiple pods mein kaise manage karoge?**

A: Consistent hashing use karo — har pod ek range of hash space ka responsible hai. Client library hash calculate karta hai aur correct pod ko request bhejta hai. Replication: Har key 2-3 pods pe replicate hoti hai fault tolerance ke liye. Leader election ke liye Raft consensus algorithm. Partition hone pe: CP choose karo (unavailable raho) ya AP (stale data serve karo) — use case pe depend karta hai.

---

**Q: CP subsystem kya hota hai? Example do.**

A: CP (Consistent + Partition Tolerant) systems partition hone pe availability sacrifice kar dete hain taaki data consistent rahe. Example: ZooKeeper, etcd. Hamare Grab mein configuration management ke liye etcd use karte the — wrong config se bada nuksaan ho sakta tha, toh consistency critical thi even at cost of availability.

---

## ROUND 7 — Hiring Manager Behavioral + Design Round

Technical + behavioral ka blend. High level design (messaging queues), design problems in current project, interesting problem discussions.

---

**Q: Apne current project mein kya design problems dekhte ho?**

A: Grab pe ek issue yeh hai ki kuch microservices tightly coupled hain — ek service dusri ki internal implementation assume karti hai. Isse changes ripple effect create karte hain. Better approach hogi contract-first design aur event-driven communication jisme services sirf events consume karti hain, direct API calls nahi. Aur kuch services mein shared database pattern hai jo anti-pattern hai microservices mein.

---

**Q: Haal hi mein sabse interesting problem kya solve kiya?**

A: Batch processing time 15 min se 6 min karna sabse interesting tha. Pehle profiling ki — bottleneck tha sequential processing. Golang mein goroutines use karke parallel processing implement ki with worker pool pattern. Challenge yeh tha ki database connection exhaustion avoid karna tha — connection pool size carefully tune ki. Phir idempotency ensure ki taaki retry pe duplicate processing na ho. Final result: 60% time reduction.

---

**Q: Messaging queue ka high-level diagram samjhao.**

A: `Producer → Message Broker (Queue) → Consumer`

- **Broker:** Messages durably store karta hai (disk pe), multiple queues alag-alag purposes ke liye
- **Consumer groups:** Multiple consumers same queue se parallel consume kar sakte hain
- **Dead Letter Queue:** Failed messages yahan jaate hain inspection ke liye
- **Monitoring:** Queue depth, consumer lag, throughput — sab track karo

Grab mein Kafka (high throughput, replay) aur SQS (simplicity, DLQ) dono use kiye.

---

**Q: Stakeholder management aur deadline pressure mein kaise kaam karte ho?**

A: Transparency pehle — agar kuch risk hai toh early batao, deadline ke din nahi. Expectations manage karo: kya definite ship hoga, kya might slip. Priorities stakeholder ke saath align karo — business value ke basis pe. Internally team ko protect karo unrealistic scope creep se. Hamare ek release mein maine proactively 2 weeks pehle bataya ki ek feature risky hai — alternative plan ready tha, release smooth raha.

---

## Bonus: Walmart Interview Ke Liye Key Tips

### Technical Rounds Ke Liye
- System design mein hamesha clarifying questions se shuru karo — scale, users, features
- Numbers use karo: "10M events/day", "99.9% uptime" — vague mat bolo
- Trade-offs discuss karo — har decision mein kuch gain, kuch lose hota hai
- Distributed systems: CAP theorem, consistency models, failure modes yaad rakho
- Golang/Java dono mein confident raho — Walmart Java heavy hai

### Behavioral Rounds Ke Liye
- STAR format use karo: Situation, Task, Action, Result
- Real numbers aur impact batao: "60% reduction", "200+ hours saved"
- Walmart ke values align karo: Customer obsession, collaboration, ownership
- "Job vs Career" question ka honest answer do — long-term mindset dikhao

### HR/PM Rounds Ke Liye
- Walmart ka business genuinely samjho — retail tech, supply chain, Walmart Labs
- Apni personality dikhao — hobbies, interests (PM rounds mein yeh kaam aata hai)
- Insightful questions poochho interviewer se — generic mat poochho
- Why Walmart specifically? Generic "great company" answer avoid karo

---

> **Best of luck, Ayush! 🚀 Walmart ka system build karo aur apna best do!**