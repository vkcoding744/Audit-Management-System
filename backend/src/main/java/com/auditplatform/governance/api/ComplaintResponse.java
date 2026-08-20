package com.auditplatform.governance.api;

import com.auditplatform.governance.domain.Complaint;
import com.auditplatform.governance.domain.ComplaintSource;
import com.auditplatform.governance.domain.ComplaintStatus;

import java.time.LocalDate;

public record ComplaintResponse(
        String id,
        String tenantId,
        String complaintNumber,
        String clientId,
        String subject,
        ComplaintSource source,
        LocalDate receivedOn,
        ComplaintStatus status,
        String description,
        String resolution,
        LocalDate closedOn
) {
    public static ComplaintResponse from(Complaint complaint) {
        return new ComplaintResponse(
                complaint.getId(),
                complaint.getTenantId(),
                complaint.getComplaintNumber(),
                complaint.getClientId(),
                complaint.getSubject(),
                complaint.getSource(),
                complaint.getReceivedOn(),
                complaint.getStatus(),
                complaint.getDescription(),
                complaint.getResolution(),
                complaint.getClosedOn()
        );
    }
}
