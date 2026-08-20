package com.auditplatform.auditor.service;

import com.auditplatform.auditor.api.EligibilityResponse;
import com.auditplatform.auditor.domain.Auditor;
import com.auditplatform.auditor.domain.AuditorCompetency;
import com.auditplatform.auditor.domain.AuditorStatus;
import com.auditplatform.auditor.domain.AvailabilityKind;
import com.auditplatform.auditor.domain.CompetencyStatus;
import com.auditplatform.auditor.repository.AuditorAvailabilityRepository;
import com.auditplatform.auditor.repository.AuditorCompetencyRepository;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditorEligibilityService {

    private final AuditorService auditorService;
    private final AuditorCompetencyRepository competencyRepository;
    private final AuditorAvailabilityRepository availabilityRepository;
    private final Clock clock;

    public AuditorEligibilityService(
            AuditorService auditorService,
            AuditorCompetencyRepository competencyRepository,
            AuditorAvailabilityRepository availabilityRepository,
            Clock clock
    ) {
        this.auditorService = auditorService;
        this.competencyRepository = competencyRepository;
        this.availabilityRepository = availabilityRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public EligibilityResponse evaluate(String auditorId, String standardId, String schemeId, LocalDate on) {
        if ((standardId == null || standardId.isBlank()) && (schemeId == null || schemeId.isBlank())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "standardId or schemeId is required for assignment eligibility");
        }
        LocalDate date = on == null ? LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC) : on;
        Auditor auditor = auditorService.requireAuditor(auditorId);
        List<String> reasons = new ArrayList<>();
        if (auditor.getStatus() == AuditorStatus.INACTIVE) {
            reasons.add("AUDITOR_INACTIVE");
        }
        if (auditor.getStatus() == AuditorStatus.SUSPENDED) {
            reasons.add("AUDITOR_SUSPENDED");
        }

        List<AuditorCompetency> matching = competencyRepository
                .findByTenantIdAndAuditorIdAndDeletedAtIsNullOrderByValidFromDesc(auditor.getTenantId(), auditor.getId())
                .stream()
                .filter(item -> item.covers(blankToNull(standardId), blankToNull(schemeId)))
                .toList();
        if (matching.isEmpty()) {
            reasons.add("NO_COMPETENCY");
        } else if (matching.stream().noneMatch(item -> item.isCurrentOn(date))) {
            if (matching.stream().anyMatch(item -> item.getStatus() == CompetencyStatus.ACTIVE && item.isExpiredOn(date))) {
                reasons.add("COMPETENCY_EXPIRED");
            } else {
                reasons.add("COMPETENCY_SUSPENDED");
            }
        }

        boolean unavailable = availabilityRepository
                .findByTenantIdAndAuditorIdAndDeletedAtIsNullOrderByStartOnAsc(auditor.getTenantId(), auditor.getId())
                .stream()
                .anyMatch(item -> item.getKind() == AvailabilityKind.UNAVAILABLE && item.covers(date));
        if (unavailable) {
            reasons.add("UNAVAILABLE");
        }

        return new EligibilityResponse(
                auditor.getId(),
                blankToNull(standardId),
                blankToNull(schemeId),
                date,
                reasons.isEmpty(),
                reasons
        );
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
