# Audit Platform

Multi-tenant SaaS for professional audit, inspection, testing, and certification organizations.

This repository is a **modular monolith**: Spring Boot 3 / Java 21 API, React + TypeScript SPA, MySQL 8, Flyway, and Docker Compose.

Phase 19 (current) adds a tenant-scoped audit log viewer for the Phase 2 `audit_logs` table. Phases 2–18 remain. Set `AUDIT_PLATFORM_BOOTSTRAP_ADMIN_EMAIL` and `AUDIT_PLATFORM_BOOTSTRAP_ADMIN_PASSWORD` before first start to create the platform super admin. Platform admins must choose a tenant (header `X-Tenant-Id`) before creating clients, standards, auditors, or programmes.

## Documentation

| Document | Contents |
| --- | --- |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Bounded contexts, tenancy, API/security decisions |
| [docs/DATABASE.md](docs/DATABASE.md) | Schema and Flyway conventions |
| [docs/API.md](docs/API.md) | Envelope and versioned routes |
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

- Web: http://localhost:8080 (sign in with the bootstrap admin after first boot)
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
