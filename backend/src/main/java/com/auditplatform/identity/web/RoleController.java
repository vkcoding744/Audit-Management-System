package com.auditplatform.identity.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.identity.api.PermissionResponse;
import com.auditplatform.identity.api.RoleResponse;
import com.auditplatform.identity.service.RoleQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.MDC;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Roles")
public class RoleController {

    private final RoleQueryService roleQueryService;

    public RoleController(RoleQueryService roleQueryService) {
        this.roleQueryService = roleQueryService;
    }

    @GetMapping("/api/v1/roles")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ApiResponse<List<RoleResponse>> roles() {
        return ApiResponse.ok(roleQueryService.listRoles(), MDC.get(CorrelationId.MDC_KEY));
    }

    @GetMapping("/api/v1/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public ApiResponse<List<PermissionResponse>> permissions() {
        return ApiResponse.ok(roleQueryService.listPermissions(), MDC.get(CorrelationId.MDC_KEY));
    }
}
