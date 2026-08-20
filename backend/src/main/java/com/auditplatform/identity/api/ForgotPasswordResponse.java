package com.auditplatform.identity.api;

public record ForgotPasswordResponse(String message, String resetToken) {
}
