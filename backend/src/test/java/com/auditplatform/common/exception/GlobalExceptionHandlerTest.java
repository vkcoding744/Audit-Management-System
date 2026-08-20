package com.auditplatform.common.exception;

import com.auditplatform.common.api.ApiError;
import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.web.CorrelationId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void apiExceptionUsesErrorCodeStatus() {
        MDC.put(CorrelationId.MDC_KEY, "cid-1");
        ResponseEntity<ApiResponse<Void>> response = handler.handleApi(
                new ApiException(ErrorCode.SYS_CONFLICT, "Duplicate code")
        );

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        ApiError error = response.getBody().error();
        assertThat(error.code()).isEqualTo("SYS_CONFLICT");
        assertThat(error.message()).isEqualTo("Duplicate code");
        assertThat(response.getBody().meta().correlationId()).isEqualTo("cid-1");
    }

    @Test
    void genericExceptionDoesNotLeakMessage() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneric(new RuntimeException("secret details"));
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().message()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().error().message()).doesNotContain("secret");
    }
}
