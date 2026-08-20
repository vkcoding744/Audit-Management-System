package com.auditplatform.common.security;

import com.auditplatform.common.api.ApiError;
import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.web.CorrelationId;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JsonAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.error(
                ApiError.of(ErrorCode.SYS_FORBIDDEN.code(), "Access denied"),
                MDC.get(CorrelationId.MDC_KEY)
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
