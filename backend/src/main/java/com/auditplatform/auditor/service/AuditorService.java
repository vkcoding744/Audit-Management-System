package com.auditplatform.auditor.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.auditor.api.AuditorResponse;
import com.auditplatform.auditor.api.CreateAuditorRequest;
import com.auditplatform.auditor.api.CreateQualificationRequest;
import com.auditplatform.auditor.api.QualificationResponse;
import com.auditplatform.auditor.api.UpdateAuditorRequest;
import com.auditplatform.auditor.domain.Auditor;
import com.auditplatform.auditor.domain.AuditorQualification;
import com.auditplatform.auditor.domain.AuditorStatus;
import com.auditplatform.auditor.domain.EmploymentType;
import com.auditplatform.auditor.repository.AuditorQualificationRepository;
import com.auditplatform.auditor.repository.AuditorRepository;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.identity.domain.UserAccount;
import com.auditplatform.identity.repository.UserAccountRepository;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AuditorService {

    private final AuditorRepository auditorRepository;
    private final AuditorQualificationRepository qualificationRepository;
    private final AuditorNumberService auditorNumberService;
    private final UserAccountRepository userAccountRepository;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;

    public AuditorService(
            AuditorRepository auditorRepository,
            AuditorQualificationRepository qualificationRepository,
            AuditorNumberService auditorNumberService,
            UserAccountRepository userAccountRepository,
            IsolationService isolationService,
            AuditLogService auditLogService
    ) {
        this.auditorRepository = auditorRepository;
        this.qualificationRepository = qualificationRepository;
        this.auditorNumberService = auditorNumberService;
        this.userAccountRepository = userAccountRepository;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditorResponse> list(String query, AuditorStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<Auditor> page;
        if (query != null && !query.isBlank()) {
            page = auditorRepository.search(tenantId, query.trim(), pageable);
        } else if (status != null) {
            page = auditorRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = auditorRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.from(page.map(AuditorResponse::from));
    }

    @Transactional(readOnly = true)
    public AuditorResponse get(String id) {
        return AuditorResponse.from(requireAuditor(id));
    }

    @Transactional
    public AuditorResponse create(CreateAuditorRequest request) {
        String tenantId = isolationService.requireTenantScope();
        Auditor auditor = new Auditor();
        auditor.setTenantId(tenantId);
        auditor.setEmployeeNumber(auditorNumberService.next(tenantId));
        auditor.setFirstName(request.firstName().trim());
        auditor.setLastName(request.lastName().trim());
        auditor.setEmail(blankToNull(request.email()));
        auditor.setPhone(blankToNull(request.phone()));
        auditor.setJobTitle(blankToNull(request.jobTitle()));
        auditor.setEmploymentType(request.employmentType() == null ? EmploymentType.EMPLOYEE : request.employmentType());
        auditor.setStatus(request.status() == null ? AuditorStatus.ACTIVE : request.status());
        auditor.setBaseLocation(blankToNull(request.baseLocation()));
        auditor.setCountry(blankToNull(request.country()));
        auditor.setNotes(blankToNull(request.notes()));
        auditor.setUserId(resolveUser(tenantId, request.userId(), null));
        auditorRepository.save(auditor);
        auditLogService.record("AUDITOR_CREATE", "Auditor", auditor.getId(), null, auditor.getEmployeeNumber(), null, null);
        return AuditorResponse.from(auditor);
    }

    @Transactional
    public AuditorResponse update(String id, UpdateAuditorRequest request) {
        Auditor auditor = requireAuditor(id);
        if (request.firstName() != null && !request.firstName().isBlank()) {
            auditor.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            auditor.setLastName(request.lastName().trim());
        }
        if (request.email() != null) {
            auditor.setEmail(blankToNull(request.email()));
        }
        if (request.phone() != null) {
            auditor.setPhone(blankToNull(request.phone()));
        }
        if (request.jobTitle() != null) {
            auditor.setJobTitle(blankToNull(request.jobTitle()));
        }
        if (request.employmentType() != null) {
            auditor.setEmploymentType(request.employmentType());
        }
        if (request.status() != null) {
            auditor.setStatus(request.status());
        }
        if (request.baseLocation() != null) {
            auditor.setBaseLocation(blankToNull(request.baseLocation()));
        }
        if (request.country() != null) {
            auditor.setCountry(blankToNull(request.country()));
        }
        if (request.notes() != null) {
            auditor.setNotes(blankToNull(request.notes()));
        }
        if (request.userId() != null) {
        auditor.setUserId(resolveUser(auditor.getTenantId(), request.userId().isBlank() ? null : request.userId(), auditor.getId()));
        }
        auditorRepository.save(auditor);
        auditLogService.record("AUDITOR_UPDATE", "Auditor", auditor.getId(), null, auditor.getEmployeeNumber(), null, null);
        return AuditorResponse.from(auditor);
    }

    @Transactional
    public void delete(String id) {
        Auditor auditor = requireAuditor(id);
        Instant now = Instant.now();
        auditor.setDeletedAt(now);
        auditorRepository.save(auditor);
        qualificationRepository.findByTenantIdAndAuditorIdAndDeletedAtIsNullOrderByTitleAsc(auditor.getTenantId(), auditor.getId())
                .forEach(item -> {
                    item.setDeletedAt(now);
                    qualificationRepository.save(item);
                });
        auditLogService.record("AUDITOR_DELETE", "Auditor", auditor.getId(), auditor.getEmployeeNumber(), null, null, null);
    }

    @Transactional(readOnly = true)
    public List<QualificationResponse> listQualifications(String auditorId) {
        Auditor auditor = requireAuditor(auditorId);
        return qualificationRepository
                .findByTenantIdAndAuditorIdAndDeletedAtIsNullOrderByTitleAsc(auditor.getTenantId(), auditor.getId())
                .stream()
                .map(QualificationResponse::from)
                .toList();
    }

    @Transactional
    public QualificationResponse addQualification(String auditorId, CreateQualificationRequest request) {
        Auditor auditor = requireAuditor(auditorId);
        if (request.issuedOn() != null && request.expiresOn() != null && request.expiresOn().isBefore(request.issuedOn())) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Qualification expiry cannot be before the issue date");
        }
        AuditorQualification qualification = new AuditorQualification();
        qualification.setTenantId(auditor.getTenantId());
        qualification.setAuditorId(auditor.getId());
        qualification.setTitle(request.title().trim());
        qualification.setIssuer(blankToNull(request.issuer()));
        qualification.setIssuedOn(request.issuedOn());
        qualification.setExpiresOn(request.expiresOn());
        qualification.setNotes(blankToNull(request.notes()));
        qualificationRepository.save(qualification);
        return QualificationResponse.from(qualification);
    }

    @Transactional
    public void deleteQualification(String qualificationId) {
        AuditorQualification qualification = qualificationRepository.findByIdAndDeletedAtIsNull(qualificationId)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Qualification not found"));
        isolationService.assertCanAccessTenant(qualification.getTenantId());
        qualification.setDeletedAt(Instant.now());
        qualificationRepository.save(qualification);
    }

    public Auditor requireAuditor(String id) {
        Auditor auditor = auditorRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Auditor not found"));
        isolationService.assertCanAccessTenant(auditor.getTenantId());
        return auditor;
    }

    private String resolveUser(String tenantId, String userId, String currentAuditorId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        UserAccount user = userAccountRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "User not found"));
        isolationService.assertCanAccessTenant(user.getTenantId());
        if (user.getTenantId() != null && !tenantId.equals(user.getTenantId())) {
            throw new ApiException(ErrorCode.AUTH_TENANT_MISMATCH, "User does not belong to this tenant");
        }
        boolean taken = currentAuditorId == null
                ? auditorRepository.existsByTenantIdAndUserIdAndDeletedAtIsNull(tenantId, user.getId())
                : auditorRepository.existsByTenantIdAndUserIdAndIdNotAndDeletedAtIsNull(tenantId, user.getId(), currentAuditorId);
        if (taken) {
            throw new ApiException(ErrorCode.SYS_CONFLICT, "This user is already linked to an auditor profile");
        }
        return user.getId();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
