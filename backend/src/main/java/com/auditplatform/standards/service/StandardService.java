package com.auditplatform.standards.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.api.CreateStandardRequest;
import com.auditplatform.standards.api.StandardResponse;
import com.auditplatform.standards.api.UpdateStandardRequest;
import com.auditplatform.standards.domain.Standard;
import com.auditplatform.standards.domain.StandardStatus;
import com.auditplatform.standards.repository.ChecklistRepository;
import com.auditplatform.standards.repository.StandardClauseRepository;
import com.auditplatform.standards.repository.StandardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class StandardService {

    private final StandardRepository standardRepository;
    private final StandardClauseRepository clauseRepository;
    private final ChecklistRepository checklistRepository;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;

    public StandardService(
            StandardRepository standardRepository,
            StandardClauseRepository clauseRepository,
            ChecklistRepository checklistRepository,
            IsolationService isolationService,
            AuditLogService auditLogService
    ) {
        this.standardRepository = standardRepository;
        this.clauseRepository = clauseRepository;
        this.checklistRepository = checklistRepository;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<StandardResponse> list(String query, StandardStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<Standard> page;
        if (query != null && !query.isBlank()) {
            page = standardRepository.search(tenantId, query.trim(), pageable);
        } else if (status != null) {
            page = standardRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = standardRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(StandardResponse::from));
    }

    @Transactional(readOnly = true)
    public StandardResponse get(String id) {
        return StandardResponse.from(requireStandard(id));
    }

    @Transactional
    public StandardResponse create(CreateStandardRequest request) {
        String tenantId = isolationService.requireTenantScope();
        String code = request.code().trim();
        if (standardRepository.existsByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code)) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "A standard with this code already exists");
        }
        Standard standard = new Standard();
        standard.setTenantId(tenantId);
        standard.setCode(code);
        standard.setName(request.name().trim());
        standard.setPublisher(blankToNull(request.publisher()));
        standard.setEdition(blankToNull(request.edition()));
        standard.setDescription(blankToNull(request.description()));
        StandardStatus status = request.status() == null ? StandardStatus.DRAFT : request.status();
        if (status != StandardStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "New standards must be created as DRAFT");
        }
        standard.setStatus(status);
        standard.setNotes(blankToNull(request.notes()));
        standardRepository.save(standard);
        auditLogService.record("STANDARD_CREATE", "Standard", standard.getId(), null, standard.getCode(), null, null);
        return StandardResponse.from(standard);
    }

    @Transactional
    public StandardResponse update(String id, UpdateStandardRequest request) {
        Standard standard = requireDraft(id);
        if (request.code() != null && !request.code().isBlank()) {
            String code = request.code().trim();
            if (!code.equals(standard.getCode())
                    && standardRepository.existsByTenantIdAndCodeAndDeletedAtIsNull(standard.getTenantId(), code)) {
                throw new ApiException(ErrorCode.SYS_CONFLICT, "A standard with this code already exists");
            }
            standard.setCode(code);
        }
        if (request.name() != null && !request.name().isBlank()) {
            standard.setName(request.name().trim());
        }
        if (request.publisher() != null) {
            standard.setPublisher(blankToNull(request.publisher()));
        }
        if (request.edition() != null) {
            standard.setEdition(blankToNull(request.edition()));
        }
        if (request.description() != null) {
            standard.setDescription(blankToNull(request.description()));
        }
        if (request.notes() != null) {
            standard.setNotes(blankToNull(request.notes()));
        }
        if (request.status() != null && request.status() != standard.getStatus()) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Use publish, supersede, or withdraw to change status");
        }
        standardRepository.save(standard);
        auditLogService.record("STANDARD_UPDATE", "Standard", standard.getId(), null, standard.getCode(), null, null);
        return StandardResponse.from(standard);
    }

    @Transactional
    public StandardResponse publish(String id) {
        Standard standard = requireStandard(id);
        if (standard.getStatus() != StandardStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only draft standards can be published");
        }
        standard.setStatus(StandardStatus.PUBLISHED);
        standard.setPublishedAt(Instant.now());
        standardRepository.save(standard);
        auditLogService.record("STANDARD_PUBLISH", "Standard", standard.getId(), "DRAFT", "PUBLISHED", null, null);
        return StandardResponse.from(standard);
    }

    @Transactional
    public StandardResponse supersede(String id) {
        Standard standard = requireStandard(id);
        if (standard.getStatus() != StandardStatus.PUBLISHED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only published standards can be superseded");
        }
        standard.setStatus(StandardStatus.SUPERSEDED);
        standardRepository.save(standard);
        auditLogService.record("STANDARD_SUPERSEDE", "Standard", standard.getId(), "PUBLISHED", "SUPERSEDED", null, null);
        return StandardResponse.from(standard);
    }

    @Transactional
    public StandardResponse withdraw(String id) {
        Standard standard = requireStandard(id);
        if (standard.getStatus() != StandardStatus.PUBLISHED && standard.getStatus() != StandardStatus.SUPERSEDED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only published or superseded standards can be withdrawn");
        }
        String previous = standard.getStatus().name();
        standard.setStatus(StandardStatus.WITHDRAWN);
        standardRepository.save(standard);
        auditLogService.record("STANDARD_WITHDRAW", "Standard", standard.getId(), previous, "WITHDRAWN", null, null);
        return StandardResponse.from(standard);
    }

    @Transactional
    public void delete(String id) {
        Standard standard = requireStandard(id);
        if (checklistRepository.existsByStandardIdAndDeletedAtIsNull(standard.getId())) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "Cannot delete a standard that is used by a checklist");
        }
        Instant now = Instant.now();
        standard.setDeletedAt(now);
        standardRepository.save(standard);
        clauseRepository.findByTenantIdAndStandardIdAndDeletedAtIsNullOrderBySortOrderAscClauseCodeAsc(
                standard.getTenantId(),
                standard.getId()
        ).forEach(clause -> {
            clause.setDeletedAt(now);
            clauseRepository.save(clause);
        });
        auditLogService.record("STANDARD_DELETE", "Standard", standard.getId(), standard.getCode(), null, null, null);
    }

    public Standard requireStandard(String id) {
        Standard standard = standardRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Standard not found"));
        isolationService.assertCanAccessTenant(standard.getTenantId());
        return standard;
    }

    Standard requireDraft(String id) {
        Standard standard = requireStandard(id);
        if (standard.getStatus() != StandardStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Clauses and metadata can only be edited while the standard is DRAFT");
        }
        return standard;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
