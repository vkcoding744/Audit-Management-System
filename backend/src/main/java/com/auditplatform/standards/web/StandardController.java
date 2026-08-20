package com.auditplatform.standards.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.standards.api.CreateStandardRequest;
import com.auditplatform.standards.api.StandardResponse;
import com.auditplatform.standards.api.UpdateStandardRequest;
import com.auditplatform.standards.domain.StandardStatus;
import com.auditplatform.standards.service.StandardService;
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
@RequestMapping("/api/v1/standards")
@Tag(name = "Standards")
public class StandardController {

    private final StandardService standardService;

    public StandardController(StandardService standardService) {
        this.standardService = standardService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STANDARD_VIEW')")
    public ApiResponse<PageResponse<StandardResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) StandardStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(standardService.list(q, status, pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STANDARD_VIEW')")
    public ApiResponse<StandardResponse> get(@PathVariable String id) {
        return ApiResponse.ok(standardService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('STANDARD_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StandardResponse> create(@Valid @RequestBody CreateStandardRequest request) {
        return ApiResponse.ok(standardService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('STANDARD_UPDATE')")
    public ApiResponse<StandardResponse> update(@PathVariable String id, @Valid @RequestBody UpdateStandardRequest request) {
        return ApiResponse.ok(standardService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('STANDARD_UPDATE')")
    public ApiResponse<StandardResponse> publish(@PathVariable String id) {
        return ApiResponse.ok(standardService.publish(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/supersede")
    @PreAuthorize("hasAuthority('STANDARD_UPDATE')")
    public ApiResponse<StandardResponse> supersede(@PathVariable String id) {
        return ApiResponse.ok(standardService.supersede(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasAuthority('STANDARD_UPDATE')")
    public ApiResponse<StandardResponse> withdraw(@PathVariable String id) {
        return ApiResponse.ok(standardService.withdraw(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('STANDARD_DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        standardService.delete(id);
    }
}
