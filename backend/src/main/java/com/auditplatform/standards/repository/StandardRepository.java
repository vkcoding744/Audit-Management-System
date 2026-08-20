package com.auditplatform.standards.repository;

import com.auditplatform.standards.domain.Standard;
import com.auditplatform.standards.domain.StandardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StandardRepository extends JpaRepository<Standard, String> {

    Optional<Standard> findByIdAndDeletedAtIsNull(String id);

    Page<Standard> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Standard> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, StandardStatus status, Pageable pageable);

    boolean existsByTenantIdAndCodeAndDeletedAtIsNull(String tenantId, String code);

    @Query("""
            select s from Standard s
            where s.tenantId = :tenantId and s.deletedAt is null
              and (
                lower(s.code) like lower(concat('%', :q, '%'))
                or lower(s.name) like lower(concat('%', :q, '%'))
                or lower(coalesce(s.publisher, '')) like lower(concat('%', :q, '%'))
              )
            """)
    Page<Standard> search(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);
}
