package com.auditplatform.identity.repository;

import com.auditplatform.identity.domain.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {

    Optional<UserAccount> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            select distinct u from UserAccount u
            left join fetch u.roles r
            left join fetch r.permissions
            where lower(u.email) = lower(:email) and u.deletedAt is null
            """)
    Optional<UserAccount> findByEmailWithRoles(@Param("email") String email);

    @Query("""
            select distinct u from UserAccount u
            left join fetch u.roles r
            left join fetch r.permissions
            where u.id = :id and u.deletedAt is null
            """)
    Optional<UserAccount> findByIdWithRoles(@Param("id") String id);

    Page<UserAccount> findByTenantIdAndDeletedAtIsNull(String tenantId, Pageable pageable);

    Page<UserAccount> findByDeletedAtIsNull(Pageable pageable);

    long countByDeletedAtIsNull();
}
