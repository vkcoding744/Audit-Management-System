package com.auditplatform.crm.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.crm.api.CreateSiteRequest;
import com.auditplatform.crm.api.SiteResponse;
import com.auditplatform.crm.api.UpdateSiteRequest;
import com.auditplatform.crm.service.SiteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Sites")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping("/api/v1/clients/{clientId}/sites")
    @PreAuthorize("hasAuthority('SITE_VIEW')")
    public ApiResponse<List<SiteResponse>> list(@PathVariable String clientId) {
        return ApiResponse.ok(siteService.listByClient(clientId), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/api/v1/clients/{clientId}/sites")
    @PreAuthorize("hasAuthority('SITE_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SiteResponse> create(@PathVariable String clientId, @Valid @RequestBody CreateSiteRequest request) {
        return ApiResponse.ok(siteService.create(clientId, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/api/v1/sites/{id}")
    @PreAuthorize("hasAuthority('SITE_UPDATE')")
    public ApiResponse<SiteResponse> update(@PathVariable String id, @Valid @RequestBody UpdateSiteRequest request) {
        return ApiResponse.ok(siteService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/api/v1/sites/{id}")
    @PreAuthorize("hasAuthority('SITE_DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        siteService.delete(id);
    }
}
