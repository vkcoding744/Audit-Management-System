package com.auditplatform.common.ratelimit;

import com.auditplatform.common.config.AuditPlatformProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfiguration {

    @Bean
    @ConditionalOnProperty(name = "audit.rate-limit.provider", havingValue = "memory", matchIfMissing = true)
    public RateLimitPort memoryRateLimitPort() {
        return new MemoryRateLimitPort();
    }

    @Bean
    @ConditionalOnProperty(name = "audit.rate-limit.provider", havingValue = "redis")
    public RateLimitPort redisRateLimitPort(AuditPlatformProperties properties) {
        return new RedisRateLimitPort(properties);
    }
}
