package com.auditplatform.dashboard.service;

import com.auditplatform.ai.domain.AiGenerationStatus;
import com.auditplatform.ai.repository.AiGenerationRepository;
import com.auditplatform.audit.domain.AuditStatus;
import com.auditplatform.audit.domain.CapaStatus;
import com.auditplatform.audit.domain.FindingStatus;
import com.auditplatform.audit.repository.AuditRepository;
import com.auditplatform.audit.repository.CapaActionRepository;
import com.auditplatform.audit.repository.FindingRepository;
import com.auditplatform.certification.domain.CertificateStatus;
import com.auditplatform.certification.repository.CertificateRepository;
import com.auditplatform.crm.repository.ClientRepository;
import com.auditplatform.dashboard.api.TenantDashboardResponse;
import com.auditplatform.finance.domain.InvoiceStatus;
import com.auditplatform.finance.repository.InvoiceRepository;
import com.auditplatform.governance.domain.AppealStatus;
import com.auditplatform.governance.domain.ComplaintStatus;
import com.auditplatform.governance.repository.AppealRepository;
import com.auditplatform.governance.repository.ComplaintRepository;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class TenantDashboardService {

    private static final List<AuditStatus> UPCOMING = List.of(
            AuditStatus.PLANNED,
            AuditStatus.SCHEDULED,
            AuditStatus.IN_PROGRESS
    );

    private final IsolationService isolationService;
    private final ClientRepository clientRepository;
    private final AuditRepository auditRepository;
    private final FindingRepository findingRepository;
    private final CapaActionRepository capaRepository;
    private final CertificateRepository certificateRepository;
    private final InvoiceRepository invoiceRepository;
    private final ComplaintRepository complaintRepository;
    private final AppealRepository appealRepository;
    private final AiGenerationRepository aiGenerationRepository;
    private final Clock clock;

    public TenantDashboardService(
            IsolationService isolationService,
            ClientRepository clientRepository,
            AuditRepository auditRepository,
            FindingRepository findingRepository,
            CapaActionRepository capaRepository,
            CertificateRepository certificateRepository,
            InvoiceRepository invoiceRepository,
            ComplaintRepository complaintRepository,
            AppealRepository appealRepository,
            AiGenerationRepository aiGenerationRepository,
            Clock clock
    ) {
        this.isolationService = isolationService;
        this.clientRepository = clientRepository;
        this.auditRepository = auditRepository;
        this.findingRepository = findingRepository;
        this.capaRepository = capaRepository;
        this.certificateRepository = certificateRepository;
        this.invoiceRepository = invoiceRepository;
        this.complaintRepository = complaintRepository;
        this.appealRepository = appealRepository;
        this.aiGenerationRepository = aiGenerationRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public TenantDashboardResponse summary() {
        String tenantId = isolationService.requireTenantScope();
        LocalDate today = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
        return new TenantDashboardResponse(
                clientRepository.countByTenantIdAndDeletedAtIsNull(tenantId),
                auditRepository.countByTenantIdAndStatusInAndDeletedAtIsNull(tenantId, UPCOMING),
                auditRepository.countByTenantIdAndStatusInAndDeletedAtIsNull(tenantId, List.of(AuditStatus.COMPLETED)),
                findingRepository.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, FindingStatus.OPEN),
                capaRepository.countOverdueForTenant(tenantId, CapaStatus.OPEN, today),
                certificateRepository.countByTenantIdAndStatusAndExpiresOnGreaterThanEqualAndDeletedAtIsNull(
                        tenantId,
                        CertificateStatus.ACTIVE,
                        today
                ),
                certificateRepository.countByTenantIdAndStatusAndExpiresOnGreaterThanEqualAndExpiresOnLessThanEqualAndDeletedAtIsNull(
                        tenantId,
                        CertificateStatus.ACTIVE,
                        today,
                        today.plusDays(90)
                ),
                invoiceRepository.countByTenantIdAndStatusInAndDeletedAtIsNull(
                        tenantId,
                        List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID)
                ),
                complaintRepository.countByTenantIdAndStatusInAndDeletedAtIsNull(
                        tenantId,
                        List.of(ComplaintStatus.OPEN, ComplaintStatus.IN_REVIEW)
                ),
                appealRepository.countByTenantIdAndStatusInAndDeletedAtIsNull(
                        tenantId,
                        List.of(AppealStatus.OPEN, AppealStatus.UNDER_REVIEW)
                ),
                aiGenerationRepository.countByTenantIdAndStatusAndDeletedAtIsNull(
                        tenantId,
                        AiGenerationStatus.PENDING_REVIEW
                )
        );
    }
}
