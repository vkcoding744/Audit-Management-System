package com.auditplatform.reporting.repository;

import com.auditplatform.reporting.domain.ReportDataset;
import com.auditplatform.reporting.domain.ReportDefinition;
import com.auditplatform.reporting.domain.ReportDefinitionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportDefinitionRepository extends JpaRepository<ReportDefinition, String> {

    Optional<ReportDefinition> findByIdAndDeletedAtIsNull(String id);

    Page<ReportDefinition> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<ReportDefinition> findByTenantIdAndStatusAndDeletedAtIsNull(
            String tenantId,
            ReportDefinitionStatus status,
            Pageable pageable
    );

    Page<ReportDefinition> findByTenantIdAndDatasetAndDeletedAtIsNull(
            String tenantId,
            ReportDataset dataset,
            Pageable pageable
    );

    Page<ReportDefinition> findByTenantIdAndStatusAndDatasetAndDeletedAtIsNull(
            String tenantId,
            ReportDefinitionStatus status,
            ReportDataset dataset,
            Pageable pageable
    );
}
