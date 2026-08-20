package com.auditplatform.identity.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.identity.api.CreateUserRequest;
import com.auditplatform.identity.api.UpdateUserRequest;
import com.auditplatform.identity.api.UserSummaryResponse;
import com.auditplatform.identity.api.VerifyEmailIssueResponse;
import com.auditplatform.identity.domain.Role;
import com.auditplatform.identity.domain.UserAccount;
import com.auditplatform.identity.domain.UserStatus;
import com.auditplatform.identity.repository.RoleRepository;
import com.auditplatform.identity.repository.UserAccountRepository;
import com.auditplatform.tenant.repository.TenantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final IsolationService isolationService;
    private final AuthService authService;
    private final AuditLogService auditLogService;

    public UserService(
            UserAccountRepository userAccountRepository,
            RoleRepository roleRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder,
            IsolationService isolationService,
            AuthService authService,
            AuditLogService auditLogService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.isolationService = isolationService;
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> list(Pageable pageable) {
        PlatformPrincipal principal = isolationService.requirePrincipal();
        Page<UserAccount> page;
        if (principal.platformAdmin()) {
            String scoped = isolationService.effectiveTenantId();
            page = scoped == null
                    ? userAccountRepository.findByDeletedAtIsNull(pageable)
                    : userAccountRepository.findByTenantIdAndDeletedAtIsNull(scoped, pageable);
        } else {
            page = userAccountRepository.findByTenantIdAndDeletedAtIsNull(principal.tenantId(), pageable);
        }
        return PageResponse.from(page.map(UserMapper::toSummary));
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse get(String id) {
        UserAccount user = load(id);
        isolationService.assertCanAccessTenant(user.getTenantId());
        if (user.getTenantId() == null && !isolationService.requirePrincipal().platformAdmin()) {
            throw new ApiException(ErrorCode.SYS_FORBIDDEN, "Access denied");
        }
        return UserMapper.toSummary(requireRoles(user));
    }

    @Transactional
    public UserSummaryResponse create(CreateUserRequest request) {
        PlatformPrincipal actor = isolationService.requirePrincipal();
        if (userAccountRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "Email is already registered");
        }
        String tenantId = resolveTenantForCreate(actor, request);
        if (request.roleCodes().contains("PLATFORM_SUPER_ADMIN") && !actor.platformAdmin()) {
            throw new ApiException(ErrorCode.SYS_FORBIDDEN, "Cannot assign platform super admin");
        }
        List<Role> roles = roleRepository.findSystemRolesWithPermissions(request.roleCodes());
        if (roles.size() != Set.copyOf(request.roleCodes()).size()) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Unknown role code");
        }
        UserAccount user = new UserAccount();
        user.setTenantId(tenantId);
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setStatus(request.active() ? UserStatus.ACTIVE : UserStatus.PENDING_ACTIVATION);
        user.setPasswordChangedAt(Instant.now());
        user.setRoles(new HashSet<>(roles));
        userAccountRepository.save(user);
        authService.issueEmailVerification(user);
        auditLogService.record("USER_CREATE", "User", user.getId(), null, user.getEmail(), null, null);
        return UserMapper.toSummary(requireRoles(user));
    }

    @Transactional
    public UserSummaryResponse update(String id, UpdateUserRequest request) {
        UserAccount user = load(id);
        isolationService.assertCanAccessTenant(user.getTenantId());
        if (request.firstName() != null && !request.firstName().isBlank()) {
            user.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            user.setLastName(request.lastName().trim());
        }
        if (request.roleCodes() != null) {
            PlatformPrincipal actor = isolationService.requirePrincipal();
            if (request.roleCodes().contains("PLATFORM_SUPER_ADMIN") && !actor.platformAdmin()) {
                throw new ApiException(ErrorCode.SYS_FORBIDDEN, "Cannot assign platform super admin");
            }
            List<Role> roles = roleRepository.findSystemRolesWithPermissions(request.roleCodes());
            if (roles.size() != Set.copyOf(request.roleCodes()).size()) {
                throw new ApiException(ErrorCode.SYS_VALIDATION, "Unknown role code");
            }
            user.setRoles(new HashSet<>(roles));
            auditLogService.record("PERMISSION_CHANGE", "User", user.getId(), null, String.join(",", request.roleCodes()), null, null);
        }
        userAccountRepository.save(user);
        return UserMapper.toSummary(requireRoles(user));
    }

    @Transactional
    public UserSummaryResponse setActive(String id, boolean active) {
        UserAccount user = load(id);
        isolationService.assertCanAccessTenant(user.getTenantId());
        user.setStatus(active ? UserStatus.ACTIVE : UserStatus.DISABLED);
        if (!active) {
            authService.logoutAll(user.getId(), null, null);
        }
        userAccountRepository.save(user);
        auditLogService.record(active ? "USER_ACTIVATE" : "USER_DEACTIVATE", "User", user.getId(), null, user.getStatus().name(), null, null);
        return UserMapper.toSummary(requireRoles(user));
    }

    @Transactional
    public VerifyEmailIssueResponse resendVerification(String userId) {
        UserAccount user = requireRoles(load(userId));
        isolationService.assertCanAccessTenant(user.getTenantId());
        String token = authService.issueEmailVerification(user);
        return new VerifyEmailIssueResponse("Verification message queued", token);
    }

    private String resolveTenantForCreate(PlatformPrincipal actor, CreateUserRequest request) {
        boolean platformRole = request.roleCodes().contains("PLATFORM_SUPER_ADMIN");
        if (platformRole) {
            if (!actor.platformAdmin()) {
                throw new ApiException(ErrorCode.SYS_FORBIDDEN, "Access denied");
            }
            return null;
        }
        if (actor.platformAdmin()) {
            if (request.tenantId() == null || request.tenantId().isBlank()) {
                throw new ApiException(ErrorCode.SYS_VALIDATION, "tenantId is required");
            }
            tenantRepository.findById(request.tenantId())
                    .filter(tenant -> !tenant.isDeleted())
                    .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Tenant not found"));
            return request.tenantId();
        }
        return actor.tenantId();
    }

    private UserAccount load(String id) {
        return userAccountRepository.findById(id)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "User not found"));
    }

    private UserAccount requireRoles(UserAccount user) {
        return userAccountRepository.findByIdWithRoles(user.getId()).orElse(user);
    }
}
