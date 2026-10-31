# LUA Script Usage in Dakota Lending Modules

## विषय सूची (Table of Contents)
1. [परिचय (Introduction)](#परिचय-introduction)
2. [Redis LUA Script का उपयोग (Redis LUA Script Usage)](#redis-lua-script-का-उपयोग)
3. [Race Condition से बचाव (Race Condition Prevention)](#race-condition-से-बचाव)
4. [Implementation Details](#implementation-details)
5. [Lending Modules में उपयोग (Usage in Lending Modules)](#lending-modules-में-उपयोग)
6. [Code Examples](#code-examples)
7. [Best Practices](#best-practices)

---

## परिचय (Introduction)

हाँ, आप बिल्कुल सही हैं! Dakota project में **LUA Script का उपयोग Redis में race conditions से बचने के लिए किया गया है**।

LUA Scripts Redis में **atomic operations** को execute करने के लिए उपयोग होती हैं, जिससे:
- Multiple concurrent requests के बीच race conditions नहीं होती
- Distributed locking mechanism बनाया जा सकता है
- Transaction safety ensure होती है

---

## Redis LUA Script का उपयोग

### Location: `common/redis/lock.go`

Dakota project में Redis LUA script का मुख्य उपयोग **Distributed Locking** के लिए होता है।

### LUA Script Definition

```lua
luaRefreshScript = `
	if #KEYS ~= 1 then
		return redis.error_reply("Wrong number of keys provided")
	end
	if redis.call("get", KEYS[1]) ~= ARGV[1] then
		return redis.error_reply("Lock value does not match, lock not held by this token")
	end
	redis.call("pexpire", KEYS[1], ARGV[2])
	return redis.status_reply("OK")
`
```

### Script का Purpose

यह LUA script **lock refresh** करने के लिए उपयोग होती है:

1. **Atomic Check-and-Set**: Lock की validity को verify करती है और expire time update करती है
2. **Race Condition Prevention**: पूरी operation एक atomic transaction में execute होती है
3. **Token Verification**: सुनिश्चित करती है कि सही token holder ही lock को refresh कर सकता है

---

## Race Condition से बचाव

### समस्या (Problem)

बिना LUA script के, Redis में lock refresh करने के लिए multiple commands की जरूरत होती:

```go
// ❌ Race condition possible (WITHOUT LUA)
// Step 1: GET the lock value
currentValue := redis.Get(key)
// Step 2: Check if it matches
if currentValue == myToken {
    // Step 3: Refresh expiry
    redis.Expire(key, newDuration)  // ⚠️ Another process might have changed the lock!
}
```

**Issue**: Steps 1-3 के बीच में दूसरी process lock को change कर सकती है।

### समाधान (Solution with LUA)

LUA script पूरी operation को **atomic** बनाती है:

```go
// ✅ No race condition (WITH LUA)
redis.Eval(ctx, luaRefreshScript, []string{key}, token, duration)
// All checks and updates happen atomically in Redis!
```

---

## Implementation Details

### 1. Lock Interface (`common/redis/lock.go`)

```go
type Lock interface {
    // TryLock attempts to acquire a lock (non-blocking)
    TryLock(ctx context.Context, lockTime time.Duration) error
    
    // Unlock releases the lock
    Unlock(ctx context.Context) error
    
    // Refresh extends the lock duration (uses LUA script)
    Refresh(ctx context.Context, lockTime time.Duration) error
}
```

### 2. Lock Implementation

#### TryLock - Lock Acquire करना
```go
func (l *lock) TryLock(ctx context.Context, lockTime time.Duration) error {
    // SetNX = Set if Not eXists (atomic Redis command)
    _, err := l.client.SetNX(ctx, l.key(), l.token, lockTime)
    if err != nil {
        if err == ErrAlreadyExists {
            return ErrLockOccupied  // Lock already held
        }
        return err
    }
    return nil
}
```

**क्यों Safe है?**: `SetNX` खुद एक atomic Redis command है।

#### Refresh - Lock Duration Extend करना (LUA Script यहाँ उपयोग होती है!)
```go
func (l *lock) Refresh(ctx context.Context, lockTime time.Duration) error {
    // 🔥 LUA Script execution होती है यहाँ
    _, err := l.client.Eval(ctx, luaRefreshScript, 
        []string{l.key()},           // KEYS[1]
        l.token,                      // ARGV[1]
        int64(lockTime/time.Millisecond)) // ARGV[2]
    return err
}
```

**क्यों LUA की जरूरत है?**: 
- Token verification + expiry update को एक साथ atomic transaction में करना पड़ता है
- बिना LUA के, check और update के बीच race condition possible है

#### Unlock - Lock Release करना
```go
func (l *lock) Unlock(ctx context.Context) error {
    token, err := l.client.GetString(ctx, l.key())
    if err != nil {
        return err
    }
    if token != l.token {
        // Token not matching = lock already released
        return nil
    }
    _, err = l.client.Delete(ctx, l.key())
    return err
}
```

### 3. Eval Function (`common/redis/keys_impl.go`)

```go
// Eval allows you to run lua script in redis.
func (c *client) Eval(ctx context.Context, script string, keys []string, args ...interface{}) (interface{}, error) {
    startTime := time.Now()
    
    cmd := c.redisCli.Eval(ctx, script, c.generateKeys(keys), args)
    defer c.stats.Duration(logTag, elapsedKey, startTime, c.getStatsTags(cmd)...)
    
    return cmd.Result()
}
```

---

## Lending Modules में उपयोग

### हाँ! `lending/loan-exp` और `lending/loan-core` दोनों modules में Redis Lock (जो LUA script पर based है) का extensive उपयोग होता है।

### 1. **loan-core Module में उपयोग**

#### A. Scheduler Jobs में Lock
**File**: `lending/loan-core/logic/jobs/job.go`

```go
func getSchedulerLock(ctx context.Context, jobDetails *storage.SchedulerJob, logTag string) (bool, redis.Lock) {
    lockDuration := time.Second * time.Duration(jobDetails.LockDurationSecs)
    
    // Redis Lock acquire करते हैं
    lock, err := cache.RedisClient.TryLock(ctx, jobDetails.SchedulerID, lockDuration)
    if err != nil {
        slog.FromContext(ctx).Warn(logTag, 
            fmt.Sprintf("scheduler_lock_duration has not expired for job_scheduler_id: %s", 
                jobDetails.SchedulerID))
        return false, nil
    }
    
    return true, lock
}
```

**Use Case**: Multiple scheduler instances एक साथ run हो रहे हों तो ensure करना कि एक ही instance job execute करे।

**Scheduler Jobs जहाँ lock use होती है:**
- `TrackFlexiTermLoanDPDScheduler`
- `ThoughtMachineFailedSchedulerJobs`
- `TrackIncidentalOverdraftDPDScheduler`
- `ExpireTermLoanDrawdownsScheduler`
- `LoanAutoRepaymentScheduler`
- `TrackCreditCardDPDScheduler`
- `TrackBizTermLoanDPDScheduler`
- `CloseFlexiLoanStuckAccJobs`

#### B. Billing Cycle Creation में Lock
**File**: `lending/loan-core/logic/events/rloccontractnotificationevent/create_billing_cycle.go`

```go
func (w *WorkflowImpl) createBillingCycle(ctx context.Context, ...) error {
    // Create a distributed lock for billing cycle creation to prevent race conditions
    lockKey := common.ConstructKey(constants.RLOCInsertBillingCycleDetailLockKey, 
        accountID)
    lockDuration := time.Second * time.Duration(
        w.AppConfig.CacheTimeoutConfig.RLOCInsertBillingCycleDetailLockTTLInSeconds)
    
    lock, err := cache.RedisClient.TryLock(ctx, lockKey, lockDuration)
    if err != nil {
        return err
    }
    if lock == nil {
        return fmt.Errorf("failed to obtain redis lock for billing cycle creation")
    }
    
    // Release the lock when the function exits
    defer func() {
        if unlockErr := lock.Unlock(ctx); unlockErr != nil {
            slog.FromContext(ctx).Warn(logTag, 
                fmt.Sprintf("failed to release redis lock, %s", unlockErr.Error()))
        }
    }()
    
    // Save billing cycle (protected by lock)
    err = w.saveBillingCycle(ctx, nextCtx)
    return err
}
```

**Use Case**: RLOC (Revolving Line of Credit) के लिए billing cycle create करते समय duplicate entries prevent करना।

#### C. Transaction Eligibility Checks में Lock
**File**: `lending/loan-core/logic/implementation/txneligibility/loan_transaction_token_strategy.go`

```go
func processLock(ctx context.Context, lockHandler string, accountID string, transactionID string) (bool, redis.Lock) {
    lock, err := cache.RedisClient.TryLock(ctx, lockHandler+"#"+accountID)
    // Lock logic...
    return lockObtained, lock
}
```

**Use Cases:**
- `EligibilityCheckLock`: Transaction eligibility verify करते समय
- `GenerateTokenLock`: Transaction token generate करते समय
- `TransactionEligibilityLock`: Transaction blackout period check करते समय

#### D. Workflow Run ID Lock
**File**: `lending/loan-core/logic/jobs/expire_flexi_term_loan_drawdowns_job.go`

```go
func getRedisLockOnWorkflowRunID(ctx context.Context, workflowRunID string, ...) (bool, redis.Lock) {
    lock, err := cache.RedisClient.TryLock(ctx, workflowRunID)
    if err != nil {
        slog.FromContext(ctx).Warn(logExpireFlexiTermLoanDrawdowns, 
            fmt.Sprintf("failed to get lock on workflow_run_id: %s", workflowRunID))
        return false, nil
    }
    return true, lock
}
```

**Use Case**: Flexi term loan drawdowns expire करते समय same workflow multiple times न चले।

---

### 2. **loan-exp Module में उपयोग**

#### A. Scheduler Lock
**File**: `lending/loan-exp/logic/jobs/scheduler.go`

```go
func GetSchedulerLock(ctx context.Context, jobDetails *storage.SchedulerJob, logTag string) (bool, redis.Lock) {
    lockDuration := time.Second * time.Duration(jobDetails.LockDurationSecs)
    
    lock, err := cache.RedisClient.TryLock(ctx, jobDetails.SchedulerID, lockDuration)
    if err != nil {
        slog.FromContext(ctx).Warn(logTag, 
            fmt.Sprintf("scheduler_lock_duration has not expired"))
        return false, nil
    }
    
    return true, lock
}
```

#### B. Full Settlement Workflow में Lock
**File**: `lending/loan-exp/logic/workflow/termloans/fullsettlement/execute_fullsettlement_workflow.go`

```go
func (w *WorkflowImpl) getRedisLock(ctx context.Context, eventType string) (bool, redis.Lock, error) {
    lock, err := cache.RedisClient.TryLock(ctx, eventType, w.getLockDuration())
    if err != nil {
        return false, nil, err
    }
    
    if lock == nil {
        slog.FromContext(ctx).Warn(logFullSettlementRepayment, 
            fmt.Sprintf("Not able to obtain lock for event: %s", eventType))
        return false, nil, nil
    }
    
    return true, lock, nil
}

func (w *WorkflowImpl) validateLoanRepayment(ctx context.Context, locID string) error {
    lockKey := w.generateRedisKey(locID)  // Format: "logFullSettlementRepayment:locID"
    
    lockObtained, runIDLock, err := w.getRedisLock(ctx, lockKey)
    if err != nil {
        return err
    }
    
    if !lockObtained || runIDLock == nil {
        return loanExpErrors.ErrLoanRepaymentProcessing
    }
    
    // Check if loan repayment is already processing
    // ... validation logic
    
    return nil
}
```

**Use Case**: Full settlement transaction process होते समय duplicate/concurrent requests prevent करना।

**Lock Duration**: Configurable (default 15 seconds)
```go
func (w *WorkflowImpl) getLockDuration() time.Duration {
    duration := 15 * time.Second
    if w.AppConfig.FullSettlementConfig.LockDurationInSeconds > 0 {
        duration = time.Duration(w.AppConfig.FullSettlementConfig.LockDurationInSeconds) * time.Second
    }
    return duration
}
```

#### C. Auto Repayment Workflow में Lock
**File**: `lending/loan-exp/logic/workflow/termloans/autorepayment/execute_autorepayment_workflow.go`

```go
func (w *WorkflowImpl) getRedisLock(ctx context.Context, eventType string) (bool, redis.Lock, error) {
    lock, err := cache.RedisClient.TryLock(ctx, eventType, w.getLockDuration())
    // Similar implementation as full settlement
    return lockObtained, lock, err
}
```

**Use Case**: Auto repayment process करते समय duplicate transactions prevent करना।

---

### 3. **asset-offline Module में उपयोग**

#### Generic Lock Helper
**File**: `lending/asset-offline/logic/helper/lock.go`

```go
// ProcessLock applies the redis lock based on given key
func ProcessLock(ctx context.Context, key, logTag string, duration ...time.Duration) (bool, redis.Lock) {
    lock, err := cache.RedisClient.TryLock(ctx, key, duration...)
    if err != nil {
        slog.FromContext(ctx).Info(logTag, 
            fmt.Sprintf("processLock failed for key: %s", key))
        return false, nil
    }
    if lock == nil {
        slog.FromContext(ctx).Warn(logTag, 
            fmt.Sprintf("Not able to obtain lock for key: %s", key))
        return false, nil
    }
    
    slog.FromContext(ctx).Info(logTag, 
        fmt.Sprintf("Lock obtained successfully for key: %s", key))
    return true, lock
}
```

---

## Code Examples

### Example 1: Scheduler Job with Lock

```go
func ExecuteSchedulerJob(ctx context.Context, jobDetails *storage.SchedulerJob) error {
    logTag := "scheduler_job"
    
    // Step 1: Try to acquire lock
    lockObtained, lock := getSchedulerLock(ctx, jobDetails, logTag)
    if !lockObtained {
        slog.FromContext(ctx).Warn(logTag, "Lock not obtained, skipping job execution")
        return nil
    }
    
    // Step 2: Ensure lock is released at the end
    defer func() {
        if lock != nil {
            if err := lock.Unlock(ctx); err != nil {
                slog.FromContext(ctx).Error(logTag, 
                    fmt.Sprintf("Failed to unlock: %s", err.Error()))
            }
        }
    }()
    
    // Step 3: Execute your job logic (protected by lock)
    err := performJobLogic(ctx, jobDetails)
    if err != nil {
        return err
    }
    
    return nil
}
```

### Example 2: Preventing Duplicate Transactions

```go
func ProcessPayment(ctx context.Context, locAccountID string, amount float64) error {
    logTag := "process_payment"
    lockKey := fmt.Sprintf("payment_lock:%s", locAccountID)
    lockDuration := 30 * time.Second
    
    // Try to acquire lock
    lock, err := cache.RedisClient.TryLock(ctx, lockKey, lockDuration)
    if err != nil {
        if err == redis.ErrLockOccupied {
            return fmt.Errorf("payment already in progress for account: %s", locAccountID)
        }
        return err
    }
    
    // Ensure lock release
    defer func() {
        if unlockErr := lock.Unlock(ctx); unlockErr != nil {
            slog.FromContext(ctx).Warn(logTag, 
                fmt.Sprintf("Failed to release lock: %s", unlockErr.Error()))
        }
    }()
    
    // Process payment (safe from concurrent modifications)
    err = processPaymentInternal(ctx, locAccountID, amount)
    return err
}
```

### Example 3: Lock with Refresh (Long Running Task)

```go
func LongRunningTask(ctx context.Context, resourceID string) error {
    logTag := "long_running_task"
    lockKey := fmt.Sprintf("task_lock:%s", resourceID)
    initialDuration := 10 * time.Second
    
    // Acquire lock
    lock, err := cache.RedisClient.TryLock(ctx, lockKey, initialDuration)
    if err != nil {
        return err
    }
    
    defer lock.Unlock(ctx)
    
    // Start a goroutine to refresh lock periodically
    refreshCtx, cancel := context.WithCancel(ctx)
    defer cancel()
    
    go func() {
        ticker := time.NewTicker(5 * time.Second)
        defer ticker.Stop()
        
        for {
            select {
            case <-refreshCtx.Done():
                return
            case <-ticker.C:
                // 🔥 LUA Script executes here!
                if err := lock.Refresh(refreshCtx, 10*time.Second); err != nil {
                    slog.FromContext(ctx).Error(logTag, 
                        fmt.Sprintf("Failed to refresh lock: %s", err.Error()))
                    return
                }
                slog.FromContext(ctx).Debug(logTag, "Lock refreshed successfully")
            }
        }
    }()
    
    // Execute long-running task
    err = performLongTask(ctx, resourceID)
    return err
}
```

---

## Best Practices

### 1. हमेशा defer में Unlock करें

```go
✅ GOOD:
lock, err := cache.RedisClient.TryLock(ctx, key, duration)
if err != nil {
    return err
}
defer lock.Unlock(ctx)  // Ensures lock is released even if panic occurs

❌ BAD:
lock, err := cache.RedisClient.TryLock(ctx, key, duration)
// ... do work ...
lock.Unlock(ctx)  // May not be called if error occurs
```

### 2. Lock Duration Appropriately

```go
✅ GOOD:
// Short operations
lockDuration := 5 * time.Second

// Long operations with refresh
lockDuration := 10 * time.Second
// Use lock.Refresh() periodically

❌ BAD:
// Too short - operation may fail
lockDuration := 100 * time.Millisecond

// Too long - blocks other processes unnecessarily
lockDuration := 1 * time.Hour
```

### 3. Handle Lock Acquisition Failures Gracefully

```go
✅ GOOD:
lock, err := cache.RedisClient.TryLock(ctx, key, duration)
if err != nil {
    if err == redis.ErrLockOccupied {
        // Resource is busy, return appropriate error
        return fmt.Errorf("resource is currently locked")
    }
    // Other error, log and return
    return err
}

❌ BAD:
lock, _ := cache.RedisClient.TryLock(ctx, key, duration)
// Ignoring errors!
```

### 4. Use Descriptive Lock Keys

```go
✅ GOOD:
lockKey := fmt.Sprintf("billing_cycle_creation:%s", accountID)
lockKey := fmt.Sprintf("payment_processing:%s:%s", locID, transactionID)
lockKey := fmt.Sprintf("scheduler_job:%s", schedulerID)

❌ BAD:
lockKey := accountID  // Not descriptive
lockKey := "lock1"     // Too generic
```

### 5. Log Lock Operations for Debugging

```go
✅ GOOD:
slog.FromContext(ctx).Info(logTag, 
    fmt.Sprintf("Attempting to acquire lock for key: %s", lockKey))

lock, err := cache.RedisClient.TryLock(ctx, lockKey, duration)
if err != nil {
    slog.FromContext(ctx).Warn(logTag, 
        fmt.Sprintf("Failed to acquire lock: %s", err.Error()))
    return err
}

slog.FromContext(ctx).Info(logTag, "Lock acquired successfully")
```

---

## Summary

### LUA Script का उपयोग कहाँ-कहाँ होता है?

| Module | Component | Purpose | Race Condition Prevention |
|--------|-----------|---------|--------------------------|
| **common/redis** | `lock.go` | Distributed locking implementation | ✅ LUA script for atomic refresh |
| **loan-core** | Scheduler Jobs | Prevent duplicate job execution | ✅ Via Redis Lock |
| **loan-core** | Billing Cycle | Prevent duplicate billing cycles | ✅ Via Redis Lock |
| **loan-core** | Transaction Eligibility | Prevent concurrent eligibility checks | ✅ Via Redis Lock |
| **loan-core** | Workflow Execution | Prevent duplicate workflow runs | ✅ Via Redis Lock |
| **loan-exp** | Full Settlement | Prevent duplicate settlement transactions | ✅ Via Redis Lock |
| **loan-exp** | Auto Repayment | Prevent duplicate auto-repayment | ✅ Via Redis Lock |
| **loan-exp** | Scheduler Jobs | Prevent duplicate job execution | ✅ Via Redis Lock |
| **asset-offline** | Generic Lock Helper | General purpose distributed locking | ✅ Via Redis Lock |

### Key Takeaways

1. **LUA Script का direct उपयोग**: केवल `lock.Refresh()` method में
2. **Indirect उपयोग**: सभी modules जो `redis.Lock` interface use करते हैं
3. **Race Condition Prevention**: सभी critical sections में distributed locking के through
4. **Atomic Operations**: LUA script ensures करती है कि token verification और expiry update atomically हों
5. **Production Grade**: Extensive error handling, logging, और graceful failures के साथ implemented

### क्या चीज़ें नहीं हैं?

❌ **Separate `.lua` files**: सभी LUA scripts Go code में inline strings के रूप में हैं  
❌ **EVALSHA**: Currently direct `EVAL` command use होती है (optimization opportunity)  
❌ **Multiple LUA scripts**: Currently केवल एक LUA script (`luaRefreshScript`) use होती है

### Future Improvements (Potential)

1. **EVALSHA implementation**: Script को cache करके performance improve कर सकते हैं
2. **Additional LUA scripts**: Complex atomic operations के लिए और scripts add कर सकते हैं
3. **Script versioning**: LUA scripts को version करना for easier debugging

---

**Document Created By**: Copilot  
**Date**: June 14, 2026  
**Version**: 1.0  
**Repository**: gitlab.com/gx-regional/dakota

