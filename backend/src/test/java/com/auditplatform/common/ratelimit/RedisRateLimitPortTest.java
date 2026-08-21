package com.auditplatform.common.ratelimit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedisRateLimitPortTest {

    @Test
    void redisKeyIsPrefixed() {
        assertThat(RedisRateLimitPort.redisKey("10.0.0.1")).isEqualTo("audit:ratelimit:10.0.0.1");
    }
}
