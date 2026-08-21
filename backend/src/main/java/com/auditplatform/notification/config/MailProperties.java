package com.auditplatform.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audit.mail")
public record MailProperties(
        String provider,
        String from,
        String smtpHost,
        Integer smtpPort
) {
    public String providerOrDefault() {
        return provider == null || provider.isBlank() ? "logging" : provider;
    }

    public String fromOrDefault() {
        return from == null || from.isBlank() ? "noreply@localhost" : from;
    }
}
