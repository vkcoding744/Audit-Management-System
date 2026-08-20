package com.auditplatform.audit.web;

import com.auditplatform.audit.api.AuditItemResponse;
import com.auditplatform.audit.api.AuditResponse;
import com.auditplatform.audit.api.UpdateAuditItemRequest;
import com.auditplatform.audit.api.UpdateExecutionRequest;
import com.auditplatform.audit.service.ExecutionService;
import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.web.CorrelationId;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Audit execution")
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/audits/{id}/start")
    @PreAuthorize("hasAuthority('AUDIT_UPDATE')")
    public ApiResponse<AuditResponse> start(@PathVariable String id) {
        return ApiResponse.ok(executionService.start(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/audits/{id}/complete")
    @PreAuthorize("hasAuthority('AUDIT_UPDATE')")
    public ApiResponse<AuditResponse> complete(@PathVariable String id) {
        return ApiResponse.ok(executionService.complete(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/audits/{id}/execution")
    @PreAuthorize("hasAuthority('AUDIT_UPDATE')")
    public ApiResponse<AuditResponse> updateNotes(
            @PathVariable String id,
            @Valid @RequestBody UpdateExecutionRequest request
    ) {
        return ApiResponse.ok(executionService.updateNotes(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/audits/{id}/responses")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<List<AuditItemResponse>> responses(@PathVariable String id) {
        return ApiResponse.ok(executionService.listResponses(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/audit-responses/{responseId}")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<AuditItemResponse> getResponse(@PathVariable String responseId) {
        return ApiResponse.ok(executionService.getResponse(responseId), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/audit-responses/{responseId}")
    @PreAuthorize("hasAuthority('AUDIT_UPDATE')")
    public ApiResponse<AuditItemResponse> updateResponse(
            @PathVariable String responseId,
            @Valid @RequestBody UpdateAuditItemRequest request
    ) {
        return ApiResponse.ok(executionService.updateResponse(responseId, request), MDC.get(CorrelationId.MDC_KEY));
    }
}
