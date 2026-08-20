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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.error(
                ApiError.of(ErrorCode.SYS_UNAUTHORIZED.code(), "Authentication required"),
                MDC.get(CorrelationId.MDC_KEY)
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
