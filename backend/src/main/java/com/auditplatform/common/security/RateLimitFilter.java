package com.auditplatform.common.security;

import com.auditplatform.common.api.ApiError;
import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.config.AuditPlatformProperties;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.web.CorrelationId;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single-node in-memory rate limiter. Disabled by default.
 * Replace with a Redis-backed implementation before multi-instance production traffic.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RateLimitFilter extends OncePerRequestFilter {

    private final AuditPlatformProperties properties;
    private final ObjectMapper objectMapper;
    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(AuditPlatformProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
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
        long now = Instant.now().toEpochMilli();
        long windowStart = now - 60_000L;
        Deque<Long> hits = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (hits) {
            while (!hits.isEmpty() && hits.peekFirst() < windowStart) {
                hits.removeFirst();
            }
            if (hits.size() >= limit) {
                writeTooManyRequests(response);
                return;
            }
            hits.addLast(now);
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
