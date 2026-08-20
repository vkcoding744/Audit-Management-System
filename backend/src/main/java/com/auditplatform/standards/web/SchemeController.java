package com.auditplatform.standards.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.standards.api.CreateSchemeRequest;
import com.auditplatform.standards.api.LinkStandardRequest;
import com.auditplatform.standards.api.SchemeResponse;
import com.auditplatform.standards.api.UpdateSchemeRequest;
import com.auditplatform.standards.domain.SchemeStatus;
import com.auditplatform.standards.service.SchemeService;
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

@RestController
@RequestMapping("/api/v1/schemes")
@Tag(name = "Schemes")
public class SchemeController {

    private final SchemeService schemeService;

    public SchemeController(SchemeService schemeService) {
        this.schemeService = schemeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCHEME_VIEW')")
    public ApiResponse<PageResponse<SchemeResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) SchemeStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(schemeService.list(q, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCHEME_VIEW')")
    public ApiResponse<SchemeResponse> get(@PathVariable String id) {
        return ApiResponse.ok(schemeService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCHEME_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SchemeResponse> create(@Valid @RequestBody CreateSchemeRequest request) {
        return ApiResponse.ok(schemeService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('SCHEME_UPDATE')")
    public ApiResponse<SchemeResponse> update(@PathVariable String id, @Valid @RequestBody UpdateSchemeRequest request) {
        return ApiResponse.ok(schemeService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('SCHEME_UPDATE')")
    public ApiResponse<SchemeResponse> activate(@PathVariable String id) {
        return ApiResponse.ok(schemeService.activate(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('SCHEME_UPDATE')")
    public ApiResponse<SchemeResponse> suspend(@PathVariable String id) {
        return ApiResponse.ok(schemeService.suspend(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/retire")
    @PreAuthorize("hasAuthority('SCHEME_UPDATE')")
    public ApiResponse<SchemeResponse> retire(@PathVariable String id) {
        return ApiResponse.ok(schemeService.retire(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/standards")
    @PreAuthorize("hasAuthority('SCHEME_UPDATE')")
    public ApiResponse<SchemeResponse> linkStandard(
            @PathVariable String id,
            @Valid @RequestBody LinkStandardRequest request
    ) {
        return ApiResponse.ok(schemeService.linkStandard(id, request.standardId()), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/{id}/standards/{standardId}")
    @PreAuthorize("hasAuthority('SCHEME_UPDATE')")
    public ApiResponse<SchemeResponse> unlinkStandard(@PathVariable String id, @PathVariable String standardId) {
        return ApiResponse.ok(schemeService.unlinkStandard(id, standardId), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCHEME_DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        schemeService.delete(id);
    }
}
