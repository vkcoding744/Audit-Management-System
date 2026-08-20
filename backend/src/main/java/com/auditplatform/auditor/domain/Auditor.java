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

@Entity
@Table(name = "auditors")
@Getter
@Setter
public class Auditor extends TenantAwareEntity {

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "employee_number", nullable = false, length = 32)
    private String employeeNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 64)
    private String phone;

    @Column(name = "job_title", length = 128)
    private String jobTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 32)
    private EmploymentType employmentType = EmploymentType.EMPLOYEE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AuditorStatus status = AuditorStatus.ACTIVE;

    @Column(name = "base_location")
    private String baseLocation;

    @Column(name = "country", length = 128)
    private String country;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
