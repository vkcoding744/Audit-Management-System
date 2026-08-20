package com.auditplatform.auditor.repository;

import com.auditplatform.auditor.domain.Auditor;
import com.auditplatform.auditor.domain.AuditorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuditorRepository extends JpaRepository<Auditor, String> {

    Optional<Auditor> findByIdAndDeletedAtIsNull(String id);

    Page<Auditor> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Auditor> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, AuditorStatus status, Pageable pageable);

    boolean existsByTenantIdAndUserIdAndDeletedAtIsNull(String tenantId, String userId);

    boolean existsByTenantIdAndUserIdAndIdNotAndDeletedAtIsNull(String tenantId, String userId, String id);

    @Query("""
            select a from Auditor a
            where a.tenantId = :tenantId and a.deletedAt is null
              and (
                lower(a.employeeNumber) like lower(concat('%', :q, '%'))
                or lower(a.firstName) like lower(concat('%', :q, '%'))
                or lower(a.lastName) like lower(concat('%', :q, '%'))
                or lower(coalesce(a.email, '')) like lower(concat('%', :q, '%'))
              )
            """)
    Page<Auditor> search(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);
}
