package com.auditplatform.notification.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.notification.api.ChannelResponse;
import com.auditplatform.notification.api.UpdateChannelRequest;
import com.auditplatform.notification.service.NotificationChannelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification-channels")
@Tag(name = "Notification channels")
public class NotificationChannelController {

    private final NotificationChannelService channelService;

    public NotificationChannelController(NotificationChannelService channelService) {
        this.channelService = channelService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    public ApiResponse<List<ChannelResponse>> list() {
        return ApiResponse.ok(channelService.list(), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE')")
    public ApiResponse<ChannelResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateChannelRequest request
    ) {
        return ApiResponse.ok(channelService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }
}
