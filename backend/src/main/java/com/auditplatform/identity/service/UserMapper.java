package com.auditplatform.identity.service;

import com.auditplatform.identity.api.UserSummaryResponse;
import com.auditplatform.identity.domain.Role;
import com.auditplatform.identity.domain.UserAccount;

import java.util.stream.Collectors;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserSummaryResponse toSummary(UserAccount user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus().name(),
                user.getEmailVerifiedAt() != null,
                user.isMfaEnabled(),
                user.isPlatformAdmin(),
                user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet()),
                user.permissionCodes()
        );
    }
}
