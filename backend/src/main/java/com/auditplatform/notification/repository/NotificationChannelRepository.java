package com.auditplatform.notification.repository;

import com.auditplatform.notification.domain.NotificationChannel;
import com.auditplatform.notification.domain.NotificationChannelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, String> {

    Optional<NotificationChannel> findByIdAndDeletedAtIsNull(String id);

    Optional<NotificationChannel> findByTenantIdAndChannelAndDeletedAtIsNull(String tenantId, NotificationChannelType channel);

    List<NotificationChannel> findByTenantIdAndDeletedAtIsNullOrderByChannelAsc(String tenantId);
}
