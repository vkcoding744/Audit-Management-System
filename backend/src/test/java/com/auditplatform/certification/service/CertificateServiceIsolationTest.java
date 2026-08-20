package com.auditplatform.certification.service;

import com.auditplatform.audit.repository.FindingRepository;
import com.auditplatform.audit.service.AuditService;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.certification.domain.Certificate;
import com.auditplatform.certification.repository.CertificateRepository;
import com.auditplatform.certification.repository.CertificateSurveillanceRepository;
import com.auditplatform.certification.repository.CertificationDecisionRepository;
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

class CertificateServiceIsolationTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getDoesNotReturnAnotherTenantsCertificate() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("CERTIFICATE_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        Certificate foreign = new Certificate();
        foreign.setTenantId("tenant-b");
        CertificateRepository certificates = mock(CertificateRepository.class);
        when(certificates.findByIdAndDeletedAtIsNull("c1")).thenReturn(Optional.of(foreign));

        CertificateService service = new CertificateService(
                certificates,
                mock(CertificationDecisionRepository.class),
                mock(CertificateSurveillanceRepository.class),
                mock(CertificateNumberService.class),
                mock(AuditService.class),
                mock(FindingRepository.class),
                isolationService,
                mock(AuditLogService.class),
                Clock.fixed(Instant.parse("2026-08-20T00:00:00Z"), ZoneOffset.UTC)
        );

        assertThatThrownBy(() -> service.get("c1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_TENANT_MISMATCH);
    }
}
