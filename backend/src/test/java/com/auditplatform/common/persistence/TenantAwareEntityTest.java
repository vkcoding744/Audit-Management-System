package com.auditplatform.common.persistence;

import com.auditplatform.common.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantAwareEntityTest {

    @AfterEach
    void clear() {
        TenantContext.clear();
    }

    @Test
    void persistRequiresTenantContext() {
        TestEntity entity = new TestEntity();
        assertThatThrownBy(entity::assignTenant)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant context is required");
    }

    private static final class TestEntity extends TenantAwareEntity {
    }
}
