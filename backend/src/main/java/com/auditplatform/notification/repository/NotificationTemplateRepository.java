package com.auditplatform.notification.repository;

import com.auditplatform.notification.domain.NotificationTemplate;
import com.auditplatform.notification.domain.TemplateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, String> {

    Optional<NotificationTemplate> findByIdAndDeletedAtIsNull(String id);

    Optional<NotificationTemplate> findByTenantIdAndCodeAndDeletedAtIsNull(String tenantId, String code);

    Page<NotificationTemplate> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<NotificationTemplate> findByTenantIdAndStatusAndDeletedAtIsNull(
            String tenantId,
            TemplateStatus status,
            Pageable pageable
    );
}
