package com.auditplatform.ai.service;

import com.auditplatform.ai.config.AiProperties;
import com.auditplatform.ai.domain.AiGeneration;
import com.auditplatform.ai.repository.AiGenerationRepository;
import com.auditplatform.ai.spi.AiGenerationPort;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.identity.service.IsolationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiGenerationServiceIsolationTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getDoesNotReturnAnotherTenantsGeneration() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("AI_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        AiGeneration foreign = new AiGeneration();
        foreign.setTenantId("tenant-b");
        AiGenerationRepository generations = mock(AiGenerationRepository.class);
        when(generations.findByIdAndDeletedAtIsNull("aig-1")).thenReturn(Optional.of(foreign));

        AiGenerationService service = new AiGenerationService(
                generations,
                mock(AiNumberService.class),
                mock(AiGenerationPort.class),
                new AiProperties("stub", "stub-v1", "v1"),
                isolationService,
                mock(AuditLogService.class),
                Clock.fixed(Instant.parse("2026-08-21T00:00:00Z"), ZoneOffset.UTC)
        );

        assertThatThrownBy(() -> service.get("aig-1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_TENANT_MISMATCH);
    }
}
