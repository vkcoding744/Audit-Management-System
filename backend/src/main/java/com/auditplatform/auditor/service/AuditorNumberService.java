package com.auditplatform.auditor.service;

import com.auditplatform.crm.domain.CrmSequence;
import com.auditplatform.crm.repository.CrmSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditorNumberService {

    private final CrmSequenceRepository crmSequenceRepository;

    public AuditorNumberService(CrmSequenceRepository crmSequenceRepository) {
        this.crmSequenceRepository = crmSequenceRepository;
    }

    @Transactional
    public String next(String tenantId) {
        CrmSequence sequence = crmSequenceRepository.findForUpdate(tenantId, "AUDITOR").orElseGet(() -> {
            CrmSequence created = new CrmSequence();
            created.setTenantId(tenantId);
            created.setSequenceName("AUDITOR");
            created.setNextValue(1);
            return crmSequenceRepository.saveAndFlush(created);
        });
        long value = sequence.getNextValue();
        sequence.setNextValue(value + 1);
        crmSequenceRepository.save(sequence);
        return "AUD-%06d".formatted(value);
    }
}
