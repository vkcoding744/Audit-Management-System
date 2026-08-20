package com.auditplatform.identity.service;

import com.auditplatform.identity.api.PermissionResponse;
import com.auditplatform.identity.api.RoleResponse;
import com.auditplatform.identity.domain.Permission;
import com.auditplatform.identity.domain.Role;
import com.auditplatform.identity.repository.PermissionRepository;
import com.auditplatform.identity.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleQueryService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleQueryService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        List<String> codes = List.of(
                "PLATFORM_SUPER_ADMIN", "TENANT_ADMIN", "CERTIFICATION_MANAGER", "AUDIT_MANAGER",
                "LEAD_AUDITOR", "AUDITOR", "TECHNICAL_REVIEWER", "CERTIFICATION_DECISION_MAKER",
                "ACCOUNTANT", "SALES_MANAGER", "SALES_EXECUTIVE", "DOCUMENT_CONTROLLER",
                "HR_COMPETENCY_MANAGER", "CLIENT_ADMIN", "CLIENT_USER", "READ_ONLY"
        );
        return roleRepository.findSystemRolesWithPermissions(codes).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionResponse(p.getId(), p.getCode(), p.getName(), p.getModule()))
                .toList();
    }

    private RoleResponse toResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.isSystemRole(),
                role.getPermissions().stream().map(Permission::getCode).collect(Collectors.toSet())
        );
    }
}
