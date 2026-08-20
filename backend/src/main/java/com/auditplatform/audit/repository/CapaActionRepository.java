package com.auditplatform.audit.repository;

import com.auditplatform.audit.domain.CapaAction;
import com.auditplatform.audit.domain.CapaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CapaActionRepository extends JpaRepository<CapaAction, String> {

    Optional<CapaAction> findByIdAndDeletedAtIsNull(String id);

    List<CapaAction> findByTenantIdAndFindingIdAndDeletedAtIsNullOrderByDueOnAsc(String tenantId, String findingId);

    boolean existsByFindingIdAndStatusAndDeletedAtIsNull(String findingId, CapaStatus status);

    long countByFindingIdAndDeletedAtIsNull(String findingId);

    @Query("""
            select count(c) from CapaAction c, Finding f
            where c.findingId = f.id
              and c.tenantId = :tenantId
              and f.clientId = :clientId
              and c.status = :status
              and c.dueOn < :today
              and c.deletedAt is null
              and f.deletedAt is null
            """)
    long countOverdueForClient(
            @Param("tenantId") String tenantId,
            @Param("clientId") String clientId,
            @Param("status") CapaStatus status,
            @Param("today") LocalDate today
    );
}
