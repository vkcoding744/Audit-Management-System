package com.auditplatform.common.security;

import com.auditplatform.common.api.ApiError;
import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.tenant.TenantContext;
import com.auditplatform.common.tenant.TenantContextFilter;
import com.auditplatform.common.web.CorrelationId;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantBindingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    public TenantBindingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof PlatformPrincipal principal) {
            String header = request.getHeader(TenantContextFilter.TENANT_HEADER);
            if (principal.platformAdmin()) {
                if (header != null && !header.isBlank()) {
                    TenantContext.setTenantId(header.trim());
                } else {
                    TenantContext.setTenantId(principal.tenantId());
                }
                TenantContext.setPlatformAdmin(true);
            } else {
                if (header != null && !header.isBlank() && principal.tenantId() != null
                        && !header.trim().equals(principal.tenantId())) {
                    writeForbidden(response);
                    return;
                }
                TenantContext.setTenantId(principal.tenantId());
                TenantContext.setPlatformAdmin(false);
            }
            TenantContext.setUserId(principal.userId());
            if (TenantContext.getTenantId() != null) {
                MDC.put("tenantId", TenantContext.getTenantId());
            }
        }
        filterChain.doFilter(request, response);
    }

    private void writeForbidden(HttpServletResponse response) throws IOException {
        response.setStatus(ErrorCode.AUTH_TENANT_MISMATCH.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.error(
                ApiError.of(ErrorCode.AUTH_TENANT_MISMATCH.code(), "Cross-tenant access is not allowed"),
                MDC.get(CorrelationId.MDC_KEY)
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
