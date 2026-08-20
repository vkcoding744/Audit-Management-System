package com.auditplatform.crm.service;

import com.auditplatform.crm.domain.CrmSequence;
import com.auditplatform.crm.repository.CrmSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientNumberService {

    private final CrmSequenceRepository crmSequenceRepository;

    public ClientNumberService(CrmSequenceRepository crmSequenceRepository) {
        this.crmSequenceRepository = crmSequenceRepository;
    }

    @Transactional
    public String next(String tenantId) {
        CrmSequence sequence = crmSequenceRepository.findForUpdate(tenantId, "CLIENT").orElseGet(() -> {
            CrmSequence created = new CrmSequence();
            created.setTenantId(tenantId);
            created.setSequenceName("CLIENT");
            created.setNextValue(1);
            return crmSequenceRepository.saveAndFlush(created);
        });
        long value = sequence.getNextValue();
        sequence.setNextValue(value + 1);
        crmSequenceRepository.save(sequence);
        return "CLIENT-%06d".formatted(value);
    }
}
