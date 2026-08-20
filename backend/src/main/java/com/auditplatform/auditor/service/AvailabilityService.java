package com.auditplatform.auditor.service;

import com.auditplatform.auditor.api.AvailabilityResponse;
import com.auditplatform.auditor.api.CreateAvailabilityRequest;
import com.auditplatform.auditor.domain.Auditor;
import com.auditplatform.auditor.domain.AuditorAvailability;
import com.auditplatform.auditor.domain.AvailabilityKind;
import com.auditplatform.auditor.repository.AuditorAvailabilityRepository;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AvailabilityService {

    private final AuditorAvailabilityRepository availabilityRepository;
    private final AuditorService auditorService;
    private final IsolationService isolationService;

    public AvailabilityService(
            AuditorAvailabilityRepository availabilityRepository,
            AuditorService auditorService,
            IsolationService isolationService
    ) {
        this.availabilityRepository = availabilityRepository;
        this.auditorService = auditorService;
        this.isolationService = isolationService;
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> list(String auditorId) {
        Auditor auditor = auditorService.requireAuditor(auditorId);
        return availabilityRepository
                .findByTenantIdAndAuditorIdAndDeletedAtIsNullOrderByStartOnAsc(auditor.getTenantId(), auditor.getId())
                .stream()
                .map(AvailabilityResponse::from)
                .toList();
    }

    @Transactional
    public AvailabilityResponse create(String auditorId, CreateAvailabilityRequest request) {
        Auditor auditor = auditorService.requireAuditor(auditorId);
        if (request.endOn().isBefore(request.startOn())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "endOn cannot be before startOn");
        }
        AuditorAvailability availability = new AuditorAvailability();
        availability.setTenantId(auditor.getTenantId());
        availability.setAuditorId(auditor.getId());
        availability.setStartOn(request.startOn());
        availability.setEndOn(request.endOn());
        availability.setKind(request.kind() == null ? AvailabilityKind.UNAVAILABLE : request.kind());
        availability.setReason(blankToNull(request.reason()));
        availabilityRepository.save(availability);
        return AvailabilityResponse.from(availability);
    }

    @Transactional
    public void delete(String availabilityId) {
        AuditorAvailability availability = availabilityRepository.findByIdAndDeletedAtIsNull(availabilityId)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Availability window not found"));
        isolationService.assertCanAccessTenant(availability.getTenantId());
        availability.setDeletedAt(Instant.now());
        availabilityRepository.save(availability);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
