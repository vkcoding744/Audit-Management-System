# Audit Platform

Multi-tenant SaaS for professional audit, inspection, testing, and certification organizations.

This repository is a **modular monolith**: Spring Boot 3 / Java 21 API, React + TypeScript SPA, MySQL 8, Flyway, and Docker Compose.

Phase 1 (current) is the **project foundation** only: repository layout, security baseline, API envelope, logging, health endpoints, tenant persistence primitives, and the application shell. Authentication and business modules start in Phase 2.

## Documentation

| Document | Contents |
| --- | --- |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Bounded contexts, tenancy, API/security decisions |
| [docs/DATABASE.md](docs/DATABASE.md) | Schema and Flyway conventions |
| [docs/API.md](docs/API.md) | Envelope, Phase 1 routes |
| [docs/SECURITY.md](docs/SECURITY.md) | Secrets, CSRF, headers |
| [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) | Local setup |
| [docs/TESTING.md](docs/TESTING.md) | Test strategy |
| [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) | Compose and AWS-oriented layout |
| [docs/AI.md](docs/AI.md) | Provider-agnostic AI rules (Phase 17) |

## Quick start

```bash
cp .env.example .env
docker compose up --build
```

- Web: http://localhost:8080
- API health: http://localhost:8081/api/v1/system/health
- OpenAPI (dev): http://localhost:8081/swagger-ui.html

Without Docker, see [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).

## Layout

```
backend/     Spring Boot API
frontend/    React + Vite SPA
docs/        Architecture and runbooks
docker-compose.yml
```
