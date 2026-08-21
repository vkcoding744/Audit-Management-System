package com.auditplatform.search.api;

import java.util.List;

public record SearchResponse(String provider, String query, List<SearchHitResponse> hits) {
}
