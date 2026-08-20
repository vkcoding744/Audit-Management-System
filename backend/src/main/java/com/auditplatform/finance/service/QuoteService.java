package com.auditplatform.finance.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.crm.service.ClientService;
import com.auditplatform.finance.api.CreateQuoteRequest;
import com.auditplatform.finance.api.LineRequest;
import com.auditplatform.finance.api.LineResponse;
import com.auditplatform.finance.api.QuoteResponse;
import com.auditplatform.finance.domain.Money;
import com.auditplatform.finance.domain.Quote;
import com.auditplatform.finance.domain.QuoteLine;
import com.auditplatform.finance.domain.QuoteStatus;
import com.auditplatform.finance.repository.QuoteLineRepository;
import com.auditplatform.finance.repository.QuoteRepository;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuoteLineRepository quoteLineRepository;
    private final FinanceNumberService numberService;
    private final ClientService clientService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public QuoteService(
            QuoteRepository quoteRepository,
            QuoteLineRepository quoteLineRepository,
            FinanceNumberService numberService,
            ClientService clientService,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.quoteRepository = quoteRepository;
        this.quoteLineRepository = quoteLineRepository;
        this.numberService = numberService;
        this.clientService = clientService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<QuoteResponse> list(String clientId, QuoteStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<Quote> page;
        if (clientId != null && !clientId.isBlank()) {
            page = quoteRepository.findByTenantIdAndClientIdAndDeletedAtIsNull(tenantId, clientId, pageable);
        } else if (status != null) {
            page = quoteRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = quoteRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(this::summary));
    }

    @Transactional(readOnly = true)
    public QuoteResponse get(String id) {
        return toDetail(requireQuote(id));
    }

    @Transactional
    public QuoteResponse create(CreateQuoteRequest request) {
        var client = clientService.requireClient(request.clientId());
        Quote quote = new Quote();
        quote.setTenantId(client.getTenantId());
        quote.setQuoteNumber(numberService.nextQuote(client.getTenantId()));
        quote.setClientId(client.getId());
        quote.setCurrency(currency(request.currency()));
        quote.setStatus(QuoteStatus.DRAFT);
        quote.setValidUntil(request.validUntil());
        quote.setNotes(blankToNull(request.notes()));
        Totals totals = applyLines(quote, request.lines());
        quote.setSubtotal(totals.subtotal());
        quote.setTotalAmount(totals.subtotal());
        quoteRepository.save(quote);
        persistLines(quote, totals.lines());
        auditLogService.record("QUOTE_CREATE", "Quote", quote.getId(), null, quote.getQuoteNumber(), null, null);
        return toDetail(quote);
    }

    @Transactional
    public QuoteResponse issue(String id) {
        Quote quote = requireQuote(id);
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only draft quotes can be issued");
        }
        if (quote.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Quote total must be greater than zero");
        }
        quote.setStatus(QuoteStatus.ISSUED);
        quoteRepository.save(quote);
        auditLogService.record("QUOTE_ISSUE", "Quote", quote.getId(), "DRAFT", "ISSUED", null, null);
        return toDetail(quote);
    }

    @Transactional
    public QuoteResponse accept(String id) {
        Quote quote = requireQuote(id);
        if (quote.getStatus() != QuoteStatus.ISSUED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only issued quotes can be accepted");
        }
        quote.setStatus(QuoteStatus.ACCEPTED);
        quoteRepository.save(quote);
        auditLogService.record("QUOTE_ACCEPT", "Quote", quote.getId(), "ISSUED", "ACCEPTED", null, null);
        return toDetail(quote);
    }

    @Transactional
    public QuoteResponse decline(String id) {
        Quote quote = requireQuote(id);
        if (quote.getStatus() != QuoteStatus.ISSUED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only issued quotes can be declined");
        }
        quote.setStatus(QuoteStatus.DECLINED);
        quoteRepository.save(quote);
        auditLogService.record("QUOTE_DECLINE", "Quote", quote.getId(), "ISSUED", "DECLINED", null, null);
        return toDetail(quote);
    }

    public Quote requireQuote(String id) {
        Quote quote = quoteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Quote not found"));
        isolationService.assertCanAccessTenant(quote.getTenantId());
        return quote;
    }

    private QuoteResponse summary(Quote quote) {
        return QuoteResponse.from(quote, expired(quote), List.of());
    }

    private QuoteResponse toDetail(Quote quote) {
        List<LineResponse> lines = quoteLineRepository
                .findByTenantIdAndQuoteIdAndDeletedAtIsNullOrderByCreatedAtAsc(quote.getTenantId(), quote.getId())
                .stream()
                .map(LineResponse::from)
                .toList();
        return QuoteResponse.from(quote, expired(quote), lines);
    }

    private boolean expired(Quote quote) {
        return quote.getStatus() == QuoteStatus.ISSUED
                && quote.getValidUntil() != null
                && quote.getValidUntil().isBefore(today());
    }

    private LocalDate today() {
        return LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    static Totals applyLines(Quote quote, List<LineRequest> requests) {
        List<QuoteLine> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (LineRequest request : requests) {
            BigDecimal quantity = Money.scale(request.quantity());
            BigDecimal unit = Money.scale(request.unitAmount());
            BigDecimal lineAmount = Money.lineAmount(quantity, unit);
            QuoteLine line = new QuoteLine();
            line.setTenantId(quote.getTenantId());
            line.setDescription(request.description().trim());
            line.setQuantity(quantity);
            line.setUnitAmount(unit);
            line.setLineAmount(lineAmount);
            lines.add(line);
            subtotal = subtotal.add(lineAmount);
        }
        return new Totals(Money.scale(subtotal), lines);
    }

    private void persistLines(Quote quote, List<QuoteLine> lines) {
        for (QuoteLine line : lines) {
            line.setQuoteId(quote.getId());
            quoteLineRepository.save(line);
        }
    }

    static String currency(String value) {
        if (value == null || value.isBlank()) {
            return "USD";
        }
        return value.trim().toUpperCase();
    }

    static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    record Totals(BigDecimal subtotal, List<QuoteLine> lines) {
    }
}
