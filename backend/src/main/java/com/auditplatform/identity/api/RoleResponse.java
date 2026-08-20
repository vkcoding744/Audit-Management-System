package com.auditplatform.identity.api;

public record RoleResponse(
        String id,
        String code,
        String name,
        String description,
        boolean systemRole,
        java.util.Set<String> permissions
) {
}
