package com.auditplatform.auditor.domain;

import com.auditplatform.common.persistence.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "auditor_availability")
@Getter
@Setter
public class AuditorAvailability extends TenantAwareEntity {

    @Column(name = "auditor_id", nullable = false, length = 36)
    private String auditorId;

    @Column(name = "start_on", nullable = false)
    private LocalDate startOn;

    @Column(name = "end_on", nullable = false)
    private LocalDate endOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 32)
    private AvailabilityKind kind = AvailabilityKind.UNAVAILABLE;

    @Column(name = "reason")
    private String reason;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean covers(LocalDate on) {
        return (on.isEqual(startOn) || on.isAfter(startOn)) && (on.isEqual(endOn) || on.isBefore(endOn));
    }
}
