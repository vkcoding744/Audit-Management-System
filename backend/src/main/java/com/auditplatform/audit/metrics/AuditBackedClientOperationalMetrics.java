package com.auditplatform.audit.metrics;

import com.auditplatform.audit.domain.AuditStatus;
import com.auditplatform.audit.domain.CapaStatus;
import com.auditplatform.audit.domain.FindingStatus;
import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.audit.repository.CapaActionRepository;
import com.auditplatform.audit.repository.FindingRepository;
import com.auditplatform.certification.domain.CertificateStatus;
import com.auditplatform.certification.repository.CertificateRepository;
import com.auditplatform.crm.metrics.ClientOperationalMetrics;
import com.auditplatform.crm.metrics.ClientOperationalMetricsPort;
import com.auditplatform.document.repository.DocumentRepository;
import com.auditplatform.finance.domain.InvoiceStatus;
import com.auditplatform.finance.repository.InvoiceRepository;
import com.auditplatform.governance.domain.AppealStatus;
import com.auditplatform.governance.domain.ComplaintStatus;
import com.auditplatform.governance.repository.AppealRepository;
import com.auditplatform.governance.repository.ComplaintRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Component
@Primary
public class AuditBackedClientOperationalMetrics implements ClientOperationalMetricsPort {

    private static final List<AuditStatus> UPCOMING = List.of(AuditStatus.PLANNED, AuditStatus.SCHEDULED, AuditStatus.IN_PROGRESS);

    private final AuditRepository auditRepository;
    private final FindingRepository findingRepository;
    private final CapaActionRepository capaRepository;
    private final CertificateRepository certificateRepository;
    private final DocumentRepository documentRepository;
    private final InvoiceRepository invoiceRepository;
    private final ComplaintRepository complaintRepository;
    private final AppealRepository appealRepository;
    private final Clock clock;

    public AuditBackedClientOperationalMetrics(
            AuditRepository auditRepository,
            FindingRepository findingRepository,
            CapaActionRepository capaRepository,
            CertificateRepository certificateRepository,
            DocumentRepository documentRepository,
            InvoiceRepository invoiceRepository,
            ComplaintRepository complaintRepository,
            AppealRepository appealRepository,
            Clock clock
    ) {
        this.auditRepository = auditRepository;
        this.findingRepository = findingRepository;
        this.capaRepository = capaRepository;
        this.certificateRepository = certificateRepository;
        this.documentRepository = documentRepository;
        this.invoiceRepository = invoiceRepository;
        this.complaintRepository = complaintRepository;
        this.appealRepository = appealRepository;
        this.clock = clock;
    }

    @Override
    public ClientOperationalMetrics load(String tenantId, String clientId) {
        LocalDate today = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
        long upcoming = auditRepository.countByTenantIdAndClientIdAndStatusInAndDeletedAtIsNull(tenantId, clientId, UPCOMING);
        long completed = auditRepository.countByTenantIdAndClientIdAndStatusInAndDeletedAtIsNull(
                tenantId,
                clientId,
                List.of(AuditStatus.COMPLETED)
        );
        long openFindings = findingRepository.countByTenantIdAndClientIdAndStatusAndDeletedAtIsNull(
                tenantId,
                clientId,
                FindingStatus.OPEN
        );
        long overdueCapa = capaRepository.countOverdueForClient(
                tenantId,
                clientId,
                CapaStatus.OPEN,
                today
        );
        long activeCertificates = certificateRepository
                .countByTenantIdAndClientIdAndStatusAndExpiresOnGreaterThanEqualAndDeletedAtIsNull(
                        tenantId,
                        clientId,
                        CertificateStatus.ACTIVE,
                        today
                );
        long expiringSoon = certificateRepository
                .countByTenantIdAndClientIdAndStatusAndExpiresOnGreaterThanEqualAndExpiresOnLessThanEqualAndDeletedAtIsNull(
                        tenantId,
                        clientId,
                        CertificateStatus.ACTIVE,
                        today,
                        today.plusDays(90)
                );
        long documents = documentRepository.countByTenantIdAndClientIdAndDeletedAtIsNull(tenantId, clientId);
        long outstandingPayments = invoiceRepository.countByTenantIdAndClientIdAndStatusInAndDeletedAtIsNull(
                tenantId,
                clientId,
                List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID)
        );
        long openComplaints = complaintRepository.countByTenantIdAndClientIdAndStatusInAndDeletedAtIsNull(
                tenantId,
                clientId,
                List.of(ComplaintStatus.OPEN, ComplaintStatus.IN_REVIEW)
        );
        long openAppeals = appealRepository.countByTenantIdAndClientIdAndStatusInAndDeletedAtIsNull(
                tenantId,
                clientId,
                List.of(AppealStatus.OPEN, AppealStatus.UNDER_REVIEW)
        );
        return new ClientOperationalMetrics(
                upcoming,
                completed,
                openFindings,
                overdueCapa,
                activeCertificates,
                expiringSoon,
                outstandingPayments,
                documents,
                openComplaints,
                openAppeals
        );
    }
}
