package com.auditplatform.crm.api;

import com.auditplatform.crm.domain.LeadSource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLeadRequest(
        @NotBlank @Size(max = 255) String organisationName,
        @Size(max = 255) String contactName,
        @Email @Size(max = 255) String email,
        @Size(max = 64) String phone,
        LeadSource source,
        String notes
) {
}
