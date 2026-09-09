package com.example.maidmanager.auth.repository;

import com.example.maidmanager.auth.entity.AuthRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthRefreshTokenRepository extends JpaRepository<AuthRefreshToken, UUID> {
    Optional<AuthRefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE AuthRefreshToken t SET t.revokedAt = :now WHERE t.owner.id = :ownerId AND t.revokedAt IS NULL")
    int revokeAllByOwnerId(@Param("ownerId") UUID ownerId, @Param("now") Instant now);
}
