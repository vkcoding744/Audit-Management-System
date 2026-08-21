package com.auditplatform.search.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.search.api.SearchResponse;
import com.auditplatform.search.service.SearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.MDC;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SEARCH_VIEW')")
    public ApiResponse<SearchResponse> search(
            @RequestParam String q,
            @RequestParam(required = false) String type
    ) {
        return ApiResponse.ok(searchService.search(q, type), MDC.get(CorrelationId.MDC_KEY));
    }
}
