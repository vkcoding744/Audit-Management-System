package com.auditplatform.common.web;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientRequest {

    private ClientRequest() {
    }

    public static String ipAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public static String userAgent(HttpServletRequest request) {
        String value = request.getHeader("User-Agent");
        if (value == null) {
            return null;
        }
        return value.length() <= 512 ? value : value.substring(0, 512);
    }
}
