package com.auditplatform.identity.repository;

import com.auditplatform.identity.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findByCodeAndTenantIdIsNull(String code);

    @Query("""
            select distinct r from Role r
            left join fetch r.permissions
            where r.code in :codes and r.tenantId is null
            """)
    List<Role> findSystemRolesWithPermissions(@Param("codes") Collection<String> codes);
}
