package com.auditplatform.auditlog.repository;

import com.auditplatform.auditlog.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    Optional<AuditLog> findById(String id);

    Page<AuditLog> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    Page<AuditLog> findByTenantIdAndActionOrderByCreatedAtDesc(String tenantId, String action, Pageable pageable);

    Page<AuditLog> findByTenantIdAndEntityTypeOrderByCreatedAtDesc(String tenantId, String entityType, Pageable pageable);

    Page<AuditLog> findByTenantIdAndActionAndEntityTypeOrderByCreatedAtDesc(
            String tenantId,
            String action,
            String entityType,
            Pageable pageable
    );
}
