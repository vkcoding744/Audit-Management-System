package com.auditplatform.common.ratelimit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryRateLimitPortTest {

    @Test
    void allowsUntilLimitThenDenies() {
        MemoryRateLimitPort port = new MemoryRateLimitPort();
        assertThat(port.tryAcquire("10.0.0.1", 2)).isTrue();
        assertThat(port.tryAcquire("10.0.0.1", 2)).isTrue();
        assertThat(port.tryAcquire("10.0.0.1", 2)).isFalse();
        assertThat(port.tryAcquire("10.0.0.2", 2)).isTrue();
    }
}
