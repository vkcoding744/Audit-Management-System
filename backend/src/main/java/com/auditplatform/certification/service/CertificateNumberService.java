package com.auditplatform.certification.service;

import com.auditplatform.crm.domain.CrmSequence;
import com.auditplatform.crm.repository.CrmSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CertificateNumberService {

    private final CrmSequenceRepository crmSequenceRepository;

    public CertificateNumberService(CrmSequenceRepository crmSequenceRepository) {
        this.crmSequenceRepository = crmSequenceRepository;
    }

    @Transactional
    public String nextCertificate(String tenantId) {
        CrmSequence sequence = crmSequenceRepository.findForUpdate(tenantId, "CERTIFICATE").orElseGet(() -> {
            CrmSequence created = new CrmSequence();
            created.setTenantId(tenantId);
            created.setSequenceName("CERTIFICATE");
            created.setNextValue(1);
            return crmSequenceRepository.saveAndFlush(created);
        });
        long value = sequence.getNextValue();
        sequence.setNextValue(value + 1);
        crmSequenceRepository.save(sequence);
        return "CERT-%06d".formatted(value);
    }
}
