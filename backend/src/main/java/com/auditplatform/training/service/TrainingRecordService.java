package com.auditplatform.training.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.auditor.domain.Auditor;
import com.auditplatform.auditor.service.AuditorService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.standards.domain.Scheme;
import com.auditplatform.standards.domain.Standard;
import com.auditplatform.standards.service.SchemeService;
import com.auditplatform.standards.service.StandardService;
import com.auditplatform.training.api.CompleteTrainingRequest;
import com.auditplatform.training.api.CreateTrainingRecordRequest;
import com.auditplatform.training.api.TrainingRecordResponse;
import com.auditplatform.training.api.UpdateTrainingRecordRequest;
import com.auditplatform.training.domain.TrainingRecord;
import com.auditplatform.training.domain.TrainingStatus;
import com.auditplatform.training.repository.TrainingRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class TrainingRecordService {

    private final TrainingRecordRepository trainingRecordRepository;
    private final TrainingNumberService numberService;
    private final AuditorService auditorService;
    private final StandardService standardService;
    private final SchemeService schemeService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public TrainingRecordService(
            TrainingRecordRepository trainingRecordRepository,
            TrainingNumberService numberService,
            AuditorService auditorService,
            StandardService standardService,
            SchemeService schemeService,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.trainingRecordRepository = trainingRecordRepository;
        this.numberService = numberService;
        this.auditorService = auditorService;
        this.standardService = standardService;
        this.schemeService = schemeService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<TrainingRecordResponse> list(String auditorId, TrainingStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<TrainingRecord> page;
        boolean hasAuditor = auditorId != null && !auditorId.isBlank();
        if (hasAuditor && status != null) {
            page = trainingRecordRepository.findByTenantIdAndAuditorIdAndStatusAndDeletedAtIsNull(
                    tenantId, auditorId, status, pageable);
        } else if (hasAuditor) {
            page = trainingRecordRepository.findByTenantIdAndAuditorIdAndDeletedAtIsNull(tenantId, auditorId, pageable);
        } else if (status != null) {
            page = trainingRecordRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = trainingRecordRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public TrainingRecordResponse get(String id) {
        return toResponse(requireRecord(id));
    }

    @Transactional
    public TrainingRecordResponse create(CreateTrainingRecordRequest request) {
        Auditor auditor = auditorService.requireAuditor(request.auditorId());
        TrainingRecord record = new TrainingRecord();
        record.setTenantId(auditor.getTenantId());
        record.setTrainingNumber(numberService.nextTraining(auditor.getTenantId()));
        record.setAuditorId(auditor.getId());
        record.setTitle(request.title().trim());
        record.setProvider(blankToNull(request.provider()));
        record.setPlannedOn(request.plannedOn());
        record.setCompletedOn(request.completedOn());
        record.setHours(request.hours());
        record.setExpiresOn(request.expiresOn());
        record.setStandardId(resolveStandard(auditor.getTenantId(), request.standardId()));
        record.setSchemeId(resolveScheme(auditor.getTenantId(), request.schemeId()));
        record.setNotes(blankToNull(request.notes()));
        record.setStatus(request.completedOn() != null ? TrainingStatus.COMPLETED : TrainingStatus.PLANNED);
        trainingRecordRepository.save(record);
        auditLogService.record("TRAINING_CREATE", "TrainingRecord", record.getId(), null, record.getTrainingNumber(), null, null);
        return toResponse(record);
    }

    @Transactional
    public TrainingRecordResponse update(String id, UpdateTrainingRecordRequest request) {
        TrainingRecord record = requireRecord(id);
        assertPlanned(record, "Only planned training records can be updated");
        if (request.title() != null && !request.title().isBlank()) {
            record.setTitle(request.title().trim());
        }
        if (request.provider() != null) {
            record.setProvider(blankToNull(request.provider()));
        }
        if (request.plannedOn() != null) {
            record.setPlannedOn(request.plannedOn());
        }
        if (request.completedOn() != null) {
            record.setCompletedOn(request.completedOn());
        }
        if (request.hours() != null) {
            record.setHours(request.hours());
        }
        if (request.expiresOn() != null) {
            record.setExpiresOn(request.expiresOn());
        }
        if (request.standardId() != null) {
            record.setStandardId(resolveStandard(record.getTenantId(), request.standardId()));
        }
        if (request.schemeId() != null) {
            record.setSchemeId(resolveScheme(record.getTenantId(), request.schemeId()));
        }
        if (request.notes() != null) {
            record.setNotes(blankToNull(request.notes()));
        }
        trainingRecordRepository.save(record);
        return toResponse(record);
    }

    @Transactional
    public TrainingRecordResponse complete(String id, CompleteTrainingRequest request) {
        TrainingRecord record = requireRecord(id);
        assertPlanned(record, "Only planned training records can be completed");
        LocalDate completedOn = request != null && request.completedOn() != null
                ? request.completedOn()
                : record.getCompletedOn();
        if (completedOn == null) {
            completedOn = today();
        }
        record.setCompletedOn(completedOn);
        if (request != null && request.notes() != null) {
            record.setNotes(blankToNull(request.notes()));
        }
        record.setStatus(TrainingStatus.COMPLETED);
        trainingRecordRepository.save(record);
        auditLogService.record("TRAINING_COMPLETE", "TrainingRecord", record.getId(), "PLANNED", "COMPLETED", null, null);
        return toResponse(record);
    }

    @Transactional
    public TrainingRecordResponse cancel(String id) {
        TrainingRecord record = requireRecord(id);
        assertPlanned(record, "Only planned training records can be cancelled");
        record.setStatus(TrainingStatus.CANCELLED);
        trainingRecordRepository.save(record);
        auditLogService.record("TRAINING_CANCEL", "TrainingRecord", record.getId(), "PLANNED", "CANCELLED", null, null);
        return toResponse(record);
    }

    public TrainingRecord requireRecord(String id) {
        TrainingRecord record = trainingRecordRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Training record not found"));
        isolationService.assertCanAccessTenant(record.getTenantId());
        return record;
    }

    private void assertPlanned(TrainingRecord record, String message) {
        if (record.getStatus() != TrainingStatus.PLANNED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, message);
        }
    }

    private TrainingRecordResponse toResponse(TrainingRecord record) {
        return TrainingRecordResponse.from(record, expired(record));
    }

    private boolean expired(TrainingRecord record) {
        return record.getStatus() == TrainingStatus.COMPLETED
                && record.getExpiresOn() != null
                && record.getExpiresOn().isBefore(today());
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
