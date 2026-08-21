package com.auditplatform.notification.repository;

import com.auditplatform.notification.domain.NotificationJob;
import com.auditplatform.notification.domain.NotificationJobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationJobRepository extends JpaRepository<NotificationJob, String> {

    Optional<NotificationJob> findByIdAndDeletedAtIsNull(String id);

    Page<NotificationJob> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<NotificationJob> findByTenantIdAndStatusAndDeletedAtIsNull(
            String tenantId,
            NotificationJobStatus status,
            Pageable pageable
    );
}
