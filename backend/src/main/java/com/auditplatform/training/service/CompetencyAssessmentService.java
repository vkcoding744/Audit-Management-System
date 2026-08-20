package com.auditplatform.training.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.auditor.domain.Auditor;
import com.auditplatform.auditor.domain.AuditorCompetency;
import com.auditplatform.auditor.service.AuditorService;
import com.auditplatform.auditor.service.CompetencyService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.domain.Scheme;
import com.auditplatform.standards.domain.Standard;
import com.auditplatform.standards.service.SchemeService;
import com.auditplatform.standards.service.StandardService;
import com.auditplatform.training.api.AssessmentResponse;
import com.auditplatform.training.api.CompleteAssessmentRequest;
import com.auditplatform.training.api.CreateAssessmentRequest;
import com.auditplatform.training.api.UpdateAssessmentRequest;
import com.auditplatform.training.domain.AssessmentStatus;
import com.auditplatform.training.domain.CompetencyAssessment;
import com.auditplatform.training.repository.CompetencyAssessmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompetencyAssessmentService {

    private final CompetencyAssessmentRepository assessmentRepository;
    private final TrainingNumberService numberService;
    private final AuditorService auditorService;
    private final CompetencyService competencyService;
    private final StandardService standardService;
    private final SchemeService schemeService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;

    public CompetencyAssessmentService(
            CompetencyAssessmentRepository assessmentRepository,
            TrainingNumberService numberService,
            AuditorService auditorService,
            CompetencyService competencyService,
            StandardService standardService,
            SchemeService schemeService,
            IsolationService isolationService,
            AuditLogService auditLogService
    ) {
        this.assessmentRepository = assessmentRepository;
        this.numberService = numberService;
        this.auditorService = auditorService;
        this.competencyService = competencyService;
        this.standardService = standardService;
        this.schemeService = schemeService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AssessmentResponse> list(String auditorId, AssessmentStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<CompetencyAssessment> page;
        boolean hasAuditor = auditorId != null && !auditorId.isBlank();
        if (hasAuditor && status != null) {
            page = assessmentRepository.findByTenantIdAndAuditorIdAndStatusAndDeletedAtIsNull(
                    tenantId, auditorId, status, pageable);
        } else if (hasAuditor) {
            page = assessmentRepository.findByTenantIdAndAuditorIdAndDeletedAtIsNull(tenantId, auditorId, pageable);
        } else if (status != null) {
            page = assessmentRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = assessmentRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(AssessmentResponse::from));
    }

    @Transactional(readOnly = true)
    public AssessmentResponse get(String id) {
        return AssessmentResponse.from(requireAssessment(id));
    }

    @Transactional
    public AssessmentResponse create(CreateAssessmentRequest request) {
        Auditor auditor = auditorService.requireAuditor(request.auditorId());
        CompetencyAssessment assessment = new CompetencyAssessment();
        assessment.setTenantId(auditor.getTenantId());
        assessment.setAssessmentNumber(numberService.nextAssessment(auditor.getTenantId()));
        assessment.setAuditorId(auditor.getId());
        assessment.setAssessedOn(request.assessedOn());
        assessment.setAssessorName(blankToNull(request.assessorName()));
        assessment.setStandardId(resolveStandard(auditor.getTenantId(), request.standardId()));
        assessment.setSchemeId(resolveScheme(auditor.getTenantId(), request.schemeId()));
        assessment.setCompetencyId(resolveCompetency(auditor, request.competencyId()));
        assessment.setNotes(blankToNull(request.notes()));
        assessment.setStatus(AssessmentStatus.DRAFT);
        assessmentRepository.save(assessment);
        auditLogService.record(
                "ASSESSMENT_CREATE", "CompetencyAssessment", assessment.getId(), null, assessment.getAssessmentNumber(), null, null);
        return AssessmentResponse.from(assessment);
    }

    @Transactional
    public AssessmentResponse update(String id, UpdateAssessmentRequest request) {
        CompetencyAssessment assessment = requireAssessment(id);
        assertDraft(assessment);
        Auditor auditor = auditorService.requireAuditor(assessment.getAuditorId());
        if (request.assessedOn() != null) {
            assessment.setAssessedOn(request.assessedOn());
        }
        if (request.assessorName() != null) {
            assessment.setAssessorName(blankToNull(request.assessorName()));
        }
        if (request.standardId() != null) {
            assessment.setStandardId(resolveStandard(assessment.getTenantId(), request.standardId()));
        }
        if (request.schemeId() != null) {
            assessment.setSchemeId(resolveScheme(assessment.getTenantId(), request.schemeId()));
        }
        if (request.competencyId() != null) {
            assessment.setCompetencyId(resolveCompetency(auditor, request.competencyId()));
        }
        if (request.notes() != null) {
            assessment.setNotes(blankToNull(request.notes()));
        }
        assessmentRepository.save(assessment);
        return AssessmentResponse.from(assessment);
    }

    @Transactional
    public AssessmentResponse complete(String id, CompleteAssessmentRequest request) {
        CompetencyAssessment assessment = requireAssessment(id);
        assertDraft(assessment);
        assessment.setResult(request.result());
        if (request.notes() != null) {
            assessment.setNotes(blankToNull(request.notes()));
        }
        assessment.setStatus(AssessmentStatus.RECORDED);
        assessmentRepository.save(assessment);
        auditLogService.record("ASSESSMENT_COMPLETE", "CompetencyAssessment", assessment.getId(), "DRAFT", "RECORDED", null, null);
        return AssessmentResponse.from(assessment);
    }

    public CompetencyAssessment requireAssessment(String id) {
        CompetencyAssessment assessment = assessmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Competency assessment not found"));
        isolationService.assertCanAccessTenant(assessment.getTenantId());
        return assessment;
    }

    private void assertDraft(CompetencyAssessment assessment) {
        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Recorded assessments cannot be changed");
        }
    }

    private String resolveCompetency(Auditor auditor, String competencyId) {
        if (competencyId == null || competencyId.isBlank()) {
            return null;
        }
        AuditorCompetency competency = competencyService.requireCompetency(competencyId);
        if (!auditor.getId().equals(competency.getAuditorId()) || !auditor.getTenantId().equals(competency.getTenantId())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Competency does not belong to this auditor");
        }
        return competency.getId();
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

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
