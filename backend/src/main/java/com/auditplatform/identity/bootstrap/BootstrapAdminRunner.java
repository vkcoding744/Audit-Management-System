package com.auditplatform.identity.bootstrap;

import com.auditplatform.common.config.AuditPlatformProperties;
import com.auditplatform.identity.domain.Role;
import com.auditplatform.identity.domain.UserAccount;
import com.auditplatform.identity.domain.UserStatus;
import com.auditplatform.identity.repository.RoleRepository;
import com.auditplatform.identity.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

@Component
public class BootstrapAdminRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminRunner.class);

    private final AuditPlatformProperties properties;
    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public BootstrapAdminRunner(
            AuditPlatformProperties properties,
            UserAccountRepository userAccountRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.properties = properties;
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = properties.auth().bootstrapAdminEmail();
        String password = properties.auth().bootstrapAdminPassword();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            log.info("Bootstrap admin skipped (AUDIT_PLATFORM_BOOTSTRAP_ADMIN_EMAIL/PASSWORD not set)");
            return;
        }
        if (userAccountRepository.countByDeletedAtIsNull() > 0) {
            return;
        }
        List<Role> roles = roleRepository.findSystemRolesWithPermissions(List.of("PLATFORM_SUPER_ADMIN"));
        if (roles.isEmpty()) {
            throw new IllegalStateException("PLATFORM_SUPER_ADMIN role is missing from the database");
        }
        UserAccount admin = new UserAccount();
        admin.setEmail(email.trim().toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setFirstName("Platform");
        admin.setLastName("Admin");
        admin.setStatus(UserStatus.ACTIVE);
        admin.setEmailVerifiedAt(Instant.now());
        admin.setPasswordChangedAt(Instant.now());
        admin.setRoles(new HashSet<>(roles));
        userAccountRepository.save(admin);
        log.info("Bootstrap platform admin created for {}", email.charAt(0) + "***");
    }
}
