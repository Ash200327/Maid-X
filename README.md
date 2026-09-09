# Maid & Worker Salary Manager (Maid-X)

A production-ready mobile application and backend API for household employers to manage domestic workers, record attendance, and compute deterministic monthly payroll.

## Monorepo Structure

```text
/
  backend/           # Spring Boot 3.3.4 (Java 21) REST API + Flyway + PostgreSQL
  mobile/            # React Native (Expo SDK 57, TypeScript) mobile application
  maid_manager_docs/ # Architecture, SRS, PRD, DB, and API documentation
  scripts/           # Automation and deployment scripts
  .github/workflows/ # CI/CD workflows
```

## Quickstart

### Backend
1. Requires JDK 21 and Maven 3.9+.
2. Configure `backend/.env` based on `backend/.env.example`.
3. Run tests:
   ```bash
   cd backend
   mvn test
   ```
4. Start local development server:
   ```bash
   mvn spring-boot:run
   ```

### Mobile
1. Requires Node.js 20+ and npm.
2. Install dependencies:
   ```bash
   cd mobile
   npm install
   ```
3. Start Expo development server:
   ```bash
   npm start
   ```

## Development Plan
Refer to `maid_manager_docs/10-DEVELOPMENT-PLAN.md` for phase-by-phase implementation progress and `11-AI-CODING-GUIDELINES.md` for engineering standards.
