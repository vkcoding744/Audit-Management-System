package com.auditplatform.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    SYS_VALIDATION("SYS_VALIDATION", HttpStatus.BAD_REQUEST),
    SYS_UNAUTHORIZED("SYS_UNAUTHORIZED", HttpStatus.UNAUTHORIZED),
    SYS_FORBIDDEN("SYS_FORBIDDEN", HttpStatus.FORBIDDEN),
    SYS_NOT_FOUND("SYS_NOT_FOUND", HttpStatus.NOT_FOUND),
    SYS_CONFLICT("SYS_CONFLICT", HttpStatus.CONFLICT),
    SYS_RATE_LIMITED("SYS_RATE_LIMITED", HttpStatus.TOO_MANY_REQUESTS),
    SYS_INTERNAL("SYS_INTERNAL", HttpStatus.INTERNAL_SERVER_ERROR);

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
