package com.auditplatform.auditlog.repository;

import com.auditplatform.auditlog.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
}
