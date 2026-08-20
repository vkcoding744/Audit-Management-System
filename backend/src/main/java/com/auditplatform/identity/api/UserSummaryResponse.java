package com.auditplatform.identity.api;

import java.util.Set;

public record UserSummaryResponse(
        String id,
        String tenantId,
        String email,
        String firstName,
        String lastName,
        String status,
        boolean emailVerified,
        boolean mfaEnabled,
        boolean platformAdmin,
        Set<String> roles,
        Set<String> permissions
) {
}
