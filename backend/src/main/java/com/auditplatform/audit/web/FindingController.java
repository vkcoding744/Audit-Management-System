package com.auditplatform.audit.web;

import com.auditplatform.audit.api.CapaResponse;
import com.auditplatform.audit.api.CreateCapaRequest;
import com.auditplatform.audit.api.CreateFindingRequest;
import com.auditplatform.audit.api.FindingResponse;
import com.auditplatform.audit.api.UpdateCapaRequest;
import com.auditplatform.audit.api.UpdateFindingRequest;
import com.auditplatform.audit.domain.FindingStatus;
import com.auditplatform.audit.service.FindingService;
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
@Tag(name = "Findings")
public class FindingController {

    private final FindingService findingService;

    public FindingController(FindingService findingService) {
        this.findingService = findingService;
    }

    @GetMapping("/findings")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<PageResponse<FindingResponse>> list(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) FindingStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(findingService.list(clientId, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/findings")
    @PreAuthorize("hasAuthority('FINDING_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FindingResponse> create(@Valid @RequestBody CreateFindingRequest request) {
        return ApiResponse.ok(findingService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/findings/{id}")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<FindingResponse> get(@PathVariable String id) {
        return ApiResponse.ok(findingService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/findings/{id}")
    @PreAuthorize("hasAuthority('FINDING_UPDATE')")
    public ApiResponse<FindingResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateFindingRequest request
    ) {
        return ApiResponse.ok(findingService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/findings/{id}/close")
    @PreAuthorize("hasAuthority('FINDING_CLOSE')")
    public ApiResponse<FindingResponse> close(@PathVariable String id) {
        return ApiResponse.ok(findingService.close(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/audits/{auditId}/findings")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<List<FindingResponse>> byAudit(@PathVariable String auditId) {
        return ApiResponse.ok(findingService.listByAudit(auditId), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/findings/{id}/capa")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<List<CapaResponse>> listCapa(@PathVariable String id) {
        return ApiResponse.ok(findingService.listCapa(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/findings/{id}/capa")
    @PreAuthorize("hasAuthority('FINDING_UPDATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CapaResponse> addCapa(
            @PathVariable String id,
            @Valid @RequestBody CreateCapaRequest request
    ) {
        return ApiResponse.ok(findingService.addCapa(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/capa/{capaId}")
    @PreAuthorize("hasAuthority('FINDING_UPDATE')")
    public ApiResponse<CapaResponse> updateCapa(
            @PathVariable String capaId,
            @Valid @RequestBody UpdateCapaRequest request
    ) {
        return ApiResponse.ok(findingService.updateCapa(capaId, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/capa/{capaId}/complete")
    @PreAuthorize("hasAuthority('FINDING_UPDATE')")
    public ApiResponse<CapaResponse> completeCapa(@PathVariable String capaId) {
        return ApiResponse.ok(findingService.completeCapa(capaId), MDC.get(CorrelationId.MDC_KEY));
    }
}
