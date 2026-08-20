package com.auditplatform.auditor.api;

import com.auditplatform.auditor.domain.Auditor;
import com.auditplatform.auditor.domain.AuditorStatus;
import com.auditplatform.auditor.domain.EmploymentType;

public record AuditorResponse(
        String id,
        String tenantId,
        String userId,
        String employeeNumber,
        String firstName,
        String lastName,
        String email,
        String phone,
        String jobTitle,
        EmploymentType employmentType,
        AuditorStatus status,
        String baseLocation,
        String country,
        String notes
) {
    public static AuditorResponse from(Auditor auditor) {
        return new AuditorResponse(
                auditor.getId(),
                auditor.getTenantId(),
                auditor.getUserId(),
                auditor.getEmployeeNumber(),
                auditor.getFirstName(),
                auditor.getLastName(),
                auditor.getEmail(),
                auditor.getPhone(),
                auditor.getJobTitle(),
                auditor.getEmploymentType(),
                auditor.getStatus(),
                auditor.getBaseLocation(),
                auditor.getCountry(),
                auditor.getNotes()
        );
    }
}
