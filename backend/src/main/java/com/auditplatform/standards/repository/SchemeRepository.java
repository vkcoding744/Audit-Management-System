package com.auditplatform.standards.repository;

import com.auditplatform.standards.domain.Scheme;
import com.auditplatform.standards.domain.SchemeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SchemeRepository extends JpaRepository<Scheme, String> {

    Optional<Scheme> findByIdAndDeletedAtIsNull(String id);

    Page<Scheme> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Scheme> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, SchemeStatus status, Pageable pageable);

    boolean existsByTenantIdAndCodeAndDeletedAtIsNull(String tenantId, String code);

    @Query("""
            select s from Scheme s
            where s.tenantId = :tenantId and s.deletedAt is null
              and (
                lower(s.code) like lower(concat('%', :q, '%'))
                or lower(s.name) like lower(concat('%', :q, '%'))
              )
            """)
    Page<Scheme> search(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);
}
