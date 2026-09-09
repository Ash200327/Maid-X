# Maid-X: Household Workforce & Salary Manager

A production-grade, owner-centric workforce management mobile app and Spring Boot REST API for tracking household maids and domestic workers, attendance sessions, leaves, and deterministic monthly payroll calculations.

---

## 🏛 Architecture & Tech Stack

- **Backend:** Spring Boot 3.3.4 (Java 21), Spring Security 6 (Stateless JWT + Refresh Token Rotation), Spring Data JPA, PostgreSQL (Neon Cloud / Local), Flyway Database Migrations, SLF4J + MDC Request Correlation.
- **Mobile:** React Native (Expo SDK 57, React 19, TypeScript), SecureStore token persistence, accessible typography & semantic design system tokens.
- **Production Infrastructure:**
  - **Database:** Neon PostgreSQL with connection pooling & automated point-in-time recovery.
  - **Backend Hosting:** Render Web Service (Multi-stage Docker container build, health check at `/actuator/health`).
  - **Mobile Distribution:** Expo Application Services (EAS) Build for Android (`.apk`/`.aab`) and iOS (`.ipa`).

---

## 📁 Repository Layout

```text
/
├── backend/                       # Spring Boot 3.3.4 (Java 21) REST API
│   ├── src/main/java/             # Domain modules: auth, maid, employment, attendance, leave, holiday, payroll
│   ├── src/main/resources/        # application.yml & Flyway migrations (V1 - V5)
│   ├── src/test/java/             # 53 Automated unit, integration, and security pen-tests
│   └── Dockerfile                 # Multi-stage production container
├── mobile/                        # Expo / React Native TypeScript mobile client
│   ├── src/components/            # Accessible UX feedback states (Loading, Error/Retry, EmptyState, Toast)
│   ├── src/context/               # AuthContext & storage hooks
│   ├── src/screens/               # Auth, Dashboard, MaidList, AddMaid, MaidDetail, Payroll, Settings
│   ├── src/services/              # Strongly-typed API client services (Auth, Maids, Attendance, Payroll, etc.)
│   ├── app.json & eas.json        # Expo & EAS Build release configuration
├── maid_manager_docs/             # Complete specification suite (PRD, SRS, Architecture, Salary Rules, etc.)
├── render.yaml                    # Render Infrastructure-as-Code deployment blueprint
└── README.md                      # Project documentation
```

---

## 🚀 Quickstart & Local Setup

### 1. Prerequisites
- **Java 21** (OpenJDK or Eclipse Temurin)
- **Node.js 20+** and npm
- **Maven 3.9+**
- **PostgreSQL 15+** (Local or Neon development database)

### 2. Backend Setup
1. Configure environment variables in `backend/.env` or export them:
   ```bash
   export SPRING_PROFILES_ACTIVE=local
   export DATABASE_URL=jdbc:postgresql://localhost:5432/maid_manager
   export DATABASE_USERNAME=postgres
   export DATABASE_PASSWORD=postgres
   export JWT_ACCESS_SECRET=your-256-bit-access-secret-minimum-32-chars!
   export JWT_REFRESH_SECRET=your-256-bit-refresh-secret-minimum-32-chars!
   ```
2. Run database migrations & all tests:
   ```bash
   cd backend
   mvn test
   ```
3. Start the backend:
   ```bash
   mvn spring-boot:run
   ```
   The API will be available at `http://localhost:8080/api/v1` and health check at `http://localhost:8080/actuator/health`.

### 3. Mobile Setup
1. Install dependencies:
   ```bash
   cd mobile
   npm install
   ```
2. Start the Expo development server:
   ```bash
   npx expo start
   ```

---

## 🧪 Verification & Test Suite

The project includes an end-to-end automated test suite with **53 automated tests** and **100% type safety**:

| Test Suite | Purpose | Tests | Status |
| :--- | :--- | :---: | :---: |
| **`SalaryEngineTest`** | Mathematical verification of monthly, daily, and hourly salary strategies, proration, grace floor thresholds, and overtime | 21 | PASS |
| **`SecurityAndReliabilityPenTest`** | Penetration testing of unauthenticated access, cross-tenant data isolation attacks, request correlation, and validation | 5 | PASS |
| **`PayrollIntegrationTest`** | Full payroll lifecycle: draft runs, adjustments, immutable finalization snapshot, mark paid, and monthly CSV report | 4 | PASS |
| **`AttendanceIntegrationTest`** | Daily check-in/out, multi-session tracking, conflicts, and timezone-aware business dates | 6 | PASS |
| **`MaidIntegrationTest`** | Worker CRUD, multi-schedule employment contracts, and tenant scoping | 4 | PASS |
| **`AuthIntegrationTest`** | Registration, login, token refresh rotation, and logout token invalidation | 5 | PASS |
| **`RepositoryConstraintIntegrationTest`** | Database unique constraints, cascades, and temporal checks | 8 | PASS |
| **Mobile TypeScript** | Zero compilation errors across all screens, hooks, and services (`npx tsc --noEmit`) | - | PASS |

---

## 🚢 Production Deployment

### 1. Database (Neon PostgreSQL)
1. Provision a PostgreSQL 16 database on [Neon.tech](https://neon.tech).
2. Enable connection pooling (`sslmode=require`).
3. Note the host, database, user, and password for Render.

### 2. Backend (Render)
1. Push repository to GitHub/GitLab.
2. In [Render Dashboard](https://dashboard.render.com), create a new **Blueprint** and link this repository (`render.yaml`).
3. Set the confidential environment variables: `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`.
4. Render will automatically build the multi-stage Dockerfile and start the container with health check at `/actuator/health`.

### 3. Mobile (EAS Build)
1. Install EAS CLI: `npm install -g eas-cli`
2. Log in: `eas login`
3. Configure project ID: `eas init`
4. Build Android APK/AAB:
   ```bash
   cd mobile
   eas build --platform android --profile production
   ```
5. Build iOS IPA:
   ```bash
   cd mobile
   eas build --platform ios --profile production
   ```
