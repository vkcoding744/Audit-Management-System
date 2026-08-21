# Development

## Prerequisites

- JDK 21
- Maven 3.9+ (or `./backend/mvnw`)
- Node.js 20+
- Docker + Docker Compose (recommended)
- MySQL 8 if running services without Compose

## Quick start (Compose)

```bash
cp .env.example .env
docker compose up --build
```

- Frontend: http://localhost:8080
- Backend API: http://localhost:8080/api/v1/system/health (via Nginx) or http://localhost:8081/api/v1/system/health
- Swagger (dev): http://localhost:8081/swagger-ui.html
- MySQL: localhost:3306

Cookie sessions default off (`AUDIT_PLATFORM_COOKIE_SESSIONS=false`). To use httpOnly cookies in local Compose, set that flag and `VITE_COOKIE_SESSIONS=true` (rebuild the frontend image). CSRF: call `GET /api/v1/auth/csrf` before mutating requests.

Optional MailHog (`docker compose --profile mailhog up`): SMTP 1025, UI 8025. Set `AUDIT_PLATFORM_MAIL_PROVIDER=smtp` and `AUDIT_PLATFORM_SMTP_HOST=localhost`.

AI drafts default to `AUDIT_PLATFORM_AI_PROVIDER=stub`. Vendor keys belong in the environment, not MySQL.

Search defaults to `AUDIT_PLATFORM_SEARCH_PROVIDER=mysql`. Elasticsearch is not started by Compose.

Due notification jobs are dispatched when `AUDIT_PLATFORM_NOTIFICATION_DISPATCH_ENABLED=true` (default). The `test` profile turns the scheduler off. Interval and batch size: `AUDIT_PLATFORM_NOTIFICATION_DISPATCH_INTERVAL_MS` (30000) and `AUDIT_PLATFORM_NOTIFICATION_DISPATCH_BATCH_SIZE` (50).

## Backend only

```bash
cd backend
cp ../.env.example ../.env
# Point datasource at a local MySQL 8 instance
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Set `AUDIT_PLATFORM_BOOTSTRAP_ADMIN_EMAIL` and `AUDIT_PLATFORM_BOOTSTRAP_ADMIN_PASSWORD` (12+ characters, mixed case and a digit) to create the first platform user when the database has no users.

## Frontend only

```bash
cd frontend
npm install
npm run dev
```

Vite proxies `/api` to `http://localhost:8081` (see `vite.config.ts`).

## Tests

Web MVC tests do not need Docker. Integration tests (`*IT.java`) use Testcontainers MySQL 8:

```bash
cd backend
./mvnw test          # unit + web tests (no Docker)
./mvnw verify        # also runs Testcontainers IT when Docker is available
```

```bash
cd frontend
npm test
```

## Profiles

| Profile | Use |
| --- | --- |
| `dev` | Local run, OpenAPI on, verbose logs |
| `test` | Automated tests |
| `prod` | JSON logs, docs off, stricter actuator |

Object storage: default `audit.storage.provider=local` writes under `AUDIT_PLATFORM_STORAGE_LOCAL_ROOT` (or the JVM temp dir). Set `AUDIT_PLATFORM_STORAGE_PROVIDER=s3` plus bucket/region (optional `AUDIT_PLATFORM_STORAGE_S3_ENDPOINT` for MinIO).

## Conventions

- Do not put business logic in controllers.
- Do not expose JPA entities over HTTP.
- New tables only via Flyway.
- Feature work follows the phase plan in `docs/ARCHITECTURE.md`.
