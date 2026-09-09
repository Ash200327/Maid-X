# 07 — UI/UX Specification

## 1. UX principle

The app is primarily a daily attendance tool. The fastest path should be visible immediately after login.

## 2. Navigation

Recommended bottom navigation:

```text
Home | Maids | Payroll | Settings
```

Attendance is primarily accessed through Home and Maid Details rather than becoming a separate top-level tab.

## 3. Screen list

### Authentication
- Splash
- Login
- Register
- Forgot password

### Home
- Dashboard
- Today status
- Quick Entry/Exit
- Monthly salary snapshot

### Maids
- Maid list
- Add maid
- Maid details
- Edit maid
- Employment/salary settings
- Attendance history
- Leave history

### Payroll
- Payroll month selector
- Payroll summary
- Maid payroll detail
- Adjustments
- Finalize confirmation
- Payment confirmation
- Export

### Settings
- Owner profile
- Timezone/currency
- Holiday management
- Account/logout

## 4. Dashboard wireframe-level behavior

```text
--------------------------------
Today, 9 Sep

5 Active Maids

Rani       Working  09:05   [Exit]
Sita       Not started       [Entry]
Pooja      Complete 09:00-17:00
Maya       Incomplete        [Review]
Rekha      On Leave

--------------------------------
September
Salary payable: ₹58,000
[View Payroll]
--------------------------------
```

The actual design should use accessible typography, large touch targets and clear status labels.

## 5. Entry/Exit interaction

Entry:
1. Owner taps Entry.
2. App shows immediate loading state.
3. On success, worker changes to Working.
4. Record timestamp from backend authoritative time.

Exit:
1. Owner taps Exit.
2. App confirms only where accidental taps are plausible; avoid unnecessary dialogs if the control is clearly distinct.
3. On success, worker changes to Completed.

Do not rely solely on the mobile clock for authoritative payroll timestamps.

## 6. Maid detail

Sections:
- Profile
- Today
- Attendance calendar/list
- Employment & Salary
- Leave
- Payroll summary

## 7. Employment form

Fields:
- Joining date
- Leaving date (optional)
- Salary mode selector
- Salary amount
- Expected hours/day
- Working weekdays
- Shortfall threshold
- Overtime enabled
- Overtime multiplier
- Effective from

Show contextual examples based on selected salary mode.

## 8. Attendance history

Date rows should show:
- Status
- Sessions
- Worked duration
- Warning/conflict indicator
- Edit action

Multiple sessions should be visually grouped under one date.

## 9. Payroll detail

Use a transparent breakdown:

```text
Base earnings                         ₹15,000
Shortfall deduction                    -₹144
Overtime                              +₹0
Paid leave                             included
Advance                              -₹1,000
Bonus                                 +₹500
-------------------------------------------
Final payable                        ₹14,356
```

Also expose expected vs actual minutes and a day-by-day exception list.

## 10. Payroll finalization

Finalization modal:
- Month
- Maid
- Current amount
- Unresolved attendance warnings
- Adjustments summary
- Explicit "Finalize payroll" action

After finalization, show that the result is locked and provide a controlled correction path if implemented.

## 11. Payment screen

- Status: Unpaid/Paid
- Payment date
- Payment method: Cash / UPI / Bank / Other
- Note

## 12. Loading/error/empty states

Every API-driven screen needs:
- Skeleton/loading state where appropriate.
- Retry action for network failures.
- Empty state when no maids/payroll records exist.
- Inline field validation.
- Non-destructive toast/banner for successful edits.

Never show a blank screen after a failed API request.

## 13. Offline/network handling

MVP can remain online-first. The mobile app should cache recent read data but must not pretend an attendance write succeeded while offline.

When offline during an attendance action:
- Show clear offline state.
- Do not silently queue unless an explicit offline queue is implemented.
- Offer retry.

Architecture should leave room for a later durable offline command queue.

## 14. Accessibility

- Large touch targets.
- Do not encode status by color alone.
- Good contrast.
- Screen-reader labels for Entry/Exit buttons.
- Clear date/time formats.
- Avoid dense tables on phone width.
