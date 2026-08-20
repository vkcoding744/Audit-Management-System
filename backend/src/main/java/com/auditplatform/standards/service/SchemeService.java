package com.auditplatform.standards.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.api.CreateSchemeRequest;
import com.auditplatform.standards.api.SchemeResponse;
import com.auditplatform.standards.api.StandardResponse;
import com.auditplatform.standards.api.UpdateSchemeRequest;
import com.auditplatform.standards.domain.Scheme;
import com.auditplatform.standards.domain.SchemeStandard;
import com.auditplatform.standards.domain.SchemeStatus;
import com.auditplatform.standards.domain.Standard;
import com.auditplatform.standards.domain.StandardStatus;
import com.auditplatform.standards.repository.ChecklistRepository;
import com.auditplatform.standards.repository.SchemeRepository;
import com.auditplatform.standards.repository.SchemeStandardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SchemeService {

    private final SchemeRepository schemeRepository;
    private final SchemeStandardRepository schemeStandardRepository;
    private final ChecklistRepository checklistRepository;
    private final StandardService standardService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;

    public SchemeService(
            SchemeRepository schemeRepository,
            SchemeStandardRepository schemeStandardRepository,
            ChecklistRepository checklistRepository,
            StandardService standardService,
            IsolationService isolationService,
            AuditLogService auditLogService
    ) {
        this.schemeRepository = schemeRepository;
        this.schemeStandardRepository = schemeStandardRepository;
        this.checklistRepository = checklistRepository;
        this.standardService = standardService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<SchemeResponse> list(String query, SchemeStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<Scheme> page;
        if (query != null && !query.isBlank()) {
            page = schemeRepository.search(tenantId, query.trim(), pageable);
        } else if (status != null) {
            page = schemeRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = schemeRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(scheme -> toResponse(scheme, false)));
    }

    @Transactional(readOnly = true)
    public SchemeResponse get(String id) {
        return toResponse(requireScheme(id), true);
    }

    @Transactional
    public SchemeResponse create(CreateSchemeRequest request) {
        String tenantId = isolationService.requireTenantScope();
        String code = request.code().trim();
        if (schemeRepository.existsByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code)) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "A scheme with this code already exists");
        }
        Scheme scheme = new Scheme();
        scheme.setTenantId(tenantId);
        scheme.setCode(code);
        scheme.setName(request.name().trim());
        scheme.setDescription(blankToNull(request.description()));
        scheme.setAccreditationBody(blankToNull(request.accreditationBody()));
        scheme.setCycleMonths(request.cycleMonths());
        scheme.setSurveillanceIntervalMonths(request.surveillanceIntervalMonths());
        SchemeStatus status = request.status() == null ? SchemeStatus.DRAFT : request.status();
        if (status != SchemeStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "New schemes must be created as DRAFT");
        }
        scheme.setStatus(status);
        scheme.setNotes(blankToNull(request.notes()));
        schemeRepository.save(scheme);
        auditLogService.record("SCHEME_CREATE", "Scheme", scheme.getId(), null, scheme.getCode(), null, null);
        return toResponse(scheme, true);
    }

    @Transactional
    public SchemeResponse update(String id, UpdateSchemeRequest request) {
        Scheme scheme = requireScheme(id);
        if (scheme.getStatus() == SchemeStatus.RETIRED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Retired schemes cannot be edited");
        }
        if (request.code() != null && !request.code().isBlank()) {
            String code = request.code().trim();
            if (!code.equals(scheme.getCode())
                    && schemeRepository.existsByTenantIdAndCodeAndDeletedAtIsNull(scheme.getTenantId(), code)) {
                throw new ApiException(ErrorCode.SYS_CONFLICT, "A scheme with this code already exists");
            }
            scheme.setCode(code);
        }
        if (request.name() != null && !request.name().isBlank()) {
            scheme.setName(request.name().trim());
        }
        if (request.description() != null) {
            scheme.setDescription(blankToNull(request.description()));
        }
        if (request.accreditationBody() != null) {
            scheme.setAccreditationBody(blankToNull(request.accreditationBody()));
        }
        if (request.cycleMonths() != null) {
            scheme.setCycleMonths(request.cycleMonths());
        }
        if (request.surveillanceIntervalMonths() != null) {
            scheme.setSurveillanceIntervalMonths(request.surveillanceIntervalMonths());
        }
        if (request.notes() != null) {
            scheme.setNotes(blankToNull(request.notes()));
        }
        if (request.status() != null && request.status() != scheme.getStatus()) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Use activate, suspend, or retire to change status");
        }
        schemeRepository.save(scheme);
        auditLogService.record("SCHEME_UPDATE", "Scheme", scheme.getId(), null, scheme.getCode(), null, null);
        return toResponse(scheme, true);
    }

    @Transactional
    public SchemeResponse activate(String id) {
        Scheme scheme = requireScheme(id);
        if (scheme.getStatus() != SchemeStatus.DRAFT && scheme.getStatus() != SchemeStatus.SUSPENDED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only draft or suspended schemes can be activated");
        }
        scheme.setStatus(SchemeStatus.ACTIVE);
        schemeRepository.save(scheme);
        auditLogService.record("SCHEME_ACTIVATE", "Scheme", scheme.getId(), null, "ACTIVE", null, null);
        return toResponse(scheme, true);
    }

    @Transactional
    public SchemeResponse suspend(String id) {
        Scheme scheme = requireScheme(id);
        if (scheme.getStatus() != SchemeStatus.ACTIVE) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only active schemes can be suspended");
        }
        scheme.setStatus(SchemeStatus.SUSPENDED);
        schemeRepository.save(scheme);
        return toResponse(scheme, true);
    }

    @Transactional
    public SchemeResponse retire(String id) {
        Scheme scheme = requireScheme(id);
        if (scheme.getStatus() == SchemeStatus.RETIRED) {
            return toResponse(scheme, true);
        }
        scheme.setStatus(SchemeStatus.RETIRED);
        schemeRepository.save(scheme);
        auditLogService.record("SCHEME_RETIRE", "Scheme", scheme.getId(), null, "RETIRED", null, null);
        return toResponse(scheme, true);
    }

    @Transactional
    public SchemeResponse linkStandard(String schemeId, String standardId) {
        Scheme scheme = requireScheme(schemeId);
        Standard standard = standardService.requireStandard(standardId);
        if (!scheme.getTenantId().equals(standard.getTenantId())) {
            throw new ApiException(ErrorCode.AUTH_TENANT_MISMATCH, "Cross-tenant access is not allowed");
        }
        if (standard.getStatus() == StandardStatus.WITHDRAWN) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Withdrawn standards cannot be linked to a scheme");
        }
        if (!schemeStandardRepository.existsBySchemeIdAndStandardIdAndDeletedAtIsNull(scheme.getId(), standard.getId())) {
            SchemeStandard link = new SchemeStandard();
            link.setTenantId(scheme.getTenantId());
            link.setSchemeId(scheme.getId());
            link.setStandardId(standard.getId());
            schemeStandardRepository.save(link);
            auditLogService.record("SCHEME_LINK_STANDARD", "Scheme", scheme.getId(), null, standard.getCode(), null, null);
        }
        return toResponse(scheme, true);
    }

    @Transactional
    public SchemeResponse unlinkStandard(String schemeId, String standardId) {
        Scheme scheme = requireScheme(schemeId);
        schemeStandardRepository.findBySchemeIdAndStandardIdAndDeletedAtIsNull(scheme.getId(), standardId)
                .ifPresent(link -> {
                    isolationService.assertCanAccessTenant(link.getTenantId());
                    link.setDeletedAt(Instant.now());
                    schemeStandardRepository.save(link);
                });
        return toResponse(scheme, true);
    }

    @Transactional
    public void delete(String id) {
        Scheme scheme = requireScheme(id);
        if (checklistRepository.existsBySchemeIdAndDeletedAtIsNull(scheme.getId())) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "Cannot delete a scheme that has checklists");
        }
        Instant now = Instant.now();
        scheme.setDeletedAt(now);
        schemeRepository.save(scheme);
        schemeStandardRepository.findByTenantIdAndSchemeIdAndDeletedAtIsNull(scheme.getTenantId(), scheme.getId())
                .forEach(link -> {
                    link.setDeletedAt(now);
                    schemeStandardRepository.save(link);
                });
        auditLogService.record("SCHEME_DELETE", "Scheme", scheme.getId(), scheme.getCode(), null, null, null);
    }

    public Scheme requireScheme(String id) {
        Scheme scheme = schemeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Scheme not found"));
        isolationService.assertCanAccessTenant(scheme.getTenantId());
        return scheme;
    }

    private SchemeResponse toResponse(Scheme scheme, boolean includeStandards) {
        List<StandardResponse> standards = List.of();
        if (includeStandards) {
            standards = schemeStandardRepository
                    .findByTenantIdAndSchemeIdAndDeletedAtIsNull(scheme.getTenantId(), scheme.getId())
                    .stream()
                    .map(link -> StandardResponse.from(standardService.requireStandard(link.getStandardId())))
                    .toList();
        }
        return SchemeResponse.from(scheme, standards);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
