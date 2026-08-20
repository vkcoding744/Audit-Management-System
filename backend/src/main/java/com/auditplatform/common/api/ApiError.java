package com.auditplatform.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String code,
        String message,
        List<FieldErrorDetail> details
) {
    public static ApiError of(String code, String message) {
        return new ApiError(code, message, List.of());
    }

    public static ApiError of(String code, String message, List<FieldErrorDetail> details) {
        return new ApiError(code, message, details == null ? List.of() : details);
    }

    public record FieldErrorDetail(String field, String message) {
    }
}
