package com.auditplatform.crm.web;

import com.auditplatform.common.api.ApiResponse;
import com.auditplatform.common.web.CorrelationId;
import com.auditplatform.crm.api.ContactResponse;
import com.auditplatform.crm.api.CreateContactRequest;
import com.auditplatform.crm.api.UpdateContactRequest;
import com.auditplatform.crm.service.ContactService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/api/v1/clients/{clientId}/contacts")
    @PreAuthorize("hasAuthority('CONTACT_VIEW')")
    public ApiResponse<List<ContactResponse>> list(@PathVariable String clientId) {
        return ApiResponse.ok(contactService.listByClient(clientId), MDC.get(CorrelationId.MDC_KEY));
    }

    @PostMapping("/api/v1/clients/{clientId}/contacts")
    @PreAuthorize("hasAuthority('CONTACT_CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ContactResponse> create(
            @PathVariable String clientId,
            @Valid @RequestBody CreateContactRequest request
    ) {
        return ApiResponse.ok(contactService.create(clientId, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @PatchMapping("/api/v1/contacts/{id}")
    @PreAuthorize("hasAuthority('CONTACT_UPDATE')")
    public ApiResponse<ContactResponse> update(@PathVariable String id, @Valid @RequestBody UpdateContactRequest request) {
        return ApiResponse.ok(contactService.update(id, request), MDC.get(CorrelationId.MDC_KEY));
    }

    @DeleteMapping("/api/v1/contacts/{id}")
    @PreAuthorize("hasAuthority('CONTACT_DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        contactService.delete(id);
    }
}
