# Maid & Worker Salary Manager — Documentation Blueprint

## Purpose

This documentation set is the implementation blueprint for a production-ready mobile application that lets an owner/employer manage domestic maids/workers, record attendance, and calculate salary.

## Core product model

Owner → many maids/workers → employment configuration + attendance + leave + holidays + payroll.

The owner is the primary application user. Maids do not need an application account in MVP.

## Finalized decisions

1. Each maid can use a salary calculation mode chosen by the owner:
   - Monthly
   - Daily
   - Hourly
2. Short working time is deducted only when shortage crosses a configurable threshold. The product should support a threshold such as 7h 30m for an 8-hour schedule.
3. Overtime is configurable per maid: ON/OFF plus multiplier. Default: OFF.
4. Working weekdays are decided by the owner for each maid.
5. Leave supports Paid and Unpaid types.
6. Holidays are manually managed by the owner for MVP.
7. Mid-month join/leave proration uses expected working days.
8. Attendance stores a local business date plus entry/exit timestamps.
9. Multiple attendance sessions per business day are supported.
10. The owner can edit attendance at any time. Manual edits create audit records.
11. Monthly salary reports can be exported as CSV and PDF.
12. Monthly payroll supports Draft → Finalized → Paid lifecycle.
13. Advances and additions/deductions are supported as payroll adjustments.
14. Salary/employment settings are versioned with effective dates so historical calculations remain correct.

## Documentation set

- 01-PRD.md — Product Requirements Document
- 02-SRS.md — Software Requirements Specification
- 03-ARCHITECTURE.md — System and module architecture
- 04-DATABASE.md — PostgreSQL database design and Mermaid ERD
- 05-API-SPEC.md — Versioned REST API
- 06-SALARY-RULES.md — Deterministic payroll rules
- 07-UI-UX.md — Mobile UX, navigation, screens and states
- 08-TEST-PLAN.md — Test strategy and cases
- 09-DEPLOYMENT.md — Local and production deployment
- 10-DEVELOPMENT-PLAN.md — Incremental implementation roadmap
- 11-AI-CODING-GUIDELINES.md — Rules for an AI coding agent

## MVP principles

- Fastest possible daily attendance workflow.
- Simple owner-centric UX.
- Strict owner data isolation.
- Financial calculations are deterministic, auditable and reproducible.
- No hidden business rules.
- No premature microservices or complex infrastructure.
- Keep future extensibility at domain boundaries.
