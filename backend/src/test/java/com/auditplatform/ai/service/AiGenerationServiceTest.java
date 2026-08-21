package com.auditplatform.ai.service;

import com.auditplatform.ai.api.ReviewAiGenerationRequest;
import com.auditplatform.ai.config.AiProperties;
import com.auditplatform.ai.domain.AiGeneration;
import com.auditplatform.ai.domain.AiGenerationStatus;
import com.auditplatform.ai.domain.AiPurpose;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiGenerationServiceTest {

    private final IsolationService isolationService = new IsolationService();
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-21T12:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void approveBlockedWhenAlreadyApproved() {
        bindUser();
        AiGeneration generation = pending();
        generation.setStatus(AiGenerationStatus.APPROVED);
        AiGenerationRepository generations = mock(AiGenerationRepository.class);
        when(generations.findByIdAndDeletedAtIsNull("aig-1")).thenReturn(Optional.of(generation));

        assertThatThrownBy(() -> service(generations).approve("aig-1", new ReviewAiGenerationRequest(null)))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getErrorCode()).isEqualTo(ErrorCode.SYS_VALIDATION);
                    assertThat(api.getMessage()).contains("Only generations pending review can be changed");
                });
    }

    private AiGenerationService service(AiGenerationRepository generations) {
        return new AiGenerationService(
                generations,
                mock(AiNumberService.class),
                mock(AiGenerationPort.class),
                new AiProperties("stub", "stub-v1", "v1"),
                isolationService,
                mock(AuditLogService.class),
                clock
        );
    }

    private static AiGeneration pending() {
        AiGeneration generation = new AiGeneration();
        ReflectionTestUtils.setField(generation, "id", "aig-1");
        generation.setTenantId("tenant-a");
        generation.setGenerationNumber("AIG-000001");
        generation.setPurpose(AiPurpose.GENERIC);
        generation.setPrompt("Summarise");
        generation.setOutput("Draft");
        generation.setProvider("stub");
        generation.setModel("stub-v1");
        generation.setPromptVersion("v1");
        generation.setStatus(AiGenerationStatus.PENDING_REVIEW);
        return generation;
    }

    private static void bindUser() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("AI_UPDATE")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
