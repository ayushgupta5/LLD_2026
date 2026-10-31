# Connection Pooling Analysis - Dakota Codebase

**Analysis Date:** May 26, 2026  
**Analyzed By:** GitHub Copilot  
**Codebase:** Dakota (Golang Microservices)

---

## Executive Summary

This document provides a detailed analysis of connection pooling implementation across the Dakota codebase. Unlike Java which commonly uses HikariCP for database connection pooling, **Go uses built-in connection pooling** provided by the `database/sql` package for database connections, and custom/library-based pooling for Redis and HTTP clients.

---

## 1. Database Connection Pooling (MySQL)

### 1.1 Technology Stack
- **Driver:** `github.com/go-sql-driver/mysql` v1.6.0
- **Connection Pooling:** Built-in Go `database/sql` package
- **Pattern:** Singleton pattern with `sync.Once` for connection initialization

### 1.2 Implementation Locations

#### Primary Implementation Files:
1. **lending/loan-app/storage/mysql.go**
2. **lending/loan-core/storage/database_store.go**

### 1.3 Connection Pool Configuration

#### Configuration Parameters:
```go
// Found in config files like: lending/loan-app/config_files/gxb-service-conf.json
{
  "data": {
    "mysql": {
      "master": {
        "dsn": "root:@tcp($MYSQL_HOST$:3306)/$DB_NAME$?parseTime=true&loc=UTC",
        "maxIdle": 25,           // Maximum idle connections in pool
        "maxOpen": 50,           // Maximum open connections
        "connMaxLifetime": "1800s"  // Connection lifetime (30 minutes)
      },
      "slave": {
        "dsn": "root:@tcp($MYSQL_HOST$:3306)/$DB_NAME$?parseTime=true&loc=UTC",
        "maxIdle": 25,
        "maxOpen": 50,
        "connMaxLifetime": "1800s"
      }
    }
  }
}
```

### 1.4 Implementation Details

#### Code Pattern (from loan-app/storage/mysql.go):
```go
var createDB = func(config *data.MysqlConfig) (db *sql.DB, err error) {
    // Open database connection
    db, err = sql.Open("mysql", config.Dsn)
    if err != nil {
        logging.Error(constants.TransactionConnectionLogTag, "Failed to open database connection. Error:", err)
        return
    }
    
    // Configure connection pool
    db.SetMaxIdleConns(config.MaxIdle)        // Set max idle connections
    db.SetMaxOpenConns(config.MaxOpen)        // Set max open connections
    
    if (config.ConnMaxLifetime.Duration) > 0 {
        db.SetConnMaxLifetime(config.ConnMaxLifetime.Duration)  // Set connection lifetime
    }
    
    // Store connection for reuse
    setDB(config, db)
    return
}
```

#### Singleton Pattern Implementation:
```go
var getDatabase = func(config *data.MysqlConfig) (db *sql.DB, err error) {
    defer panicRecovery("commons.data.getDatabase")
    
    connect := func() {
        db, err = createDB(config)
    }
    
    // Ensures createDB is called only once
    config.ConnectOnce.Do(connect)
    
    if err != nil {
        logging.Fatal(constants.TransactionConnectionLogTag, "createDB error: %v", err)
        return nil, err
    }
    
    // Return cached connection
    db = getDB(config)
    if db == nil {
        return nil, errors.New("failed to connect to DB")
    }
    return db, nil
}
```

### 1.5 Connection Pool Settings Across Services

| Service | Environment | Max Idle | Max Open | Conn Max Lifetime |
|---------|-------------|----------|----------|-------------------|
| loan-app | dev | 25 | 50 | 1800s (30 min) |
| batchify | dev | 2 | 10 | Not set |
| loan-core | dev | Similar to loan-app | Similar | Similar |

---

## 2. Redis Connection Pooling

### 2.1 Technology Stack
- **Library:** `github.com/redis/go-redis/v9`
- **Custom Wrapper:** `gitlab.com/gx-regional/dakota/common/redis`
- **Pattern:** Cluster client with connection pooling

### 2.2 Implementation Location
**Primary File:** `common/redis/redis.go`

### 2.3 Redis Pool Configuration

#### Configuration Parameters:
```go
type Config struct {
    Addr               string `json:"addr"`
    PoolSize           int    `json:"poolSize"`           // Connection pool size
    ReadTimeoutInSec   int    `json:"readTimeoutInSec"`
    WriteTimeoutInSec  int    `json:"writeTimeoutInSec"`
    IdleTimeoutInSec   int    `json:"idleTimeoutInSec"`   // Idle connection timeout
    ReadOnlyFromSlaves bool   `json:"readOnlyFromSlaves"`
    Password           string `json:"password"`
    TLSEnabled         bool   `json:"tlsEnabled"`
}
```

#### Example Configuration (from loan-app/config_files/gxb-service-conf.json):
```json
{
  "redisConfig": {
    "addr": "127.0.0.1:6379",
    "idleTimeoutInSec": 1,
    "poolSize": 300,              // 300 connections in pool
    "readOnlyFromSlaves": false,
    "readTimeoutInSec": 1,
    "writeTimeoutInSec": 1
  }
}
```

### 2.4 Implementation Details

#### Connection Pool Setup:
```go
func makeClientConfig(config *Config, options ...Option) *redisConfig {
    conf := &redisConfig{
        options: redis.UniversalOptions{
            Addrs:           []string{config.Addr},
            ReadOnly:        true,
            RouteByLatency:  true,
            Password:        config.Password,
            DialTimeout:     defaultDialTimeout,
            ReadTimeout:     time.Duration(config.ReadTimeoutInSec) * time.Second,
            WriteTimeout:    time.Duration(config.WriteTimeoutInSec) * time.Second,
            PoolSize:        config.PoolSize,              // Pool size configuration
            ConnMaxIdleTime: time.Duration(config.IdleTimeoutInSec) * time.Second,  // Idle timeout
        },
        stats:  statsd.NewNoop(),
        logger: slog.FallbackLogger(),
    }
    return conf
}
```

#### Client Creation with Circuit Breaker:
```go
func MakeClusterClientV2(config *ConfigV2, options ...Option) Client {
    applyRedisDefaultCBValue(config)
    conf := makeClientConfig(&config.Config, options...)
    
    // Create Redis cluster client (with connection pool)
    universalCli := redisNewCluster(conf.options.Cluster())
    
    // Configure circuit breaker
    circuitbreaker.ConfigureCircuitV2(config.CommandName,
        circuitbreaker.WithTimeoutMs(config.Timeout),
        circuitbreaker.WithErrorPercentageThreshold(config.ErrorPercentThreshold),
        circuitbreaker.WithMaxConcurrentRequests(config.MaxConcurrentRequests),
        // ... other circuit breaker settings
    )
    
    return &clientV2{
        client: client{
            redisCli:      universalCli,
            clustered:     true,
            maxExpiryTime: maxExpiryTime,
            // ...
        },
        circuitName: config.CommandName,
        wrappers:    []wrapper{},
    }
}
```

### 2.5 Redis Pool Settings Across Services

| Service | Pool Size | Idle Timeout | Read Timeout | Write Timeout |
|---------|-----------|--------------|--------------|---------------|
| loan-app | 300 | 1s | 1s | 1s |
| batchify | 300 | 1s | 1s | 1s |
| customer-experience | 300 | Similar | Similar | Similar |

---

## 3. HTTP Client Connection Pooling

### 3.1 Technology Stack
- **Built-in:** Go's `net/http` package with `http.Transport`
- **Pattern:** Custom HTTP client configuration per service

### 3.2 Implementation Location
Multiple services implement custom HTTP clients with connection pooling:
- **lending/perfios-adapter/external/perfios/client.go**
- **lending/cb-id-adapter/external/client.go**
- **lending/asset-offline/server/helper.go**

### 3.3 HTTP Pool Configuration

#### Configuration Parameters:
```go
type ClientConfig struct {
    MaxIdleConnsPerHost           int `json:"maxIdleConnsPerHost"`
    IdleConnTimeoutInMillis       int `json:"idleConnTimeoutInSMillis"`
    TimeoutInMillis               int `json:"timeoutInMillis"`
}
```

#### Example Configuration:
```json
{
  "loanAppConfig": {
    "clientConfig": {
      "maxIdleConnsPerHost": 64,
      "idleConnTimeoutInSMillis": 10000,
      "timeoutInMillis": 60000
    }
  }
}
```

### 3.4 Implementation Details

#### HTTP Client Creation (from perfios-adapter):
```go
func makeHTTPClient(appCfg *config.AppConfig) *http.Client {
    httpClient := &http.Client{
        Transport: &http.Transport{
            MaxIdleConnsPerHost: appCfg.PerfiosAdapterClientConfig.MaxIdleConnsPerHost,
            IdleConnTimeout:     time.Duration(appCfg.PerfiosAdapterClientConfig.IdleConnTimeoutInMillis) * time.Millisecond,
        },
        Timeout: time.Duration(appCfg.PerfiosAdapterClientConfig.TimeoutInMillis) * time.Millisecond,
    }
    return httpClient
}
```

### 3.5 HTTP Pool Settings Across Services

| Service | Max Idle Conns Per Host | Idle Conn Timeout | Request Timeout |
|---------|-------------------------|-------------------|-----------------|
| perfios-adapter | 64 | 10000ms | 60000ms |
| cb-id-adapter | Configured per service | Configured | Configured |
| AWS clients (S3, SQS, KMS) | 50 | Default | Default |
| Test automation | 512 | Default | Default |

---

## 4. Key Differences: Go vs Java (HikariCP)

| Aspect | Go (database/sql) | Java (HikariCP) |
|--------|-------------------|-----------------|
| **Pooling Library** | Built-in with `database/sql` | External library (HikariCP) |
| **Configuration** | Simple, 3-4 parameters | More extensive configuration options |
| **Thread Safety** | Goroutine-safe by default | Thread-safe with advanced options |
| **Performance Monitoring** | Basic stats via `db.Stats()` | Extensive metrics and monitoring |
| **Connection Validation** | Via `db.Ping()` | Built-in connection validation |
| **Leak Detection** | Manual implementation needed | Built-in leak detection |
| **Initialization** | Lazy (on first use) | Can be eager or lazy |

---

## 5. Best Practices Observed

### 5.1 Database Connections
✅ **Good Practices:**
- Singleton pattern with `sync.Once` prevents multiple connection pool creation
- Separate master/slave configurations for read/write splitting
- Connection lifetime configured to prevent stale connections
- Proper error handling and logging

⚠️ **Potential Improvements:**
- Add connection health checks
- Implement connection retry logic
- Add metrics for pool utilization monitoring
- Consider adding connection validation queries

### 5.2 Redis Connections
✅ **Good Practices:**
- Circuit breaker pattern integrated for resilience
- Cluster mode support
- Proper timeout configurations
- Connection pooling with reasonable pool sizes

### 5.3 HTTP Connections
✅ **Good Practices:**
- Reusing HTTP clients (avoid creating per request)
- Configurable idle connection pool
- Proper timeout settings

⚠️ **Potential Improvements:**
- Standardize HTTP client configuration across services
- Add connection pool metrics

---

## 6. Connection Pool Sizing Guidelines

### 6.1 Database Connection Pool Sizing Formula
```
Optimal Pool Size = (Core Count × 2) + Effective Spindle Count
```

**Current Settings Analysis:**
- loan-app: MaxOpen=50, MaxIdle=25
  - Reasonable for moderate load
  - Consider increasing for high-traffic scenarios

- batchify: MaxOpen=10, MaxIdle=2
  - Suitable for batch processing
  - Low settings reduce resource usage

### 6.2 Redis Connection Pool Sizing
```
Pool Size = Expected Concurrent Operations × Safety Factor
```

**Current Settings:**
- Most services: PoolSize=300
  - Generous pool size
  - Good for high-concurrency scenarios

### 6.3 HTTP Connection Pool Sizing
```
MaxIdleConnsPerHost = Expected Concurrent Connections to Same Host
```

**Current Settings:**
- Range: 50-512 connections
- Varies based on service requirements

---

## 7. Monitoring and Observability

### 7.1 Available Metrics
```go
// For database/sql
stats := db.Stats()
// Available metrics:
// - stats.MaxOpenConnections
// - stats.OpenConnections
// - stats.InUse
// - stats.Idle
// - stats.WaitCount
// - stats.WaitDuration
```

### 7.2 Current Implementation
- StatsD integration for metrics collection
- Custom logging for connection events
- Circuit breaker metrics for Redis

### 7.3 Recommended Monitoring
1. Track connection pool exhaustion
2. Monitor connection wait times
3. Alert on connection failures
4. Track connection lifetime distribution

---

## 8. Performance Considerations

### 8.1 Database Connection Pooling
**Impact Areas:**
- Connection acquisition latency
- Memory usage (each connection consumes resources)
- Database server load

**Tuning Recommendations:**
- Start with conservative pool sizes
- Monitor and adjust based on load
- Set appropriate connection lifetimes (30 min is reasonable)

### 8.2 Redis Connection Pooling
**Impact Areas:**
- Network socket usage
- Memory per connection
- Redis server connection limits

**Tuning Recommendations:**
- Pool size of 300 is generous but reasonable for microservices
- Adjust idle timeout based on usage patterns
- Enable cluster mode for scalability

### 8.3 HTTP Connection Pooling
**Impact Areas:**
- Socket exhaustion
- Latency reduction through connection reuse
- Memory usage

**Tuning Recommendations:**
- Keep MaxIdleConnsPerHost proportional to expected traffic
- Set reasonable idle timeouts to free resources
- Reuse HTTP clients across requests

---

## 9. Comparison Summary: Go Built-in vs HikariCP

### Golang's `database/sql` Package
**Advantages:**
- ✅ Built-in, no external dependencies
- ✅ Lightweight and efficient
- ✅ Goroutine-safe by default
- ✅ Simple configuration
- ✅ Automatic connection management

**Limitations:**
- ⚠️ Limited monitoring capabilities
- ⚠️ No built-in leak detection
- ⚠️ Basic connection validation
- ⚠️ Fewer tuning options

### Java's HikariCP
**Advantages:**
- ✅ Extensive metrics and monitoring
- ✅ Built-in leak detection
- ✅ Advanced connection validation
- ✅ Highly optimized for performance
- ✅ Rich configuration options

**Disadvantages:**
- ⚠️ External dependency
- ⚠️ More complex configuration
- ⚠️ Higher learning curve

---

## 10. Conclusion

### Summary of Findings

1. **Database Connection Pooling:**
   - Go's built-in `database/sql` package provides robust connection pooling
   - Configuration is simpler than HikariCP but equally effective
   - Implementation follows singleton pattern for efficiency

2. **Redis Connection Pooling:**
   - Custom wrapper around go-redis library
   - Integrated with circuit breaker for resilience
   - Pool size of 300 connections is standard across services

3. **HTTP Connection Pooling:**
   - Uses Go's native `http.Transport` pooling
   - Configuration varies by service requirements
   - Proper connection reuse implemented

### Key Takeaways

- **Go doesn't need HikariCP equivalent** - the standard library is sufficient
- **Connection pooling is automatic** - just configure the parameters
- **Pattern consistency** - similar patterns across different connection types
- **Production-ready** - implementations follow best practices

### Recommendations

1. **Short-term:**
   - Add connection pool metrics dashboard
   - Implement health check endpoints for connection pools
   - Standardize HTTP client configurations

2. **Long-term:**
   - Implement connection leak detection
   - Add automated pool size tuning based on load
   - Create connection pool monitoring alerts

---

## Appendix: Code References

### Key Files to Review

**Database Connection Pooling:**
- `/lending/loan-app/storage/mysql.go`
- `/lending/loan-core/storage/database_store.go`

**Redis Connection Pooling:**
- `/common/redis/redis.go`
- `/batchify/util/cache/cache.go`

**HTTP Connection Pooling:**
- `/lending/perfios-adapter/external/perfios/client.go`
- `/lending/cb-id-adapter/external/client.go`

**Configuration Files:**
- `/lending/loan-app/config_files/gxb-service-conf.json`
- `/batchify/config_files/service-conf.json`
- `/customer-experience/config_files/service-conf.json`

---

**End of Analysis**

