package com.auditplatform.audit.repository;

import com.auditplatform.audit.domain.Audit;
import com.auditplatform.audit.domain.AuditStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AuditRepository extends JpaRepository<Audit, String> {

    Optional<Audit> findByIdAndDeletedAtIsNull(String id);

    Page<Audit> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Audit> findByTenantIdAndClientIdAndDeletedAtIsNull(String tenantId, String clientId, Pageable pageable);

    Page<Audit> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, AuditStatus status, Pageable pageable);

    List<Audit> findByTenantIdAndProgrammeIdAndDeletedAtIsNullOrderByPlannedStartOnAsc(String tenantId, String programmeId);

    boolean existsByProgrammeIdAndDeletedAtIsNull(String programmeId);

    long countByTenantIdAndClientIdAndStatusInAndDeletedAtIsNull(
            String tenantId,
            String clientId,
            Collection<AuditStatus> statuses
    );
}
