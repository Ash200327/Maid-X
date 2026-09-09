# 05 — REST API Specification

Base URL:

```text
/api/v1
```

JSON content type. HTTPS in production.

## 1. Standard error shape

```json
{
  "code": "VALIDATION_ERROR",
  "message": "One or more fields are invalid.",
  "fieldErrors": {
    "salaryAmount": "Must be greater than 0"
  },
  "requestId": "uuid"
}
```

Never return stack traces to the mobile client.

## 2. Authentication

### POST /auth/register
Auth: public

Request:
```json
{"name":"Ash","email":"owner@example.com","password":"..."}
```

Response 201: owner profile + token/session response.

### POST /auth/login
Auth: public

Request: credentials.

Response 200: access token + refresh token/session metadata.

### POST /auth/refresh
Auth: refresh token/session.

### POST /auth/logout
Auth: authenticated.

## 3. Owner

### GET /me
Returns authenticated owner profile.

### PATCH /me
Updates profile/timezone/currency.

## 4. Maids

### GET /maids
Query:
- active=true|false
- search
- page
- size

### POST /maids
Request fields:
- name
- phone
- joiningDate
- notes
- initial employment configuration

### GET /maids/{maidId}
Returns maid + active employment config + schedule.

### PATCH /maids/{maidId}
Updates profile fields.

### POST /maids/{maidId}/archive
Archives/deactivates worker.

### POST /maids/{maidId}/employment-configs
Creates a new effective-dated configuration.

### GET /maids/{maidId}/employment-configs
Returns configuration history.

## 5. Attendance

### GET /maids/{maidId}/attendance
Query:
- from
- to

### POST /maids/{maidId}/attendance/sessions/entry
Creates a new open session for the resolved business date.

Optional request:
```json
{"businessDate":"2026-09-09","clientRequestId":"uuid"}
```

The backend validates the date against timezone and ownership policy. Client should not be able to create a session for arbitrary owners.

### POST /maids/{maidId}/attendance/sessions/{sessionId}/exit
Closes an open session.

### PATCH /maids/{maidId}/attendance/sessions/{sessionId}
Owner-only manual edit.

### DELETE /maids/{maidId}/attendance/sessions/{sessionId}
Owner-only. Should require confirmation in UI and may be blocked once linked to a finalized payroll unless a reopening policy is used.

### POST /maids/{maidId}/attendance/manual-status
Request:
```json
{"businessDate":"2026-09-09","status":"ABSENT","note":"Sick"}
```

Conflict rules must prevent contradictory states with existing open sessions.

## 6. Leave

### GET /maids/{maidId}/leave
Query from/to.

### POST /maids/{maidId}/leave
Request:
```json
{"leaveDate":"2026-09-11","type":"PAID","note":"Personal"}
```

### PATCH /maids/{maidId}/leave/{leaveId}

### DELETE /maids/{maidId}/leave/{leaveId}

## 7. Holidays

### GET /holidays
Query month/year.

### POST /holidays
Request:
```json
{"holidayDate":"2026-10-02","name":"Gandhi Jayanti"}
```

### PATCH /holidays/{holidayId}
### DELETE /holidays/{holidayId}

## 8. Dashboard

### GET /dashboard
Query: date optional.

Response example:
```json
{
  "date":"2026-09-09",
  "activeMaidCount":5,
  "entries":[
    {"maidId":"uuid","maidName":"Rani","state":"WORKING","entryAt":"2026-09-09T03:30:00Z"}
  ],
  "currentMonth": {
    "salaryPayable": 58000.00,
    "currency":"INR"
  }
}
```

## 9. Payroll

### POST /payroll/runs/calculate
Request:
```json
{"maidId":"uuid","month":"2026-09"}
```

Returns calculation without changing finalized source data.

### GET /payroll/runs
Query month, status.

### GET /payroll/runs/{payrollRunId}
Returns detailed breakdown.

### POST /payroll/runs/{payrollRunId}/adjustments
Adds advance/bonus/deduction/addition.

### POST /payroll/runs/{payrollRunId}/finalize
Persists snapshot and changes DRAFT to FINALIZED.

### POST /payroll/runs/{payrollRunId}/mark-paid
Request:
```json
{"paidAt":"2026-10-01","paymentMethod":"UPI","paymentNote":"Paid"}
```

## 10. Reports

### GET /reports/monthly
Query month.
Returns owner summary + per-maid breakdown.

### GET /reports/monthly.csv
Streams CSV.

### GET /reports/monthly.pdf
Generates PDF.

## 11. Common status codes

- 200 OK
- 201 Created
- 204 No Content
- 400 Validation/business request error
- 401 Unauthenticated
- 403 Authenticated but not allowed
- 404 Resource not accessible/found
- 409 State conflict or uniqueness conflict
- 422 Optional semantic validation if the backend standard chooses it
- 429 Rate limited
- 500 Unexpected server error

## 12. Idempotency

Attendance entry/exit and payment-changing requests should accept a client request ID or Idempotency-Key. The backend stores a bounded request result or uses a transaction-safe unique key to make repeated submissions harmless.
