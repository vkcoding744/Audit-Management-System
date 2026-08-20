package com.auditplatform.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error,
        ApiMeta meta
) {
    public static <T> ApiResponse<T> ok(T data, String correlationId) {
        return new ApiResponse<>(true, data, null, ApiMeta.of(correlationId));
    }

    public static ApiResponse<Void> error(ApiError error, String correlationId) {
        return new ApiResponse<>(false, null, error, ApiMeta.of(correlationId));
    }

    public record ApiMeta(String correlationId, Instant timestamp) {
        static ApiMeta of(String correlationId) {
            return new ApiMeta(correlationId, Instant.now());
        }
    }
}
