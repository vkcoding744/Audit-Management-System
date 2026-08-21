# Deployment

## Docker Compose (reference / non-production HA)

`docker-compose.yml` starts MySQL 8, backend, and frontend Nginx. Optional:

```bash
docker compose --profile redis --profile mailhog up --build
```

Set secrets in `.env`. Never bake them into images.

## AWS-oriented layout (target)

| Concern | Typical service |
| --- | --- |
| Compute | ECS Fargate or EKS (backend + frontend tasks) |
| Database | RDS MySQL 8 (Multi-AZ) |
| Files | S3 via `ObjectStoragePort` (`audit.storage.provider=s3`) or local disk in non-AWS environments |
| Secrets | SSM Parameter Store / Secrets Manager |
| TLS / HTTP | ALB + ACM |
| Cache / rate limit | ElastiCache Redis via `RateLimitPort` (`audit.rate-limit.provider=redis`) |
| Email | SES via `OutboundEmailPort` (`audit.mail.provider=smtp`) or logging adapter locally |
| AI | `AiGenerationPort` (`audit.ai.provider=stub` default); vendor keys in Secrets Manager, never MySQL |
| Logs / metrics | CloudWatch; Actuator + future OTel |

## Health

- Liveness: `GET /actuator/health/liveness`
- Readiness: `GET /actuator/health/readiness`
- App: `GET /api/v1/system/health`

## Configuration

`application-prod.yml` plus environment variables. `audit.api.docs-enabled` must be false in production unless explicitly required behind auth.

## Migrations

Flyway runs on application startup. Run a single writer during schema changes. Do not auto-downgrade.
