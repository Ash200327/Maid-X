# 09 — Deployment Guide

## 1. Development prerequisites

- JDK compatible with the selected Spring Boot release.
- Node.js compatible with the selected Expo SDK.
- npm/pnpm/yarn according to repository convention.
- PostgreSQL local instance or development database.
- Expo CLI/EAS CLI.
- Git.

Exact versions should be pinned by the implementation phase rather than loosely relying on latest packages.

## 2. Repository layout

```text
/
  mobile/
  backend/
  docs/
  scripts/
  README.md
```

## 3. Backend configuration

Environment variables should include values similar to:

```text
SPRING_PROFILES_ACTIVE
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
JWT_ACCESS_SECRET
JWT_REFRESH_SECRET
JWT_ACCESS_TTL
JWT_REFRESH_TTL
CORS_ALLOWED_ORIGINS
APP_DEFAULT_TIMEZONE
APP_DEFAULT_CURRENCY
```

Do not commit secrets.

## 4. Local database

Run PostgreSQL locally and configure backend connection. Flyway must run automatically on application startup in controlled environments.

## 5. Neon

Create a production PostgreSQL database in Neon.

Recommended separation:
- development
- staging, if used
- production

Use separate credentials and environment variables.

## 6. Render

Deploy the Spring Boot backend as a web service.

Build steps should:
1. checkout repository
2. build backend
3. run tests
4. start Spring Boot application

Set production environment variables in Render's secret environment configuration.

Health endpoint:

```text
/actuator/health
```

Expose only the minimum actuator endpoints publicly.

## 7. Database migrations

Migrations must execute in a controlled deployment sequence. Before destructive or large migrations, make sure a backup/recovery path exists.

Never edit an already applied migration.

## 8. Mobile environment configuration

Use environment-specific API base URLs:

```text
EXPO_PUBLIC_API_URL
```

Production mobile builds must point at production HTTPS API.

## 9. Expo/EAS

Use EAS Build for Android/iOS production artifacts.

Configure:
- bundle/package identifiers
- app icon/splash
- production environment variables
- signing credentials
- version/build numbers

## 10. Observability

Minimum production observability:
- structured application logs
- request correlation/request ID
- error monitoring
- database connection/error monitoring
- Render health checks

Never log:
- passwords
- access tokens
- refresh tokens
- full authentication headers

## 11. Backups and recovery

Use provider-supported PostgreSQL backup/recovery capabilities and test recovery at least periodically.

A backup is only useful if restoration has been validated.

## 12. Production release checklist

- Tests green.
- Flyway migrations reviewed.
- Environment variables configured.
- HTTPS verified.
- Authentication tested.
- Cross-owner authorization tests green.
- Salary matrix green.
- EAS production build points to production API.
- Database backup/recovery available.
- Error monitoring active.
