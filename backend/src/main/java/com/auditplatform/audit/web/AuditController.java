package com.auditplatform.audit.web;

import com.auditplatform.audit.api.AddAuditSiteRequest;
import com.auditplatform.audit.api.AssignmentResponse;
import com.auditplatform.audit.api.AuditResponse;
import com.auditplatform.audit.api.AuditSiteResponse;
import com.auditplatform.audit.api.CreateAssignmentRequest;
import com.auditplatform.audit.api.CreateAuditRequest;
import com.auditplatform.audit.api.UpdateAuditRequest;
import com.auditplatform.audit.domain.AuditStatus;
import com.auditplatform.audit.service.AssignmentService;
import com.auditplatform.audit.service.AuditService;
import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Audits")
public class AuditController {

    private final AuditService auditService;
    private final AssignmentService assignmentService;

    public AuditController(AuditService auditService, AssignmentService assignmentService) {
        this.auditService = auditService;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/audits")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<PageResponse<AuditResponse>> list(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) AuditStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(auditService.list(clientId, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/audits")
    @PreAuthorize("hasAuthority('AUDIT_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuditResponse> create(@Valid @RequestBody CreateAuditRequest request) {
        return ApiResponse.ok(auditService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/audits/{id}")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<AuditResponse> get(@PathVariable String id) {
        return ApiResponse.ok(auditService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/audits/{id}")
    @PreAuthorize("hasAuthority('AUDIT_UPDATE')")
    public ApiResponse<AuditResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateAuditRequest request
    ) {
        return ApiResponse.ok(auditService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/audits/{id}/schedule")
    @PreAuthorize("hasAuthority('AUDIT_UPDATE')")
    public ApiResponse<AuditResponse> schedule(@PathVariable String id) {
        return ApiResponse.ok(auditService.schedule(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/audits/{id}/cancel")
    @PreAuthorize("hasAuthority('AUDIT_UPDATE')")
    public ApiResponse<AuditResponse> cancel(@PathVariable String id) {
        return ApiResponse.ok(auditService.cancel(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/audits/{id}")
    @PreAuthorize("hasAuthority('AUDIT_DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        auditService.delete(id);
    }

    @GetMapping("/audits/{id}/sites")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<List<AuditSiteResponse>> sites(@PathVariable String id) {
        return ApiResponse.ok(auditService.listSites(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/audits/{id}/sites")
    @PreAuthorize("hasAuthority('AUDIT_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuditSiteResponse> addSite(
            @PathVariable String id,
            @Valid @RequestBody AddAuditSiteRequest request
    ) {
        return ApiResponse.ok(auditService.addSite(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/audit-sites/{siteRowId}")
    @PreAuthorize("hasAuthority('AUDIT_UPDATE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSite(@PathVariable String siteRowId) {
        auditService.removeSite(siteRowId);
    }

    @GetMapping("/audits/{id}/assignments")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<List<AssignmentResponse>> assignments(@PathVariable String id) {
        return ApiResponse.ok(assignmentService.list(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/audits/{id}/assignments")
    @PreAuthorize("hasAuthority('AUDIT_ASSIGN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AssignmentResponse> assign(
            @PathVariable String id,
            @Valid @RequestBody CreateAssignmentRequest request
    ) {
        return ApiResponse.ok(assignmentService.assign(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasAuthority('AUDIT_ASSIGN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unassign(@PathVariable String assignmentId) {
        assignmentService.unassign(assignmentId);
    }
}
