package com.auditplatform.identity.service;

import com.auditplatform.common.config.AuditPlatformProperties;
import com.auditplatform.common.security.PlatformPrincipal;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void roundTripAccessToken() {
        AuditPlatformProperties properties = new AuditPlatformProperties(
                new AuditPlatformProperties.Api(false, "0.2.0"),
                new AuditPlatformProperties.Cors("http://localhost:5173"),
                new AuditPlatformProperties.RateLimit(false, 120, "memory", "redis://localhost:6379"),
                new AuditPlatformProperties.Auth(
                        "unit-test-jwt-secret-key-32chars!!",
                        15,
                        7,
                        5,
                        15,
                        false,
                        false,
                        "",
                        "",
                        ""
                )
        );
        JwtService jwtService = new JwtService(properties);
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-1",
                "user@example.com",
                "tenant-1",
                false,
                "session-1",
                Set.of("USER_VIEW", "AUDIT_VIEW")
        );

        String token = jwtService.createAccessToken(principal);
        PlatformPrincipal parsed = jwtService.parse(token);

        assertThat(parsed).isNotNull();
        assertThat(parsed.userId()).isEqualTo("user-1");
        assertThat(parsed.tenantId()).isEqualTo("tenant-1");
        assertThat(parsed.permissions()).contains("USER_VIEW", "AUDIT_VIEW");
        assertThat(jwtService.parse("not-a-jwt")).isNull();
    }
}
