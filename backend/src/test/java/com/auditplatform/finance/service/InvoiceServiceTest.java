package com.auditplatform.finance.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.common.security.PlatformPrincipal;
import com.auditplatform.crm.service.ClientService;
import com.auditplatform.finance.api.RecordPaymentRequest;
import com.auditplatform.finance.domain.Invoice;
import com.auditplatform.finance.domain.InvoiceStatus;
import com.auditplatform.finance.domain.Money;
import com.auditplatform.finance.repository.InvoiceLineRepository;
import com.auditplatform.finance.repository.InvoiceRepository;
import com.auditplatform.finance.repository.PaymentRepository;
import com.auditplatform.finance.repository.QuoteLineRepository;
import com.auditplatform.identity.service.IsolationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InvoiceServiceTest {

    private final IsolationService isolationService = new IsolationService();
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-20T12:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void paymentExceedingAmountDueIsRejected() {
        bindUser();
        Invoice invoice = issuedInvoice();
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        when(invoices.findByIdAndDeletedAtIsNull("inv-1")).thenReturn(Optional.of(invoice));
        InvoiceService service = service(invoices);

        assertThatThrownBy(() -> service.recordPayment(
                "inv-1",
                new RecordPaymentRequest(new BigDecimal("150.00"), null, null, null, null)
        ))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getErrorCode()).isEqualTo(ErrorCode.SYS_VALIDATION);
                    assertThat(api.getMessage()).contains("exceeds the amount due");
                });
    }

    @Test
    void overdueFlagIsTrueWhenIssuedInvoiceIsPastDue() {
        bindUser();
        Invoice invoice = issuedInvoice();
        invoice.setDueOn(LocalDate.of(2026, 8, 1));
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        when(invoices.findByIdAndDeletedAtIsNull("inv-1")).thenReturn(Optional.of(invoice));
        when(invoices.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        InvoiceLineRepository lines = mock(InvoiceLineRepository.class);
        when(lines.findByTenantIdAndInvoiceIdAndDeletedAtIsNullOrderByCreatedAtAsc("tenant-a", "inv-1"))
                .thenReturn(java.util.List.of());
        PaymentRepository payments = mock(PaymentRepository.class);
        when(payments.findByTenantIdAndInvoiceIdAndDeletedAtIsNullOrderByPaidOnAsc("tenant-a", "inv-1"))
                .thenReturn(java.util.List.of());

        InvoiceService service = new InvoiceService(
                invoices,
                lines,
                payments,
                mock(QuoteLineRepository.class),
                mock(FinanceNumberService.class),
                mock(QuoteService.class),
                mock(ClientService.class),
                isolationService,
                mock(AuditLogService.class),
                clock
        );

        assertThat(service.get("inv-1").overdue()).isTrue();
        assertThat(service.get("inv-1").amountDue()).isEqualByComparingTo("100.00");
    }

    private InvoiceService service(InvoiceRepository invoices) {
        return new InvoiceService(
                invoices,
                mock(InvoiceLineRepository.class),
                mock(PaymentRepository.class),
                mock(QuoteLineRepository.class),
                mock(FinanceNumberService.class),
                mock(QuoteService.class),
                mock(ClientService.class),
                isolationService,
                mock(AuditLogService.class),
                clock
        );
    }

    private static Invoice issuedInvoice() {
        Invoice invoice = new Invoice();
        ReflectionTestUtils.setField(invoice, "id", "inv-1");
        invoice.setTenantId("tenant-a");
        invoice.setInvoiceNumber("INV-000001");
        invoice.setClientId("client-1");
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setTotalAmount(Money.scale(new BigDecimal("100.00")));
        invoice.setAmountPaid(Money.scale(BigDecimal.ZERO));
        invoice.setDueOn(LocalDate.of(2026, 9, 1));
        return invoice;
    }

    private static void bindUser() {
        PlatformPrincipal principal = new PlatformPrincipal(
                "user-a", "a@example.com", "tenant-a", false, "sid", Set.of("PAYMENT_RECORD")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }
}
