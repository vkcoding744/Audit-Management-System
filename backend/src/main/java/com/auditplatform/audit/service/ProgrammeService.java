package com.auditplatform.audit.service;

import com.auditplatform.audit.api.CreateProgrammeRequest;
import com.auditplatform.audit.api.ProgrammeResponse;
import com.auditplatform.audit.api.UpdateProgrammeRequest;
import com.auditplatform.audit.domain.AuditProgramme;
import com.auditplatform.audit.domain.ProgrammeStatus;
import com.auditplatform.audit.repository.AuditProgrammeRepository;
import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.crm.domain.Client;
import com.auditplatform.crm.service.ClientService;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.domain.Scheme;
import com.auditplatform.standards.domain.Standard;
import com.auditplatform.standards.repository.SchemeStandardRepository;
import com.auditplatform.standards.service.SchemeService;
import com.auditplatform.standards.service.StandardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ProgrammeService {

    private final AuditProgrammeRepository programmeRepository;
    private final AuditRepository auditRepository;
    private final AuditNumberService auditNumberService;
    private final ClientService clientService;
    private final SchemeService schemeService;
    private final StandardService standardService;
    private final SchemeStandardRepository schemeStandardRepository;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;

    public ProgrammeService(
            AuditProgrammeRepository programmeRepository,
            AuditRepository auditRepository,
            AuditNumberService auditNumberService,
            ClientService clientService,
            SchemeService schemeService,
            StandardService standardService,
            SchemeStandardRepository schemeStandardRepository,
            IsolationService isolationService,
            AuditLogService auditLogService
    ) {
        this.programmeRepository = programmeRepository;
        this.auditRepository = auditRepository;
        this.auditNumberService = auditNumberService;
        this.clientService = clientService;
        this.schemeService = schemeService;
        this.standardService = standardService;
        this.schemeStandardRepository = schemeStandardRepository;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProgrammeResponse> list(String clientId, ProgrammeStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<AuditProgramme> page;
        if (clientId != null && !clientId.isBlank()) {
            clientService.requireClient(clientId);
            page = programmeRepository.findByTenantIdAndClientIdAndDeletedAtIsNull(tenantId, clientId, pageable);
        } else if (status != null) {
            page = programmeRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = programmeRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(ProgrammeResponse::from));
    }

    @Transactional(readOnly = true)
    public ProgrammeResponse get(String id) {
        return ProgrammeResponse.from(requireProgramme(id));
    }

    @Transactional
    public ProgrammeResponse create(CreateProgrammeRequest request) {
        String tenantId = isolationService.requireTenantScope();
        Client client = clientService.requireClient(request.clientId());
        if (!tenantId.equals(client.getTenantId())) {
            throw new ApiException(ErrorCode.AUTH_TENANT_MISMATCH, "Client does not belong to this tenant");
        }
        Scheme scheme = schemeService.requireScheme(request.schemeId());
        if (!tenantId.equals(scheme.getTenantId())) {
            throw new ApiException(ErrorCode.AUTH_TENANT_MISMATCH, "Scheme does not belong to this tenant");
        }
        if (request.cycleStartOn() != null && request.cycleEndOn() != null && request.cycleEndOn().isBefore(request.cycleStartOn())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "cycleEndOn cannot be before cycleStartOn");
        }
        AuditProgramme programme = new AuditProgramme();
        programme.setTenantId(tenantId);
        programme.setProgrammeNumber(auditNumberService.nextProgramme(tenantId));
        programme.setClientId(client.getId());
        programme.setSchemeId(scheme.getId());
        programme.setStandardId(resolveStandard(tenantId, scheme.getId(), request.standardId()));
        programme.setName(request.name().trim());
        programme.setStatus(ProgrammeStatus.DRAFT);
        programme.setCycleStartOn(request.cycleStartOn());
        programme.setCycleEndOn(request.cycleEndOn());
        programme.setNotes(blankToNull(request.notes()));
        programmeRepository.save(programme);
        auditLogService.record("PROGRAMME_CREATE", "AuditProgramme", programme.getId(), null, programme.getProgrammeNumber(), null, null);
        return ProgrammeResponse.from(programme);
    }

    @Transactional
    public ProgrammeResponse update(String id, UpdateProgrammeRequest request) {
        AuditProgramme programme = requireMutable(id);
        if (request.name() != null && !request.name().isBlank()) {
            programme.setName(request.name().trim());
        }
        if (request.cycleStartOn() != null) {
            programme.setCycleStartOn(request.cycleStartOn());
        }
        if (request.cycleEndOn() != null) {
            programme.setCycleEndOn(request.cycleEndOn());
        }
        if (request.notes() != null) {
            programme.setNotes(blankToNull(request.notes()));
        }
        programmeRepository.save(programme);
        return ProgrammeResponse.from(programme);
    }

    @Transactional
    public ProgrammeResponse activate(String id) {
        AuditProgramme programme = requireProgramme(id);
        if (programme.getStatus() != ProgrammeStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only draft programmes can be activated");
        }
        programme.setStatus(ProgrammeStatus.ACTIVE);
        programmeRepository.save(programme);
        auditLogService.record("PROGRAMME_ACTIVATE", "AuditProgramme", programme.getId(), "DRAFT", "ACTIVE", null, null);
        return ProgrammeResponse.from(programme);
    }

    @Transactional
    public ProgrammeResponse complete(String id) {
        AuditProgramme programme = requireProgramme(id);
        if (programme.getStatus() != ProgrammeStatus.ACTIVE) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only active programmes can be completed");
        }
        programme.setStatus(ProgrammeStatus.COMPLETED);
        programmeRepository.save(programme);
        return ProgrammeResponse.from(programme);
    }

    @Transactional
    public ProgrammeResponse cancel(String id) {
        AuditProgramme programme = requireProgramme(id);
        if (programme.getStatus() == ProgrammeStatus.COMPLETED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Completed programmes cannot be cancelled");
        }
        programme.setStatus(ProgrammeStatus.CANCELLED);
        programmeRepository.save(programme);
        return ProgrammeResponse.from(programme);
    }

    @Transactional
    public void delete(String id) {
        AuditProgramme programme = requireProgramme(id);
        if (auditRepository.existsByProgrammeIdAndDeletedAtIsNull(programme.getId())) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "Cannot delete a programme that has audits");
        }
        programme.setDeletedAt(Instant.now());
        programmeRepository.save(programme);
        auditLogService.record("PROGRAMME_DELETE", "AuditProgramme", programme.getId(), programme.getProgrammeNumber(), null, null, null);
    }

    public AuditProgramme requireProgramme(String id) {
        AuditProgramme programme = programmeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Programme not found"));
        isolationService.assertCanAccessTenant(programme.getTenantId());
        return programme;
    }

    private AuditProgramme requireMutable(String id) {
        AuditProgramme programme = requireProgramme(id);
        if (programme.getStatus() == ProgrammeStatus.COMPLETED || programme.getStatus() == ProgrammeStatus.CANCELLED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Completed or cancelled programmes cannot be edited");
        }
        return programme;
    }

    private String resolveStandard(String tenantId, String schemeId, String standardId) {
        if (standardId == null || standardId.isBlank()) {
            return null;
        }
        Standard standard = standardService.requireStandard(standardId);
        if (!tenantId.equals(standard.getTenantId())) {
            throw new ApiException(ErrorCode.AUTH_TENANT_MISMATCH, "Standard does not belong to this tenant");
        }
        if (!schemeStandardRepository.existsBySchemeIdAndStandardIdAndDeletedAtIsNull(schemeId, standard.getId())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Standard is not linked to this scheme");
        }
        return standard.getId();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
