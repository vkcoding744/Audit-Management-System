package com.auditplatform.audit.repository;

import com.auditplatform.audit.domain.AuditProgramme;
import com.auditplatform.audit.domain.ProgrammeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuditProgrammeRepository extends JpaRepository<AuditProgramme, String> {

    Optional<AuditProgramme> findByIdAndDeletedAtIsNull(String id);

    Page<AuditProgramme> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<AuditProgramme> findByTenantIdAndClientIdAndDeletedAtIsNull(String tenantId, String clientId, Pageable pageable);

    Page<AuditProgramme> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, ProgrammeStatus status, Pageable pageable);
}
