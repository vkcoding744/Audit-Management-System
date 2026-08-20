package com.auditplatform.governance.domain;

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
@Table(name = "risks")
@Getter
@Setter
public class Risk extends TenantAwareEntity {

    @Column(name = "risk_number", nullable = false, length = 32)
    private String riskNumber;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private RiskCategory category = RiskCategory.OTHER;

    @Column(name = "likelihood")
    private Integer likelihood;

    @Column(name = "impact")
    private Integer impact;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private RiskStatus status = RiskStatus.OPEN;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "mitigation", columnDefinition = "TEXT")
    private String mitigation;

    @Column(name = "closed_on")
    private LocalDate closedOn;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
