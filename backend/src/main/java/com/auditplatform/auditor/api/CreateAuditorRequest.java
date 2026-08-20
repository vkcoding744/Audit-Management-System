package com.auditplatform.auditor.api;

import com.auditplatform.auditor.domain.AuditorStatus;
import com.auditplatform.auditor.domain.EmploymentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAuditorRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Email @Size(max = 255) String email,
        @Size(max = 64) String phone,
        @Size(max = 128) String jobTitle,
        EmploymentType employmentType,
        AuditorStatus status,
        @Size(max = 255) String baseLocation,
        @Size(max = 128) String country,
        String userId,
        String notes
) {
}
