package com.auditplatform.finance.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.crm.domain.Client;
import com.auditplatform.crm.service.ClientService;
import com.auditplatform.finance.api.CreateInvoiceRequest;
import com.auditplatform.finance.api.InvoiceResponse;
import com.auditplatform.finance.api.LineRequest;
import com.auditplatform.finance.api.LineResponse;
import com.auditplatform.finance.api.PaymentResponse;
import com.auditplatform.finance.api.RecordPaymentRequest;
import com.auditplatform.finance.domain.Invoice;
import com.auditplatform.finance.domain.InvoiceLine;
import com.auditplatform.finance.domain.InvoiceStatus;
import com.auditplatform.finance.domain.Money;
import com.auditplatform.finance.domain.Payment;
import com.auditplatform.finance.domain.PaymentMethod;
import com.auditplatform.finance.domain.Quote;
import com.auditplatform.finance.domain.QuoteLine;
import com.auditplatform.finance.domain.QuoteStatus;
import com.auditplatform.finance.repository.InvoiceLineRepository;
import com.auditplatform.finance.repository.InvoiceRepository;
import com.auditplatform.finance.repository.PaymentRepository;
import com.auditplatform.finance.repository.QuoteLineRepository;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class InvoiceService {

    private static final List<InvoiceStatus> OUTSTANDING = List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final PaymentRepository paymentRepository;
    private final QuoteLineRepository quoteLineRepository;
    private final FinanceNumberService numberService;
    private final QuoteService quoteService;
    private final ClientService clientService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            InvoiceLineRepository invoiceLineRepository,
            PaymentRepository paymentRepository,
            QuoteLineRepository quoteLineRepository,
            FinanceNumberService numberService,
            QuoteService quoteService,
            ClientService clientService,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.paymentRepository = paymentRepository;
        this.quoteLineRepository = quoteLineRepository;
        this.numberService = numberService;
        this.quoteService = quoteService;
        this.clientService = clientService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> list(String clientId, InvoiceStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<Invoice> page;
        if (clientId != null && !clientId.isBlank()) {
            page = invoiceRepository.findByTenantIdAndClientIdAndDeletedAtIsNull(tenantId, clientId, pageable);
        } else if (status != null) {
            page = invoiceRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = invoiceRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(this::summary));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse get(String id) {
        return toDetail(requireInvoice(id));
    }

    @Transactional
    public InvoiceResponse create(CreateInvoiceRequest request) {
        Client client = clientService.requireClient(request.clientId());
        if (request.quoteId() != null && !request.quoteId().isBlank()) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Create an invoice from a quote with POST /quotes/{id}/invoice");
        }
        Invoice invoice = newDraft(client, null, QuoteService.currency(request.currency()), request.dueOn(), request.notes());
        applyInvoiceLines(invoice, request.lines());
        invoiceRepository.save(invoice);
        persistInvoiceLines(invoice, request.lines());
        auditLogService.record("INVOICE_CREATE", "Invoice", invoice.getId(), null, invoice.getInvoiceNumber(), null, null);
        return toDetail(invoice);
    }

    @Transactional
    public InvoiceResponse createFromQuote(String quoteId) {
        Quote quote = quoteService.requireQuote(quoteId);
        if (quote.getStatus() != QuoteStatus.ACCEPTED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only accepted quotes can become invoices");
        }
        if (invoiceRepository.existsByQuoteIdAndDeletedAtIsNull(quote.getId())) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "An invoice already exists for this quote");
        }
        Client client = clientService.requireClient(quote.getClientId());
        Invoice invoice = newDraft(client, quote.getId(), quote.getCurrency(), today().plusDays(30), quote.getNotes());
        invoice.setSubtotal(quote.getSubtotal());
        invoice.setTotalAmount(quote.getTotalAmount());
        invoiceRepository.save(invoice);
        for (QuoteLine quoteLine : quoteLineRepository.findByTenantIdAndQuoteIdAndDeletedAtIsNullOrderByCreatedAtAsc(
                quote.getTenantId(),
                quote.getId()
        )) {
            InvoiceLine line = new InvoiceLine();
            line.setTenantId(invoice.getTenantId());
            line.setInvoiceId(invoice.getId());
            line.setDescription(quoteLine.getDescription());
            line.setQuantity(quoteLine.getQuantity());
            line.setUnitAmount(quoteLine.getUnitAmount());
            line.setLineAmount(quoteLine.getLineAmount());
            invoiceLineRepository.save(line);
        }
        auditLogService.record("INVOICE_FROM_QUOTE", "Invoice", invoice.getId(), quote.getQuoteNumber(), invoice.getInvoiceNumber(), null, null);
        return toDetail(invoice);
    }

    @Transactional
    public InvoiceResponse issue(String id) {
        Invoice invoice = requireInvoice(id);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only draft invoices can be issued");
        }
        if (invoice.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Invoice total must be greater than zero");
        }
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssuedOn(today());
        if (invoice.getDueOn() == null) {
            invoice.setDueOn(today().plusDays(30));
        }
        invoiceRepository.save(invoice);
        auditLogService.record("INVOICE_ISSUE", "Invoice", invoice.getId(), "DRAFT", "ISSUED", null, null);
        return toDetail(invoice);
    }

    @Transactional
    public InvoiceResponse voidInvoice(String id) {
        Invoice invoice = requireInvoice(id);
        if (invoice.getStatus() != InvoiceStatus.ISSUED && invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only draft or issued unpaid invoices can be voided");
        }
        if (invoice.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Invoices with payments cannot be voided");
        }
        String from = invoice.getStatus().name();
        invoice.setStatus(InvoiceStatus.VOID);
        invoiceRepository.save(invoice);
        auditLogService.record("INVOICE_VOID", "Invoice", invoice.getId(), from, "VOID", null, null);
        return toDetail(invoice);
    }

    @Transactional
    public PaymentResponse recordPayment(String invoiceId, RecordPaymentRequest request) {
        Invoice invoice = requireInvoice(invoiceId);
        if (!OUTSTANDING.contains(invoice.getStatus())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Payments can only be recorded on issued invoices");
        }
        BigDecimal amount = Money.scale(request.amount());
        BigDecimal due = amountDue(invoice);
        if (amount.compareTo(due) > 0) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Payment exceeds the amount due");
        }
        Payment payment = new Payment();
        payment.setTenantId(invoice.getTenantId());
        payment.setPaymentNumber(numberService.nextPayment(invoice.getTenantId()));
        payment.setInvoiceId(invoice.getId());
        payment.setAmount(amount);
        payment.setPaidOn(request.paidOn() == null ? today() : request.paidOn());
        payment.setMethod(request.method() == null ? PaymentMethod.OTHER : request.method());
        payment.setReference(QuoteService.blankToNull(request.reference()));
        payment.setNotes(QuoteService.blankToNull(request.notes()));
        paymentRepository.save(payment);
        invoice.setAmountPaid(Money.scale(invoice.getAmountPaid().add(amount)));
        if (invoice.getAmountPaid().compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        invoiceRepository.save(invoice);
        auditLogService.record("PAYMENT_RECORD", "Payment", payment.getId(), null, payment.getPaymentNumber(), null, null);
        return PaymentResponse.from(payment);
    }

    public Invoice requireInvoice(String id) {
        Invoice invoice = invoiceRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Invoice not found"));
        isolationService.assertCanAccessTenant(invoice.getTenantId());
        return invoice;
    }

    private Invoice newDraft(Client client, String quoteId, String currency, LocalDate dueOn, String notes) {
        Invoice invoice = new Invoice();
        invoice.setTenantId(client.getTenantId());
        invoice.setInvoiceNumber(numberService.nextInvoice(client.getTenantId()));
        invoice.setClientId(client.getId());
        invoice.setQuoteId(quoteId);
        invoice.setCurrency(currency);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setDueOn(dueOn);
        invoice.setAmountPaid(Money.scale(BigDecimal.ZERO));
        invoice.setNotes(QuoteService.blankToNull(notes));
        return invoice;
    }

    private void applyInvoiceLines(Invoice invoice, List<LineRequest> requests) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (LineRequest request : requests) {
            subtotal = subtotal.add(Money.lineAmount(Money.scale(request.quantity()), Money.scale(request.unitAmount())));
        }
        invoice.setSubtotal(Money.scale(subtotal));
        invoice.setTotalAmount(Money.scale(subtotal));
    }

    private void persistInvoiceLines(Invoice invoice, List<LineRequest> requests) {
        for (LineRequest request : requests) {
            BigDecimal quantity = Money.scale(request.quantity());
            BigDecimal unit = Money.scale(request.unitAmount());
            InvoiceLine line = new InvoiceLine();
            line.setTenantId(invoice.getTenantId());
            line.setInvoiceId(invoice.getId());
            line.setDescription(request.description().trim());
            line.setQuantity(quantity);
            line.setUnitAmount(unit);
            line.setLineAmount(Money.lineAmount(quantity, unit));
            invoiceLineRepository.save(line);
        }
    }

    private InvoiceResponse summary(Invoice invoice) {
        return InvoiceResponse.from(invoice, overdue(invoice), amountDue(invoice), List.of(), List.of());
    }

    private InvoiceResponse toDetail(Invoice invoice) {
        List<LineResponse> lines = invoiceLineRepository
                .findByTenantIdAndInvoiceIdAndDeletedAtIsNullOrderByCreatedAtAsc(invoice.getTenantId(), invoice.getId())
                .stream()
                .map(LineResponse::from)
                .toList();
        List<PaymentResponse> payments = paymentRepository
                .findByTenantIdAndInvoiceIdAndDeletedAtIsNullOrderByPaidOnAsc(invoice.getTenantId(), invoice.getId())
                .stream()
                .map(PaymentResponse::from)
                .toList();
        return InvoiceResponse.from(invoice, overdue(invoice), amountDue(invoice), lines, payments);
    }

    private boolean overdue(Invoice invoice) {
        return OUTSTANDING.contains(invoice.getStatus())
                && invoice.getDueOn() != null
                && invoice.getDueOn().isBefore(today());
    }

    private BigDecimal amountDue(Invoice invoice) {
        return Money.scale(invoice.getTotalAmount().subtract(invoice.getAmountPaid()));
    }

    private LocalDate today() {
        return LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
