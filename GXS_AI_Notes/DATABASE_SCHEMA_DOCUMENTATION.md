# Asset-Offline Service - Database Schema Documentation

## Schema: `asset_offline`

## Tables

| Schema | Tables | Purpose | Primary Identifier |
|--------|--------|---------|-------------------|
| **Asset_Offline** | **application** | - Store loan application data for offline processing<br>- Track application status and status changes<br>- Get application details using application_id<br>- Filter applications by status<br>- Store customer-specific application information<br>- Track application review reasons | - `id`: Auto-increment primary key<br>- `application_id`: Unique loan application identifier<br>- `customer_id`: Customer safe ID<br>- Filter by `status` |
| | **application_history** | - Maintain historical records of application changes<br>- Track all status transitions for an application<br>- Audit trail for application modifications<br>- Get application history by application_id<br>- Track when and who made changes | - `id`: Auto-increment primary key<br>- `application_id`: Reference to application<br>- Filter by `status` and `created_at` |
| | **workflow_execution** | - Store workflow execution state and data<br>- Track workflow processing steps<br>- Resume failed workflow executions<br>- Get workflow execution by run_id and workflow_id<br>- Retry failed workflow transitions<br>- Track workflow attempt count | - `id`: Auto-increment primary key<br>- `run_id`: Workflow execution identifier<br>- `workflow_id`: Workflow type identifier<br>- `transition_id`: Current state transition ID<br>- Filter by `state` and `updated_at` |

---

## Detailed Table Information

### application

**Purpose:** Stores loan application data for offline processing workflows

**Columns:**
- `id` (BIGINT UNSIGNED, PK, AUTO_INCREMENT): Unique identifier for each application record
- `application_id` (VARCHAR(36), UNIQUE): Unique ID for each loan application
- `application_type` (VARCHAR(36), DEFAULT ''): Type of loan application (e.g., guarantor, overarching limit)
- `customer_id` (VARCHAR(36)): Safe ID of the customer who submitted the application
- `status` (VARCHAR(36)): Current status of application (e.g., PENDING, APPROVED, REJECTED, REVIEW)
- `status_reason` (VARCHAR(255), NULLABLE): Detailed reason for the current status
- `review_reason` (VARCHAR(255), NULLABLE): Reason why application is under review
- `metadata` (JSON, NULLABLE): Additional datapoints related to application in JSON format
- `created_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP): Timestamp when the application was created
- `created_by` (VARCHAR(36), DEFAULT ''): Identifier of the service/user who created the record
- `updated_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP ON UPDATE): Timestamp of last update
- `updated_by` (VARCHAR(36), DEFAULT ''): Identifier of the service/user who last updated the record

**Indexes:**
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_application_id` on `application_id`
- INDEX: `index_status` on `status`
- INDEX: `index_created_at` on `created_at`
- INDEX: `index_updated_at` on `updated_at`

**Engine:** InnoDB

---

### application_history

**Purpose:** Maintains historical records of all application status changes and modifications

**Columns:**
- `id` (BIGINT UNSIGNED, PK, AUTO_INCREMENT): Unique identifier for each history record
- `application_id` (VARCHAR(36)): Reference to the application being tracked
- `application_type` (VARCHAR(36), DEFAULT ''): Type of loan application
- `customer_id` (VARCHAR(36)): Safe ID of the customer
- `status` (VARCHAR(36)): Status at the time of this history entry
- `status_reason` (VARCHAR(255), NULLABLE): Reason for the status at this point in time
- `review_reason` (VARCHAR(255), NULLABLE): Review reason at this point in time
- `metadata` (JSON, NULLABLE): Additional datapoints in JSON format
- `created_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP): Timestamp when this history record was created
- `created_by` (VARCHAR(36), DEFAULT ''): Identifier of the service/user who made the change
- `updated_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP ON UPDATE): Timestamp of last update to this history record
- `updated_by` (VARCHAR(36), DEFAULT ''): Identifier of the service/user who last updated

**Indexes:**
- PRIMARY KEY: `id`
- INDEX: `index_application_id` on `application_id`
- INDEX: `index_status` on `status`
- INDEX: `index_created_at` on `created_at`
- INDEX: `index_updated_at` on `updated_at`

**Engine:** InnoDB

---

### workflow_execution

**Purpose:** Stores workflow execution state, data, and tracks processing steps for async workflows

**Columns:**
- `id` (BIGINT UNSIGNED, PK, AUTO_INCREMENT): Unique identifier for workflow execution record
- `workflow_id` (VARCHAR(36), DEFAULT ''): Identifier of the workflow type (e.g., guarantor_application_verdict)
- `run_id` (CHAR(36), DEFAULT ''): Unique ID per workflow execution (typically same as application_id)
- `transition_id` (CHAR(36), DEFAULT ''): ID of current state transition
- `prev_trans_id` (CHAR(36), DEFAULT '', NULLABLE): ID of previous state transition
- `attempt` (INT, DEFAULT 0): Attempt count per transition (for retry logic)
- `state` (INT): Current state of the workflow (numeric state identifier)
- `data` (JSON): Data required for running workflow (execution context)
- `created_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP): Timestamp when workflow was created
- `updated_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP ON UPDATE): Timestamp of last update

**Indexes:**
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_run_id_workflow_id` on (`run_id`, `workflow_id`) - Identifier for a specific execution
- UNIQUE KEY: `uk_transition_id` on `transition_id` - Identifier for a transition within an execution
- UNIQUE KEY: `uk_prev_trans_id` on `prev_trans_id` - Identifier for continuing an existing execution
- INDEX: `index_state_updated_at` on (`workflow_id`, `state`, `updated_at`) - For worker to retry failed transitions
- INDEX: `index_created_at` on `created_at`
- INDEX: `index_updated_at` on `updated_at`

---

## Relationships

```
application (1) -----> (N) application_history
                              (via application_id)

application (1) -----> (1) workflow_execution
                              (via application_id = run_id)
```

---

## Common Use Cases

### 1. Get Application by ID
```sql
SELECT * FROM application WHERE application_id = 'APP123456';
```

### 2. Get Application History
```sql
SELECT * FROM application_history 
WHERE application_id = 'APP123456' 
ORDER BY created_at DESC;
```

### 3. Check Workflow Execution Status
```sql
SELECT * FROM workflow_execution 
WHERE run_id = 'APP123456' 
AND workflow_id = 'guarantor_application_verdict';
```

### 4. Get Applications by Status
```sql
SELECT * FROM application 
WHERE status = 'PENDING' 
ORDER BY created_at DESC;
```

### 5. Retry Failed Workflows
```sql
SELECT * FROM workflow_execution 
WHERE workflow_id = 'guarantor_application_verdict' 
AND state = 999 
AND updated_at < DATE_SUB(NOW(), INTERVAL 5 MINUTE)
ORDER BY updated_at ASC;
```

### 6. Track Status Transitions
```sql
SELECT application_id, status, created_at, created_by 
FROM application_history 
WHERE application_id = 'APP123456' 
ORDER BY created_at ASC;
```

---

## Configuration Reference

**Database Connection:**
```json
{
  "data": {
    "mysql": {
      "master": {
        "dsn": "root@tcp($MYSQL_HOST$:3306)/asset_offline?parseTime=true&loc=UTC",
        "maxIdle": 2,
        "maxOpen": 10,
        "connMaxLifetime": "1800s"
      },
      "slave": {
        "dsn": "root@tcp($MYSQL_HOST$:3306)/asset_offline?parseTime=true&loc=UTC",
        "maxIdle": 2,
        "maxOpen": 10,
        "connMaxLifetime": "1800s"
      }
    }
  }
}
```

**Location:** `lending/asset-offline/config_files/gxb-service-conf.json`

---

## Application Types

The service currently supports various application types:

1. **Guarantor Applications** - Applications involving guarantor verification and approval
2. **Overarching Limit** - Applications for overarching credit limit management
3. **Credit Decision Engine** - Applications processed through credit decision workflows

---

## Workflow Types

The service implements several workflow types:

1. **guarantor_application_verdict** - Processes guarantor application decisions
2. **backfill_overarching_limit** - Backfills overarching limit data
3. **credit_bureau_check** - Executes credit bureau validation workflows

---

## Database Migration

**Migration Tool:** Sqitch

**Configuration File:** `lending/asset-offline/db/mysql/sqitch.conf`

**Deployment Scripts Location:** `lending/asset-offline/db/mysql/deploy/`

**Migration History:**
1. `0000-application.sql` - Initial application table creation (2024-03-11)
2. `0001-application_history.sql` - Application history table for audit trail (2024-03-11)
3. `0002-workflow.sql` - Workflow execution table for async processing (2024-03-26)

---

## Key Features

### 1. Offline Processing
The service handles loan-related tasks that don't require user interaction or can be scheduled to run offline:
- Asynchronous workflow execution
- Background application processing
- Scheduled batch operations

### 2. Workflow Management
- State machine-based workflow execution
- Automatic retry mechanism for failed transitions
- Workflow data persistence in JSON format

### 3. Audit Trail
- Complete history of application changes in `application_history`
- Track who made changes and when
- Full status transition tracking

### 4. Idempotency
- Unique constraints ensure no duplicate workflow executions
- Transition IDs prevent duplicate state processing

---

## Notes

- All timestamps are stored in UTC timezone
- The database uses `utf8mb4` charset with `utf8mb4_unicode_ci` collation
- JSON columns are used for flexible metadata and workflow data storage
- The service supports both master and slave database connections for read/write separation
- Auto-increment is used for primary keys across all tables
- Timestamps (`created_at`, `updated_at`) are automatically managed by the database
- Workflow execution uses unique composite keys to ensure only one workflow run per application
- The `state` field in workflow_execution uses numeric values to represent different workflow states
- Application status transitions are automatically recorded in application_history table

