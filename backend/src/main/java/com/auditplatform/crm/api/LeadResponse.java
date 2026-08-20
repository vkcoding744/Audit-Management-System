package com.auditplatform.crm.api;

import com.auditplatform.crm.domain.Lead;
import com.auditplatform.crm.domain.LeadSource;
import com.auditplatform.crm.domain.LeadStatus;

import java.time.Instant;

public record LeadResponse(
        String id,
        String tenantId,
        String leadNumber,
        String organisationName,
        String contactName,
        String email,
        String phone,
        LeadSource source,
        LeadStatus status,
        String convertedClientId,
        Instant convertedAt,
        String lostReason,
        String notes
) {
    public static LeadResponse from(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getTenantId(),
                lead.getLeadNumber(),
                lead.getOrganisationName(),
                lead.getContactName(),
                lead.getEmail(),
                lead.getPhone(),
                lead.getSource(),
                lead.getStatus(),
                lead.getConvertedClientId(),
                lead.getConvertedAt(),
                lead.getLostReason(),
                lead.getNotes()
        );
    }
}
