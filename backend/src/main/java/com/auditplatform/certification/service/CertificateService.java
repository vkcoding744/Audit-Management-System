package com.auditplatform.certification.service;

import com.auditplatform.audit.domain.Audit;
import com.auditplatform.audit.domain.AuditStatus;
import com.auditplatform.audit.domain.FindingSeverity;
import com.auditplatform.audit.domain.FindingStatus;
import com.auditplatform.audit.repository.FindingRepository;
import com.auditplatform.audit.service.AuditService;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.certification.api.CertificateActionRequest;
import com.auditplatform.certification.api.CertificateResponse;
import com.auditplatform.certification.api.CreateCertificateRequest;
import com.auditplatform.certification.api.CreateSurveillanceRequest;
import com.auditplatform.certification.api.DecisionResponse;
import com.auditplatform.certification.api.SurveillanceResponse;
import com.auditplatform.certification.domain.Certificate;
import com.auditplatform.certification.domain.CertificateStatus;
import com.auditplatform.certification.domain.CertificateSurveillance;
import com.auditplatform.certification.domain.CertificationDecision;
import com.auditplatform.certification.domain.DecisionType;
import com.auditplatform.certification.domain.SurveillanceStatus;
import com.auditplatform.certification.repository.CertificateRepository;
import com.auditplatform.certification.repository.CertificateSurveillanceRepository;
import com.auditplatform.certification.repository.CertificationDecisionRepository;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class CertificateService {

    private static final List<FindingSeverity> BLOCKING = List.of(FindingSeverity.MAJOR, FindingSeverity.MINOR);

    private final CertificateRepository certificateRepository;
    private final CertificationDecisionRepository decisionRepository;
    private final CertificateSurveillanceRepository surveillanceRepository;
    private final CertificateNumberService numberService;
    private final AuditService auditService;
    private final FindingRepository findingRepository;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public CertificateService(
            CertificateRepository certificateRepository,
            CertificationDecisionRepository decisionRepository,
            CertificateSurveillanceRepository surveillanceRepository,
            CertificateNumberService numberService,
            AuditService auditService,
            FindingRepository findingRepository,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.certificateRepository = certificateRepository;
        this.decisionRepository = decisionRepository;
        this.surveillanceRepository = surveillanceRepository;
        this.numberService = numberService;
        this.auditService = auditService;
        this.findingRepository = findingRepository;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<CertificateResponse> list(String clientId, CertificateStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<Certificate> page;
        if (clientId != null && !clientId.isBlank()) {
            page = certificateRepository.findByTenantIdAndClientIdAndDeletedAtIsNull(tenantId, clientId, pageable);
        } else if (status != null) {
            page = certificateRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = certificateRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(this::summary));
    }

    @Transactional(readOnly = true)
    public CertificateResponse get(String id) {
        return toDetail(requireCertificate(id));
    }

    @Transactional
    public CertificateResponse create(CreateCertificateRequest request) {
        Audit audit = requireCompletableAudit(request.auditId());
        LocalDate validFrom = request.validFrom() == null ? today() : request.validFrom();
        if (request.expiresOn().isBefore(validFrom)) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "expiresOn cannot be before validFrom");
        }
        Certificate certificate = new Certificate();
        certificate.setTenantId(audit.getTenantId());
        certificate.setCertificateNumber(numberService.nextCertificate(audit.getTenantId()));
        certificate.setClientId(audit.getClientId());
        certificate.setSchemeId(audit.getSchemeId());
        certificate.setStandardId(audit.getStandardId());
        certificate.setProgrammeId(audit.getProgrammeId());
        certificate.setAuditId(audit.getId());
        certificate.setScopeText(blankToNull(request.scopeText()));
        certificate.setStatus(CertificateStatus.DRAFT);
        certificate.setValidFrom(validFrom);
        certificate.setExpiresOn(request.expiresOn());
        certificate.setNextSurveillanceOn(request.nextSurveillanceOn());
        certificate.setNotes(blankToNull(request.notes()));
        certificateRepository.save(certificate);
        auditLogService.record(
                "CERTIFICATE_CREATE",
                "Certificate",
                certificate.getId(),
                null,
                certificate.getCertificateNumber(),
                null,
                null
        );
        return toDetail(certificate);
    }

    @Transactional
    public CertificateResponse issue(String id) {
        Certificate certificate = requireCertificate(id);
        if (certificate.getStatus() != CertificateStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only draft certificates can be issued");
        }
        requireCompletableAudit(certificate.getAuditId());
        assertNoBlockingFindings(certificate.getAuditId());
        if (certificateRepository.existsByTenantIdAndClientIdAndSchemeIdAndStatusAndDeletedAtIsNull(
                certificate.getTenantId(),
                certificate.getClientId(),
                certificate.getSchemeId(),
                CertificateStatus.ACTIVE
        )) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "An active certificate already exists for this client and scheme");
        }
        certificate.setStatus(CertificateStatus.ACTIVE);
        certificateRepository.save(certificate);
        recordDecision(certificate, DecisionType.ISSUE, "Issued after completed audit with no open major or minor findings");
        auditLogService.record("CERTIFICATE_ISSUE", "Certificate", certificate.getId(), "DRAFT", "ACTIVE", null, null);
        return toDetail(certificate);
    }

    @Transactional
    public CertificateResponse suspend(String id, CertificateActionRequest request) {
        Certificate certificate = requireCertificate(id);
        if (certificate.getStatus() != CertificateStatus.ACTIVE) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only active certificates can be suspended");
        }
        certificate.setStatus(CertificateStatus.SUSPENDED);
        certificateRepository.save(certificate);
        recordDecision(certificate, DecisionType.SUSPEND, request.reason().trim());
        auditLogService.record("CERTIFICATE_SUSPEND", "Certificate", certificate.getId(), "ACTIVE", "SUSPENDED", null, null);
        return toDetail(certificate);
    }

    @Transactional
    public CertificateResponse reinstate(String id, CertificateActionRequest request) {
        Certificate certificate = requireCertificate(id);
        if (certificate.getStatus() != CertificateStatus.SUSPENDED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only suspended certificates can be reinstated");
        }
        certificate.setStatus(CertificateStatus.ACTIVE);
        certificateRepository.save(certificate);
        recordDecision(certificate, DecisionType.REINSTATE, request.reason().trim());
        auditLogService.record("CERTIFICATE_REINSTATE", "Certificate", certificate.getId(), "SUSPENDED", "ACTIVE", null, null);
        return toDetail(certificate);
    }

    @Transactional
    public CertificateResponse withdraw(String id, CertificateActionRequest request) {
        Certificate certificate = requireCertificate(id);
        if (certificate.getStatus() != CertificateStatus.ACTIVE && certificate.getStatus() != CertificateStatus.SUSPENDED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only active or suspended certificates can be withdrawn");
        }
        String from = certificate.getStatus().name();
        certificate.setStatus(CertificateStatus.WITHDRAWN);
        certificateRepository.save(certificate);
        recordDecision(certificate, DecisionType.WITHDRAW, request.reason().trim());
        auditLogService.record("CERTIFICATE_WITHDRAW", "Certificate", certificate.getId(), from, "WITHDRAWN", null, null);
        return toDetail(certificate);
    }

    @Transactional
    public SurveillanceResponse addSurveillance(String certificateId, CreateSurveillanceRequest request) {
        Certificate certificate = requireCertificate(certificateId);
        if (certificate.getStatus() == CertificateStatus.WITHDRAWN) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Cannot plan surveillance on a withdrawn certificate");
        }
        CertificateSurveillance row = new CertificateSurveillance();
        row.setTenantId(certificate.getTenantId());
        row.setCertificateId(certificate.getId());
        row.setPlannedOn(request.plannedOn());
        row.setStatus(SurveillanceStatus.PLANNED);
        row.setNotes(blankToNull(request.notes()));
        surveillanceRepository.save(row);
        return SurveillanceResponse.from(row);
    }

    @Transactional
    public SurveillanceResponse completeSurveillance(String surveillanceId) {
        CertificateSurveillance row = surveillanceRepository.findByIdAndDeletedAtIsNull(surveillanceId)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Surveillance visit not found"));
        isolationService.assertCanAccessTenant(row.getTenantId());
        if (row.getStatus() != SurveillanceStatus.PLANNED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only planned surveillance can be completed");
        }
        row.setStatus(SurveillanceStatus.COMPLETED);
        row.setCompletedOn(today());
        surveillanceRepository.save(row);
        return SurveillanceResponse.from(row);
    }

    public Certificate requireCertificate(String id) {
        Certificate certificate = certificateRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Certificate not found"));
        isolationService.assertCanAccessTenant(certificate.getTenantId());
        return certificate;
    }

    private Audit requireCompletableAudit(String auditId) {
        Audit audit = auditService.requireAudit(auditId);
        if (audit.getStatus() != AuditStatus.COMPLETED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Certificates can only be based on a completed audit");
        }
        return audit;
    }

    private void assertNoBlockingFindings(String auditId) {
        long open = findingRepository.countByAuditIdAndStatusAndSeverityInAndDeletedAtIsNull(
                auditId,
                FindingStatus.OPEN,
                BLOCKING
        );
        if (open > 0) {
            throw new ApiException(
                    ErrorCode.SYS_VALIDATION,
                    "Certificate issue requires all major and minor findings to be closed"
            );
        }
    }

    private void recordDecision(Certificate certificate, DecisionType type, String reason) {
        CertificationDecision decision = new CertificationDecision();
        decision.setTenantId(certificate.getTenantId());
        decision.setCertificateId(certificate.getId());
        decision.setDecisionType(type);
        decision.setReason(reason);
        decision.setDecidedOn(today());
        decisionRepository.save(decision);
    }

    private CertificateResponse summary(Certificate certificate) {
        return CertificateResponse.from(certificate, expired(certificate), List.of(), List.of());
    }

    private CertificateResponse toDetail(Certificate certificate) {
        List<DecisionResponse> decisions = decisionRepository
                .findByTenantIdAndCertificateIdAndDeletedAtIsNullOrderByDecidedOnAsc(
                        certificate.getTenantId(),
                        certificate.getId()
                )
                .stream()
                .map(DecisionResponse::from)
                .toList();
        List<SurveillanceResponse> surveillance = surveillanceRepository
                .findByTenantIdAndCertificateIdAndDeletedAtIsNullOrderByPlannedOnAsc(
                        certificate.getTenantId(),
                        certificate.getId()
                )
                .stream()
                .map(SurveillanceResponse::from)
                .toList();
        return CertificateResponse.from(certificate, expired(certificate), decisions, surveillance);
    }

    private boolean expired(Certificate certificate) {
        return certificate.getStatus() == CertificateStatus.ACTIVE
                && certificate.getExpiresOn().isBefore(today());
    }

    private LocalDate today() {
        return LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
