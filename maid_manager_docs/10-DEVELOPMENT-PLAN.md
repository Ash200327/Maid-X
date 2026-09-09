# 10 — Development Plan

## Phase 0 — Requirements lock

Objective: eliminate business ambiguity.

Tasks:
- Freeze salary modes/rules.
- Freeze leave/holiday semantics.
- Freeze attendance state model.
- Freeze payroll lifecycle.
- Review all docs together.

Definition of done:
- No unresolved business rule exists in the implementation contract.

## Phase 1 — Repository foundation

Tasks:
- Create monorepo folders.
- Spring Boot project.
- Expo TypeScript project.
- Shared docs conventions.
- Environment templates.
- CI baseline.

Tests:
- Backend starts.
- Mobile app starts.

## Phase 2 — Database and persistence

Tasks:
- Flyway migrations.
- Entities and repositories.
- Base auditing timestamps.
- Constraints/indexes.

Tests:
- Migration integration test.
- Constraint tests.

## Phase 3 — Authentication/security

Tasks:
- Registration/login/logout/refresh.
- Password hashing.
- Secure token storage mobile-side.
- Owner authorization.

Definition of done:
- Cross-owner access tests fail safely.

## Phase 4 — Maid and employment management

Tasks:
- Maid CRUD/archive.
- Employment configuration.
- Working weekday selection.
- Effective-dated history.

## Phase 5 — Attendance

Tasks:
- Entry/exit.
- Multiple sessions.
- Manual edit/delete.
- Attendance statuses.
- Idempotency.
- Audit events.

Definition of done:
- Common daily workflow works with one to several workers.

## Phase 6 — Leave and holidays

Tasks:
- Paid/unpaid leave.
- Manual holidays.
- Conflict validation.

## Phase 7 — Salary engine

Tasks:
- Calculation context.
- Monthly strategy.
- Daily strategy.
- Hourly strategy.
- Threshold rule.
- Overtime rule.
- Proration.
- Adjustments.
- Rounding.
- Snapshot.

Definition of done:
- Full salary test matrix passes.

## Phase 8 — Payroll UI and reports

Tasks:
- Month selection.
- Detail breakdown.
- Review/finalize.
- Payment status.
- CSV/PDF export.

## Phase 9 — Mobile integration and UX hardening

Tasks:
- Dashboard quick actions.
- Error/loading/empty states.
- Navigation.
- Accessibility basics.
- Network retry behavior.

## Phase 10 — Security/performance/reliability

Tasks:
- Pen-test-style authorization checks.
- Input validation review.
- Query/index review.
- Idempotency review.
- Logging review.
- Crash/error monitoring.

## Phase 11 — Release

Tasks:
- Neon production DB.
- Render production backend.
- EAS production build.
- Smoke tests.
- Backup/recovery confirmation.
- Documentation finalization.

## Recommended AI-agent execution format

Each phase should be implemented as small PR-sized tasks:

```text
Task → inspect existing code → implement → tests → run tests → summarize changed files → stop
```

An AI coding agent should not silently jump to another phase.
