package com.auditplatform.identity.config;

import com.auditplatform.common.config.AuditPlatformProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AuthSettingsValidator {

    private static final String PLACEHOLDER = "change-me-in-env-jwt-secret-32b!!";

    private final AuditPlatformProperties properties;
    private final Environment environment;

    public AuthSettingsValidator(AuditPlatformProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validate() {
        if (environment.matchesProfiles("prod") && PLACEHOLDER.equals(properties.auth().jwtSecret())) {
            throw new IllegalStateException("Set AUDIT_PLATFORM_JWT_SECRET to a unique value in production");
        }
    }
}
