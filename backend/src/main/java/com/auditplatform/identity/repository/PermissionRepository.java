package com.auditplatform.identity.repository;

import com.auditplatform.identity.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, String> {

    Optional<Permission> findByCode(String code);
}
