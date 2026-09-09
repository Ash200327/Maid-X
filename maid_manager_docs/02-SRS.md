# 02 — Software Requirements Specification

## 1. Functional requirements

### FR-001 Authentication
The system shall allow owner registration, login and logout. Passwords shall be stored only as secure password hashes.

### FR-002 Tenant isolation
Every owner-owned resource shall be associated with an owner/tenant identifier. Every read/write operation shall enforce owner authorization at the service boundary.

### FR-003 Maid management
The system shall support CRUD-like management with archive/deactivate rather than destructive removal where historical payroll depends on the record.

### FR-004 Employment configuration
A maid shall have versioned salary/work schedule configuration with effective dates.

### FR-005 Attendance
A maid may have zero or more attendance sessions for a business date. Each session has optional entry and exit timestamps.

### FR-006 Attendance state
Derived states include NOT_STARTED, WORKING, COMPLETED, INCOMPLETE, ABSENT, LEAVE and HOLIDAY/WEEKLY_OFF as applicable.

### FR-007 Leave
Leave record shall support PAID and UNPAID types.

### FR-008 Holidays
Owner-managed holidays shall be stored independently of attendance and evaluated during payroll calculation.

### FR-009 Payroll
The system shall calculate payroll deterministically from the effective employment configuration, schedule, calendar, attendance, leave, holidays and adjustments.

### FR-010 Payroll snapshot
Finalization shall persist a snapshot of the calculation inputs/results needed to reproduce the payroll result.

### FR-011 Payments
Payroll can be marked paid with payment date, method and optional note.

### FR-012 Export
Selected-month payroll can be exported to CSV and PDF.

## 2. Non-functional requirements

### Performance
- Dashboard API should be optimized for one owner and its active maids.
- Attendance entry/exit endpoint should be lightweight and idempotent.
- Monthly payroll calculation should be fast enough for a normal household scale without background jobs in MVP.

### Reliability
- No silent loss of attendance on API failure.
- Retries must not create duplicate sessions or duplicate exits.
- Database transactions must protect state transitions.

### Security
- TLS in production.
- Password hashing using a modern adaptive algorithm.
- Short-lived access token and rotating/revocable refresh tokens.
- Authorization checks on every owner-owned resource.
- No sensitive data in logs.
- Validate and sanitize user input.
- Never trust client-provided owner IDs.

### Financial integrity
- Java BigDecimal for money.
- Integer minutes for durations.
- Explicit rounding mode documented in salary rules.
- Finalized payroll must be immutable except through an explicit correction/reopening workflow.

## 3. Core validation rules

- Worker name is required.
- Salary must be positive unless a special zero-paid configuration is intentionally supported; MVP recommendation: > 0.
- Expected working hours/day must be greater than zero for salary modes that depend on hours.
- Working weekdays must contain at least one day.
- Overtime multiplier must be >= 1.0 when overtime is enabled.
- Shortfall threshold must be between 0 and expected minutes/day.
- Exit timestamp cannot precede its entry timestamp for the same session unless an explicit overnight shift policy is enabled.
- An exit cannot be recorded when no open session exists.
- Duplicate client requests must not create duplicate state transitions.
- Joining date cannot be after leaving date.
- Leave and manual attendance states must follow the documented conflict matrix.

## 4. State and conflict principles

Attendance data is source data. Payroll is a derived financial result. Editing attendance does not mutate a finalized payroll snapshot automatically.

When a draft payroll is recalculated, current valid source data is used.

When finalized, the snapshot is authoritative until explicitly corrected/reopened.

## 5. Auditing

At minimum audit:
- Attendance create/update/delete by owner.
- Maid employment configuration changes.
- Payroll adjustments.
- Payroll finalization/reopening.
- Payment status changes.
