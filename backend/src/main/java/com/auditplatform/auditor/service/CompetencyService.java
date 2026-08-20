package com.auditplatform.auditor.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.auditor.api.CompetencyResponse;
import com.auditplatform.auditor.api.CreateCompetencyRequest;
import com.auditplatform.auditor.domain.Auditor;
import com.auditplatform.auditor.domain.AuditorCompetency;
import com.auditplatform.auditor.domain.CompetencyRole;
import com.auditplatform.auditor.domain.CompetencyStatus;
import com.auditplatform.auditor.repository.AuditorCompetencyRepository;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.domain.Scheme;
import com.auditplatform.standards.domain.Standard;
import com.auditplatform.standards.service.SchemeService;
import com.auditplatform.standards.service.StandardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class CompetencyService {

    private final AuditorCompetencyRepository competencyRepository;
    private final AuditorService auditorService;
    private final StandardService standardService;
    private final SchemeService schemeService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public CompetencyService(
            AuditorCompetencyRepository competencyRepository,
            AuditorService auditorService,
            StandardService standardService,
            SchemeService schemeService,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.competencyRepository = competencyRepository;
        this.auditorService = auditorService;
        this.standardService = standardService;
        this.schemeService = schemeService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<CompetencyResponse> list(String auditorId) {
        Auditor auditor = auditorService.requireAuditor(auditorId);
        LocalDate on = today();
        return competencyRepository
                .findByTenantIdAndAuditorIdAndDeletedAtIsNullOrderByValidFromDesc(auditor.getTenantId(), auditor.getId())
                .stream()
                .map(item -> CompetencyResponse.from(item, on))
                .toList();
    }

    @Transactional
    public CompetencyResponse create(String auditorId, CreateCompetencyRequest request) {
        Auditor auditor = auditorService.requireAuditor(auditorId);
        if ((request.standardId() == null || request.standardId().isBlank())
                && (request.schemeId() == null || request.schemeId().isBlank())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Competency must reference a standard and/or a scheme");
        }
        if (request.validTo() != null && request.validTo().isBefore(request.validFrom())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "validTo cannot be before validFrom");
        }
        AuditorCompetency competency = new AuditorCompetency();
        competency.setTenantId(auditor.getTenantId());
        competency.setAuditorId(auditor.getId());
        competency.setStandardId(resolveStandard(auditor.getTenantId(), request.standardId()));
        competency.setSchemeId(resolveScheme(auditor.getTenantId(), request.schemeId()));
        competency.setCompetencyRole(request.competencyRole() == null ? CompetencyRole.TEAM : request.competencyRole());
        competency.setStatus(CompetencyStatus.ACTIVE);
        competency.setValidFrom(request.validFrom());
        competency.setValidTo(request.validTo());
        competency.setNotes(blankToNull(request.notes()));
        competencyRepository.save(competency);
        auditLogService.record("COMPETENCY_CREATE", "AuditorCompetency", competency.getId(), null, competency.getCompetencyRole().name(), null, null);
        return CompetencyResponse.from(competency, today());
    }

    @Transactional
    public CompetencyResponse suspend(String competencyId) {
        AuditorCompetency competency = requireCompetency(competencyId);
        competency.setStatus(CompetencyStatus.SUSPENDED);
        competencyRepository.save(competency);
        return CompetencyResponse.from(competency, today());
    }

    @Transactional
    public CompetencyResponse revoke(String competencyId) {
        AuditorCompetency competency = requireCompetency(competencyId);
        competency.setStatus(CompetencyStatus.REVOKED);
        competencyRepository.save(competency);
        auditLogService.record("COMPETENCY_REVOKE", "AuditorCompetency", competency.getId(), null, "REVOKED", null, null);
        return CompetencyResponse.from(competency, today());
    }

    @Transactional
    public void delete(String competencyId) {
        AuditorCompetency competency = requireCompetency(competencyId);
        competency.setDeletedAt(Instant.now());
        competencyRepository.save(competency);
    }

    public AuditorCompetency requireCompetency(String id) {
        AuditorCompetency competency = competencyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Competency not found"));
        isolationService.assertCanAccessTenant(competency.getTenantId());
        return competency;
    }

    private String resolveStandard(String tenantId, String standardId) {
        if (standardId == null || standardId.isBlank()) {
            return null;
        }
        Standard standard = standardService.requireStandard(standardId);
        if (!tenantId.equals(standard.getTenantId())) {
            throw new ApiException(ErrorCode.AUTH_TENANT_MISMATCH, "Standard does not belong to this tenant");
        }
        return standard.getId();
    }

    private String resolveScheme(String tenantId, String schemeId) {
        if (schemeId == null || schemeId.isBlank()) {
            return null;
        }
        Scheme scheme = schemeService.requireScheme(schemeId);
        if (!tenantId.equals(scheme.getTenantId())) {
            throw new ApiException(ErrorCode.AUTH_TENANT_MISMATCH, "Scheme does not belong to this tenant");
        }
        return scheme.getId();
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
