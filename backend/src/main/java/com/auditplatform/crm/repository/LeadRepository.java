package com.auditplatform.crm.repository;

import com.auditplatform.crm.domain.Lead;
import com.auditplatform.crm.domain.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeadRepository extends JpaRepository<Lead, String> {

    Optional<Lead> findByIdAndDeletedAtIsNull(String id);

    Page<Lead> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Lead> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, LeadStatus status, Pageable pageable);
}
