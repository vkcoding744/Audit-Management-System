package com.auditplatform.identity.repository;

import com.auditplatform.identity.domain.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, String> {

    Optional<AuthSession> findByRefreshTokenHash(String refreshTokenHash);

    List<AuthSession> findByUserIdOrderByCreatedAtDesc(String userId);

    List<AuthSession> findByFamilyId(String familyId);

    @Modifying
    @Query("update AuthSession s set s.revokedAt = :now, s.updatedBy = 'system' where s.familyId = :familyId and s.revokedAt is null")
    int revokeFamily(@Param("familyId") String familyId, @Param("now") Instant now);

    @Modifying
    @Query("update AuthSession s set s.revokedAt = :now, s.updatedBy = 'system' where s.userId = :userId and s.revokedAt is null")
    int revokeAllForUser(@Param("userId") String userId, @Param("now") Instant now);
}
