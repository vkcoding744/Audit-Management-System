package com.auditplatform.crm.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateContactRequest(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Size(max = 128) String designation,
        @Email @Size(max = 255) String email,
        @Size(max = 64) String phone,
        @Size(max = 128) String department,
        String siteId,
        Boolean primaryContact,
        Boolean active
) {
}
