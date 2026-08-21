package com.auditplatform.search.spi;

import com.auditplatform.search.domain.SearchType;

public record SearchHit(SearchType type, String id, String title, String subtitle, String path) {
}
