package com.auditplatform.crm.service;

import com.auditplatform.crm.domain.CrmSequence;
import com.auditplatform.crm.repository.CrmSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeadNumberService {

    private final CrmSequenceRepository crmSequenceRepository;

    public LeadNumberService(CrmSequenceRepository crmSequenceRepository) {
        this.crmSequenceRepository = crmSequenceRepository;
    }

    @Transactional
    public String next(String tenantId) {
        CrmSequence sequence = crmSequenceRepository.findForUpdate(tenantId, "LEAD").orElseGet(() -> {
            CrmSequence created = new CrmSequence();
            created.setTenantId(tenantId);
            created.setSequenceName("LEAD");
            created.setNextValue(1);
            return crmSequenceRepository.saveAndFlush(created);
        });
        long value = sequence.getNextValue();
        sequence.setNextValue(value + 1);
        crmSequenceRepository.save(sequence);
        return "LEAD-%06d".formatted(value);
    }
}
