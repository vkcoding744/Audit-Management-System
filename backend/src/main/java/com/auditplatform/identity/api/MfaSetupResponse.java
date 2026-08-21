package com.auditplatform.identity.api;

public record MfaSetupResponse(String secret, String otpauthUri, boolean mfaEnabled) {
}
