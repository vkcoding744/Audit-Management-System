# AI

AI is a **separate abstraction** (`com.auditplatform.ai` in Phase 17). Business workflows must not depend on a single vendor.

Rules:

- Human review is mandatory.
- AI must never issue a certificate or close a nonconformity by itself.
- Persist provider, model, timestamp, user, prompt/version metadata, and approval status with generated content.

Phase 1 does not include an AI provider. Settings keys may be reserved in `platform_settings` later; secret API keys stay in the environment, not the database.
