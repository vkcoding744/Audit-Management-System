package com.auditplatform.certification.service;

import com.auditplatform.audit.domain.Audit;
import com.auditplatform.audit.domain.AuditStatus;
import com.auditplatform.audit.domain.FindingStatus;
import com.auditplatform.audit.repository.FindingRepository;
import com.auditplatform.audit.service.AuditService;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.certification.domain.Certificate;
import com.auditplatform.certification.domain.CertificateStatus;
import com.auditplatform.certification.repository.CertificateRepository;
import com.auditplatform.certification.repository.CertificateSurveillanceRepository;
import com.auditplatform.certification.repository.CertificationDecisionRepository;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CertificateServiceTest {

    private final IsolationService isolationService = new IsolationService();
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-20T00:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void issueBlockedWhenOpenMajorFindingExists() {
        bindUser();
        Certificate draft = draftCertificate();
        Audit audit = completedAudit();

        CertificateRepository certificates = mock(CertificateRepository.class);
        when(certificates.findByIdAndDeletedAtIsNull("cert-1")).thenReturn(Optional.of(draft));
        AuditService audits = mock(AuditService.class);
        when(audits.requireAudit("audit-1")).thenReturn(audit);
        FindingRepository findings = mock(FindingRepository.class);
        when(findings.countByAuditIdAndStatusAndSeverityInAndDeletedAtIsNull(
                eq("audit-1"),
                eq(FindingStatus.OPEN),
                any()
        )).thenReturn(1L);

        CertificateService service = service(certificates, audits, findings);

        assertThatThrownBy(() -> service.issue("cert-1"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getErrorCode()).isEqualTo(ErrorCode.SYS_VALIDATION);
                    assertThat(api.getMessage()).contains("major and minor findings");
                });
    }

    @Test
    void expiredFlagIsTrueWhenActiveCertificateIsPastExpiry() {
        bindUser();
        Certificate certificate = draftCertificate();
        certificate.setStatus(CertificateStatus.ACTIVE);
        certificate.setExpiresOn(LocalDate.of(2026, 1, 1));
        CertificateRepository certificates = mock(CertificateRepository.class);
        when(certificates.findByIdAndDeletedAtIsNull("cert-1")).thenReturn(Optional.of(certificate));
        CertificationDecisionRepository decisions = mock(CertificationDecisionRepository.class);
        when(decisions.findByTenantIdAndCertificateIdAndDeletedAtIsNullOrderByDecidedOnAsc("tenant-a", "cert-1"))
                .thenReturn(List.of());
        CertificateSurveillanceRepository surveillance = mock(CertificateSurveillanceRepository.class);
        when(surveillance.findByTenantIdAndCertificateIdAndDeletedAtIsNullOrderByPlannedOnAsc("tenant-a", "cert-1"))
                .thenReturn(List.of());

        CertificateService service = new CertificateService(
                certificates,
                decisions,
                surveillance,
                mock(CertificateNumberService.class),
                mock(AuditService.class),
                mock(FindingRepository.class),
                isolationService,
                mock(AuditLogService.class),
                clock
        );

        assertThat(service.get("cert-1").expired()).isTrue();
    }

    private CertificateService service(
            CertificateRepository certificates,
            AuditService audits,
            FindingRepository findings
    ) {
        return new CertificateService(
                certificates,
                mock(CertificationDecisionRepository.class),
                mock(CertificateSurveillanceRepository.class),
                mock(CertificateNumberService.class),
                audits,
                findings,
                isolationService,
                mock(AuditLogService.class),
                clock
        );
    }

    private static Certificate draftCertificate() {
        Certificate certificate = new Certificate();
        ReflectionTestUtils.setField(certificate, "id", "cert-1");
        certificate.setTenantId("tenant-a");
        certificate.setClientId("client-1");
        certificate.setSchemeId("scheme-1");
        certificate.setAuditId("audit-1");
        certificate.setStatus(CertificateStatus.DRAFT);
        certificate.setValidFrom(LocalDate.of(2026, 1, 1));
        certificate.setExpiresOn(LocalDate.of(2029, 1, 1));
        return certificate;
    }

    private static Audit completedAudit() {
        Audit audit = new Audit();
        ReflectionTestUtils.setField(audit, "id", "audit-1");
        audit.setTenantId("tenant-a");
        audit.setClientId("client-1");
        audit.setSchemeId("scheme-1");
        audit.setStatus(AuditStatus.COMPLETED);
        return audit;
    }

    private static void bindUser() {
        var principal = new com.auditplatform.common.security.PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("CERTIFICATE_ISSUE")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
