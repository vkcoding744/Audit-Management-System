package com.auditplatform.audit.repository;

import com.auditplatform.audit.domain.AuditSite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuditSiteRepository extends JpaRepository<AuditSite, String> {

    Optional<AuditSite> findByIdAndDeletedAtIsNull(String id);

    List<AuditSite> findByTenantIdAndAuditIdAndDeletedAtIsNull(String tenantId, String auditId);

    boolean existsByAuditIdAndSiteIdAndDeletedAtIsNull(String auditId, String siteId);
}
