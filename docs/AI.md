# AI

AI is a **separate abstraction** (`com.auditplatform.ai`). Business workflows must not depend on a single vendor.

Rules:

- Human review is mandatory (`PENDING_REVIEW` until approve or reject).
- AI must never issue a certificate or close a nonconformity by itself. Approve/reject only updates the generation row.
- Persist provider, model, timestamp, user, prompt/version metadata, and approval status with generated content.
- Secret API keys stay in the environment (`AUDIT_PLATFORM_AI_*`), not the database.

The default `AiGenerationPort` is `StubAiGenerationAdapter` when `audit.ai.provider=stub` (default). Swap the bean for another provider without changing controllers or domain services.
