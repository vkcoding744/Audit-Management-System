package com.auditplatform.governance.repository;

import com.auditplatform.governance.domain.Complaint;
import com.auditplatform.governance.domain.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
