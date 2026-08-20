package com.auditplatform.crm.domain;

import com.auditplatform.common.persistence.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "sites")
@Getter
@Setter
public class Site extends TenantAwareEntity {

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city", length = 128)
    private String city;

    @Column(name = "state", length = 128)
    private String state;

    @Column(name = "postal_code", length = 32)
    private String postalCode;

    @Column(name = "country", length = 128)
    private String country;

    @Column(name = "scope", columnDefinition = "TEXT")
    private String scope;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "processes", columnDefinition = "TEXT")
    private String processes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private SiteStatus status = SiteStatus.ACTIVE;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
