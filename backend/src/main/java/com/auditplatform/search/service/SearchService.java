package com.auditplatform.search.service;

import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.search.api.SearchHitResponse;
import com.auditplatform.search.api.SearchResponse;
import com.auditplatform.search.config.SearchProperties;
import com.auditplatform.search.domain.SearchType;
import com.auditplatform.search.spi.SearchHit;
import com.auditplatform.search.spi.SearchPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class SearchService {

    private final IsolationService isolationService;
    private final SearchPort searchPort;
    private final SearchProperties properties;

    public SearchService(IsolationService isolationService, SearchPort searchPort, SearchProperties properties) {
        this.isolationService = isolationService;
        this.searchPort = searchPort;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public SearchResponse search(String query, String type) {
        String tenantId = isolationService.requireTenantScope();
        if (query == null || query.trim().length() < 2) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Search query must be at least 2 characters");
        }
        String trimmed = query.trim();
        if (trimmed.length() > 80) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Search query is too long");
        }
        SearchType parsed = parseType(type);
        List<SearchHit> hits = searchPort.search(tenantId, trimmed, parsed, properties.perTypeOrDefault());
        return new SearchResponse(
                properties.providerOrMysql(),
                trimmed,
                hits.stream().map(SearchService::toResponse).toList()
        );
    }

    private static SearchType parseType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return SearchType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Unknown search type");
        }
    }

    private static SearchHitResponse toResponse(SearchHit hit) {
        return new SearchHitResponse(hit.type().name(), hit.id(), hit.title(), hit.subtitle(), hit.path());
    }
}
