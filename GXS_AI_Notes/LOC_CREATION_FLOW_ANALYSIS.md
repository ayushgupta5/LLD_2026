# LOC (Line of Credit) Creation Flow - Deep Analysis

## Complete Flow with Line Numbers and References

---

## 1. LOAN-APP: Underwriting LOC Creation (Entry Point)

### File: `lending/loan-app/logic/workflow/bizflexicreditapplication/create_loc_account.go`

#### Function: `createBizFlexiCreditAccount` (Lines 21-53)
**Purpose**: Creates LOC account by calling account-service

**Key Steps**:
- **Line 36**: Constructs the CreateLOC request
  ```go
  createLOCAccountRequest := constructCreateLOCRequest(nextCtx)
  ```
- **Line 38**: Logs the request to account-service
  ```go
  slog.FromContext(ctx).Info(logFlexiCreditApplication, fmt.Sprintf("create LOC account request to account-service: %s", utils.ToJSON(createLOCAccountRequest)), utils.GetTraceID(ctx))
  ```
- **Line 39**: **CRITICAL CALL** - Calls account-service to create LOC
  ```go
  createLOCAccountResponse, err := common.CreateLOCAccount(ctx, w.AccountServiceClientGXMigrated, createLOCAccountRequest, logFlexiCreditApplication)
  ```
- **Line 46**: Sets state to LOCAccountCreationProcessing
  ```go
  nextCtx.SetState(stLOCAccountCreationProcessing)
  ```

### File: `lending/loan-app/logic/common/account_service_api.go`

#### Function: `CreateLOCAccount` (Lines 14-28)
**Purpose**: Wrapper to call account-service CreateLOCAccount API

- **Line 17**: Calls account-service client
  ```go
  response, err := a.CreateLOCAccount(ctx, createLOCAccountRequest)
  ```

---

## 2. ACCOUNT-SERVICE: Workflow Execution

### Account-service calls loan-core via HTTP

The account-service receives the request and executes its workflow, which internally calls loan-core's `CreateFlexiLOCAccount` endpoint.

**Expected Call Pattern**:
```
account-service → HTTP POST → loan-core/CreateFlexiLOCAccount
```

---

## 3. LOAN-CORE: Handler Layer

### File: `lending/loan-core/handlers/loancore_create_flexi_l_o_c_account.handler.go`

#### Function: `CreateFlexiLOCAccount` (Lines 18-35)
**Purpose**: Entry point handler for LOC creation in loan-core

**Key Steps**:
- **Line 20**: Logs incoming request
  ```go
  slog.FromContext(ctx).Info(constants.CreateFlexiLOCHandlerLogTag, fmt.Sprintf("Received Request: %s", common.ToJSON(req)), common.GetTraceID(ctx))
  ```
- **Line 22-26**: Validates the request
  ```go
  if errs := handlerlogic.ValidateCreateLOCRequest(req); len(errs) != 0 {
    return nil, commonErr.BuildErrorResponseWithErrorDetail(...)
  }
  ```
- **Line 28**: **CRITICAL** - Calls workflow to execute LOC creation
  ```go
  resp, err := l.CreateLOCWorkflow.ExecuteLOCWorkflow(ctx, req)
  ```

---

## 4. LOAN-CORE: Workflow Execution

### File: `lending/loan-core/logic/workflow/lineofcredit/createlocaccount/line_of_credit.go`

#### Function: `ExecuteLOCWorkflow` (Lines 21-98)
**Purpose**: Main workflow orchestration for LOC creation

**Key Steps**:
- **Line 32**: Fetches product details from product-master
- **Line 49**: Validates product parameters
- **Line 61-78**: Initializes workflow or retrieves existing one
- **Line 81-88**: Executes workflow event
- **Line 92-95**: Triggers async execution for DB persistence

### File: `lending/loan-core/logic/workflow/lineofcredit/createlocaccount/persist_db.go`

#### Function: `persistDB` (Lines 30-77)
**Purpose**: Persists LOC details in database before creating in vault

**Key Steps**:
- **Line 47**: Persists LOC detail to AssetAccount table
  ```go
  locDetail, err := w.persistLOCDetail(ctx, nextCtx)
  ```
- **Line 55**: Persists TM account metadata
  ```go
  tmAccountMetadata, err := w.persistTMAccountMetadata(ctx, nextCtx)
  ```
- **Line 75**: Sets state to persisted
  ```go
  nextCtx.SetState(stPersisted)
  ```

### File: `lending/loan-core/logic/workflow/lineofcredit/createlocaccount/create_loc_account.go`

#### Function: `createLOCAccount` (Lines 22-84)
**Purpose**: Creates LOC account in ThoughtMachine (Vault)

**Key Steps**:
- **Line 32**: Creates customer if not present in TM
- **Line 38-68**: Constructs CreateAccount parameters
- **Line 71**: **CRITICAL** - Creates account in ThoughtMachine/Vault
  ```go
  locCreationResponse, createErr := w.ThoughtMachineClient.Accounts.Create(locCreationRequest)
  ```
- **Line 82**: Sets state to LOCCreationProcessing
  ```go
  nextCtx.SetState(stLOCCreationProcessing)
  ```

---

## 5. LOAN-CORE: Publishing Account Creation Event

### File: `lending/loan-core/logic/workflow/lineofcredit/createlocaccount/publish.go`

#### Function: `publish` (Lines 21-50)
**Purpose**: Publishes LOC creation event to Kafka

**Key Steps**:
- **Line 32**: Constructs LOC DTO
  ```go
  locDTO := constructLOCDTO(ctx, nextCtx, w.CustomerMasterClient)
  ```
- **Line 33**: **CRITICAL** - Publishes to Kafka topic
  ```go
  if err := w.Publisher.Publish(ctx, locDTO); err != nil {
  ```

Publisher Type: `publishers.Publisher` (injected as `publisher.loc`)

### File: `lending/loan-core/kafka/publishers/account_creation_publisher.go`

#### Function: `Publish` (Lines 24-55)
**Purpose**: Publishes account creation event to loan_core_account_creation stream

**Key Steps**:
- **Line 25**: Casts to AccountStreamMessage
  ```go
  accountCreationDTO, _ := i.(*dto.AccountStreamMessage)
  ```
- **Line 26-48**: **CRITICAL** - Publishes to Kafka stream `loan_core_account_creation`
  ```go
  if err := p.KafkaWriter.Save(&loan_core_account_creation.LoanCoreAccountCreation{
    ReferenceID:             accountCreationDTO.ReferenceID,
    ParentAccountID:         accountCreationDTO.ParentAccountID,
    AccountID:               accountCreationDTO.AccountID,
    ...
  }); err != nil {
  ```
- **Line 53**: Logs success
  ```go
  slog.FromContext(ctx).Info(constants.AccountCreationEventPublisherLogTag, fmt.Sprintf("Data published: %s", common.ToJSON(accountCreationDTO)))
  ```

**Kafka Topic**: Configured in `lending/loan-core/server/register_kafka_writer.go` (Line 116)

---

## 6. LOAN-APP: Consuming Account Creation Event

### File: `lending/loan-app/kafka/consumers/loc_account_creation_event_stream.go`

#### Function: `handleLOCAccountCreationEvent` (Lines 88-119)
**Purpose**: Consumes LOC account creation event from account-service

**Key Steps**:
- **Line 94**: Maps stream event to DTO
  ```go
  eventDTO := mapper.MapLOCAccountCreationStreamToDTO(data)
  ```
- **Line 96-99**: Routes to BizFlexiCredit workflow
  ```go
  if eventDTO.ProductVariantCode == commonConst.BizLineOfCreditProductVariantCode {
    if consumerErr := bizflexicreditapplication.ConsumeBizLOCAccountCreationEvent(ctx, eventDTO); consumerErr != nil {
  ```

**Kafka Topic**: `loc_account_creation_event` (from account-service)

### File: `lending/loan-app/logic/workflow/bizflexicreditapplication/common.go`

#### Function: `ConsumeBizLOCAccountCreationEvent` (Line 180)
**Purpose**: Consumes and processes LOC account creation event in workflow

This triggers the workflow transition:
```go
evLOCAccountCreation → persistLOCAccountCreationStream → persistLOCAccountCreationInDB
```

### File: `lending/loan-app/logic/workflow/bizflexicreditapplication/create_loc_account.go`

#### Function: `persistLOCAccountCreationStream` (Lines 55-73)
**Purpose**: Persists the stream message in workflow context

- **Line 64-68**: Validates and extracts stream message
  ```go
  streamMessage, ok := params.(*dto.LOCAccountCreationStreamMessage)
  ```
- **Line 70**: Stores in context
  ```go
  nextCtx.LOCAccountCreationStreamMessage = streamMessage
  ```
- **Line 71**: Sets state
  ```go
  nextCtx.SetState(stPersistedLOCAccountCreation)
  ```

#### Function: `persistLOCAccountCreationInDB` (Lines 104-140)
**Purpose**: Updates application status based on LOC creation result

**Key Steps**:
- **Line 115**: Checks if LOC account is ACTIVE
  ```go
  if nextCtx.LOCAccountCreationStreamMessage.Status == string(accountServiceAPI.LOCAccountStatus_Status_ACTIVE) {
  ```
- **Line 116**: Updates status to APPROVED
  ```go
  nextCtx = updateStatusAndStatusReason(nextCtx, api.ApplicationStatus_ApplicationStatus_APPROVED, api.ApplicationStatusReason_LIMIT_CREATION_SUCCESSFUL, false)
  ```
- **Line 120**: Sets state to success
  ```go
  nextCtx.SetState(stLOCAccountCreationSuccess)
  ```
- **Line 127-129**: Saves application to database
  ```go
  stmts = append(stmts, w.BizFlexiCreditQueryGenerator.CreateUpdateApplicationStatement(nextCtx.Application))
  err := w.Store.ExecuteTransaction(ctx, w.AppConfig, stmts)
  ```

---

## 7. LOAN-EXP: Consuming LOC Creation Event

### File: `lending/loan-exp/kafka/consumers/loan_core_loc_stream.go`

#### Consumer: `LoanCoreLOCEventStreamImpl`

#### Function: `startConsumeLoanCoreLOCStream` (Lines 54-73)
**Purpose**: Starts Kafka consumer for loan_core_loc stream

- **Line 63**: Creates reader for `loan_core_loc` stream
  ```go
  reader, err := streams.NewStaticReader(ctx, loanCoreLOCStream, *appCfg.LoanCoreLOCKafkaConfig.KafkaConfig, &loanCoreLOCSchema.LoanCoreLoc{})
  ```

#### Function: `handleLoanCoreLOCStream` (Lines 109-236)
**Purpose**: Main event handler for LOC events

**Key Steps**:
- **Line 117**: Maps stream to DTO
  ```go
  eventDTO, err := mapper.MapLoanCoreLOCStreamToDTO(data)
  ```
- **Line 122-130**: **CRITICAL** - Handles LOC Creation event type
  ```go
  case string(commonConst.LOCCreationEventType):
    if l.AppConfig.FeatureFlags.EnableLOCInvoiceGeneration && eventDTO.Metadata != nil {
      if errLocInvoiceGen := common.GenerateLOCInvoice(ctx, eventDTO, 
        l.Service.AccountServiceClient, 
        l.Service.CustomerMasterClient, 
        l.Service.TransactionStatementClient, 
        l.Service.ProductMasterClient); errLocInvoiceGen != nil {
  ```

---

## 8. LOAN-EXP: Transaction Statement Call & Invoice Generation

### File: `lending/loan-exp/logic/common/invoice.go`

#### Function: `GenerateLOCInvoice` (Lines 32-76)
**Purpose**: Generates LOC invoice/welcome letter by calling transaction-statement service

**Key Steps**:
- **Line 40-42**: Unmarshals metadata
  ```go
  metadata := dto.LOCMetadata{}
  if err := json.Unmarshal(data.Metadata, &metadata); err != nil {
  ```
- **Line 54**: Gets SafeID from customer-master
  ```go
  safeID, err := GetSafeIDByCIFNumber(ctx, customerMasterClient, cifNumber)
  ```
- **Line 59**: Gets customer details
  ```go
  customerDetail, err := GetCustomerDetails(ctx, constants.LoanCoreLOCKafkaConsumerLogTag, customerMasterClient, safeID)
  ```
- **Line 64**: Generates LOC invoice payload
  ```go
  locInvoicePayload := generateLOCInvoicePayload(ctx, metadata, customerDetail, productMasterClient)
  ```
- **Line 65-68**: Constructs request for transaction-statement
  ```go
  invoiceGenerateRequest := transactionStatement.GenerateInvoiceRequest{
    ProductVariantCode: commonConst.FlexiLineOfCreditProductVariantCode,
    AccountID:          data.AccountID,
    Payload:            commonUtils.ConvertStructToMapStringInterface(locInvoicePayload),
  }
  ```
- **Line 71**: **CRITICAL** - Calls transaction-statement service
  ```go
  if _, err = trxStatementClient.GenerateInvoice(ctx, &invoiceGenerateRequest); err != nil {
  ```

#### Function: `generateLOCInvoicePayload` (Lines 131-180)
**Purpose**: Constructs payload for LOC invoice

**Key Data Included**:
- Customer name and address
- Offered LOC amount
- Interest rate
- Max tenor in months
- Reference ID
- Opening timestamp
- Admin fee (if enabled)
- Early repayment fee (if enabled)

---

## 9. Workflow State Transitions (loan-app)

### File: `lending/loan-app/logic/workflow/bizflexicreditapplication/workflow_sg.go`

**LOC Creation Workflow States** (Lines 119-129):

```go
Line 120: stOperatingAccountCreated → evNoNeed → createBizFlexiCreditAccount → stLOCAccountCreationProcessing
Line 121: stLOCAccountCreationProcessing → evLOCAccountCreation → persistLOCAccountCreationStream → stPersistedLOCAccountCreation
Line 122: stPersistedLOCAccountCreation → evNoNeed → persistLOCAccountCreationInDB → stLOCAccountCreationSuccess
Line 125: stLOCAccountCreationSuccess → evNoNeed → publishStatus → stLOCCreationSuccessPublished
Line 128: stLOCCreationSuccessPublished → evNoNeed → sendLOCAccountCreatedPushInboxNotification → stLOCAccountCreationSuccessPushInboxNotified
Line 129: stLOCAccountCreationSuccessPushInboxNotified → evNoNeed → sendLOCAccountCreatedEmailNotification → stLOCAccountCreationSuccessEmailNotified
```

---

## 10. Workflow State Transitions (loan-core)

### File: `lending/loan-core/logic/workflow/lineofcredit/createlocaccount/workflow.go`

**LOC Creation Workflow States** (Lines 102-129):

```go
Line 102: stInit → evIntermittentStateToExecuteAsync → intermittentStateToExecuteAsync → stIntermittentStateToExecuteAsync
Line 103: stIntermittentStateToExecuteAsync → evPersistDB → persistDB → stPersisted
Line 106: stPersisted → evNoNeed → createLOCAccount → stLOCCreationProcessing/stLOCCreationCompleted
Line 107: stLOCCreationProcessing → evPersistCreateLOCStream → persistCreationLOCAccountStream → stLOCCreationStreamPersisted
Line 108: stLOCCreationStreamPersisted → evNoNeed → completeCreateLOCAccount → stLOCCreationCompleted/stLOCCreationFailed
Line 111: stLOCCreationCompleted → evNoNeed → createPlan → stPlanCreationProcessing/stPlanCreationCompleted
Line 112: stPlanCreationProcessing → evPersistCreatePlanStream → persistCreationPlanStream → stPlanCreationStreamPersisted
Line 113: stPlanCreationStreamPersisted → evNoNeed → completeCreatePlan → stPlanCreationCompleted/stPlanCreationFailed
Line 116: stPlanCreationCompleted → evNoNeed → associatedLOCToPlan → stAssociatePlanProcessing/stAssociatePlanCompleted
Line 117: stAssociatePlanProcessing → evPersistAssociatePlanStream → persistAssociatePlanStream → stAssociatePlanStreamPersisted
Line 118: stAssociatePlanStreamPersisted → evNoNeed → completeAssociatePlan → stAssociatePlanCompleted/stAssociatePlanFailed
Line 121: stAssociatePlanCompleted → evNoNeed → locAccountOpeningPosting → stLOCAccountOpeningPostingProcessing/stLOCAccountOpeningPostingCompleted
Line 122: stLOCAccountOpeningPostingProcessing → evPersistLOCAccountOpeningStream → persistLOCAccountOpeningPostingStream → stLOCAccountOpeningPostingStreamPersisted
Line 123: stLOCAccountOpeningPostingStreamPersisted → evNoNeed → completeLOCAccountOpeningPosting → stLOCAccountOpeningPostingCompleted/stLOCAccountOpeningPostingFailed
Line 126: stLOCAccountOpeningPostingCompleted → evNoNeed → persistLOCParams → stLOCParamsPersisted
Line 129: stLOCParamsPersisted → evNoNeed → publish → stSuccessPublished
```

---

## Summary Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│ 1. LOAN-APP (Underwriting)                                               │
│    File: create_loc_account.go:39                                        │
│    └─ CreateLOCAccount() → account-service                               │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│ 2. ACCOUNT-SERVICE                                                        │
│    └─ Workflow executes → HTTP call to loan-core                         │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│ 3. LOAN-CORE (Handler)                                                    │
│    File: loancore_create_flexi_l_o_c_account.handler.go:28               │
│    └─ ExecuteLOCWorkflow(ctx, req)                                       │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│ 4. LOAN-CORE (Workflow)                                                   │
│    File: line_of_credit.go:23                                            │
│    └─ ExecuteLOCWorkflow                                                 │
│       ├─ persist_db.go:30 → persistDB (Save to AssetAccount)            │
│       ├─ create_loc_account.go:71 → Accounts.Create (ThoughtMachine)    │
│       └─ publish.go:33 → Publisher.Publish                               │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│ 5. LOAN-CORE (Kafka Publisher)                                           │
│    File: account_creation_publisher.go:26                                │
│    Topic: loan_core_account_creation                                     │
│    └─ KafkaWriter.Save(LoanCoreAccountCreation)                          │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│ 6. LOAN-APP (Kafka Consumer)                                             │
│    File: loc_account_creation_event_stream.go:97                         │
│    Topic: loc_account_creation_event (from account-service)              │
│    └─ ConsumeBizLOCAccountCreationEvent                                  │
│       ├─ create_loc_account.go:55 → persistLOCAccountCreationStream     │
│       └─ create_loc_account.go:104 → persistLOCAccountCreationInDB      │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│ 7. LOAN-EXP (Kafka Consumer)                                             │
│    File: loan_core_loc_stream.go:126                                     │
│    Topic: loan_core_loc                                                  │
│    Event: LOCCreationEventType                                           │
│    └─ GenerateLOCInvoice                                                 │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│ 8. LOAN-EXP (Invoice Generation)                                         │
│    File: invoice.go:71                                                   │
│    └─ TransactionStatementClient.GenerateInvoice(ctx, request)          │
│       ├─ invoice.go:54 → GetSafeIDByCIFNumber                           │
│       ├─ invoice.go:59 → GetCustomerDetails                             │
│       └─ invoice.go:64 → generateLOCInvoicePayload                      │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Key Kafka Topics/Streams

1. **loan_core_account_creation** (loan-core → loan-exp)
   - Schema: `LoanCoreAccountCreation`
   - Published by: `lending/loan-core/kafka/publishers/account_creation_publisher.go:26`
   - Consumed by: `lending/loan-exp/kafka/consumers/loan_core_loc_stream.go:63`

2. **loc_account_creation_event** (account-service → loan-app)
   - Schema: `LOCAccountCreationEvent`
   - Published by: account-service (not in this codebase)
   - Consumed by: `lending/loan-app/kafka/consumers/loc_account_creation_event_stream.go:69`

3. **loan_core_loc** (loan-core → loan-exp)
   - Schema: `LoanCoreLoc`
   - Used for LOC lifecycle events including creation
   - Consumed by: `lending/loan-exp/kafka/consumers/loan_core_loc_stream.go:85`

---

## Critical Integration Points

1. **Loan-App → Account-Service**
   - File: `lending/loan-app/logic/common/account_service_api.go:17`
   - Method: HTTP call via account service client

2. **Account-Service → Loan-Core**
   - Method: HTTP POST to loan-core's CreateFlexiLOCAccount endpoint
   - Entry: `lending/loan-core/handlers/loancore_create_flexi_l_o_c_account.handler.go:19`

3. **Loan-Core → ThoughtMachine/Vault**
   - File: `lending/loan-core/logic/workflow/lineofcredit/createlocaccount/create_loc_account.go:71`
   - Method: `w.ThoughtMachineClient.Accounts.Create()`

4. **Loan-Exp → Transaction-Statement**
   - File: `lending/loan-exp/logic/common/invoice.go:71`
   - Method: `trxStatementClient.GenerateInvoice()`

---

## Notes

- The flow involves **3 microservices**: loan-app, account-service, loan-core, loan-exp
- **2 Kafka streams** are used for async communication
- **ThoughtMachine (Vault)** is the core banking system where accounts are created
- **Transaction-Statement** service generates the actual invoice PDFs
- The workflow uses a state machine pattern with transitions and events
- Invoice generation happens **only in loan-exp** when LOCCreationEventType is received

