package com.auditplatform.certification.repository;

import com.auditplatform.certification.domain.CertificationDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificationDecisionRepository extends JpaRepository<CertificationDecision, String> {

    List<CertificationDecision> findByTenantIdAndCertificateIdAndDeletedAtIsNullOrderByDecidedOnAsc(String tenantId, String certificateId);
}
