package com.auditplatform.identity.api;

import java.util.List;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        List<String> roleCodes
) {
}
