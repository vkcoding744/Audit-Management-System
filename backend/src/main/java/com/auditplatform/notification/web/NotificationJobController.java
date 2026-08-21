package com.auditplatform.notification.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.notification.api.CreateJobRequest;
import com.auditplatform.notification.api.NotificationJobResponse;
import com.auditplatform.notification.domain.NotificationJobStatus;
import com.auditplatform.notification.service.NotificationJobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification-jobs")
@Tag(name = "Notification jobs")
public class NotificationJobController {

    private final NotificationJobService jobService;

    public NotificationJobController(NotificationJobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    public ApiResponse<PageResponse<NotificationJobResponse>> list(
            @RequestParam(required = false) NotificationJobStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(jobService.list(status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NotificationJobResponse> create(@Valid @RequestBody CreateJobRequest request) {
        return ApiResponse.ok(jobService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    public ApiResponse<NotificationJobResponse> get(@PathVariable String id) {
        return ApiResponse.ok(jobService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE')")
    public ApiResponse<NotificationJobResponse> send(@PathVariable String id) {
        return ApiResponse.ok(jobService.send(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE')")
    public ApiResponse<NotificationJobResponse> cancel(@PathVariable String id) {
        return ApiResponse.ok(jobService.cancel(id), MDC.get(CorrelationId.MDC_KEY));
    }
}
