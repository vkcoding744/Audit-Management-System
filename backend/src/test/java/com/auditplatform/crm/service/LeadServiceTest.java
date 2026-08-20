package com.auditplatform.crm.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.crm.api.ClientResponse;
import com.auditplatform.crm.domain.ClientStatus;
import com.auditplatform.crm.domain.Lead;
import com.auditplatform.crm.domain.LeadStatus;
import com.auditplatform.crm.repository.LeadRepository;
import com.auditplatform.identity.service.IsolationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeadServiceTest {

    private final IsolationService isolationService = new IsolationService();
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-20T12:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void convertBlockedWhenAlreadyConverted() {
        bindUser();
        Lead lead = openLead();
        lead.setStatus(LeadStatus.CONVERTED);
        LeadRepository leads = mock(LeadRepository.class);
        when(leads.findByIdAndDeletedAtIsNull("lead-1")).thenReturn(Optional.of(lead));
        LeadService service = service(leads, mock(ClientService.class));

        assertThatThrownBy(() -> service.convert("lead-1"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getErrorCode()).isEqualTo(ErrorCode.SYS_CONFLICT));
    }

    @Test
    void convertCreatesProspectClient() {
        bindUser();
        Lead lead = openLead();
        LeadRepository leads = mock(LeadRepository.class);
        when(leads.findByIdAndDeletedAtIsNull("lead-1")).thenReturn(Optional.of(lead));
        when(leads.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ClientService clients = mock(ClientService.class);
        when(clients.create(any())).thenReturn(new ClientResponse(
                "client-1",
                "tenant-a",
                "CLIENT-000001",
                "Acme Ltd",
                null,
                null,
                null,
                null,
                null,
                "a@example.com",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ClientStatus.PROSPECT,
                "Converted from LEAD-000001"
        ));
        LeadService service = service(leads, clients);

        var result = service.convert("lead-1");
        assertThat(result.status()).isEqualTo(LeadStatus.CONVERTED);
        assertThat(result.convertedClientId()).isEqualTo("client-1");
        assertThat(result.convertedAt()).isEqualTo(Instant.parse("2026-08-20T12:00:00Z"));
    }

    private LeadService service(LeadRepository leads, ClientService clients) {
        return new LeadService(
                leads,
                mock(LeadNumberService.class),
                clients,
                isolationService,
                mock(AuditLogService.class),
                clock
        );
    }

    private static Lead openLead() {
        Lead lead = new Lead();
        ReflectionTestUtils.setField(lead, "id", "lead-1");
        lead.setTenantId("tenant-a");
        lead.setLeadNumber("LEAD-000001");
        lead.setOrganisationName("Acme Ltd");
        lead.setEmail("a@example.com");
        lead.setStatus(LeadStatus.OPEN);
        return lead;
    }

    private static void bindUser() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("LEAD_UPDATE", "CLIENT_CREATE")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
