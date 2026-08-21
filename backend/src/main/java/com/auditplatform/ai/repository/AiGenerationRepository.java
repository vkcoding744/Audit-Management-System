package com.auditplatform.ai.repository;

import com.auditplatform.ai.domain.AiGeneration;
import com.auditplatform.ai.domain.AiGenerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiGenerationRepository extends JpaRepository<AiGeneration, String> {

    Optional<AiGeneration> findByIdAndDeletedAtIsNull(String id);

    Page<AiGeneration> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<AiGeneration> findByTenantIdAndStatusAndDeletedAtIsNull(
            String tenantId,
            AiGenerationStatus status,
            Pageable pageable
    );

    long countByTenantIdAndStatusAndDeletedAtIsNull(String tenantId, AiGenerationStatus status);
}
