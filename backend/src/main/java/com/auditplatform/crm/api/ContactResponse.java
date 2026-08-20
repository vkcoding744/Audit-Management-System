package com.auditplatform.crm.api;

import com.auditplatform.crm.domain.Contact;

public record ContactResponse(
        String id,
        String tenantId,
        String clientId,
        String siteId,
        String firstName,
        String lastName,
        String designation,
        String email,
        String phone,
        String department,
        boolean primaryContact,
        boolean active
) {
    public static ContactResponse from(Contact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getTenantId(),
                contact.getClientId(),
                contact.getSiteId(),
                contact.getFirstName(),
                contact.getLastName(),
                contact.getDesignation(),
                contact.getEmail(),
                contact.getPhone(),
                contact.getDepartment(),
                contact.isPrimaryContact(),
                contact.isActive()
        );
    }
}
