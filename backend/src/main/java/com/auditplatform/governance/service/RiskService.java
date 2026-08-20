package com.auditplatform.governance.service;

import com.auditplatform.auditlog.service.AuditLogService;
import com.auditplatform.common.api.PageResponse;
import com.auditplatform.common.exception.ApiException;
import com.auditplatform.common.exception.ErrorCode;
import com.auditplatform.governance.api.CreateRiskRequest;
import com.auditplatform.governance.api.NotesRequest;
import com.auditplatform.governance.api.RiskResponse;
import com.auditplatform.governance.api.UpdateRiskRequest;
import com.auditplatform.governance.domain.Risk;
import com.auditplatform.governance.domain.RiskCategory;
import com.auditplatform.governance.domain.RiskStatus;
import com.auditplatform.governance.repository.RiskRepository;
import com.auditplatform.identity.service.IsolationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class RiskService {

    private final RiskRepository riskRepository;
    private final GovernanceNumberService numberService;
    private final IsolationService isolationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public RiskService(
            RiskRepository riskRepository,
            GovernanceNumberService numberService,
            IsolationService isolationService,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.riskRepository = riskRepository;
        this.numberService = numberService;
        this.isolationService = isolationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<RiskResponse> list(RiskStatus status, Pageable pageable) {
        String tenantId = isolationService.requireTenantScope();
        Page<Risk> page = status == null
                ? riskRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                : riskRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        return PageResponse.from(page.map(RiskResponse::from));
    }

    @Transactional(readOnly = true)
    public RiskResponse get(String id) {
        return RiskResponse.from(requireRisk(id));
    }

    @Transactional
    public RiskResponse create(CreateRiskRequest request) {
        String tenantId = isolationService.requireTenantScope();
        Risk risk = new Risk();
        risk.setTenantId(tenantId);
        risk.setRiskNumber(numberService.nextRisk(tenantId));
        risk.setTitle(request.title().trim());
        risk.setCategory(request.category() == null ? RiskCategory.OTHER : request.category());
        risk.setLikelihood(request.likelihood());
        risk.setImpact(request.impact());
        risk.setDescription(blankToNull(request.description()));
        risk.setStatus(RiskStatus.OPEN);
        riskRepository.save(risk);
        auditLogService.record("RISK_CREATE", "Risk", risk.getId(), null, risk.getRiskNumber(), null, null);
        return RiskResponse.from(risk);
    }

    @Transactional
    public RiskResponse update(String id, UpdateRiskRequest request) {
        Risk risk = requireOpen(id);
        if (request.title() != null && !request.title().isBlank()) {
            risk.setTitle(request.title().trim());
        }
        if (request.category() != null) {
            risk.setCategory(request.category());
        }
        if (request.likelihood() != null) {
            risk.setLikelihood(request.likelihood());
        }
        if (request.impact() != null) {
            risk.setImpact(request.impact());
        }
        if (request.description() != null) {
            risk.setDescription(blankToNull(request.description()));
        }
        if (request.mitigation() != null) {
            risk.setMitigation(blankToNull(request.mitigation()));
        }
        riskRepository.save(risk);
        return RiskResponse.from(risk);
    }

    @Transactional
    public RiskResponse startMitigation(String id, NotesRequest request) {
        Risk risk = requireRisk(id);
        if (risk.getStatus() != RiskStatus.OPEN) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Only open risks can start mitigation");
        }
        if (request != null && request.notes() != null) {
            risk.setMitigation(blankToNull(request.notes()));
        }
        risk.setStatus(RiskStatus.MITIGATING);
        riskRepository.save(risk);
        auditLogService.record("RISK_MITIGATE", "Risk", risk.getId(), "OPEN", "MITIGATING", null, null);
        return RiskResponse.from(risk);
    }

    @Transactional
    public RiskResponse close(String id, NotesRequest request) {
        Risk risk = requireOpen(id);
        if (request != null && request.notes() != null) {
            risk.setMitigation(blankToNull(request.notes()));
        }
        risk.setClosedOn(today());
        risk.setStatus(RiskStatus.CLOSED);
        riskRepository.save(risk);
        auditLogService.record("RISK_CLOSE", "Risk", risk.getId(), null, "CLOSED", null, null);
        return RiskResponse.from(risk);
    }

    public Risk requireRisk(String id) {
        Risk risk = riskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SYS_NOT_FOUND, "Risk not found"));
        isolationService.assertCanAccessTenant(risk.getTenantId());
        return risk;
    }

    private Risk requireOpen(String id) {
        Risk risk = requireRisk(id);
        if (risk.getStatus() == RiskStatus.CLOSED) {
            throw new ApiException(ErrorCode.SYS_VALIDATION, "Closed risks cannot be changed");
        }
        return risk;
    }

    private LocalDate today() {
        return LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
