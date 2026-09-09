# 11 — AI Coding Guidelines

## 1. General rule

Do not generate the entire application in one pass. Implement the documented roadmap phase-by-phase.

Before changing code:
- inspect repository structure
- inspect existing module boundaries
- read relevant tests
- identify affected migrations/API contracts

## 2. Architecture rules

- Keep backend as a modular monolith.
- Controllers handle transport concerns.
- Services/use-cases handle business logic.
- Salary calculation lives in payroll/domain services, not controllers.
- Repository queries enforce ownership constraints.
- DTOs separate API models from persistence entities where appropriate.

## 3. Database rules

- Flyway only for migrations.
- Never rewrite an applied migration.
- Add indexes based on access patterns.
- Preserve historical payroll data.
- Do not use floating-point types for money.

## 4. Security rules

- Never trust client ownerId.
- Derive authenticated owner from security context.
- Check parent ownership before child resource access.
- Hash passwords securely.
- Do not log credentials/tokens.
- Validate every external input.
- Keep secrets out of source control.

## 5. API contract rules

- Preserve /api/v1 contracts after implementation unless a documented version change is approved.
- Use stable response/error shapes.
- Use pagination where list size can grow.
- Return clear business conflict errors.

## 6. Attendance implementation rules

- Backend owns authoritative timestamps.
- Business date is timezone-aware.
- Entry/exit transitions are transactional.
- Repeated network submissions must be idempotent.
- Multiple sessions/day are intentional.
- Manual edits produce audit records.

## 7. Salary engine rules

- Implement pure/deterministic calculation where possible.
- Inputs must be explicit in a calculation context.
- Do not query random application state from inside the core calculation logic.
- Use BigDecimal.
- Keep duration arithmetic in integer minutes.
- Write tests before modifying complicated payroll rules.
- Do not silently invent new salary behavior.

## 8. UI rules

- Optimize the dashboard for one-tap attendance.
- Use large touch targets.
- Avoid unnecessary navigation.
- Every network mutation needs loading/success/failure state.
- Never show an optimistic attendance success unless the backend confirms success, unless a durable offline queue is later introduced.

## 9. Ambiguity handling

When requirements are ambiguous:

1. Check the documentation first.
2. Check existing tests/accepted decisions.
3. Prefer the smallest behavior consistent with the documented product.
4. If ambiguity materially changes financial/security behavior, stop implementation and record an explicit decision request.
5. Never silently change a business rule because it seems convenient.

## 10. Avoid breaking functionality

Before a change:
- identify impacted endpoints/screens/tests
- make a focused change
- run relevant tests
- run regression tests

Do not perform unrelated refactors during feature work.

## 11. Dependency rules

Before adding a library:
- verify the project does not already solve the problem
- prefer stable, well-supported dependencies
- minimize dependencies
- document the reason for significant additions

## 12. Testing rules

Every new business rule gets tests.

Every security-sensitive endpoint gets authorization tests.

Every migration gets an integration path.

Payroll changes must include regression tests against the salary matrix.

## 13. Documentation rules

When behavior changes:
- update API docs
- update salary rules if financial behavior changed
- update schema docs for DB changes
- update tests
- update changelog/release notes as appropriate

## 14. Agent output format

After each implementation task, return:

```text
Implemented:
- ...

Files changed:
- ...

Tests:
- ...

Known limitations:
- ...

Next task:
- ...
```

## 15. Definition of done

A feature is not done merely because it compiles.

It is done when:
- implementation matches the docs
- validation exists
- error states exist
- authorization is covered
- tests pass
- relevant documentation is updated
- no unrelated behavior was broken
