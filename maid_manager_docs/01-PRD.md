# 01 — Product Requirements Document

## 1. Product vision

A simple mobile-first household workforce manager that helps an owner record daily attendance and reliably calculate, review, finalize and track monthly pay for domestic maids/workers.

## 2. Problem

Owners often track attendance and salary mentally, in notebooks, chats or spreadsheets. This makes mistakes likely when workers have different salaries, schedules, absences, leave, overtime, partial workdays or salary changes.

## 3. Goals

- Record entry/exit in seconds.
- Manage multiple maids under one owner account.
- Support monthly, daily and hourly salary modes per maid.
- Apply configurable attendance, leave, holiday and overtime rules consistently.
- Make salary calculations explainable.
- Preserve historical payroll results.
- Prevent cross-owner data access.
- Provide monthly reports and PDF/CSV export.

## 4. Non-goals for MVP

- Maid-facing app/account.
- Government payroll, tax, PF/ESI or statutory payroll processing.
- Automatic India holiday calendars.
- GPS, geofencing, biometrics or facial recognition.
- Complex shift planning.
- Chat/messaging.
- Multi-level organization/employee permissions.
- Full accounting integration.

## 5. Primary persona

### Household owner/employer

Needs a fast, low-friction way to record attendance and know exactly what to pay each worker.

## 6. Key user journeys

### Add worker
Login → Add Maid → enter profile → configure working weekdays/hours → choose salary mode → save.

### Start work
Dashboard → tap Entry for worker → backend creates a session for today's business date.

### End work
Dashboard → tap Exit → backend closes the active session.

### Correct mistake
Maid → Attendance → select date/session → Edit → save. System writes audit event.

### Month-end payroll
Reports → select month → review per-maid calculation → review adjustments → finalize → optionally mark paid → export PDF/CSV.

## 7. MVP features

### Authentication
- Register
- Login
- Logout
- Access/refresh token strategy
- Password hashing
- Password reset flow

### Owner profile
- View/edit basic profile
- Timezone configuration
- Currency configuration, default INR for India deployment

### Maid management
- Add/edit/view
- Archive/deactivate
- Search/filter
- Active/inactive status
- Joining/leaving date
- Notes

### Employment configuration
- Salary mode: Monthly/Daily/Hourly
- Salary amount
- Expected hours/day
- Working weekdays
- Shortfall threshold
- Overtime enabled + multiplier
- Effective date

### Attendance
- Multiple sessions/day
- Entry
- Exit
- Edit
- Delete
- Absent
- Paid leave
- Unpaid leave
- Holiday/weekly off interpretation
- Incomplete attendance state
- Attendance notes

### Holiday management
- Create/edit/delete manual holidays
- Holiday date
- Name
- Optional paid/unpaid classification if the salary policy needs it

### Payroll
- Calculate selected month
- Detailed line items
- Shortfall deduction
- Overtime
- Advances
- Other additions/deductions
- Proration for join/leave
- Draft/finalized/paid lifecycle
- Historical snapshot

### Reports
- Owner monthly summary
- Per-maid details
- PDF export
- CSV export

### Audit
- Manual attendance edits
- Payroll finalization
- Payroll payment changes
- Important configuration changes

## 8. Success metrics

- Median time to record an attendance event: < 5 seconds after opening the relevant action.
- Monthly payroll recalculation matches expected test matrix 100%.
- No authorized cross-owner data access in security tests.
- Crash-free critical attendance flow target suitable for production monitoring.
- Low correction rate for newly entered attendance.

## 9. Acceptance criteria

The product is MVP-complete when an owner can:

1. Create an account and log in securely.
2. Add multiple workers with independent configurations.
3. Record multiple attendance sessions on the same business date.
4. Correct attendance and see audit history.
5. Configure monthly, daily or hourly salary mode per maid.
6. Calculate pay using explicit documented rules.
7. Add paid/unpaid leave, holidays and adjustments.
8. Review a monthly payroll result before finalization.
9. Finalize and later mark payroll paid.
10. Export the monthly report as CSV and PDF.
11. Never access another owner's worker or payroll data.
