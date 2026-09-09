# 06 — Salary Rules and Calculation Engine

## 1. Business vocabulary

- Expected working day: a date that is in the maid's configured working weekdays and is not excluded by joining/leaving bounds.
- Expected minutes: expected hours/day × 60.
- Actual worked minutes: sum of completed attendance session durations for the business date.
- Shortfall: expected minutes − actual minutes when actual is lower.
- Overtime: actual minutes above expected minutes, subject to overtime rules.
- Paid leave: excluded from absence/shortfall deductions and counts toward expected pay treatment.
- Unpaid leave: excluded from worked time and may reduce earnings according to salary mode.

## 2. Salary modes

### Monthly
Base monthly salary is the configured amount for a full eligible month. Join/leave proration uses expected working days.

For a partial month:

```text
Prorated base = Monthly salary × Eligible expected working days / Full-month expected working days
```

Shortfall and overtime are then applied according to the worker's configuration.

### Daily
Daily base rate is the configured daily salary amount. Pay is based on eligible expected working days and leave/pay policy.

Recommended MVP interpretation:
- worked expected day => daily amount
- paid leave => daily amount
- unpaid leave => 0
- weekly off/holiday => no deduction
- shortfall deduction applies if the configured threshold is crossed.

### Hourly
Base rate is the configured hourly amount. Actual payable minutes are determined by the attendance/leave/threshold policy.

## 3. Working calendar

Expected working dates are derived from:

1. Month boundaries.
2. Joining date.
3. Leaving date if present.
4. Owner-selected working weekdays.
5. Manual holidays.
6. Leave records.

A weekly off or holiday is not treated as an absence merely because no attendance exists.

## 4. Shortfall threshold

The owner configures a threshold per employment configuration.

For an 8-hour expected day with threshold 7h30m:

- Actual >= 7h30m → no shortfall deduction for that day.
- Actual < 7h30m → shortage is chargeable/deductible according to the selected salary mode.

This means the threshold acts as a grace floor, not as a partial-credit threshold.

Recommended calculation:

```text
Chargeable shortfall = expected minutes - actual minutes
```

when actual is below the threshold.

The engine must expose both raw shortfall and whether the threshold was crossed.

## 5. Overtime

Per maid:
- enabled: boolean
- multiplier: decimal >= 1.0

Default: disabled.

For an expected 8h day and 10h actual:

```text
Overtime = 120 minutes
Overtime pay = overtime hours × base hourly equivalent × multiplier
```

For monthly/daily modes, calculate an effective hourly equivalent using the configured salary and full-month expected working time relevant to the payroll period.

For hourly mode, use the configured hourly rate.

## 6. Leave

### Paid leave
The expected-day entitlement remains paid. Leave hours are not counted as worked hours for overtime.

### Unpaid leave
No base pay is earned for that day under the daily/monthly proration portion. It does not become a shortfall merely because there is no attendance.

## 7. Public holidays

Manual owner-defined holidays are excluded from expected working days for payroll by default.

The owner can manage the holiday list. Automatic government holiday synchronization is not part of MVP.

## 8. Weekly off

Owner chooses working weekdays. Non-working weekdays are expected-off days and do not generate absence deductions.

## 9. Joining/leaving mid-month

Proration uses expected working days.

Example:

```text
Full-month expected working days = 26
Eligible expected days after joining/leaving bounds = 18
Monthly salary = ₹15,000
Prorated base = 15,000 × 18 / 26
```

For a daily salary configuration, the eligible expected-day count determines base earnings directly.

## 10. Salary/configuration changes during month

Configuration is effective-dated. The payroll engine splits the month into configuration periods and calculates each period independently.

Example:

```text
1–14 Sep: ₹15,000/month config
15–30 Sep: ₹18,000/month config
```

Each period uses its own expected minutes, weekdays and threshold.

## 11. Multiple sessions per day

Actual worked minutes are the sum of completed sessions.

Example:

```text
09:00–13:00 = 240 min
16:00–18:00 = 120 min
Total = 360 min = 6h
```

Overnight sessions are supported at the timestamp level; the business-date assignment must follow the configured attendance policy. MVP should allow overnight sessions only if the owner explicitly enables an overnight setting. Otherwise reject a session crossing midnight.

## 12. Missing/incomplete attendance

- Entry without exit → INCOMPLETE and excluded from finalized salary calculation unless manually corrected/closed.
- Exit without open entry → reject.
- Duplicate entry for an open session → reject.
- Duplicate entry after completing one session → allowed as a new session only through explicit user action.
- Exit before entry → reject.

For draft payroll, an incomplete session should be surfaced as a blocking warning or unresolved exception rather than silently assuming zero or full hours.

## 13. Attendance vs leave/holiday conflicts

Recommended precedence:

1. Explicit manual correction.
2. Attendance sessions.
3. Leave.
4. Holiday/weekly-off classification.
5. Ordinary expected workday.

If attendance exists on a leave day, the payroll engine should flag a conflict. MVP policy: attendance wins for worked-time calculation; leave remains visible as a conflict requiring owner review.

## 14. Adjustments

A payroll run can include:
- ADVANCE — subtraction from final payable.
- BONUS — addition.
- DEDUCTION — subtraction.
- OTHER_ADD — addition.
- OTHER_DEDUCTION — subtraction.

Each adjustment requires amount, date and optional reason.

## 15. Rounding

Durations: integer minutes; do not round each session prematurely.

Money: calculate using BigDecimal. Recommended final currency rounding to 2 decimal places with HALF_UP. Keep full precision internally until final currency rounding.

## 16. Illustrative calculation

For monthly mode:

```text
Base prorated earnings
- shortfall deductions
+ overtime pay
+ bonuses/additions
- advances
- deductions
= final payable
```

The detailed result should expose every component instead of only returning one total.

## 17. Result contract

```json
{
  "month":"2026-09",
  "salaryMode":"MONTHLY",
  "monthlySalary":15000.00,
  "expectedWorkingDays":24,
  "eligibleWorkingDays":24,
  "expectedMinutes":11520,
  "actualWorkedMinutes":11400,
  "shortfallMinutes":120,
  "thresholdCrossedDays":1,
  "overtimeMinutes":0,
  "baseEarnings":15000.00,
  "shortfallDeduction":144.23,
  "overtimePay":0.00,
  "additions":500.00,
  "deductions":1000.00,
  "finalPayable":14355.77,
  "currency":"INR"
}
```

The numbers are illustrative only.

## 18. Payroll lifecycle

```text
DRAFT -> FINALIZED -> PAID
```

A finalized run stores a snapshot. Editing source records does not silently mutate the final amount.

A correction workflow may later be implemented:

```text
PAID -> REOPENED/CORRECTION -> DRAFT -> FINALIZED -> PAID
```

That is a controlled administrative action, not an automatic side effect.
