package com.auditplatform.finance.service;

import com.auditplatform.crm.domain.CrmSequence;
import com.auditplatform.crm.repository.CrmSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceNumberService {

    private final CrmSequenceRepository crmSequenceRepository;

    public FinanceNumberService(CrmSequenceRepository crmSequenceRepository) {
        this.crmSequenceRepository = crmSequenceRepository;
    }

    @Transactional
    public String nextQuote(String tenantId) {
        return next(tenantId, "QUOTE", "QUOTE");
    }

    @Transactional
    public String nextInvoice(String tenantId) {
        return next(tenantId, "INVOICE", "INV");
    }

    @Transactional
    public String nextPayment(String tenantId) {
        return next(tenantId, "PAYMENT", "PAY");
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
