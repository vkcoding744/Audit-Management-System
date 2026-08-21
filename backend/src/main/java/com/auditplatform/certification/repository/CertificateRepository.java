package com.auditplatform.certification.repository;

import com.auditplatform.certification.domain.Certificate;
import com.auditplatform.certification.domain.CertificateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, String> {

    Optional<Certificate> findByIdAndDeletedAtIsNull(String id);

    Page<Certificate> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<Certificate> findByTenantIdAndClientIdAndDeletedAtIsNull(String tenantId, String clientId, Pageable pageable);

    Page<Certificate> findByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, CertificateStatus status, Pageable pageable);

    boolean existsByTenantIdAndClientIdAndSchemeIdAndStatusAndDeletedAtIsNull(
            String tenantId,
            String clientId,
            String schemeId,
            CertificateStatus status
    );

    long countByTenantIdAndClientIdAndStatusAndExpiresOnGreaterThanEqualAndDeletedAtIsNull(
            String tenantId,
            String clientId,
            CertificateStatus status,
            LocalDate expiresOn
    );

    long countByTenantIdAndClientIdAndStatusAndExpiresOnGreaterThanEqualAndExpiresOnLessThanEqualAndDeletedAtIsNull(
            String tenantId,
            String clientId,
            CertificateStatus status,
            LocalDate from,
            LocalDate to
    );

    long countByTenantIdAndStatusAndExpiresOnGreaterThanEqualAndDeletedAtIsNull(
            String tenantId,
            CertificateStatus status,
            LocalDate expiresOn
    );

    long countByTenantIdAndStatusAndExpiresOnGreaterThanEqualAndExpiresOnLessThanEqualAndDeletedAtIsNull(
            String tenantId,
            CertificateStatus status,
            LocalDate from,
            LocalDate to
    );
}
