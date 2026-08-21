package com.auditplatform.identity.api;

public record CsrfTokenResponse(boolean enabled, String headerName, String token) {
}
