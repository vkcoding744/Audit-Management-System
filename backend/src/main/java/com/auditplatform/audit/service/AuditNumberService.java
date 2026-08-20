package com.auditplatform.audit.service;

import com.auditplatform.crm.domain.CrmSequence;
import com.auditplatform.crm.repository.CrmSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditNumberService {

    private final CrmSequenceRepository crmSequenceRepository;

    public AuditNumberService(CrmSequenceRepository crmSequenceRepository) {
        this.crmSequenceRepository = crmSequenceRepository;
    }

    @Transactional
    public String nextProgramme(String tenantId) {
        return next(tenantId, "PROGRAMME", "PROG-%06d");
    }

    @Transactional
    public String nextAudit(String tenantId) {
        return next(tenantId, "AUDIT", "AUDIT-%06d");
    }

    @Transactional
    public String nextFinding(String tenantId) {
        return next(tenantId, "FINDING", "FIND-%06d");
    }

    @Transactional
    public String nextCapa(String tenantId) {
        return next(tenantId, "CAPA", "CAPA-%06d");
    }

    private String next(String tenantId, String name, String pattern) {
        CrmSequence sequence = crmSequenceRepository.findForUpdate(tenantId, name).orElseGet(() -> {
            CrmSequence created = new CrmSequence();
            created.setTenantId(tenantId);
            created.setSequenceName(name);
            created.setNextValue(1);
            return crmSequenceRepository.saveAndFlush(created);
        });
        long value = sequence.getNextValue();
        sequence.setNextValue(value + 1);
        crmSequenceRepository.save(sequence);
        return pattern.formatted(value);
    }
}
