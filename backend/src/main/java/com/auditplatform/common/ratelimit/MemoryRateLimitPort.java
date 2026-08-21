package com.auditplatform.common.ratelimit;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single-node sliding window limiter. Default when {@code audit.rate-limit.provider=memory}.
 */
public class MemoryRateLimitPort implements RateLimitPort {

    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key, int limitPerMinute) {
        int limit = Math.max(1, limitPerMinute);
        long now = Instant.now().toEpochMilli();
        long windowStart = now - 60_000L;
        Deque<Long> hits = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (hits) {
            while (!hits.isEmpty() && hits.peekFirst() < windowStart) {
                hits.removeFirst();
            }
            if (hits.size() >= limit) {
                return false;
            }
            hits.addLast(now);
            return true;
        }
    }
}
