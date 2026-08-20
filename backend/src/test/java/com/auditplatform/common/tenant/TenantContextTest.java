package com.auditplatform.common.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class TenantContextTest {

    @AfterEach
    void clear() {
        TenantContext.clear();
    }

    @Test
    void storesTenantOnCurrentThreadOnly() throws InterruptedException {
        TenantContext.setTenantId("tenant-a");
        assertThat(TenantContext.getTenantId()).isEqualTo("tenant-a");

        AtomicReference<String> otherThreadValue = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Thread worker = new Thread(() -> {
            try {
                otherThreadValue.set(TenantContext.getTenantId());
            } finally {
                done.countDown();
            }
        });
        worker.start();
        done.await();

        assertThat(otherThreadValue.get()).isNull();
    }

    @Test
    void clearRemovesTenant() {
        TenantContext.setTenantId("tenant-a");
        TenantContext.clear();
        assertThat(TenantContext.getTenantId()).isNull();
    }
}
