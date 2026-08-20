package com.auditplatform.governance.service;

import com.auditplatform.audit.domain.Finding;
import com.auditplatform.audit.service.FindingService;
import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.certification.domain.Certificate;
import com.auditplatform.certification.service.CertificateService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.crm.service.ClientService;
import com.auditplatform.governance.api.AppealResponse;
import com.auditplatform.governance.api.CreateAppealRequest;
import com.auditplatform.governance.api.DecideAppealRequest;
import com.auditplatform.governance.api.UpdateAppealRequest;
import com.auditplatform.governance.domain.Appeal;
import com.auditplatform.governance.domain.AppealStatus;
import com.auditplatform.governance.repository.AppealRepository;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class AppealService {

    private final AppealRepository appealRepository;
    private final GovernanceNumberService numberService;
    private final ClientService clientService;
    private final CertificateService certificateService;
    private final FindingService findingService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public AppealService(
            AppealRepository appealRepository,
            GovernanceNumberService numberService,
            ClientService clientService,
            CertificateService certificateService,
            FindingService findingService,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.appealRepository = appealRepository;
        this.numberService = numberService;
        this.clientService = clientService;
        this.certificateService = certificateService;
        this.findingService = findingService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<AppealResponse> list(String clientId, AppealStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<Appeal> page;
        if (clientId != null && !clientId.isBlank()) {
            page = appealRepository.findByTenantIdAndClientIdAndDeletedAtIsNull(tenantId, clientId, pageable);
        } else if (status != null) {
            page = appealRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = appealRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(AppealResponse::from));
    }

    @Transactional(readOnly = true)
    public AppealResponse get(String id) {
        return AppealResponse.from(requireAppeal(id));
    }

    @Transactional
    public AppealResponse create(CreateAppealRequest request) {
        String tenantId = isolationService.requireTenantScope();
        String clientId = request.clientId() == null || request.clientId().isBlank() ? null : clientService.requireClient(request.clientId()).getId();
        String certificateId = null;
        if (request.certificateId() != null && !request.certificateId().isBlank()) {
            Certificate certificate = certificateService.requireCertificate(request.certificateId());
            if (clientId != null && !clientId.equals(certificate.getClientId())) {
                throw new ApiException(ErrorCode.SYS_VALIDATION, "Certificate does not belong to this client");
            }
            if (clientId == null) {
                clientId = certificate.getClientId();
            }
            certificateId = certificate.getId();
        }
        String findingId = null;
        if (request.findingId() != null && !request.findingId().isBlank()) {
            Finding finding = findingService.requireFinding(request.findingId());
            findingId = finding.getId();
        }
        Appeal appeal = new Appeal();
        appeal.setTenantId(tenantId);
        appeal.setAppealNumber(numberService.nextAppeal(tenantId));
        appeal.setClientId(clientId);
        appeal.setCertificateId(certificateId);
        appeal.setFindingId(findingId);
        appeal.setSubject(request.subject().trim());
        appeal.setReceivedOn(request.receivedOn() == null ? today() : request.receivedOn());
        appeal.setDescription(blankToNull(request.description()));
        appeal.setStatus(AppealStatus.OPEN);
        appealRepository.save(appeal);
        auditLogService.record("APPEAL_CREATE", "Appeal", appeal.getId(), null, appeal.getAppealNumber(), null, null);
        return AppealResponse.from(appeal);
    }

    @Transactional
    public AppealResponse update(String id, UpdateAppealRequest request) {
        Appeal appeal = requireOpen(id);
        if (request.subject() != null && !request.subject().isBlank()) {
            appeal.setSubject(request.subject().trim());
        }
        if (request.receivedOn() != null) {
            appeal.setReceivedOn(request.receivedOn());
        }
        if (request.description() != null) {
            appeal.setDescription(blankToNull(request.description()));
        }
        appealRepository.save(appeal);
        return AppealResponse.from(appeal);
    }

    @Transactional
    public AppealResponse startReview(String id) {
        Appeal appeal = requireAppeal(id);
        if (appeal.getStatus() != AppealStatus.OPEN) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only open appeals can enter review");
        }
        appeal.setStatus(AppealStatus.UNDER_REVIEW);
        appealRepository.save(appeal);
        auditLogService.record("APPEAL_REVIEW", "Appeal", appeal.getId(), "OPEN", "UNDER_REVIEW", null, null);
        return AppealResponse.from(appeal);
    }

    @Transactional
    public AppealResponse decide(String id, DecideAppealRequest request) {
        Appeal appeal = requireAppeal(id);
        if (appeal.getStatus() == AppealStatus.UPHELD || appeal.getStatus() == AppealStatus.DISMISSED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Decided appeals cannot be changed");
        }
        AppealStatus next = request.outcome() == com.auditplatform.governance.domain.AppealOutcome.UPHELD
                ? AppealStatus.UPHELD
                : AppealStatus.DISMISSED;
        appeal.setOutcome(request.outcome());
        appeal.setDecisionNotes(blankToNull(request.notes()));
        appeal.setDecidedOn(today());
        appeal.setStatus(next);
        appealRepository.save(appeal);
        auditLogService.record("APPEAL_DECIDE", "Appeal", appeal.getId(), null, next.name(), null, null);
        return AppealResponse.from(appeal);
    }

    public Appeal requireAppeal(String id) {
        Appeal appeal = appealRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Appeal not found"));
        isolationService.assertCanAccessTenant(appeal.getTenantId());
        return appeal;
    }

    private Appeal requireOpen(String id) {
        Appeal appeal = requireAppeal(id);
        if (appeal.getStatus() != AppealStatus.OPEN && appeal.getStatus() != AppealStatus.UNDER_REVIEW) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Decided appeals cannot be changed");
        }
        return appeal;
    }

    private LocalDate today() {
        return LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
