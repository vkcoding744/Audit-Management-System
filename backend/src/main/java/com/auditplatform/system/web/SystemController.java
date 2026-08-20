package com.auditplatform.system.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.config.AuditPlatformProperties;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.system.api.SystemHealthResponse;
import com.auditplatform.system.api.SystemInfoResponse;
import com.auditplatform.system.service.SystemHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "System")
public class SystemController {

    private final SystemHealthService healthService;
    private final AuditPlatformProperties properties;
    private final Environment environment;

    public SystemController(
            SystemHealthService healthService,
            AuditPlatformProperties properties,
            Environment environment
    ) {
        this.healthService = healthService;
        this.properties = properties;
        this.environment = environment;
    }

    @GetMapping("/health")
    @Operation(summary = "Application and database health")
    public ResponseEntity<ApiResponse<SystemHealthResponse>> health() {
        SystemHealthResponse payload = healthService.health();
        HttpStatus status = "UP".equals(payload.status()) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(ApiResponse.ok(payload, MDC.get(CorrelationId.MDC_KEY)));
    }

    @GetMapping("/info")
    @Operation(summary = "Non-sensitive platform information")
    public ApiResponse<SystemInfoResponse> info() {
        String envName = environment.getActiveProfiles().length == 0
                ? "default"
                : String.join(",", environment.getActiveProfiles());
        SystemInfoResponse payload = new SystemInfoResponse(
                "audit-platform",
                properties.api().version(),
                envName
        );
        return ApiResponse.ok(payload, MDC.get(CorrelationId.MDC_KEY));
    }
}
