package com.auditplatform.governance.repository;

import com.auditplatform.governance.domain.Complaint;
import com.auditplatform.governance.domain.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, String> {

    Optional<Complaint> findByIdAndDeletedAtIsNull(String id);

    Page<Complaint> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Complaint> findByTenantIdAndClientIdAndDeletedAtIsNull(String tenantId, String clientId, Pageable pageable);

    Page<Complaint> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, ComplaintStatus status, Pageable pageable);

    long countByTenantIdAndClientIdAndStatusInAndDeletedAtIsNull(
            String tenantId,
            String clientId,
            Collection<ComplaintStatus> statuses
    );

    long countByTenantIdAndStatusInAndDeletedAtIsNull(String tenantId, Collection<ComplaintStatus> statuses);

    @Query("""
            select c from Complaint c
            where c.tenantId = :tenantId and c.deletedAt is null
              and (
                lower(c.subject) like lower(concat('%', :q, '%')) escape '\\'
                or lower(c.complaintNumber) like lower(concat('%', :q, '%')) escape '\\'
              )
            """)
    Page<Complaint> search(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);
}
