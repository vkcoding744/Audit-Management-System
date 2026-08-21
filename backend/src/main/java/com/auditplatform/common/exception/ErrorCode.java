package com.auditplatform.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    SYS_VALIDATION("SYS_VALIDATION", HttpStatus.BAD_REQUEST),
    SYS_UNAUTHORIZED("SYS_UNAUTHORIZED", HttpStatus.UNAUTHORIZED),
    SYS_FORBIDDEN("SYS_FORBIDDEN", HttpStatus.FORBIDDEN),
    SYS_NOT_FOUND("SYS_NOT_FOUND", HttpStatus.NOT_FOUND),
    SYS_CONFLICT("SYS_CONFLICT", HttpStatus.CONFLICT),
    SYS_RATE_LIMITED("SYS_RATE_LIMITED", HttpStatus.TOO_MANY_REQUESTS),
    SYS_INTERNAL("SYS_INTERNAL", HttpStatus.INTERNAL_SERVER_ERROR),
    AUTH_INVALID_CREDENTIALS("AUTH_INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED),
    AUTH_ACCOUNT_LOCKED("AUTH_ACCOUNT_LOCKED", HttpStatus.LOCKED),
    AUTH_ACCOUNT_DISABLED("AUTH_ACCOUNT_DISABLED", HttpStatus.FORBIDDEN),
    AUTH_EMAIL_NOT_VERIFIED("AUTH_EMAIL_NOT_VERIFIED", HttpStatus.FORBIDDEN),
    AUTH_MFA_REQUIRED("AUTH_MFA_REQUIRED", HttpStatus.UNAUTHORIZED),
    AUTH_MFA_INVALID("AUTH_MFA_INVALID", HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_INVALID("AUTH_TOKEN_INVALID", HttpStatus.UNAUTHORIZED),
    AUTH_TENANT_MISMATCH("AUTH_TENANT_MISMATCH", HttpStatus.FORBIDDEN);

    private final String code;
    private final HttpStatus status;

    ErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }
}
