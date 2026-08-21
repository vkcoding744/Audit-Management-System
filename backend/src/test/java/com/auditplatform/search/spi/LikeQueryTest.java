package com.auditplatform.search.spi;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikeQueryTest {

    @Test
    void escapesLikeWildcards() {
        assertThat(LikeQuery.escape("a%b_c\\d")).isEqualTo("a\\%b\\_c\\\\d");
    }
}
