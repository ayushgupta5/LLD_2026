# Invoice Generation Implementation for Flexi Term Loan Workflow

## Summary
Successfully added `generateInvoice()` method to the Superbank Flexi Term Loan workflow to call Transaction-statements service for generating the Overarching Invoice.

## Changes Made

### 1. **Updated workflow.go** 
- **File**: `lending/loan-app/logic/workflow/flexitermloanapplication/superbank/workflow.go`
- **Line 304**: The existing transition already calls `w.generateInvoice` to generate invoice for Overarching Limit
- Added import for transaction-statements API: `transactionStatement "gitlab.myteksi.net/bersama/transaction-statements/api"`
- Added `TransactionStatementClient` field to `WorkflowImpl` struct:
  ```go
  TransactionStatementClient   transactionStatement.TransactionStatements `inject:"client.transactionStatements"`
  ```

### 2. **Created invoice.go**
- **File**: `lending/loan-app/logic/workflow/flexitermloanapplication/superbank/invoice.go`
- Implemented `generateInvoice()` method that:
  - Validates RLOC is enabled
  - Checks if overarching limit account was created
  - Builds invoice payload with customer and account details
  - Calls Transaction-statements service `GenerateInvoice()` API
  - Gracefully handles errors without failing the workflow

### 3. **Added DTO Types**
- **File**: `lending/loan-app/dto/flexi_term_loan.go`
- Added `AddressDTO` struct for customer address information
- Added `OverarchingInvoicePayload` struct for invoice generation payload containing:
  - CustomerName
  - Address
  - OfferedLimit (Money)
  - ReferenceID
  - ApplicationID
  - OpeningTimestamp

## Workflow Transition
```go
// Line 303-304 in workflow.go
// Call Transaction-statements for generating the Overarching Invoice
workflow.AddTransition(logTag, ftl.StRLOCStatusUpdated, ftl.EvNoNeed, w.generateInvoice, 
    transactionalRetryOption, ftl.StRLOCStatusUpdated, ftl.StRLOCStatusUpdateFailure)
```

## Flow
When RLOC status is updated:
1. Workflow calls `generateInvoice()` method
2. Method checks if RLOC feature is enabled
3. Validates overarching limit account exists
4. Builds invoice payload with customer details and offered limit
5. Calls Transaction-statements service to generate PDF invoice
6. Logs success/failure but doesn't fail workflow
7. Stays in `StRLOCStatusUpdated` state on success
8. Moves to `StRLOCStatusUpdateFailure` only on critical failures

## Additional Setup Required

### 1. **Transaction-Statements Client Configuration**
Add to `server/config/config.go`:
```go
type AppConfig struct {
    // ...existing fields...
    TransactionStatements     *ServiceConfig `mapstructure:"transactionStatements"`
}
```

### 2. **Service Configuration**
Add to your service configuration file (e.g., `config_files/service-conf.json`):
```json
{
  "transactionStatements": {
    "serviceName": "transaction-statements",
    "circuitBreaker": {
      "commandConfig": {
        "timeout": 5000,
        "maxConcurrentRequests": 50,
        "errorPercentThreshold": 50
      }
    }
  }
}
```

### 3. **Client Registration**
Ensure the Transaction-statements client is registered in your dependency injection system with the tag: `inject:"client.transactionStatements"`

### 4. **Go Module Dependencies**
Ensure `go.mod` includes:
```
gitlab.myteksi.net/bersama/transaction-statements/api
gitlab.myteksi.net/dakota/lending/common/utils
```

If not present, run:
```bash
cd lending/loan-app
go get gitlab.myteksi.net/bersama/transaction-statements/api
go mod tidy
go mod vendor
```

## Testing

### Manual Testing
1. Create a Flexi Term Loan application
2. Wait for workflow to reach `StRLOCStatusUpdated` state
3. Verify invoice generation logs
4. Check Transaction-statements service for generated invoice

### Unit Test Template
Create `invoice_test.go` to test:
- Invoice generation with valid data
- Handling of missing overarching account
- Behavior when RLOC is disabled
- Payload construction accuracy

## Notes
- Invoice generation is non-blocking and won't fail the workflow
- Empty address fields can be enhanced later by fetching from customer-master service
- The product variant code "OVERARCHING_LIMIT" should match the configuration in transaction-statements service
- Timestamp is recorded as Unix epoch time when invoice generation is triggered

## Related Files
- `workflow.go` - Line 304 (transition definition)
- `invoice.go` - Complete implementation
- `flexi_term_loan.go` - DTO definitions
- `rloc.go` - Related RLOC workflow methods

## Next Steps
1. Add Transaction-statements client configuration
2. Register client in DI container
3. Verify go.mod dependencies
4. Run tests
5. Deploy and monitor logs

