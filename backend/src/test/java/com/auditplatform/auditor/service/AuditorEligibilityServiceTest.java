package com.auditplatform.auditor.service;

import com.auditplatform.auditor.api.EligibilityResponse;
import com.auditplatform.auditor.domain.Auditor;
import com.auditplatform.auditor.domain.AuditorCompetency;
import com.auditplatform.auditor.domain.AuditorStatus;
import com.auditplatform.auditor.domain.CompetencyStatus;
import com.auditplatform.auditor.repository.AuditorAvailabilityRepository;
import com.auditplatform.auditor.repository.AuditorCompetencyRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditorEligibilityServiceTest {

    @Test
    void expiredCompetencyBlocksAssignment() {
        Auditor auditor = new Auditor();
        auditor.setTenantId("tenant-a");
        auditor.setStatus(AuditorStatus.ACTIVE);

        AuditorCompetency expired = new AuditorCompetency();
        expired.setTenantId("tenant-a");
        expired.setAuditorId("a1");
        expired.setStandardId("std-1");
        expired.setStatus(CompetencyStatus.ACTIVE);
        expired.setValidFrom(LocalDate.of(2024, 1, 1));
        expired.setValidTo(LocalDate.of(2025, 12, 31));

        AuditorService auditorService = mock(AuditorService.class);
        when(auditorService.requireAuditor("a1")).thenReturn(auditor);
        AuditorCompetencyRepository competencies = mock(AuditorCompetencyRepository.class);
        when(competencies.findByTenantIdAndAuditorIdAndDeletedAtIsNullOrderByValidFromDesc("tenant-a", "a1"))
                .thenReturn(List.of(expired));
        AuditorAvailabilityRepository availability = mock(AuditorAvailabilityRepository.class);
        when(availability.findByTenantIdAndAuditorIdAndDeletedAtIsNullOrderByStartOnAsc("tenant-a", "a1"))
                .thenReturn(List.of());

        Clock clock = Clock.fixed(Instant.parse("2026-08-20T00:00:00Z"), ZoneOffset.UTC);
        AuditorEligibilityService service = new AuditorEligibilityService(auditorService, competencies, availability, clock);

        EligibilityResponse result = service.evaluate("a1", "std-1", null, LocalDate.of(2026, 8, 20));

        assertThat(result.eligible()).isFalse();
        assertThat(result.reasons()).contains("COMPETENCY_EXPIRED");
    }

    @Test
    void currentCompetencyAllowsAssignment() {
        Auditor auditor = new Auditor();
        auditor.setTenantId("tenant-a");
        auditor.setStatus(AuditorStatus.ACTIVE);

        AuditorCompetency current = new AuditorCompetency();
        current.setTenantId("tenant-a");
        current.setAuditorId("a1");
        current.setStandardId("std-1");
        current.setStatus(CompetencyStatus.ACTIVE);
        current.setValidFrom(LocalDate.of(2026, 1, 1));
        current.setValidTo(LocalDate.of(2026, 12, 31));

        AuditorService auditorService = mock(AuditorService.class);
        when(auditorService.requireAuditor("a1")).thenReturn(auditor);
        AuditorCompetencyRepository competencies = mock(AuditorCompetencyRepository.class);
        when(competencies.findByTenantIdAndAuditorIdAndDeletedAtIsNullOrderByValidFromDesc("tenant-a", "a1"))
                .thenReturn(List.of(current));
        AuditorAvailabilityRepository availability = mock(AuditorAvailabilityRepository.class);
        when(availability.findByTenantIdAndAuditorIdAndDeletedAtIsNullOrderByStartOnAsc("tenant-a", "a1"))
                .thenReturn(List.of());

        Clock clock = Clock.fixed(Instant.parse("2026-08-20T00:00:00Z"), ZoneOffset.UTC);
        AuditorEligibilityService service = new AuditorEligibilityService(auditorService, competencies, availability, clock);

        EligibilityResponse result = service.evaluate("a1", "std-1", null, null);

        assertThat(result.eligible()).isTrue();
        assertThat(result.reasons()).isEmpty();
    }
}
