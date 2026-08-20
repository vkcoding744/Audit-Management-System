package com.auditplatform.crm.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateContactRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 128) String designation,
        @Email @Size(max = 255) String email,
        @Size(max = 64) String phone,
        @Size(max = 128) String department,
        String siteId,
        boolean primaryContact
) {
}
