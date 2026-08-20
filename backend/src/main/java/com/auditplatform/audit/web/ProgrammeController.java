package com.auditplatform.audit.web;

import com.auditplatform.audit.api.AuditResponse;
import com.auditplatform.audit.api.CreateProgrammeRequest;
import com.auditplatform.audit.api.ProgrammeResponse;
import com.auditplatform.audit.api.UpdateProgrammeRequest;
import com.auditplatform.audit.domain.ProgrammeStatus;
import com.auditplatform.audit.service.AuditService;
import com.auditplatform.audit.service.ProgrammeService;
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
@RequestMapping("/api/v1/programmes")
@Tag(name = "Programmes")
public class ProgrammeController {

    private final ProgrammeService programmeService;
    private final AuditService auditService;

    public ProgrammeController(ProgrammeService programmeService, AuditService auditService) {
        this.programmeService = programmeService;
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<PageResponse<ProgrammeResponse>> list(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) ProgrammeStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(programmeService.list(clientId, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('AUDIT_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProgrammeResponse> create(@Valid @RequestBody CreateProgrammeRequest request) {
        return ApiResponse.ok(programmeService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<ProgrammeResponse> get(@PathVariable String id) {
        return ApiResponse.ok(programmeService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDIT_UPDATE')")
    public ApiResponse<ProgrammeResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateProgrammeRequest request
    ) {
        return ApiResponse.ok(programmeService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('AUDIT_UPDATE')")
    public ApiResponse<ProgrammeResponse> activate(@PathVariable String id) {
        return ApiResponse.ok(programmeService.activate(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('AUDIT_UPDATE')")
    public ApiResponse<ProgrammeResponse> complete(@PathVariable String id) {
        return ApiResponse.ok(programmeService.complete(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('AUDIT_UPDATE')")
    public ApiResponse<ProgrammeResponse> cancel(@PathVariable String id) {
        return ApiResponse.ok(programmeService.cancel(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDIT_DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        programmeService.delete(id);
    }

    @GetMapping("/{id}/audits")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<List<AuditResponse>> audits(@PathVariable String id) {
        return ApiResponse.ok(auditService.listByProgramme(id), MDC.get(CorrelationId.MDC_KEY));
    }
}
