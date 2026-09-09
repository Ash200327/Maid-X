package com.example.maidmanager.attendance.repository;

import com.example.maidmanager.attendance.entity.AttendanceSession;
import com.example.maidmanager.common.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {

    @Query("SELECT s FROM AttendanceSession s WHERE s.id = :id AND s.maid.owner.id = :ownerId")
    Optional<AttendanceSession> findByIdAndOwnerId(@Param("id") UUID id, @Param("ownerId") UUID ownerId);

    @Query("SELECT s FROM AttendanceSession s WHERE s.maid.id = :maidId AND s.maid.owner.id = :ownerId AND s.businessDate = :businessDate ORDER BY s.entryAt ASC")
    List<AttendanceSession> findAllByMaidIdAndDate(@Param("maidId") UUID maidId,
                                                   @Param("ownerId") UUID ownerId,
                                                   @Param("businessDate") LocalDate businessDate);

    @Query("SELECT s FROM AttendanceSession s WHERE s.maid.id = :maidId AND s.maid.owner.id = :ownerId " +
           "AND s.businessDate BETWEEN :fromDate AND :toDate ORDER BY s.businessDate ASC, s.entryAt ASC")
    List<AttendanceSession> findAllByMaidIdAndDateRange(@Param("maidId") UUID maidId,
                                                        @Param("ownerId") UUID ownerId,
                                                        @Param("fromDate") LocalDate fromDate,
                                                        @Param("toDate") LocalDate toDate);

    @Query("SELECT s FROM AttendanceSession s WHERE s.maid.id = :maidId AND s.maid.owner.id = :ownerId " +
           "AND s.businessDate = :businessDate AND s.status = :status AND s.exitAt IS NULL")
    Optional<AttendanceSession> findActiveOpenSession(@Param("maidId") UUID maidId,
                                                      @Param("ownerId") UUID ownerId,
                                                      @Param("businessDate") LocalDate businessDate,
                                                      @Param("status") AttendanceStatus status);
}
