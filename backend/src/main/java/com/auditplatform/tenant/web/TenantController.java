package com.auditplatform.tenant.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.tenant.api.CreateTenantRequest;
import com.auditplatform.tenant.api.TenantResponse;
import com.auditplatform.tenant.service.TenantService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants")
@Tag(name = "Tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TENANT_VIEW')")
    public ApiResponse<PageResponse<TenantResponse>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(tenantService.list(pageable), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TENANT_VIEW')")
    public ApiResponse<TenantResponse> get(@PathVariable String id) {
        return ApiResponse.ok(tenantService.get(id), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TENANT_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TenantResponse> create(@Valid @RequestBody CreateTenantRequest request) {
        return ApiResponse.ok(tenantService.create(request), MDC.get(CorrelationId.MDC_KEY));
    }
}
