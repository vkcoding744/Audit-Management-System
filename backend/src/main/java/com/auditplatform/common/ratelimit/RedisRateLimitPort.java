package com.auditplatform.common.ratelimit;

import com.auditplatform.common.config.AuditPlatformProperties;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Redis INCR + 60s expiry. Used when {@code audit.rate-limit.provider=redis}.
 * Fail-closed if Redis is unreachable.
 */
public class RedisRateLimitPort implements RateLimitPort, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimitPort.class);
    static final String KEY_PREFIX = "audit:ratelimit:";

    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;

    public RedisRateLimitPort(AuditPlatformProperties properties) {
        String uri = properties.rateLimit().redisUri();
        if (uri == null || uri.isBlank()) {
            uri = "redis://localhost:6379";
        }
        this.client = RedisClient.create(uri);
        this.connection = this.client.connect();
    }

    static String redisKey(String clientKey) {
        return KEY_PREFIX + clientKey;
    }

    @Override
    public boolean tryAcquire(String key, int limitPerMinute) {
        int limit = Math.max(1, limitPerMinute);
        try {
            RedisCommands<String, String> commands = connection.sync();
            String redisKey = redisKey(key);
            Long count = commands.incr(redisKey);
            if (count != null && count == 1L) {
                commands.expire(redisKey, 60);
            }
            return count != null && count <= limit;
        } catch (RuntimeException ex) {
            log.warn("Redis rate limit failed; denying request");
            return false;
        }
    }

    @Override
    public void destroy() {
        if (connection != null) {
            connection.close();
        }
        if (client != null) {
            client.shutdown();
        }
    }
}
