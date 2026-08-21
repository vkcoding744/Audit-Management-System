package com.auditplatform.identity.crypto;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TotpServiceTest {

    private static final String RFC_SECRET = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

    private final TotpService totpService = new TotpService();

    @Test
    void rfc6238Sha1Vectors() {
        assertThat(totpService.generateCode(RFC_SECRET, Instant.ofEpochSecond(59))).isEqualTo("287082");
        assertThat(totpService.generateCode(RFC_SECRET, Instant.ofEpochSecond(1_111_111_109))).isEqualTo("081804");
        assertThat(totpService.generateCode(RFC_SECRET, Instant.ofEpochSecond(1_111_111_111))).isEqualTo("050471");
    }

    @Test
    void verifyAcceptsAdjacentWindow() {
        Instant now = Instant.ofEpochSecond(59);
        String code = totpService.generateCode(RFC_SECRET, now);
        assertThat(totpService.verify(RFC_SECRET, code, now)).isTrue();
        assertThat(totpService.verify(RFC_SECRET, code, now.plusSeconds(30))).isTrue();
        assertThat(totpService.verify(RFC_SECRET, "000000", now)).isFalse();
    }
}
