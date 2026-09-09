# 08 — Test Plan

## 1. Test strategy

The most valuable test coverage is the salary engine and the tenant/security boundary.

Layers:
- Unit tests
- Service tests
- Repository/integration tests
- Controller/API tests
- Security/authorization tests
- Mobile component/flow tests
- End-to-end critical path tests where practical

## 2. Salary test matrix

| Case | Expected result |
|---|---|
| Full monthly attendance | Full configured base salary subject to other rules |
| One normal short day above threshold | No shortfall deduction |
| Short day below threshold | Deduction applied |
| Exact threshold | No shortfall deduction |
| Zero attendance on expected day | Absence/unpaid treatment according to mode |
| Paid leave | Paid day; no overtime from leave |
| Unpaid leave | No base pay for that day |
| Weekly off | No absence deduction |
| Manual holiday | Excluded from expected workdays |
| Overtime OFF | No overtime pay |
| Overtime ON | Overtime pay using multiplier |
| Monthly mode | Monthly calculation correct |
| Daily mode | Daily calculation correct |
| Hourly mode | Hour-based calculation correct |
| Join mid-month | Expected-working-day proration |
| Leave mid-month | Correct eligible-day treatment |
| Leaving mid-month | Correct proration |
| Salary config changes mid-month | Split-period calculation |
| Multiple sessions | Sum minutes correctly |
| Incomplete session | Blocking warning / unresolved exception |
| Duplicate submission | Idempotent; no duplicate state |
| Decimal salary | Exact BigDecimal result and final 2-decimal rounding |
| Advance | Correct subtraction |
| Bonus | Correct addition |
| Finalization | Snapshot preserved |
| Source edit after finalization | Final snapshot unchanged until controlled correction |

## 3. Attendance edge cases

- Exit before entry rejected.
- Exit without open entry rejected.
- Duplicate exit rejected.
- Multiple sessions allowed only when the user intentionally creates a new session.
- Same timestamp entry/exit is either rejected or treated as 0 minutes; MVP recommendation: allow but flag zero duration for correction rather than silently creating paid time.
- Very long duration should be validated against a configurable maximum sanity threshold for ordinary shifts.
- Overnight sessions rejected unless enabled by explicit policy.
- Network retry does not duplicate session.

## 4. Security tests

### Critical
- User A cannot GET User B maid.
- User A cannot PATCH User B attendance.
- User A cannot calculate User B payroll by ID.
- User A cannot export User B report.
- Crafted IDs cannot bypass repository/service authorization.
- Refresh token for another account is rejected.
- Archived maid cannot be mutated through stale client state without explicit policy.

## 5. API tests

Validate:
- HTTP status code.
- Response contract.
- Validation failures.
- Error codes.
- Authentication behavior.
- Authorization behavior.
- Idempotency behavior.

## 6. Mobile tests

Critical flows:
- Register/login.
- Add maid.
- Entry.
- Exit.
- Multiple sessions.
- Edit attendance.
- Leave creation.
- Monthly payroll review.
- Finalize.
- Mark paid.
- Export.
- Network failure retry.

## 7. Definition of test completeness

No payroll rule should be considered complete until its normal path, boundary values and at least one conflict/error case are tested.
