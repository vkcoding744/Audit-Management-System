package com.auditplatform.notification.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.notification.api.CreateTemplateRequest;
import com.auditplatform.notification.api.TemplateResponse;
import com.auditplatform.notification.api.UpdateTemplateRequest;
import com.auditplatform.notification.domain.TemplateStatus;
import com.auditplatform.notification.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification-templates")
@Tag(name = "Notification templates")
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    public NotificationTemplateController(NotificationTemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    public ApiResponse<PageResponse<TemplateResponse>> list(
            @RequestParam(required = false) TemplateStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(templateService.list(status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TemplateResponse> create(@Valid @RequestBody CreateTemplateRequest request) {
        return ApiResponse.ok(templateService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    public ApiResponse<TemplateResponse> get(@PathVariable String id) {
        return ApiResponse.ok(templateService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE')")
    public ApiResponse<TemplateResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateTemplateRequest request
    ) {
        return ApiResponse.ok(templateService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE')")
    public ApiResponse<TemplateResponse> activate(@PathVariable String id) {
        return ApiResponse.ok(templateService.activate(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE')")
    public ApiResponse<TemplateResponse> deactivate(@PathVariable String id) {
        return ApiResponse.ok(templateService.deactivate(id), MDC.get(CorrelationId.MDC_KEY));
    }
}
