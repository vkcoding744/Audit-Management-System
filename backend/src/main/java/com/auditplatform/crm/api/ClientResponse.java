package com.auditplatform.crm.api;

import com.auditplatform.crm.domain.Client;
import com.auditplatform.crm.domain.ClientStatus;

public record ClientResponse(
        String id,
        String tenantId,
        String clientNumber,
        String legalName,
        String tradingName,
        String registrationNumber,
        String taxNumber,
        String industry,
        Integer employeeCount,
        String email,
        String phone,
        String website,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country,
        ClientStatus status,
        String notes
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getTenantId(),
                client.getClientNumber(),
                client.getLegalName(),
                client.getTradingName(),
                client.getRegistrationNumber(),
                client.getTaxNumber(),
                client.getIndustry(),
                client.getEmployeeCount(),
                client.getEmail(),
                client.getPhone(),
                client.getWebsite(),
                client.getAddressLine1(),
                client.getAddressLine2(),
                client.getCity(),
                client.getState(),
                client.getPostalCode(),
                client.getCountry(),
                client.getStatus(),
                client.getNotes()
        );
    }
}
