package com.auditplatform.identity.crypto;

import com.auditplatform.common.config.AuditPlatformProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MfaCryptoServiceTest {

    @Test
    void roundTripEncryptDecrypt() {
        MfaCryptoService crypto = new MfaCryptoService(properties("dedicated-mfa-key-32-chars!!xx"));
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";
        assertThat(crypto.decrypt(crypto.encrypt(secret))).isEqualTo(secret);
    }

    private static AuditPlatformProperties properties(String mfaKey) {
        return new AuditPlatformProperties(
                new AuditPlatformProperties.Api(false, "0.20.0"),
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
                        mfaKey
                )
        );
    }
}
