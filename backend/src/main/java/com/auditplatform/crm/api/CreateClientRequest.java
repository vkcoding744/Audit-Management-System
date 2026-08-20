package com.auditplatform.crm.api;

import com.auditplatform.crm.domain.ClientStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateClientRequest(
        @NotBlank @Size(max = 255) String legalName,
        @Size(max = 255) String tradingName,
        @Size(max = 64) String registrationNumber,
        @Size(max = 64) String taxNumber,
        @Size(max = 128) String industry,
        @Min(0) Integer employeeCount,
        @Email @Size(max = 255) String email,
        @Size(max = 64) String phone,
        @Size(max = 255) String website,
        @Size(max = 255) String addressLine1,
        @Size(max = 255) String addressLine2,
        @Size(max = 128) String city,
        @Size(max = 128) String state,
        @Size(max = 32) String postalCode,
        @Size(max = 128) String country,
        ClientStatus status,
        String notes
) {
}
