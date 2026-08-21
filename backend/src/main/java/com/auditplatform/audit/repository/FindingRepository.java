package com.auditplatform.audit.repository;

import com.auditplatform.audit.domain.Finding;
import com.auditplatform.audit.domain.FindingSeverity;
import com.auditplatform.audit.domain.FindingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FindingRepository extends JpaRepository<Finding, String> {

    Optional<Finding> findByIdAndDeletedAtIsNull(String id);

    Page<Finding> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Finding> findByTenantIdAndClientIdAndDeletedAtIsNull(String tenantId, String clientId, Pageable pageable);

    Page<Finding> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, FindingStatus status, Pageable pageable);

    List<Finding> findByTenantIdAndAuditIdAndDeletedAtIsNullOrderByFindingNumberAsc(String tenantId, String auditId);

    long countByTenantIdAndClientIdAndStatusAndDeletedAtIsNull(String tenantId, String clientId, FindingStatus status);

    long countByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, FindingStatus status);

    long countByAuditIdAndStatusAndSeverityInAndDeletedAtIsNull(
            String auditId,
            FindingStatus status,
            Collection<FindingSeverity> severities
    );

    @Query("""
            select f from Finding f
            where f.tenantId = :tenantId and f.deletedAt is null
              and (
                lower(f.title) like lower(concat('%', :q, '%')) escape '\\'
                or lower(f.findingNumber) like lower(concat('%', :q, '%')) escape '\\'
              )
            """)
    Page<Finding> search(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);
}
