package com.auditplatform.common.security;

import com.auditplatform.common.api.ApiError;
import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.config.AuditPlatformProperties;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.ratelimit.MemoryRateLimitPort;
import com.auditplatform.common.ratelimit.RateLimitPort;
import com.auditplatform.common.web.CorrelationId;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate limiter. Disabled unless {@code audit.rate-limit.enabled=true}.
 * Default adapter is in-memory; set {@code audit.rate-limit.provider=redis} for multi-instance deployments.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RateLimitFilter extends OncePerRequestFilter {

    private final AuditPlatformProperties properties;
    private final ObjectMapper objectMapper;
    private final RateLimitPort rateLimitPort;

    public RateLimitFilter(
            AuditPlatformProperties properties,
            ObjectMapper objectMapper,
            ObjectProvider<RateLimitPort> rateLimitPorts
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.rateLimitPort = rateLimitPorts.getIfAvailable(MemoryRateLimitPort::new);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.rateLimit().enabled();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        int limit = Math.max(1, properties.rateLimit().requestsPerMinute());
        String key = clientKey(request);
        if (!rateLimitPort.tryAcquire(key, limit)) {
            writeTooManyRequests(response);
            return;
        }
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        filterChain.doFilter(request, response);
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(ErrorCode.SYS_RATE_LIMITED.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.error(
                ApiError.of(ErrorCode.SYS_RATE_LIMITED.code(), "Too many requests"),
                MDC.get(CorrelationId.MDC_KEY)
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
    }
}
