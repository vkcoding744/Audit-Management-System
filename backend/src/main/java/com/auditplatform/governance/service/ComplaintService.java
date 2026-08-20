package com.auditplatform.governance.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.crm.domain.Client;
import com.auditplatform.crm.service.ClientService;
import com.auditplatform.governance.api.CloseComplaintRequest;
import com.auditplatform.governance.api.ComplaintResponse;
import com.auditplatform.governance.api.CreateComplaintRequest;
import com.auditplatform.governance.api.UpdateComplaintRequest;
import com.auditplatform.governance.domain.Complaint;
import com.auditplatform.governance.domain.ComplaintSource;
import com.auditplatform.governance.domain.ComplaintStatus;
import com.auditplatform.governance.repository.ComplaintRepository;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final GovernanceNumberService numberService;
    private final ClientService clientService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public ComplaintService(
            ComplaintRepository complaintRepository,
            GovernanceNumberService numberService,
            ClientService clientService,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.complaintRepository = complaintRepository;
        this.numberService = numberService;
        this.clientService = clientService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<ComplaintResponse> list(String clientId, ComplaintStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<Complaint> page;
        if (clientId != null && !clientId.isBlank()) {
            page = complaintRepository.findByTenantIdAndClientIdAndDeletedAtIsNull(tenantId, clientId, pageable);
        } else if (status != null) {
            page = complaintRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = complaintRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(ComplaintResponse::from));
    }

    @Transactional(readOnly = true)
    public ComplaintResponse get(String id) {
        return ComplaintResponse.from(requireComplaint(id));
    }

    @Transactional
    public ComplaintResponse create(CreateComplaintRequest request) {
        String tenantId = isolationService.requireTenantScope();
        String clientId = resolveClient(request.clientId());
        Complaint complaint = new Complaint();
        complaint.setTenantId(tenantId);
        complaint.setComplaintNumber(numberService.nextComplaint(tenantId));
        complaint.setClientId(clientId);
        complaint.setSubject(request.subject().trim());
        complaint.setSource(request.source() == null ? ComplaintSource.OTHER : request.source());
        complaint.setReceivedOn(request.receivedOn() == null ? today() : request.receivedOn());
        complaint.setDescription(blankToNull(request.description()));
        complaint.setStatus(ComplaintStatus.OPEN);
        complaintRepository.save(complaint);
        auditLogService.record("COMPLAINT_CREATE", "Complaint", complaint.getId(), null, complaint.getComplaintNumber(), null, null);
        return ComplaintResponse.from(complaint);
    }

    @Transactional
    public ComplaintResponse update(String id, UpdateComplaintRequest request) {
        Complaint complaint = requireOpenOrReview(id);
        if (request.subject() != null && !request.subject().isBlank()) {
            complaint.setSubject(request.subject().trim());
        }
        if (request.source() != null) {
            complaint.setSource(request.source());
        }
        if (request.receivedOn() != null) {
            complaint.setReceivedOn(request.receivedOn());
        }
        if (request.description() != null) {
            complaint.setDescription(blankToNull(request.description()));
        }
        complaintRepository.save(complaint);
        return ComplaintResponse.from(complaint);
    }

    @Transactional
    public ComplaintResponse startReview(String id) {
        Complaint complaint = requireComplaint(id);
        if (complaint.getStatus() != ComplaintStatus.OPEN) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only open complaints can enter review");
        }
        complaint.setStatus(ComplaintStatus.IN_REVIEW);
        complaintRepository.save(complaint);
        auditLogService.record("COMPLAINT_REVIEW", "Complaint", complaint.getId(), "OPEN", "IN_REVIEW", null, null);
        return ComplaintResponse.from(complaint);
    }

    @Transactional
    public ComplaintResponse close(String id, CloseComplaintRequest request) {
        Complaint complaint = requireOpenOrReview(id);
        complaint.setResolution(request.resolution().trim());
        complaint.setClosedOn(today());
        complaint.setStatus(ComplaintStatus.CLOSED);
        complaintRepository.save(complaint);
        auditLogService.record("COMPLAINT_CLOSE", "Complaint", complaint.getId(), null, "CLOSED", null, null);
        return ComplaintResponse.from(complaint);
    }

    public Complaint requireComplaint(String id) {
        Complaint complaint = complaintRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Complaint not found"));
        isolationService.assertCanAccessTenant(complaint.getTenantId());
        return complaint;
    }

    private Complaint requireOpenOrReview(String id) {
        Complaint complaint = requireComplaint(id);
        if (complaint.getStatus() == ComplaintStatus.CLOSED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Closed complaints cannot be changed");
        }
        return complaint;
    }

    private String resolveClient(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return null;
        }
        Client client = clientService.requireClient(clientId);
        return client.getId();
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
