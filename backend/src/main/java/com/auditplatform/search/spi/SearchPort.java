package com.auditplatform.search.spi;

import com.auditplatform.search.domain.SearchType;

import java.util.List;

public interface SearchPort {

    List<SearchHit> search(String tenantId, String query, SearchType type, int perType);
}
