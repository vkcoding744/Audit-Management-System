package com.auditplatform.governance.service;

import com.auditplatform.crm.domain.CrmSequence;
import com.auditplatform.crm.repository.CrmSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GovernanceNumberService {

    private final CrmSequenceRepository crmSequenceRepository;

    public GovernanceNumberService(CrmSequenceRepository crmSequenceRepository) {
        this.crmSequenceRepository = crmSequenceRepository;
    }

    @Transactional
    public String nextComplaint(String tenantId) {
        return next(tenantId, "COMPLAINT", "CMP");
    }

    @Transactional
    public String nextAppeal(String tenantId) {
        return next(tenantId, "APPEAL", "APL");
    }

    @Transactional
    public String nextRisk(String tenantId) {
        return next(tenantId, "RISK", "RSK");
    }

    @Transactional
    public String nextImpartiality(String tenantId) {
        return next(tenantId, "IMPARTIALITY", "IMP");
    }

    private String next(String tenantId, String sequenceName, String prefix) {
        CrmSequence sequence = crmSequenceRepository.findForUpdate(tenantId, sequenceName).orElseGet(() -> {
            CrmSequence created = new CrmSequence();
            created.setTenantId(tenantId);
            created.setSequenceName(sequenceName);
            created.setNextValue(1);
            return crmSequenceRepository.saveAndFlush(created);
        });
        long value = sequence.getNextValue();
        sequence.setNextValue(value + 1);
        crmSequenceRepository.save(sequence);
        return prefix + "-%06d".formatted(value);
    }
}
