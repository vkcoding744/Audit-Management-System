package com.auditplatform.reporting.service;

import com.auditplatform.audit.domain.Audit;
import com.auditplatform.audit.domain.AuditStatus;
import com.auditplatform.audit.domain.Finding;
import com.auditplatform.audit.domain.FindingStatus;
import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.audit.repository.FindingRepository;
import com.auditplatform.certification.domain.Certificate;
import com.auditplatform.certification.domain.CertificateStatus;
import com.auditplatform.certification.repository.CertificateRepository;
import com.auditplatform.crm.domain.Client;
import com.auditplatform.crm.domain.ClientStatus;
import com.auditplatform.crm.repository.ClientRepository;
import com.auditplatform.finance.domain.Invoice;
import com.auditplatform.finance.domain.InvoiceStatus;
import com.auditplatform.finance.repository.InvoiceRepository;
import com.auditplatform.governance.domain.Complaint;
import com.auditplatform.governance.domain.ComplaintStatus;
import com.auditplatform.governance.repository.ComplaintRepository;
import com.auditplatform.reporting.domain.ReportDataset;
import com.auditplatform.reporting.domain.ReportDefinition;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportDatasetQueryService {

    static final int MAX_ROWS = 1000;

    private final ClientRepository clientRepository;
    private final AuditRepository auditRepository;
    private final FindingRepository findingRepository;
    private final CertificateRepository certificateRepository;
    private final InvoiceRepository invoiceRepository;
    private final ComplaintRepository complaintRepository;

    public ReportDatasetQueryService(
            ClientRepository clientRepository,
            AuditRepository auditRepository,
            FindingRepository findingRepository,
            CertificateRepository certificateRepository,
            InvoiceRepository invoiceRepository,
            ComplaintRepository complaintRepository
    ) {
        this.clientRepository = clientRepository;
        this.auditRepository = auditRepository;
        this.findingRepository = findingRepository;
        this.certificateRepository = certificateRepository;
        this.invoiceRepository = invoiceRepository;
        this.complaintRepository = complaintRepository;
    }

    public DatasetSnapshot query(String tenantId, ReportDefinition definition) {
        PageRequest page = PageRequest.of(0, MAX_ROWS, Sort.by("id"));
        return switch (definition.getDataset()) {
            case CLIENTS -> clients(tenantId, definition, page);
            case AUDITS -> audits(tenantId, definition, page);
            case FINDINGS -> findings(tenantId, definition, page);
            case CERTIFICATES -> certificates(tenantId, definition, page);
            case INVOICES -> invoices(tenantId, definition, page);
            case COMPLAINTS -> complaints(tenantId, definition, page);
        };
    }

    private DatasetSnapshot clients(String tenantId, ReportDefinition definition, PageRequest page) {
        List<String> columns = List.of("clientNumber", "legalName", "status");
        ClientStatus status = definition.getDataset().parseStatus(definition.getStatusFilter());
        List<Client> rows = status == null
                ? clientRepository.findByTenantIdAndDeletedAtIsNull(tenantId, page).getContent()
                : clientRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, page).getContent();
        return new DatasetSnapshot(columns, rows.stream().map(row -> mapOf(
                columns,
                row.getClientNumber(),
                row.getLegalName(),
                name(row.getStatus())
        )).toList());
    }

    private DatasetSnapshot audits(String tenantId, ReportDefinition definition, PageRequest page) {
        List<String> columns = List.of("auditNumber", "clientId", "status");
        AuditStatus status = definition.getDataset().parseStatus(definition.getStatusFilter());
        List<Audit> rows = status == null
                ? auditRepository.findByTenantIdAndDeletedAtIsNull(tenantId, page).getContent()
                : auditRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, page).getContent();
        return new DatasetSnapshot(columns, rows.stream().map(row -> mapOf(
                columns,
                row.getAuditNumber(),
                row.getClientId(),
                name(row.getStatus())
        )).toList());
    }

    private DatasetSnapshot findings(String tenantId, ReportDefinition definition, PageRequest page) {
        List<String> columns = List.of("findingNumber", "auditId", "severity", "status");
        FindingStatus status = definition.getDataset().parseStatus(definition.getStatusFilter());
        List<Finding> rows = status == null
                ? findingRepository.findByTenantIdAndDeletedAtIsNull(tenantId, page).getContent()
                : findingRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, page).getContent();
        return new DatasetSnapshot(columns, rows.stream().map(row -> mapOf(
                columns,
                row.getFindingNumber(),
                row.getAuditId(),
                name(row.getSeverity()),
                name(row.getStatus())
        )).toList());
    }

    private DatasetSnapshot certificates(String tenantId, ReportDefinition definition, PageRequest page) {
        List<String> columns = List.of("certificateNumber", "clientId", "status", "expiresOn");
        CertificateStatus status = definition.getDataset().parseStatus(definition.getStatusFilter());
        List<Certificate> rows = status == null
                ? certificateRepository.findByTenantIdAndDeletedAtIsNull(tenantId, page).getContent()
                : certificateRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, page).getContent();
        return new DatasetSnapshot(columns, rows.stream().map(row -> mapOf(
                columns,
                row.getCertificateNumber(),
                row.getClientId(),
                name(row.getStatus()),
                row.getExpiresOn() == null ? "" : row.getExpiresOn().toString()
        )).toList());
    }

    private DatasetSnapshot invoices(String tenantId, ReportDefinition definition, PageRequest page) {
        List<String> columns = List.of("invoiceNumber", "clientId", "status", "totalAmount", "amountPaid");
        InvoiceStatus status = definition.getDataset().parseStatus(definition.getStatusFilter());
        List<Invoice> rows = status == null
                ? invoiceRepository.findByTenantIdAndDeletedAtIsNull(tenantId, page).getContent()
                : invoiceRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, page).getContent();
        return new DatasetSnapshot(columns, rows.stream().map(row -> mapOf(
                columns,
                row.getInvoiceNumber(),
                row.getClientId(),
                name(row.getStatus()),
                row.getTotalAmount() == null ? "" : row.getTotalAmount().toPlainString(),
                row.getAmountPaid() == null ? "" : row.getAmountPaid().toPlainString()
        )).toList());
    }

    private DatasetSnapshot complaints(String tenantId, ReportDefinition definition, PageRequest page) {
        List<String> columns = List.of("complaintNumber", "clientId", "status", "source");
        ComplaintStatus status = definition.getDataset().parseStatus(definition.getStatusFilter());
        List<Complaint> rows = status == null
                ? complaintRepository.findByTenantIdAndDeletedAtIsNull(tenantId, page).getContent()
                : complaintRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, page).getContent();
        return new DatasetSnapshot(columns, rows.stream().map(row -> mapOf(
                columns,
                row.getComplaintNumber(),
                blank(row.getClientId()),
                name(row.getStatus()),
                name(row.getSource())
        )).toList());
    }

    private static Map<String, String> mapOf(List<String> columns, String... values) {
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            row.put(columns.get(i), values[i] == null ? "" : values[i]);
        }
        return row;
    }

    private static String name(Enum<?> value) {
        return value == null ? "" : value.name();
    }

    private static String blank(String value) {
        return value == null ? "" : value;
    }

    public record DatasetSnapshot(List<String> columns, List<Map<String, String>> rows) {
    }
}
