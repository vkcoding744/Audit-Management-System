package com.auditplatform.system.api;

public record SystemInfoResponse(
        String application,
        String apiVersion,
        String environment
) {
}
