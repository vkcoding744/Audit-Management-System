package com.auditplatform.reporting.service;

import com.auditplatform.crm.domain.CrmSequence;
import com.auditplatform.crm.repository.CrmSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportNumberService {

    private final CrmSequenceRepository crmSequenceRepository;

    public ReportNumberService(CrmSequenceRepository crmSequenceRepository) {
        this.crmSequenceRepository = crmSequenceRepository;
    }

    @Transactional
    public String nextReport(String tenantId) {
        return next(tenantId, "REPORT", "RPT-%06d");
    }

    @Transactional
    public String nextExport(String tenantId) {
        return next(tenantId, "EXPORT", "EXP-%06d");
    }

    private String next(String tenantId, String sequenceName, String pattern) {
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
        return pattern.formatted(value);
    }
}
