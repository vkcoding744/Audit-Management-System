package com.auditplatform.standards.repository;

import com.auditplatform.standards.domain.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, String> {

    Optional<ChecklistItem> findByIdAndDeletedAtIsNull(String id);

    List<ChecklistItem> findByTenantIdAndChecklistIdAndDeletedAtIsNullOrderBySortOrderAsc(String tenantId, String checklistId);

    long countByTenantIdAndChecklistIdAndDeletedAtIsNull(String tenantId, String checklistId);
}
