package com.auditplatform.notification.service;

import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.notification.api.ChannelResponse;
import com.auditplatform.notification.api.UpdateChannelRequest;
import com.auditplatform.notification.domain.NotificationChannel;
import com.auditplatform.notification.domain.NotificationChannelType;
import com.auditplatform.notification.repository.NotificationChannelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationChannelService {

    private final NotificationChannelRepository channelRepository;
    private final IsolationService isolationService;

    public NotificationChannelService(
            NotificationChannelRepository channelRepository,
            IsolationService isolationService
    ) {
        this.channelRepository = channelRepository;
        this.isolationService = isolationService;
    }

    @Transactional
    public List<ChannelResponse> list() {
        String tenantId = isolationService.requireTenantScope();
        ensureDefaults(tenantId);
        return channelRepository.findByTenantIdAndDeletedAtIsNullOrderByChannelAsc(tenantId).stream()
                .map(ChannelResponse::from)
                .toList();
    }

    @Transactional
    public ChannelResponse update(String id, UpdateChannelRequest request) {
        NotificationChannel channel = requireChannel(id);
        if (request.enabled() != null) {
            channel.setEnabled(request.enabled());
        }
        if (request.fromAddress() != null) {
            channel.setFromAddress(request.fromAddress().isBlank() ? null : request.fromAddress().trim());
        }
        channelRepository.save(channel);
        return ChannelResponse.from(channel);
    }

    public NotificationChannel requireEnabled(String tenantId, NotificationChannelType type) {
        ensureDefaults(tenantId);
        NotificationChannel channel = channelRepository.findByTenantIdAndChannelAndDeletedAtIsNull(tenantId, type)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Notification channel not found"));
        if (!channel.isEnabled()) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Channel " + type.name() + " is disabled");
        }
        return channel;
    }

    public void ensureDefaults(String tenantId) {
        for (NotificationChannelType type : NotificationChannelType.values()) {
            channelRepository.findByTenantIdAndChannelAndDeletedAtIsNull(tenantId, type).orElseGet(() -> {
                NotificationChannel created = new NotificationChannel();
                created.setTenantId(tenantId);
                created.setChannel(type);
                created.setEnabled(true);
                return channelRepository.save(created);
            });
        }
    }

    public NotificationChannel requireChannel(String id) {
        NotificationChannel channel = channelRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Notification channel not found"));
        isolationService.assertCanAccessTenant(channel.getTenantId());
        return channel;
    }
}
