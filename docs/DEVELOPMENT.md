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

## Backend only

```bash
cd backend
cp ../.env.example ../.env
# Point datasource at a local MySQL 8 instance
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

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

## Conventions

- Do not put business logic in controllers.
- Do not expose JPA entities over HTTP.
- New tables only via Flyway.
- Feature work follows the phase plan in `docs/ARCHITECTURE.md`.
