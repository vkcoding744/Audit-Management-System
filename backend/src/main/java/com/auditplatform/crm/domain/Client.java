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
@Table(name = "clients")
@Getter
@Setter
public class Client extends TenantAwareEntity {

    @Column(name = "client_number", nullable = false, length = 32)
    private String clientNumber;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "trading_name")
    private String tradingName;

    @Column(name = "registration_number", length = 64)
    private String registrationNumber;

    @Column(name = "tax_number", length = 64)
    private String taxNumber;

    @Column(name = "industry", length = 128)
    private String industry;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 64)
    private String phone;

    @Column(name = "website")
    private String website;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ClientStatus status = ClientStatus.PROSPECT;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
