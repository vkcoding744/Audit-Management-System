package com.auditplatform.auditlog.web;

import com.auditplatform.auditlog.api.AuditLogResponse;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "Audit logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')")
    public ApiResponse<PageResponse<AuditLogResponse>> list(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(auditLogService.list(action, entityType, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')")
    public ApiResponse<AuditLogResponse> get(@PathVariable String id) {
        return ApiResponse.ok(auditLogService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }
}
