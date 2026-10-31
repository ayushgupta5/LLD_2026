# 📒 Flipkart Machine Coding Round — Logging Library (Hinglish Notes)

## 1. Project kya karta hai?
Ye ek **custom Logging Library** hai (Log4j / SLF4J ka chhota version) jo:
- Multiple **named loggers** create kar sakti hai (`sync-logger`, `async-logger` etc.)
- Har logger ka apna **log level**, **type (SYNC/ASYNC)**, **buffer size**, aur **sink (output destination)** ho sakta hai
- **Thread-safe** hai — multiple threads ek hi logger pe parallel log kar sakte hain
- **Graceful shutdown** support karti hai (async queue drain karke close hota hai)

---

## 2. Problem Statement
Ek aisi logging library design karo jo:
1. **Multiple log levels** support kare — `DEBUG, INFO, WARN, ERROR, FATAL`
2. **Configurable sinks** ho (STDOUT, File, DB — extensible)
3. **Sync aur Async** dono modes me kaam kare
4. Async me **bounded buffer (queue)** ho — producer-consumer model
5. **Level filtering** — agar logger ka min level `INFO` hai to `DEBUG` drop ho jaye
6. **Multi-threaded** environment me safe rahe
7. Client ko ek **simple facade API** mile — `init()`, `log()`, `shutdownAll()`

---

## 3. Architecture & Flow

```
Client (Driver/TestCases)
        │
        ▼
┌─────────────────────┐
│   LoggerLibrary     │  ← Singleton Facade
│  (ConcurrentHashMap │
│   <name, Logger>)   │
└─────────┬───────────┘
          │ creates
          ▼
   ┌────────────┐         ┌──────────────┐
   │ SyncLogger │   OR    │ AsyncLogger  │  ← Strategy
   │ (synchron- │         │ (Blocking-   │
   │  ized log) │         │  Queue +     │
   │            │         │  worker thr) │
   └─────┬──────┘         └──────┬───────┘
         │  extends BaseLogger   │
         ▼                       ▼
        writeToSinks(msg) → level filter → format ts
                                   │
                                   ▼
                            ┌────────────┐
                            │   Sink     │  ← Interface
                            │ StdoutSink │
                            └────────────┘
```

**Flow (Async case):**
1. `Driver` → `LoggerLibrary.init(config)` → logger object banta hai aur map me store hota hai
2. `lib.log("async-logger", msg, INFO)` → facade Map se logger uthata hai
3. `AsyncLogger.log()` → `Message` ko `BlockingQueue.put()` karta hai (producer)
4. Background **worker thread** queue se `poll()` karta hai (consumer)
5. `BaseLogger.writeToSinks()` → level check → timestamp add → sink ke through write
6. `shutdownAll()` → `running=false` → worker queue drain karke exit

---

## 4. Important Folders / Files

| Path | Role |
|------|------|
| `Driver.java` | Entry point — basic usage demo |
| `TestCases.java` | Edge cases: filter, wrong name, duplicate init, multi-thread, post-shutdown |
| `facade/LoggerLibrary.java` | **Singleton + Facade** — single entry API for client |
| `logger/Logger.java` | Interface — `log()`, `shutdown()` |
| `logger/BaseLogger.java` | Abstract — common formatting + level filter logic |
| `logger/SyncLogger.java` | Synchronous logger (blocking, `synchronized`) |
| `logger/AsyncLogger.java` | Async logger using `BlockingQueue` + worker thread |
| `sink/Sink.java` | Sink interface (extensible — Stdout/File/DB) |
| `sink/StdoutSink.java` | Console sink implementation |
| `model/LogConfig.java` | Builder-based immutable config object |
| `model/LogLevel.java` | Enum with priority — `DEBUG(1)…FATAL(5)` |
| `model/Message.java` | Immutable log message DTO |

---

## 5. Important Functions — kya karte hain

### `LoggerLibrary`
- `getInstance()` → **Singleton** (synchronized lazy init)
- `init(LogConfig)` → naam check karke `Sync` ya `Async` logger banata hai aur `ConcurrentHashMap` me daalta hai
- `log(name, msg, level)` → map se logger fetch karke delegate
- `shutdownAll()` → har logger ka `shutdown()` call karke map clear

### `BaseLogger.writeToSinks(Message msg)`
- **Level filter:** `msg.level.priority < config.level.priority` → drop
- Timestamp banata hai `DateTimeFormatter` se
- Har sink pe **sink-level filter** check karke `sink.write()` call

### `SyncLogger.log()`
- `synchronized` method — turant `writeToSinks()` call (caller thread block hota hai)

### `AsyncLogger`
- Constructor me ek **daemon worker thread** start hota hai
- `log()` → `queue.put()` (queue full ho to producer block)
- Worker loop: `queue.poll(100ms)` → `writeToSinks()`
- `shutdown()` → `running=false`, worker `2s` tak join ka wait

### `StdoutSink.write()`
- Bas `System.out.println()` — but level filter pehle `BaseLogger` me ho chuka

---

## 6. Design Patterns Used

| Pattern | Kahaan | Kyun |
|---------|--------|------|
| **Singleton** | `LoggerLibrary.getInstance()` | Pure app me ek hi logger registry |
| **Facade** | `LoggerLibrary` | Client ko `init/log/shutdown` ka simple API |
| **Factory (inline)** | `LoggerLibrary.init()` | Config dekh ke `SyncLogger` / `AsyncLogger` banata hai |
| **Strategy** | `Logger` interface (Sync vs Async) | Runtime pe logging behaviour swap |
| **Template Method** | `BaseLogger.writeToSinks()` | Common format/filter logic, subclasses sirf `log()` override karte hain |
| **Builder** | `LogConfig.Builder` | Immutable config with optional defaults |
| **Producer–Consumer** | `AsyncLogger` queue + worker | Decouple producer threads from I/O |
| **Observer-ish (Sinks)** | `BaseLogger` → multiple `Sink` | Ek message multiple destinations pe ja sake |

---

## 7. "Database" & API Flow
Yahan koi **real DB nahi** hai (in-memory + console). But concept-wise:
- **In-memory store:** `ConcurrentHashMap<String, Logger>` (logger registry)
- **Buffer store:** `LinkedBlockingQueue<Message>` (async case)
- **Sink layer** = pluggable persistence — `FileSink` / `DBSink` easily add ho sakte hain by implementing `Sink`

**Public API (Facade):**
```java
LoggerLibrary.getInstance();
lib.init(LogConfig);                              // create
lib.log(name, message, LogLevel);                 // write
lib.shutdownAll();                                // close
```

---

## 8. Security & Performance Points

### Performance
- **Async logger** → caller thread bas `queue.put()` karta hai → I/O latency hide
- **`ConcurrentHashMap`** → reads lock-free, writes me fine-grained locking
- **`LinkedBlockingQueue` (bounded)** → backpressure milta hai, OOM se bachata hai
- `SyncLogger` me `synchronized` har log pe lock — high throughput pe bottleneck (improvement: per-sink lock ya lock-free)
- **Early level filter** in `BaseLogger` → unnecessary string formatting avoid

### Thread Safety
- `LoggerLibrary` Singleton init `synchronized`
- Logger map → `ConcurrentHashMap`
- `SyncLogger.log` → `synchronized`
- `AsyncLogger` → thread-safe `BlockingQueue`, `volatile boolean running`
- Worker = **daemon** → JVM exit ko block nahi karega

### Security / Robustness
- Wrong logger name → graceful "No logger found" (no NPE)
- Duplicate `init` → ignore with warning
- Post-`shutdownAll` log → map empty, gracefully reject
- `Message` & `LogConfig` **immutable** → race conditions nahi
- `InterruptedException` properly handled (`Thread.currentThread().interrupt()`)

### Possible Gaps (good to mention in interview)
- Worker thread me exception swallow ho raha — add try/catch around `writeToSinks`
- `shutdown()` me queue forcibly drop ho sakti hai agar worker 2s me na nikle — better: `queue.drainTo()` first
- No log rotation / file sink yet
- `tsFormat` rebuild har log pe — `DateTimeFormatter` ko cache karna chahiye

---

## 9. Possible Interview Questions + Follow-ups

1. **Singleton thread-safe kaise banaya?**
   - `synchronized getInstance()`. Follow-up: "Double-checked locking ya `enum` Singleton kyun better?"

2. **Sync vs Async logger ka trade-off?**
   - Sync = ordering guaranteed, simple, slow. Async = fast, non-blocking, but ordering across threads weak, crash pe in-flight logs lose.
   - Follow-up: "Async me logs lose hone se kaise bachoge?" → durable disk-backed queue / fsync.

3. **`LinkedBlockingQueue` kyun, `ArrayBlockingQueue` ya `Disruptor` kyun nahi?**
   - Bounded + thread-safe + simple. Disruptor higher throughput deta hai (lock-free ring buffer).

4. **Naye sink (File / Kafka) kaise add karoge?**
   - `Sink` interface implement karo, `LoggerLibrary.init()` me ek `else if` branch — **Open/Closed principle**.
   - Follow-up: "Branching avoid karne ke liye?" → `SinkFactory` registry / DI.

5. **Multiple threads ek async logger pe likh rahe — ordering kya hogi?**
   - Queue insertion order maintain karega, but producer interleaving non-deterministic.

6. **Log level filtering kahan-kahan ho rahi?**
   - 2 jagah: `BaseLogger` (logger level) + per `Sink` (sink level) — flexible.

7. **`shutdownAll()` ke baad log() call ho gaya to?**
   - Map clear ho chuka → "No logger found" print. Follow-up: "exception throw karna better hota?"

8. **Builder pattern kyun for `LogConfig`?**
   - Optional params, immutability, readability vs telescoping constructor.

9. **Agar `bufferSize` chhota hai aur producer fast hai?**
   - `queue.put()` block karega → backpressure. Alternative: `offer()` + drop policy.

10. **Distributed system me ye library kaise scale karoge?**
    - Async sink → Kafka → central ELK/Loki stack. Add `traceId` / MDC support.

11. **Memory leak ke chances?**
    - Map me logger reference rahega jab tak `shutdownAll()` na ho. Worker daemon hai, OK.

12. **Testing strategy?**
    - Mock `Sink`, assert `write()` called with expected formatted string; concurrent test for race.

---

## 10. 2-Min Project Pitch (Interview ke liye)

> "Maine ek **lightweight logging library** banayi hai jo Flipkart machine coding round ka problem solve karti hai — basically Log4j ka mini version.
>
> Architecture me **Facade pattern** use kiya hai — `LoggerLibrary` ek **Singleton** hai jo client ko sirf 3 methods deta hai: `init`, `log`, `shutdownAll`. Internally ye named loggers ko `ConcurrentHashMap` me store karta hai.
>
> Loggers do flavors me aate hain — **SyncLogger** (synchronized, immediate write) aur **AsyncLogger** (BlockingQueue + dedicated worker thread, classic producer-consumer). Dono ek `BaseLogger` abstract class extend karte hain jo **Template Method pattern** se common formatting aur level-filtering handle karta hai. Ye **Strategy pattern** hai — runtime pe sync/async swap ho sakta hai.
>
> Output ke liye `Sink` interface hai (abhi `StdoutSink`), jisme File/Kafka sink easily add ho sakte hain — **Open/Closed principle**. `LogConfig` ko **Builder pattern** se banaya hai for immutability and clean defaults.
>
> Thread-safety ke liye `ConcurrentHashMap`, `BlockingQueue`, `volatile` flags, aur `synchronized` use kiye hain. Multi-threaded test cases me 3 threads simultaneously async logger pe likhte hain — koi data race nahi hota.
>
> Edge cases handle kiye hain — wrong logger name, duplicate init, post-shutdown logs sab graceful reject hote hain. Async logger `shutdown` pe queue drain karta hai before exit, taaki logs lose na ho."

---
**TL;DR:** Singleton Facade + Strategy (Sync/Async) + Template Method (BaseLogger) + Builder (Config) + Producer-Consumer (Async queue) + pluggable Sinks. Thread-safe, configurable, extensible. ✅
