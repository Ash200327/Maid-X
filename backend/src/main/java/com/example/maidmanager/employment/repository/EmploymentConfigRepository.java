package com.example.maidmanager.employment.repository;

import com.example.maidmanager.employment.entity.EmploymentConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmploymentConfigRepository extends JpaRepository<EmploymentConfig, UUID> {

    @Query("SELECT c FROM EmploymentConfig c WHERE c.maid.id = :maidId AND c.maid.owner.id = :ownerId ORDER BY c.effectiveFrom DESC")
    List<EmploymentConfig> findAllByMaidIdAndOwnerId(@Param("maidId") UUID maidId, @Param("ownerId") UUID ownerId);

    @Query("SELECT c FROM EmploymentConfig c WHERE c.maid.id = :maidId AND c.maid.owner.id = :ownerId " +
           "AND c.effectiveFrom <= :targetDate AND (c.effectiveTo IS NULL OR c.effectiveTo >= :targetDate)")
    Optional<EmploymentConfig> findEffectiveConfigAtDate(@Param("maidId") UUID maidId,
                                                         @Param("ownerId") UUID ownerId,
                                                         @Param("targetDate") LocalDate targetDate);

    @Query("SELECT c FROM EmploymentConfig c WHERE c.maid.id = :maidId AND c.maid.owner.id = :ownerId " +
           "AND (c.effectiveTo IS NULL OR c.effectiveTo >= :monthStart) " +
           "AND c.effectiveFrom <= :monthEnd ORDER BY c.effectiveFrom ASC")
    List<EmploymentConfig> findConfigsForMonth(@Param("maidId") UUID maidId,
                                               @Param("ownerId") UUID ownerId,
                                               @Param("monthStart") LocalDate monthStart,
                                               @Param("monthEnd") LocalDate monthEnd);
}
