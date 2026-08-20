package com.auditplatform.governance.repository;

import com.auditplatform.governance.domain.ImpartialityRecord;
import com.auditplatform.governance.domain.ImpartialityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImpartialityRecordRepository extends JpaRepository<ImpartialityRecord, String> {

    Optional<ImpartialityRecord> findByIdAndDeletedAtIsNull(String id);

    Page<ImpartialityRecord> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<ImpartialityRecord> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, ImpartialityStatus status, Pageable pageable);
}
