package com.auditplatform.dashboard.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.dashboard.api.TenantDashboardResponse;
import com.auditplatform.dashboard.service.TenantDashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.MDC;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {

    private final TenantDashboardService dashboardService;

    public DashboardController(TenantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ApiResponse<TenantDashboardResponse> summary() {
        return ApiResponse.ok(dashboardService.summary(), MDC.get(CorrelationId.MDC_KEY));
    }
}
