package com.auditplatform.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audit.ai")
public record AiProperties(
        String provider,
        String model,
        String promptVersion
) {
    public String providerOrDefault() {
        return blankToDefault(provider, "stub");
    }

    public String modelOrDefault() {
        return blankToDefault(model, "stub-v1");
    }

    public String promptVersionOrDefault() {
        return blankToDefault(promptVersion, "v1");
    }

    private static String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
