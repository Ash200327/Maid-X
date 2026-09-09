package com.example.maidmanager.holiday.repository;

import com.example.maidmanager.holiday.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, UUID> {

    @Query("SELECT h FROM Holiday h WHERE h.id = :id AND h.owner.id = :ownerId")
    Optional<Holiday> findByIdAndOwnerId(@Param("id") UUID id, @Param("ownerId") UUID ownerId);

    @Query("SELECT h FROM Holiday h WHERE h.owner.id = :ownerId AND h.holidayDate BETWEEN :startDate AND :endDate ORDER BY h.holidayDate ASC")
    List<Holiday> findAllByOwnerIdAndDateRange(@Param("ownerId") UUID ownerId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT h FROM Holiday h WHERE h.owner.id = :ownerId AND h.holidayDate = :date")
    Optional<Holiday> findByOwnerIdAndDate(@Param("ownerId") UUID ownerId, @Param("date") LocalDate date);

    boolean existsByOwnerIdAndHolidayDate(UUID ownerId, LocalDate holidayDate);
}
