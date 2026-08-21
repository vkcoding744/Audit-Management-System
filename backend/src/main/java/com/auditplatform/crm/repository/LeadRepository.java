package com.auditplatform.crm.repository;

import com.auditplatform.crm.domain.Lead;
import com.auditplatform.crm.domain.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LeadRepository extends JpaRepository<Lead, String> {

    Optional<Lead> findByIdAndDeletedAtIsNull(String id);

    Page<Lead> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Lead> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, LeadStatus status, Pageable pageable);

    @Query("""
            select l from Lead l
            where l.tenantId = :tenantId and l.deletedAt is null
              and (
                lower(l.organisationName) like lower(concat('%', :q, '%')) escape '\\'
                or lower(l.leadNumber) like lower(concat('%', :q, '%')) escape '\\'
                or lower(coalesce(l.email, '')) like lower(concat('%', :q, '%')) escape '\\'
              )
            """)
    Page<Lead> search(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);
}
