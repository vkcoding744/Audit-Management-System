package com.auditplatform.reporting.repository;

import com.auditplatform.reporting.domain.ReportExport;
import com.auditplatform.reporting.domain.ReportExportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportExportRepository extends JpaRepository<ReportExport, String> {

    Optional<ReportExport> findByIdAndDeletedAtIsNull(String id);

    Page<ReportExport> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<ReportExport> findByTenantIdAndStatusAndDeletedAtIsNull(
            String tenantId,
            ReportExportStatus status,
            Pageable pageable
    );
}
