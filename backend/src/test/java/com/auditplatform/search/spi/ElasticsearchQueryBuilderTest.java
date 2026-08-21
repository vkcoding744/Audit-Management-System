package com.auditplatform.search.spi;

import com.auditplatform.search.domain.SearchType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ElasticsearchQueryBuilderTest {

    @Test
    void alwaysFiltersByTenantId() {
        String json = ElasticsearchQueryBuilder.build("tenant-a", "iso", SearchType.FINDING, 10);
        assertThat(json).contains("\"tenantId\":\"tenant-a\"");
        assertThat(json).contains("\"type\":\"FINDING\"");
        assertThat(json).contains("\"query\":\"iso\"");
        assertThat(json).doesNotContain("tenant-b");
    }
}
