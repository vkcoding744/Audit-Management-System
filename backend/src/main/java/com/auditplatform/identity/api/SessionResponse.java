package com.auditplatform.identity.api;

import java.time.Instant;

public record SessionResponse(
        String id,
        String ipAddress,
        String userAgent,
        Instant expiresAt,
        Instant createdAt,
        boolean current,
        boolean revoked
) {
}
