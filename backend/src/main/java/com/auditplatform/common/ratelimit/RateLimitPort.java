package com.auditplatform.common.ratelimit;

public interface RateLimitPort {

    /**
     * @return true if the request is allowed under {@code limitPerMinute}
     */
    boolean tryAcquire(String key, int limitPerMinute);
}
