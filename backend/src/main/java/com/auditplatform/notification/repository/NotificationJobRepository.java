package com.auditplatform.notification.repository;

import com.auditplatform.notification.domain.NotificationJob;
import com.auditplatform.notification.domain.NotificationJobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationJobRepository extends JpaRepository<NotificationJob, String> {

    Optional<NotificationJob> findByIdAndDeletedAtIsNull(String id);

    Page<NotificationJob> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<NotificationJob> findByTenantIdAndStatusAndDeletedAtIsNull(
            String tenantId,
            NotificationJobStatus status,
            Pageable pageable
    );

    @Query("""
            select j from NotificationJob j
            where j.deletedAt is null
              and j.status = com.auditplatform.notification.domain.NotificationJobStatus.QUEUED
              and j.scheduledFor is not null
              and j.scheduledFor <= :now
              and (:tenantId is null or j.tenantId = :tenantId)
            order by j.scheduledFor asc
            """)
    List<NotificationJob> findDueQueued(
            @Param("now") Instant now,
            @Param("tenantId") String tenantId,
            Pageable pageable
    );
}
