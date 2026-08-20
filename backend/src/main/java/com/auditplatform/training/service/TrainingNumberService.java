package com.auditplatform.training.service;

import com.auditplatform.crm.domain.CrmSequence;
import com.auditplatform.crm.repository.CrmSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingNumberService {

    private final CrmSequenceRepository crmSequenceRepository;

    public TrainingNumberService(CrmSequenceRepository crmSequenceRepository) {
        this.crmSequenceRepository = crmSequenceRepository;
    }

    @Transactional
    public String nextTraining(String tenantId) {
        return next(tenantId, "TRAINING", "TRN");
    }

    @Transactional
    public String nextAssessment(String tenantId) {
        return next(tenantId, "ASSESSMENT", "ASM");
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
