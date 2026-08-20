package com.auditplatform.governance.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.auditor.service.AuditorService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.crm.service.ClientService;
import com.auditplatform.governance.api.CreateImpartialityRequest;
import com.auditplatform.governance.api.ImpartialityResponse;
import com.auditplatform.governance.api.NotesRequest;
import com.auditplatform.governance.api.UpdateImpartialityRequest;
import com.auditplatform.governance.domain.ImpartialityRecord;
import com.auditplatform.governance.domain.ImpartialityStatus;
import com.auditplatform.governance.repository.ImpartialityRecordRepository;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class ImpartialityService {

    private final ImpartialityRecordRepository recordRepository;
    private final GovernanceNumberService numberService;
    private final AuditorService auditorService;
    private final ClientService clientService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public ImpartialityService(
            ImpartialityRecordRepository recordRepository,
            GovernanceNumberService numberService,
            AuditorService auditorService,
            ClientService clientService,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.recordRepository = recordRepository;
        this.numberService = numberService;
        this.auditorService = auditorService;
        this.clientService = clientService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<ImpartialityResponse> list(ImpartialityStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<ImpartialityRecord> page = status == null
                ? recordRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                : recordRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        return PageResponse.from(page.map(ImpartialityResponse::from));
    }

    @Transactional(readOnly = true)
    public ImpartialityResponse get(String id) {
        return ImpartialityResponse.from(requireRecord(id));
    }

    @Transactional
    public ImpartialityResponse create(CreateImpartialityRequest request) {
        String tenantId = isolationService.requireTenantScope();
        ImpartialityRecord record = new ImpartialityRecord();
        record.setTenantId(tenantId);
        record.setImpartialityNumber(numberService.nextImpartiality(tenantId));
        record.setTitle(request.title().trim());
        record.setAuditorId(resolveAuditor(request.auditorId()));
        record.setClientId(resolveClient(request.clientId()));
        record.setIdentifiedOn(request.identifiedOn() == null ? today() : request.identifiedOn());
        record.setDescription(blankToNull(request.description()));
        record.setStatus(ImpartialityStatus.OPEN);
        recordRepository.save(record);
        auditLogService.record("IMPARTIALITY_CREATE", "ImpartialityRecord", record.getId(), null, record.getImpartialityNumber(), null, null);
        return ImpartialityResponse.from(record);
    }

    @Transactional
    public ImpartialityResponse update(String id, UpdateImpartialityRequest request) {
        ImpartialityRecord record = requireOpen(id);
        if (request.title() != null && !request.title().isBlank()) {
            record.setTitle(request.title().trim());
        }
        if (request.identifiedOn() != null) {
            record.setIdentifiedOn(request.identifiedOn());
        }
        if (request.description() != null) {
            record.setDescription(blankToNull(request.description()));
        }
        if (request.reviewNotes() != null) {
            record.setReviewNotes(blankToNull(request.reviewNotes()));
        }
        recordRepository.save(record);
        return ImpartialityResponse.from(record);
    }

    @Transactional
    public ImpartialityResponse startReview(String id, NotesRequest request) {
        ImpartialityRecord record = requireRecord(id);
        if (record.getStatus() != ImpartialityStatus.OPEN) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only open impartiality records can enter review");
        }
        if (request != null && request.notes() != null) {
            record.setReviewNotes(blankToNull(request.notes()));
        }
        record.setStatus(ImpartialityStatus.REVIEWED);
        recordRepository.save(record);
        auditLogService.record("IMPARTIALITY_REVIEW", "ImpartialityRecord", record.getId(), "OPEN", "REVIEWED", null, null);
        return ImpartialityResponse.from(record);
    }

    @Transactional
    public ImpartialityResponse close(String id, NotesRequest request) {
        ImpartialityRecord record = requireOpen(id);
        if (request != null && request.notes() != null) {
            record.setReviewNotes(blankToNull(request.notes()));
        }
        record.setClosedOn(today());
        record.setStatus(ImpartialityStatus.CLOSED);
        recordRepository.save(record);
        auditLogService.record("IMPARTIALITY_CLOSE", "ImpartialityRecord", record.getId(), null, "CLOSED", null, null);
        return ImpartialityResponse.from(record);
    }

    public ImpartialityRecord requireRecord(String id) {
        ImpartialityRecord record = recordRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Impartiality record not found"));
        isolationService.assertCanAccessTenant(record.getTenantId());
        return record;
    }

    private ImpartialityRecord requireOpen(String id) {
        ImpartialityRecord record = requireRecord(id);
        if (record.getStatus() == ImpartialityStatus.CLOSED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Closed impartiality records cannot be changed");
        }
        return record;
    }

    private String resolveAuditor(String auditorId) {
        if (auditorId == null || auditorId.isBlank()) {
            return null;
        }
        return auditorService.requireAuditor(auditorId).getId();
    }

    private String resolveClient(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return null;
        }
        return clientService.requireClient(clientId).getId();
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
