package com.auditplatform.search.service;

import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.identity.service.IsolationService;
import com.auditplatform.search.config.SearchProperties;
import com.auditplatform.search.domain.SearchType;
import com.auditplatform.search.spi.SearchHit;
import com.auditplatform.search.spi.SearchPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchServiceTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requiresTenantScope() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "admin", "admin@example.com", null, true, "sid", Set.of("SEARCH_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
        SearchPort port = mock(SearchPort.class);
        assertThatThrownBy(() -> new SearchService(isolationService, port, properties()).search("acme", null))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SYS_VALIDATION);
    }

    @Test
    void passesAuthenticatedTenantToPort() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("SEARCH_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
        SearchPort port = mock(SearchPort.class);
        when(port.search("tenant-a", "acme", null, 5)).thenReturn(List.of(
                new SearchHit(SearchType.CLIENT, "c1", "Acme", "CLIENT-1", "/clients/c1")
        ));

        var response = new SearchService(isolationService, port, properties()).search("acme", null);

        verify(port).search("tenant-a", "acme", null, 5);
        assertThat(response.hits()).hasSize(1);
        assertThat(response.provider()).isEqualTo("mysql");
    }

    @Test
    void rejectsShortQuery() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("SEARCH_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
        assertThatThrownBy(() -> new SearchService(isolationService, mock(SearchPort.class), properties()).search("a", null))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SYS_VALIDATION);
    }

    private static SearchProperties properties() {
        return new SearchProperties("mysql", "", 5);
    }
}
