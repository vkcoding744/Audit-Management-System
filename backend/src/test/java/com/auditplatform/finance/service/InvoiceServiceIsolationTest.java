package com.auditplatform.finance.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.crm.service.ClientService;
import com.auditplatform.finance.domain.Invoice;
import com.auditplatform.finance.repository.InvoiceLineRepository;
import com.auditplatform.finance.repository.InvoiceRepository;
import com.auditplatform.finance.repository.PaymentRepository;
import com.auditplatform.finance.repository.QuoteLineRepository;
import com.auditplatform.identity.service.IsolationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InvoiceServiceIsolationTest {

    private final IsolationService isolationService = new IsolationService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getDoesNotReturnAnotherTenantsInvoice() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("INVOICE_VIEW")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        Invoice foreign = new Invoice();
        foreign.setTenantId("tenant-b");
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        when(invoices.findByIdAndDeletedAtIsNull("i1")).thenReturn(Optional.of(foreign));

        InvoiceService service = new InvoiceService(
                invoices,
                mock(InvoiceLineRepository.class),
                mock(PaymentRepository.class),
                mock(QuoteLineRepository.class),
                mock(FinanceNumberService.class),
                mock(QuoteService.class),
                mock(ClientService.class),
                isolationService,
                mock(AuditLogService.class),
                Clock.fixed(Instant.parse("2026-08-20T00:00:00Z"), ZoneOffset.UTC)
        );

        assertThatThrownBy(() -> service.get("i1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_TENANT_MISMATCH);
    }
}
