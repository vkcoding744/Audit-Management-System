package com.auditplatform.crm.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.crm.api.CreateSiteRequest;
import com.auditplatform.crm.api.SiteResponse;
import com.auditplatform.crm.api.UpdateSiteRequest;
import com.auditplatform.crm.domain.Site;
import com.auditplatform.crm.domain.SiteStatus;
import com.auditplatform.crm.repository.SiteRepository;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final ClientService clientService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;

    public SiteService(
            SiteRepository siteRepository,
            ClientService clientService,
            IsolationService isolationService,
            AuditLogService auditLogService
    ) {
        this.siteRepository = siteRepository;
        this.clientService = clientService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<SiteResponse> listByClient(String clientId) {
        var client = clientService.requireClient(clientId);
        return siteRepository.findByTenantIdAndClientIdAndDeletedAtIsNullOrderByNameAsc(client.getTenantId(), client.getId())
                .stream()
                .map(SiteResponse::from)
                .toList();
    }

    @Transactional
    public SiteResponse create(String clientId, CreateSiteRequest request) {
        var client = clientService.requireClient(clientId);
        Site site = new Site();
        site.setTenantId(client.getTenantId());
        site.setClientId(client.getId());
        site.setName(request.name().trim());
        site.setAddressLine1(blankToNull(request.addressLine1()));
        site.setAddressLine2(blankToNull(request.addressLine2()));
        site.setCity(blankToNull(request.city()));
        site.setState(blankToNull(request.state()));
        site.setPostalCode(blankToNull(request.postalCode()));
        site.setCountry(blankToNull(request.country()));
        site.setScope(blankToNull(request.scope()));
        site.setEmployeeCount(request.employeeCount());
        site.setProcesses(blankToNull(request.processes()));
        site.setStatus(request.status() == null ? SiteStatus.ACTIVE : request.status());
        siteRepository.save(site);
        auditLogService.record("SITE_CREATE", "Site", site.getId(), null, site.getName(), null, null);
        return SiteResponse.from(site);
    }

    @Transactional
    public SiteResponse update(String siteId, UpdateSiteRequest request) {
        Site site = requireSite(siteId);
        if (request.name() != null && !request.name().isBlank()) {
            site.setName(request.name().trim());
        }
        if (request.addressLine1() != null) {
            site.setAddressLine1(blankToNull(request.addressLine1()));
        }
        if (request.addressLine2() != null) {
            site.setAddressLine2(blankToNull(request.addressLine2()));
        }
        if (request.city() != null) {
            site.setCity(blankToNull(request.city()));
        }
        if (request.state() != null) {
            site.setState(blankToNull(request.state()));
        }
        if (request.postalCode() != null) {
            site.setPostalCode(blankToNull(request.postalCode()));
        }
        if (request.country() != null) {
            site.setCountry(blankToNull(request.country()));
        }
        if (request.scope() != null) {
            site.setScope(blankToNull(request.scope()));
        }
        if (request.employeeCount() != null) {
            site.setEmployeeCount(request.employeeCount());
        }
        if (request.processes() != null) {
            site.setProcesses(blankToNull(request.processes()));
        }
        if (request.status() != null) {
            site.setStatus(request.status());
        }
        siteRepository.save(site);
        return SiteResponse.from(site);
    }

    @Transactional
    public void delete(String siteId) {
        Site site = requireSite(siteId);
        site.setDeletedAt(Instant.now());
        siteRepository.save(site);
        auditLogService.record("SITE_DELETE", "Site", site.getId(), site.getName(), null, null, null);
    }

    public Site requireSite(String id) {
        Site site = siteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Site not found"));
        isolationService.assertCanAccessTenant(site.getTenantId());
        return site;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
