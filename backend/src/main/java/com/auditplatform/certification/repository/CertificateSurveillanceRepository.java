package com.auditplatform.certification.repository;

import com.auditplatform.certification.domain.CertificateSurveillance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateSurveillanceRepository extends JpaRepository<CertificateSurveillance, String> {

    Optional<CertificateSurveillance> findByIdAndDeletedAtIsNull(String id);

    List<CertificateSurveillance> findByTenantIdAndCertificateIdAndDeletedAtIsNullOrderByPlannedOnAsc(
            String tenantId,
            String certificateId
    );
}
