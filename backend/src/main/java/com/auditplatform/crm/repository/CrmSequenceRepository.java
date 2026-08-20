package com.auditplatform.crm.repository;

import com.auditplatform.crm.domain.CrmSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CrmSequenceRepository extends JpaRepository<CrmSequence, CrmSequence.Id> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from CrmSequence s where s.tenantId = :tenantId and s.sequenceName = :name")
    Optional<CrmSequence> findForUpdate(@Param("tenantId") String tenantId, @Param("name") String name);
}
