package com.nubixplus.lib.domain.repositories.auth;

import com.nubixplus.lib.domain.entities.auth.RefreshToken;
import com.nubixplus.lib.stereotype.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends BaseRepository<RefreshToken> {

    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    @Modifying
    @Query("""
            update RefreshToken t
               set t.revoked = true, t.revokedAt = :now
             where t.user.id = :userId
               and t.organization.id = :organizationId
               and t.revoked = false
            """)
    int revokeAllForUserAndOrganization(@Param("userId") Long userId,
                                        @Param("organizationId") Long organizationId,
                                        @Param("now") LocalDateTime now);

    /** Limpieza de tokens vencidos (job programado). */
    @Modifying
    @Query("delete from RefreshToken t where t.expiresAt < :cutoff")
    int deleteExpired(@Param("cutoff") LocalDateTime cutoff);
}
