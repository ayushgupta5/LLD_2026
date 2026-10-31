# JWT Authentication Flow in loan-exp Service

## Overview
loan-exp service mein JWT-based authentication **do layers** mein kaam karta hai:

1. **Service-to-Service Authentication (XFCC + SPIFFE)** - Istio level par
2. **User Authentication (JWT Session Data)** - API Gateway level par

---

## 1. Service-to-Service Authentication (XFCC + SPIFFE)

### Location
- **File**: `/vendor/gitlab.com/gx-regional/dakota/servus/v2/client_auth.go`
- **Middleware**: `ClientAuthMiddlewareID`

### Implementation

#### Route Registration
Har endpoint `WithClientIdentities()` ke saath register hoti hai:

```go
// File: handlers/loanexp.routes.go
app.POST(
    "/v1/term-loans/simulation-parameters",
    handler,
    v2.WithRequest(&api.GetTermLoanSimulationParametersRequest{}),
    v2.WithClientIdentities(servicename.SentryT6), // Authorization यहाँ define hai
)
```

#### Authentication Flow

```
Client Request
    ↓
Istio Ingress Gateway
    ↓
X-Forwarded-Client-Cert (XFCC) Header inject karta hai
    ↓
servus Middleware (ClientAuthMiddlewareID)
    ↓
VerifyClientIdentity() function
    ↓
SPIFFE Workload ID verify karta hai
    ↓
✓ Allowed: Request proceed karta hai
✗ Denied: 403 Forbidden return karta hai
```

#### Key Functions

**1. WithClientIdentities() - Middleware Setup**
```go
// Location: servus/v2/client_auth.go:44-71
func WithClientIdentities(allowedIdentities ...servicename.ServiceName) Middleware {
    services := []string{servicename.IstioIngressGW.ToString()}
    for _, identity := range allowedIdentities {
        services = append(services, identity.ToString())
    }
    
    return NewMiddleware(ClientAuthMiddlewareID, func(h HandlerFunc) HandlerFunc {
        return func(ctx context.Context, request interface{}) (interface{}, error) {
            newCtx, err := VerifyClientIdentity(ctx, services...)
            if err != nil {
                return nil, ServiceError{HTTPCode: http.StatusForbidden}
            }
            return h(newCtx, request)
        }
    })
}
```

**2. VerifyClientIdentity() - Identity Verification**
```go
// Location: servus/v2/client_auth.go:113-133
func VerifyClientIdentity(ctx context.Context, allowedIdentities ...string) (context.Context, error) {
    req := ctx.Value(ctxKeyRequest).(*http.Request)
    
    // XFCC header se client identity nikalta hai
    identity, err := GetClientIdentity(ctx, req.Header.Get(xfccparser.ForwardedClientCertHeader))
    if err != nil {
        return ctx, err
    }
    
    // Verify karta hai ki identity allowed list mein hai ya nahi
    for _, allow := range allowedIdentities {
        if identity == allow {
            return commonCtx.WithClientIdentity(ctx, identity), nil
        }
    }
    
    return commonCtx.WithClientIdentity(ctx, identity), ErrInvalidIdentity
}
```

**3. GetClientIdentity() - SPIFFE Parsing**
```go
// Location: servus/v2/client_auth.go:85-110
func GetClientIdentity(ctx context.Context, header string) (string, error) {
    // XFCC header parse karta hai
    certs, err := xfccparser.ParseXFCCHeader(header)
    
    // SPIFFE workload ID extract karta hai
    workloadID, err := spiffeid.FromString(certs[0].URI)
    
    // Kubernetes service account name return karta hai
    workload := parseKubernetesWorkloadID(workloadID.Path())
    return workload.ServiceAccount, nil
}
```

### XFCC Header Format
```
X-Forwarded-Client-Cert: By=spiffe://cluster.local/ns/backend-dakota-app-01/sa/loan-exp;
Hash=abc123;
Subject="";
URI=spiffe://cluster.local/ns/backend-dakota-app-01/sa/sentry-t6
```

#### Allowed Services (Examples)
```go
servicename.SentryT6                    // Main mobile app
servicename.SentryPartnerT6             // Partner services
servicename.CustomerPortal              // CRM portal
servicename.LoanCore                    // Internal service
servicename.IstioIngressGW             // Always allowed (default)
```

---

## 2. User Authentication (JWT Session Data)

### Location
- **File**: `/vendor/gitlab.com/gx-regional/dakota/servus/v2/context.go`
- **Function**: `injectGatewayHeaders()`

### JWT Headers from T6 API Gateway

#### Key Headers
```go
const (
    HeaderXUserID           = "X-Grab-Id-Userid"           // User ka unique ID
    HeaderXServiceID        = "X-Grab-Id-Serviceid"        // Service identifier
    HeaderXServiceUserID    = "X-Grab-Id-Serviceuserid"    // Service user ID
    HeaderXClientID         = "X-Grab-Id-Clientid"         // Client/App ID
    HeaderXJWTSessionData   = "X-Jwt-Session-Data"         // JWT session data
    HeaderXDeviceID         = "X-Grab-Device-ID"           // Device ID
    HeaderXEmailID          = "X-Grab-Email-ID"            // Email ID
    HeaderXProfileID        = "X-Grab-Profile-ID"          // Profile ID
    HeaderXProfileType      = "X-Grab-Profile-Type"        // Profile type
)
```

#### Header Injection Flow
```go
// Location: servus/v2/context.go:223-321
func injectGatewayHeaders(ctx context.Context) context.Context {
    // T6 API Gateway se aane wale JWT headers ko parse karta hai
    // Reference: http://sentry.pages.myteksi.net/t6/t6-fabric/configuration/security/t6_jwt.html
    
    userID := HeaderFromCtx(ctx).Get(commonCtx.HeaderXUserID)
    if userID != "" {
        ctx = commonCtx.WithUserID(ctx, userID)
    }
    
    jwtSessionData := HeaderFromCtx(ctx).Get(commonCtx.HeaderXJWTSessionData)
    if jwtSessionData != "" {
        ctx = commonCtx.WithJWTSessionData(ctx, jwtSessionData)
    }
    
    // ... more headers ...
    
    return ctx
}
```

---

## 3. User Authorization (CIF-based)

### Location
- **File**: `lending/loan-exp/logic/common/authorization_checker.go`
- **Function**: `CheckIfAuthorized()`, `CheckIfAuthorizedV2()`, `CheckRequestAuthorization()`

### Authorization Flow

```
Handler Function
    ↓
CheckIfAuthorized(ctx, accountID, ...)
    ↓
1. GetUserIDFromHeader(ctx) - Context se UserID nikalta hai
    ↓
2. LookUpCifFromCustomerMaster() - Customer Master se CIF number fetch karta hai
    ↓
3. CheckPermissionAccountService() - Account Service se verify karta hai
    ↓
✓ Authorized: CIF number return karta hai
✗ Unauthorized: 401 Unauthorized error throw karta hai
```

### Key Functions

**1. CheckIfAuthorized() - Main Authorization Check**
```go
// Location: logic/common/authorization_checker.go:27-48
func CheckIfAuthorized(ctx context.Context, accountID string, 
    customerMasterClient customerAPI.CustomerMaster, 
    accountServiceClient accountAPI.AccountService, 
    safeID string) (string, error) {
    
    // Step 1: CIF Number lookup
    cifNumber, httpStatus, lookUpErr := LookUpCifFromCustomerMaster(ctx, customerMasterClient, safeID)
    if lookUpErr != nil {
        return "", commonErr.BuildErrorResponse(http.StatusUnauthorized, ...)
    }
    
    // Step 2: Permission check
    status, checkErr := CheckPermissionAccountService(ctx, accountID, cifNumber, accountServiceClient)
    if checkErr != nil {
        return "", checkErr
    }
    
    // Step 3: Verify permission
    if status != constants.AccountPermissionAllowed {
        return "", commonErr.BuildErrorResponse(http.StatusUnauthorized, ...)
    }
    
    return cifNumber, nil
}
```

**2. LookUpCifFromCustomerMaster() - CIF Lookup**
```go
// Location: logic/common/authorization_checker.go:51-82
func LookUpCifFromCustomerMaster(ctx context.Context, c customerAPI.CustomerMaster, safeID string) (string, int, *servus.ErrorDetail) {
    // Context se UserID aur ServiceID nikalta hai
    userID := GetUserIDFromHeader(ctx)      // JWT se aaya hua X-Grab-Id-Userid
    serviceID := GetServiceIDFromHeader(ctx) // JWT se aaya hua X-Grab-Id-Serviceid
    
    if len(userID) == 0 {
        return "", http.StatusBadRequest, &servus.ErrorDetail{...}
    }
    
    // Customer Master API call
    req := &customerAPI.LookupCIFNumberRequest{
        ID: userID,
        Target: &customerAPI.TargetGroup{
            ServiceID: serviceID,
        },
    }
    
    lookUpCifResponse, err := c.LookupCIFNumber(ctx, req)
    if err != nil {
        return "", http.StatusInternalServerError, &servus.ErrorDetail{...}
    }
    
    return lookUpCifResponse.CifNumber, http.StatusOK, nil
}
```

**3. CheckPermissionAccountService() - Permission Verification**
```go
// Location: logic/common/authorization_checker.go:109-121
func CheckPermissionAccountService(ctx context.Context, accountID string, cifNumber string, a accountAPI.AccountService) (string, error) {
    // Account Service se verify karta hai ki CIF number is account ka owner hai ya nahi
    req := &accountAPI.CheckPermissionsForAccountRequest{
        AccountID: accountID, 
        CifNumber: cifNumber
    }
    
    checkPermissionResponse, err := a.CheckPermissionsForAccount(ctx, req)
    if err != nil {
        return "", commonErr.BuildErrorResponse(http.StatusInternalServerError, ...)
    }
    
    return checkPermissionResponse.Status, nil
}
```

### Header Helper Functions
```go
// Location: logic/common/common.go

// GetUserIDFromHeader - JWT se UserID nikalta hai
func GetUserIDFromHeader(ctx context.Context) string {
    return commonCtx.GetUserID(ctx)  // X-Grab-Id-Userid header se
}

// GetServiceIDFromHeader - JWT se ServiceID nikalta hai
func GetServiceIDFromHeader(ctx context.Context) string {
    return commonCtx.GetServiceID(ctx)  // X-Grab-Id-Serviceid header se
}

// GetDeviceIDFromHeader - JWT se DeviceID nikalta hai
func GetDeviceIDFromHeader(ctx context.Context) string {
    return commonCtx.GetDeviceID(ctx)  // X-Grab-Device-ID header se
}
```

---

## 4. Complete Request Flow - Example

### Scenario: User ek term loan simulation request karta hai

```
1. Mobile App (SentryT6)
   ↓ POST /v1/term-loans/simulation-parameters
   ↓ Headers: JWT token with user session
   
2. T6 API Gateway
   ↓ JWT token verify karta hai aur decode karta hai
   ↓ Headers inject karta hai:
       - X-Grab-Id-Userid: "user-123"
       - X-Grab-Id-Serviceid: "sentry-t6"
       - X-Jwt-Session-Data: "..."
   
3. Istio Ingress Gateway
   ↓ mTLS authentication
   ↓ XFCC header inject karta hai:
       - X-Forwarded-Client-Cert: "spiffe://...sentry-t6"
   
4. loan-exp Service
   ↓ servus Middleware (ClientAuthMiddlewareID)
   ↓ VerifyClientIdentity()
   ↓ ✓ "sentry-t6" allowed hai
   
5. servus Context Building
   ↓ injectGatewayHeaders()
   ↓ JWT headers ko context mein inject karta hai
   
6. Handler: GetTermLoanSimulationParameters()
   ↓ common.CheckIfAuthorized(ctx, req.AccountID, ...)
   
7. Authorization Check
   ↓ GetUserIDFromHeader(ctx) → "user-123"
   ↓ LookUpCifFromCustomerMaster() → CIF: "CIF-12345"
   ↓ CheckPermissionAccountService() → Status: "ALLOWED"
   ↓ ✓ Authorized
   
8. Business Logic Execution
   ↓ Loan parameters fetch karta hai
   ↓ Response return karta hai
```

---

## 5. Handler Example - Real Implementation

```go
// File: handlers/loanexp_get_term_loan_simulation_parameters.handler.go:26

func (l *LoanExpService) GetTermLoanSimulationParameters(ctx context.Context, req *api.GetTermLoanSimulationParametersRequest) (*api.GetTermLoanSimulationParametersResponse, error) {
    
    // Step 1: User authorization check
    if _, authErr := common.CheckIfAuthorized(
        ctx, 
        req.AccountID,                  // User ka account ID
        l.CustomerMasterClient,         // CIF lookup ke liye
        l.AccountServiceClient,         // Permission check ke liye
        "",                             // SafeID (optional override)
    ); authErr != nil {
        return nil, authErr  // 401 Unauthorized return karta hai
    }
    
    // Step 2: Ab yahan user authorized hai, business logic chalao
    // ... fetch parameters logic ...
    
    return response, nil
}
```

---

## 6. Testing - Mock Authentication

### Test Helper - Generate Client Identity Headers
```go
// Location: servus/v2/client_auth.go:136-143
func GenerateClientIdentities(server, client servicename.ServiceName, ns ...string) (string, string) {
    selectedNS := defaultNS
    if len(ns) > 0 {
        selectedNS = ns[0]
    }
    headerVal := fmt.Sprintf(identityFmt, selectedNS, server.ToString(), uuid.NewString(), selectedNS, client.ToString())
    return textproto.CanonicalMIMEHeaderKey(xfccparser.ForwardedClientCertHeader), headerVal
}
```

### Test Setup Example
```go
func TestHandler(t *testing.T) {
    ctx := context.Background()
    
    // Service-to-Service auth
    headerKey, headerVal := servus.GenerateClientIdentities(
        servicename.LoanExp,
        servicename.SentryT6,
    )
    ctx = servus.CtxWithHeader(ctx, headerKey, headerVal)
    
    // User authentication
    ctx = commonCtx.WithUserID(ctx, "test-user-123")
    ctx = commonCtx.WithServiceID(ctx, "sentry-t6")
    
    // Test your handler
    response, err := handler.GetTermLoanSimulationParameters(ctx, req)
    assert.NoError(t, err)
}
```

---

## 7. Error Scenarios

### 1. Service Not Allowed
```
Request from unauthorized service
↓ VerifyClientIdentity() fails
↓ Return: 403 Forbidden
{
  "code": "FORBIDDEN",
  "message": "Client identity not allowed"
}
```

### 2. Missing JWT Headers
```
Request without X-Grab-Id-Userid header
↓ GetUserIDFromHeader() returns empty
↓ LookUpCifFromCustomerMaster() fails
↓ Return: 400 Bad Request
{
  "code": "MISSING_USER_ID",
  "message": "User ID is missing"
}
```

### 3. Invalid CIF Mapping
```
Request with invalid user
↓ LookUpCifFromCustomerMaster() fails
↓ Return: 401 Unauthorized
{
  "code": "UNAUTHORIZED_ACCESS",
  "message": "Unauthorized access"
}
```

### 4. Account Permission Denied
```
Request from user who doesn't own the account
↓ CheckPermissionAccountService() returns "FORBIDDEN"
↓ Return: 401 Unauthorized
{
  "code": "UNAUTHORIZED_ACCESS",
  "message": "Unauthorized access"
}
```

---

## 8. Key Configuration Files

### Service Configuration
```json
// File: config_files/service-conf.json
{
  "security": {
    "enabled": true,
    "client_auth": {
      "enabled": true,
      "allowed_services": [
        "sentry-t6",
        "sentry-partner-t6",
        "customer-portal"
      ]
    }
  }
}
```

---

## 9. Important Notes

### Security Best Practices
1. **NEVER disable client authentication** production mein
2. **Always validate** JWT headers context se milne ke baad
3. **Log unauthorized attempts** for security monitoring
4. **Use SafeID parameter** carefully - testing ke liye hi hai

### Performance Considerations
1. CIF lookup aur permission check **synchronous** hai
2. Consider **caching** CIF mappings (with TTL)
3. Monitor **latency** of external service calls

### Dependencies
- **Customer Master Service**: CIF number lookup
- **Account Service**: Permission verification
- **T6 API Gateway**: JWT token generation & validation
- **Istio**: mTLS & XFCC header injection

---

## 10. Summary

loan-exp service mein JWT authentication **multi-layered** hai:

1. **Istio Level**: XFCC + SPIFFE se service-to-service authentication
2. **Gateway Level**: T6 API Gateway JWT tokens ko headers mein inject karta hai
3. **Application Level**: servus middleware headers ko context mein inject karta hai
4. **Handler Level**: CIF-based authorization user permission verify karta hai

**Key Point**: loan-exp service directly JWT tokens **parse nahi karta** - wo sirf T6 API Gateway se aane wale **pre-validated headers** ko trust karta hai aur use karta hai.

---

## References

1. **T6 JWT Documentation**: http://sentry.pages.myteksi.net/t6/t6-fabric/configuration/security/t6_jwt.html
2. **SPIFFE**: https://spiffe.io/
3. **XFCC Header**: https://www.envoyproxy.io/docs/envoy/latest/configuration/http/http_conn_man/headers#x-forwarded-client-cert
4. **Istio mTLS**: https://istio.io/latest/docs/concepts/security/

---

**Generated on**: 2026-05-29  
**Service**: PooreLending/loan-exp  
**Version**: Based on current codebase analysis

