package com.auditplatform.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "audit")
public record AuditPlatformProperties(
        Api api,
        Cors cors,
        RateLimit rateLimit
) {
    public record Api(boolean docsEnabled, String version) {
    }

    public record Cors(String allowedOrigins) {
        public List<String> originList() {
            if (allowedOrigins == null || allowedOrigins.isBlank()) {
                return List.of();
            }
            return List.of(allowedOrigins.split(","))
                    .stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }

    public record RateLimit(boolean enabled, int requestsPerMinute) {
    }
}
