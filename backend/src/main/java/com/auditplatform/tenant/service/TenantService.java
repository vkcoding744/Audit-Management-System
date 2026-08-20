package com.auditplatform.tenant.service;

import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.identity.api.CreateUserRequest;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.identity.service.UserService;
import com.auditplatform.tenant.api.CreateTenantRequest;
import com.auditplatform.tenant.api.TenantResponse;
import com.auditplatform.tenant.domain.Tenant;
import com.auditplatform.tenant.domain.TenantStatus;
import com.auditplatform.tenant.repository.TenantRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserService userService;
    private final IsolationService isolationService;

    public TenantService(TenantRepository tenantRepository, UserService userService, IsolationService isolationService) {
        this.tenantRepository = tenantRepository;
        this.userService = userService;
        this.isolationService = isolationService;
    }

    @Transactional(readOnly = true)
    public PageResponse<TenantResponse> list(Pageable pageable) {
        PlatformPrincipal principal = isolationService.requirePrincipal();
        if (principal.platformAdmin()) {
            return PageResponse.from(tenantRepository.findAll(pageable).map(this::toResponse));
        }
        Tenant tenant = tenantRepository.findById(principal.tenantId())
                .filter(item -> !item.isDeleted())
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Tenant not found"));
        return new PageResponse<>(List.of(toResponse(tenant)), 0, 1, 1, 1);
    }

    @Transactional(readOnly = true)
    public TenantResponse get(String id) {
        isolationService.assertCanAccessTenant(id);
        Tenant tenant = tenantRepository.findById(id)
                .filter(item -> !item.isDeleted())
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Tenant not found"));
        return toResponse(tenant);
    }

    @Transactional
    public TenantResponse create(CreateTenantRequest request) {
        tenantRepository.findByCodeAndDeletedAtIsNull(request.code().trim().toLowerCase()).ifPresent(existing -> {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "Tenant code already exists");
        });
        Tenant tenant = new Tenant();
        tenant.setCode(request.code().trim().toLowerCase());
        tenant.setName(request.name().trim());
        tenant.setStatus(TenantStatus.ACTIVE);
        tenantRepository.save(tenant);

        if (request.adminEmail() != null && !request.adminEmail().isBlank()) {
            if (request.adminPassword() == null || request.adminFirstName() == null || request.adminLastName() == null) {
                throw new ApiException(ErrorCode.SYS_VALIDATION, "Admin name and password are required when adminEmail is set");
            }
            userService.create(new CreateUserRequest(
                    request.adminEmail(),
                    request.adminPassword(),
                    request.adminFirstName(),
                    request.adminLastName(),
                    tenant.getId(),
                    List.of("TENANT_ADMIN"),
                    true
            ));
        }
        return toResponse(tenant);
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(tenant.getId(), tenant.getCode(), tenant.getName(), tenant.getStatus().name());
    }
}
