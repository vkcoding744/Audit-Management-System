package com.auditplatform.crm.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.crm.domain.Client;
import com.auditplatform.crm.metrics.ClientOperationalMetricsPort;
import com.auditplatform.crm.repository.ClientRepository;
import com.auditplatform.crm.repository.ContactRepository;
import com.auditplatform.crm.repository.SiteRepository;
import com.auditplatform.identity.service.IsolationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientServiceIsolationTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getDoesNotReturnAnotherTenantsClient() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("CLIENT_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        Client foreign = new Client();
        foreign.setTenantId("tenant-b");
        foreign.setLegalName("Other Co");
        ClientRepository clients = mock(ClientRepository.class);
        when(clients.findByIdAndDeletedAtIsNull("c1")).thenReturn(Optional.of(foreign));

        ClientService service = new ClientService(
                clients,
                mock(SiteRepository.class),
                mock(ContactRepository.class),
                mock(ClientNumberService.class),
                isolationService,
                mock(AuditLogService.class),
                mock(ClientOperationalMetricsPort.class)
        );

        assertThatThrownBy(() -> service.get("c1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_TENANT_MISMATCH);
    }
}
