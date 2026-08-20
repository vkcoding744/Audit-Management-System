package com.auditplatform.training.repository;

import com.auditplatform.training.domain.TrainingRecord;
import com.auditplatform.training.domain.TrainingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, String> {

    Optional<TrainingRecord> findByIdAndDeletedAtIsNull(String id);

    Page<TrainingRecord> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<TrainingRecord> findByTenantIdAndAuditorIdAndDeletedAtIsNull(String tenantId, String auditorId, Pageable pageable);

    Page<TrainingRecord> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, TrainingStatus status, Pageable pageable);

    Page<TrainingRecord> findByTenantIdAndAuditorIdAndStatusAndDeletedAtIsNull(
            String tenantId,
            String auditorId,
            TrainingStatus status,
            Pageable pageable
    );
}
