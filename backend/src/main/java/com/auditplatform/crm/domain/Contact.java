package com.auditplatform.crm.domain;

import com.auditplatform.common.persistence.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "contacts")
@Getter
@Setter
public class Contact extends TenantAwareEntity {

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(name = "site_id", length = 36)
    private String siteId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "designation", length = 128)
    private String designation;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 64)
    private String phone;

    @Column(name = "department", length = 128)
    private String department;

    @Column(name = "primary_contact", nullable = false)
    private boolean primaryContact;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
