package com.auditplatform.crm.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.crm.api.ClientResponse;
import com.auditplatform.crm.api.CreateClientRequest;
import com.auditplatform.crm.api.CreateLeadRequest;
import com.auditplatform.crm.api.LeadResponse;
import com.auditplatform.crm.api.LoseLeadRequest;
import com.auditplatform.crm.api.UpdateLeadRequest;
import com.auditplatform.crm.domain.ClientStatus;
import com.auditplatform.crm.domain.Lead;
import com.auditplatform.crm.domain.LeadSource;
import com.auditplatform.crm.domain.LeadStatus;
import com.auditplatform.crm.repository.LeadRepository;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadNumberService leadNumberService;
    private final ClientService clientService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public LeadService(
            LeadRepository leadRepository,
            LeadNumberService leadNumberService,
            ClientService clientService,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.leadRepository = leadRepository;
        this.leadNumberService = leadNumberService;
        this.clientService = clientService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<LeadResponse> list(LeadStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<Lead> page = status == null
                ? leadRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                : leadRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        return PageResponse.from(page.map(LeadResponse::from));
    }

    @Transactional(readOnly = true)
    public LeadResponse get(String id) {
        return LeadResponse.from(requireLead(id));
    }

    @Transactional
    public LeadResponse create(CreateLeadRequest request) {
        String tenantId = isolationService.requireTenantScope();
        Lead lead = new Lead();
        lead.setTenantId(tenantId);
        lead.setLeadNumber(leadNumberService.next(tenantId));
        lead.setOrganisationName(request.organisationName().trim());
        lead.setContactName(blankToNull(request.contactName()));
        lead.setEmail(blankToNull(request.email()));
        lead.setPhone(blankToNull(request.phone()));
        lead.setSource(request.source() == null ? LeadSource.OTHER : request.source());
        lead.setStatus(LeadStatus.OPEN);
        lead.setNotes(blankToNull(request.notes()));
        leadRepository.save(lead);
        auditLogService.record("LEAD_CREATE", "Lead", lead.getId(), null, lead.getLeadNumber(), null, null);
        return LeadResponse.from(lead);
    }

    @Transactional
    public LeadResponse update(String id, UpdateLeadRequest request) {
        Lead lead = requireLead(id);
        assertEditable(lead);
        if (request.organisationName() != null && !request.organisationName().isBlank()) {
            lead.setOrganisationName(request.organisationName().trim());
        }
        if (request.contactName() != null) {
            lead.setContactName(blankToNull(request.contactName()));
        }
        if (request.email() != null) {
            lead.setEmail(blankToNull(request.email()));
        }
        if (request.phone() != null) {
            lead.setPhone(blankToNull(request.phone()));
        }
        if (request.source() != null) {
            lead.setSource(request.source());
        }
        if (request.notes() != null) {
            lead.setNotes(blankToNull(request.notes()));
        }
        leadRepository.save(lead);
        auditLogService.record("LEAD_UPDATE", "Lead", lead.getId(), null, lead.getLeadNumber(), null, null);
        return LeadResponse.from(lead);
    }

    @Transactional
    public LeadResponse qualify(String id) {
        Lead lead = requireLead(id);
        if (lead.getStatus() != LeadStatus.OPEN) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only open leads can be qualified");
        }
        lead.setStatus(LeadStatus.QUALIFIED);
        leadRepository.save(lead);
        auditLogService.record("LEAD_QUALIFY", "Lead", lead.getId(), "OPEN", "QUALIFIED", null, null);
        return LeadResponse.from(lead);
    }

    @Transactional
    public LeadResponse lose(String id, LoseLeadRequest request) {
        Lead lead = requireLead(id);
        assertEditable(lead);
        lead.setStatus(LeadStatus.LOST);
        lead.setLostReason(request.reason().trim());
        leadRepository.save(lead);
        auditLogService.record("LEAD_LOSE", "Lead", lead.getId(), null, "LOST", null, null);
        return LeadResponse.from(lead);
    }

    @Transactional
    public LeadResponse convert(String id) {
        Lead lead = requireLead(id);
        if (lead.getStatus() == LeadStatus.CONVERTED) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "Lead is already converted");
        }
        if (lead.getStatus() == LeadStatus.LOST) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Lost leads cannot be converted");
        }
        String notes = lead.getNotes() == null
                ? "Converted from " + lead.getLeadNumber()
                : lead.getNotes() + "\nConverted from " + lead.getLeadNumber();
        ClientResponse client = clientService.create(new CreateClientRequest(
                lead.getOrganisationName(),
                null,
                null,
                null,
                null,
                null,
                lead.getEmail(),
                lead.getPhone(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ClientStatus.PROSPECT,
                notes
        ));
        lead.setStatus(LeadStatus.CONVERTED);
        lead.setConvertedClientId(client.id());
        lead.setConvertedAt(Instant.now(clock));
        leadRepository.save(lead);
        auditLogService.record("LEAD_CONVERT", "Lead", lead.getId(), lead.getLeadNumber(), client.clientNumber(), null, null);
        return LeadResponse.from(lead);
    }

    public Lead requireLead(String id) {
        Lead lead = leadRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Lead not found"));
        isolationService.assertCanAccessTenant(lead.getTenantId());
        return lead;
    }

    private void assertEditable(Lead lead) {
        if (lead.getStatus() == LeadStatus.CONVERTED || lead.getStatus() == LeadStatus.LOST) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Converted or lost leads cannot be edited");
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
