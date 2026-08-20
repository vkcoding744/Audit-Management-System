package com.auditplatform.crm.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.crm.api.ContactResponse;
import com.auditplatform.crm.api.CreateContactRequest;
import com.auditplatform.crm.api.UpdateContactRequest;
import com.auditplatform.crm.domain.Contact;
import com.auditplatform.crm.repository.ContactRepository;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final ClientService clientService;
    private final SiteService siteService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;

    public ContactService(
            ContactRepository contactRepository,
            ClientService clientService,
            SiteService siteService,
            IsolationService isolationService,
            AuditLogService auditLogService
    ) {
        this.contactRepository = contactRepository;
        this.clientService = clientService;
        this.siteService = siteService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> listByClient(String clientId) {
        var client = clientService.requireClient(clientId);
        return contactRepository.findByTenantIdAndClientIdAndDeletedAtIsNullOrderByLastNameAsc(client.getTenantId(), client.getId())
                .stream()
                .map(ContactResponse::from)
                .toList();
    }

    @Transactional
    public ContactResponse create(String clientId, CreateContactRequest request) {
        var client = clientService.requireClient(clientId);
        Contact contact = new Contact();
        contact.setTenantId(client.getTenantId());
        contact.setClientId(client.getId());
        contact.setFirstName(request.firstName().trim());
        contact.setLastName(request.lastName().trim());
        contact.setDesignation(blankToNull(request.designation()));
        contact.setEmail(blankToNull(request.email()));
        contact.setPhone(blankToNull(request.phone()));
        contact.setDepartment(blankToNull(request.department()));
        contact.setSiteId(resolveSite(client.getId(), request.siteId()));
        contact.setPrimaryContact(request.primaryContact());
        contact.setActive(true);
        contactRepository.save(contact);
        if (contact.isPrimaryContact()) {
            contactRepository.clearPrimaryExcept(client.getTenantId(), client.getId(), contact.getId());
        }
        auditLogService.record("CONTACT_CREATE", "Contact", contact.getId(), null, contact.getEmail(), null, null);
        return ContactResponse.from(contact);
    }

    @Transactional
    public ContactResponse update(String contactId, UpdateContactRequest request) {
        Contact contact = requireContact(contactId);
        if (request.firstName() != null && !request.firstName().isBlank()) {
            contact.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            contact.setLastName(request.lastName().trim());
        }
        if (request.designation() != null) {
            contact.setDesignation(blankToNull(request.designation()));
        }
        if (request.email() != null) {
            contact.setEmail(blankToNull(request.email()));
        }
        if (request.phone() != null) {
            contact.setPhone(blankToNull(request.phone()));
        }
        if (request.department() != null) {
            contact.setDepartment(blankToNull(request.department()));
        }
        if (request.siteId() != null) {
            contact.setSiteId(resolveSite(contact.getClientId(), request.siteId().isBlank() ? null : request.siteId()));
        }
        if (request.active() != null) {
            contact.setActive(request.active());
        }
        if (request.primaryContact() != null) {
            contact.setPrimaryContact(request.primaryContact());
        }
        contactRepository.save(contact);
        if (contact.isPrimaryContact()) {
            contactRepository.clearPrimaryExcept(contact.getTenantId(), contact.getClientId(), contact.getId());
        }
        return ContactResponse.from(contact);
    }

    @Transactional
    public void delete(String contactId) {
        Contact contact = requireContact(contactId);
        contact.setDeletedAt(Instant.now());
        contactRepository.save(contact);
        auditLogService.record("CONTACT_DELETE", "Contact", contact.getId(), contact.getEmail(), null, null, null);
    }

    private String resolveSite(String clientId, String siteId) {
        if (siteId == null || siteId.isBlank()) {
            return null;
        }
        var site = siteService.requireSite(siteId);
        if (!clientId.equals(site.getClientId())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Site does not belong to this client");
        }
        return site.getId();
    }

    private Contact requireContact(String id) {
        Contact contact = contactRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Contact not found"));
        isolationService.assertCanAccessTenant(contact.getTenantId());
        return contact;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
