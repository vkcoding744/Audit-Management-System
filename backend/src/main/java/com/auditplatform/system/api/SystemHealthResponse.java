package com.auditplatform.system.api;

public record SystemHealthResponse(
        String status,
        String database,
        long tenantCount
) {
}
