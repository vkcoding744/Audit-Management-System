package com.auditplatform.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "audit")
public record AuditPlatformProperties(
        Api api,
        Cors cors,
        RateLimit rateLimit,
        Auth auth
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

    public record RateLimit(
            boolean enabled,
            int requestsPerMinute,
            @DefaultValue("memory") String provider,
            @DefaultValue("redis://localhost:6379") String redisUri
    ) {
        public String providerOrMemory() {
            if (provider == null || provider.isBlank()) {
                return "memory";
            }
            return provider.trim().toLowerCase();
        }
    }

    public record Auth(
            String jwtSecret,
            int accessTokenMinutes,
            int refreshTokenDays,
            int maxFailedLogins,
            int lockoutMinutes,
            boolean exposeDevTokens,
            boolean requireEmailVerified,
            String bootstrapAdminEmail,
            String bootstrapAdminPassword,
            @DefaultValue("") String mfaEncryptKey,
            @DefaultValue("false") boolean cookieSessions,
            @DefaultValue("false") boolean cookieSecure
    ) {
    }
}
