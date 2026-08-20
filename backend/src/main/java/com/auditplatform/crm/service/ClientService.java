package com.auditplatform.crm.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.crm.api.ClientDashboardResponse;
import com.auditplatform.crm.api.ClientResponse;
import com.auditplatform.crm.api.CreateClientRequest;
import com.auditplatform.crm.api.UpdateClientRequest;
import com.auditplatform.crm.domain.Client;
import com.auditplatform.crm.domain.ClientStatus;
import com.auditplatform.crm.metrics.ClientOperationalMetrics;
import com.auditplatform.crm.metrics.ClientOperationalMetricsPort;
import com.auditplatform.crm.repository.ClientRepository;
import com.auditplatform.crm.repository.ContactRepository;
import com.auditplatform.crm.repository.SiteRepository;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final SiteRepository siteRepository;
    private final ContactRepository contactRepository;
    private final ClientNumberService clientNumberService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final ClientOperationalMetricsPort operationalMetricsPort;

    public ClientService(
            ClientRepository clientRepository,
            SiteRepository siteRepository,
            ContactRepository contactRepository,
            ClientNumberService clientNumberService,
            IsolationService isolationService,
            AuditLogService auditLogService,
            ClientOperationalMetricsPort operationalMetricsPort
    ) {
        this.clientRepository = clientRepository;
        this.siteRepository = siteRepository;
        this.contactRepository = contactRepository;
        this.clientNumberService = clientNumberService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.operationalMetricsPort = operationalMetricsPort;
    }

    @Transactional(readOnly = true)
    public PageResponse<ClientResponse> list(String query, ClientStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<Client> page;
        if (query != null && !query.isBlank()) {
            page = clientRepository.search(tenantId, query.trim(), pageable);
        } else if (status != null) {
            page = clientRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = clientRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(ClientResponse::from));
    }

    @Transactional(readOnly = true)
    public ClientResponse get(String id) {
        return ClientResponse.from(requireClient(id));
    }

    @Transactional
    public ClientResponse create(CreateClientRequest request) {
        String tenantId = isolationService.requireTenantScope();
        Client client = new Client();
        client.setTenantId(tenantId);
        client.setClientNumber(clientNumberService.next(tenantId));
        client.setLegalName(request.legalName().trim());
        applyCreate(client, request);
        clientRepository.save(client);
        auditLogService.record("CLIENT_CREATE", "Client", client.getId(), null, client.getLegalName(), null, null);
        return ClientResponse.from(client);
    }

    @Transactional
    public ClientResponse update(String id, UpdateClientRequest request) {
        Client client = requireClient(id);
        if (request.legalName() != null && !request.legalName().isBlank()) {
            client.setLegalName(request.legalName().trim());
        }
        if (request.tradingName() != null) {
            client.setTradingName(blankToNull(request.tradingName()));
        }
        if (request.registrationNumber() != null) {
            client.setRegistrationNumber(blankToNull(request.registrationNumber()));
        }
        if (request.taxNumber() != null) {
            client.setTaxNumber(blankToNull(request.taxNumber()));
        }
        if (request.industry() != null) {
            client.setIndustry(blankToNull(request.industry()));
        }
        if (request.employeeCount() != null) {
            client.setEmployeeCount(request.employeeCount());
        }
        if (request.email() != null) {
            client.setEmail(blankToNull(request.email()));
        }
        if (request.phone() != null) {
            client.setPhone(blankToNull(request.phone()));
        }
        if (request.website() != null) {
            client.setWebsite(blankToNull(request.website()));
        }
        if (request.addressLine1() != null) {
            client.setAddressLine1(blankToNull(request.addressLine1()));
        }
        if (request.addressLine2() != null) {
            client.setAddressLine2(blankToNull(request.addressLine2()));
        }
        if (request.city() != null) {
            client.setCity(blankToNull(request.city()));
        }
        if (request.state() != null) {
            client.setState(blankToNull(request.state()));
        }
        if (request.postalCode() != null) {
            client.setPostalCode(blankToNull(request.postalCode()));
        }
        if (request.country() != null) {
            client.setCountry(blankToNull(request.country()));
        }
        if (request.status() != null) {
            client.setStatus(request.status());
        }
        if (request.notes() != null) {
            client.setNotes(blankToNull(request.notes()));
        }
        clientRepository.save(client);
        auditLogService.record("CLIENT_UPDATE", "Client", client.getId(), null, client.getLegalName(), null, null);
        return ClientResponse.from(client);
    }

    @Transactional
    public ClientResponse setStatus(String id, ClientStatus status) {
        Client client = requireClient(id);
        client.setStatus(status);
        clientRepository.save(client);
        auditLogService.record("CLIENT_STATUS", "Client", client.getId(), null, status.name(), null, null);
        return ClientResponse.from(client);
    }

    @Transactional
    public void delete(String id) {
        Client client = requireClient(id);
        Instant now = Instant.now();
        client.setDeletedAt(now);
        clientRepository.save(client);
        siteRepository.findByTenantIdAndClientIdAndDeletedAtIsNullOrderByNameAsc(client.getTenantId(), client.getId())
                .forEach(site -> {
                    site.setDeletedAt(now);
                    siteRepository.save(site);
                });
        contactRepository.findByTenantIdAndClientIdAndDeletedAtIsNullOrderByLastNameAsc(client.getTenantId(), client.getId())
                .forEach(contact -> {
                    contact.setDeletedAt(now);
                    contactRepository.save(contact);
                });
        auditLogService.record("CLIENT_DELETE", "Client", client.getId(), client.getLegalName(), null, null, null);
    }

    @Transactional(readOnly = true)
    public ClientDashboardResponse dashboard(String id) {
        Client client = requireClient(id);
        String tenantId = client.getTenantId();
        ClientOperationalMetrics metrics = operationalMetricsPort.load(tenantId, client.getId());
        return new ClientDashboardResponse(
                ClientResponse.from(client),
                siteRepository.countByTenantIdAndClientIdAndDeletedAtIsNull(tenantId, client.getId()),
                contactRepository.countByTenantIdAndClientIdAndDeletedAtIsNull(tenantId, client.getId()),
                metrics.upcomingAudits(),
                metrics.completedAudits(),
                metrics.openFindings(),
                metrics.overdueCapa(),
                metrics.activeCertificates(),
                metrics.certificatesExpiringSoon(),
                metrics.outstandingPayments(),
                metrics.documents(),
                metrics.openComplaints(),
                metrics.openAppeals()
        );
    }

    public Client requireClient(String id) {
        Client client = clientRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Client not found"));
        isolationService.assertCanAccessTenant(client.getTenantId());
        return client;
    }

    private void applyCreate(Client client, CreateClientRequest request) {
        client.setTradingName(blankToNull(request.tradingName()));
        client.setRegistrationNumber(blankToNull(request.registrationNumber()));
        client.setTaxNumber(blankToNull(request.taxNumber()));
        client.setIndustry(blankToNull(request.industry()));
        client.setEmployeeCount(request.employeeCount());
        client.setEmail(blankToNull(request.email()));
        client.setPhone(blankToNull(request.phone()));
        client.setWebsite(blankToNull(request.website()));
        client.setAddressLine1(blankToNull(request.addressLine1()));
        client.setAddressLine2(blankToNull(request.addressLine2()));
        client.setCity(blankToNull(request.city()));
        client.setState(blankToNull(request.state()));
        client.setPostalCode(blankToNull(request.postalCode()));
        client.setCountry(blankToNull(request.country()));
        client.setStatus(request.status() == null ? ClientStatus.PROSPECT : request.status());
        client.setNotes(blankToNull(request.notes()));
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
