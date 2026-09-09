package com.example.maidmanager.leave.repository;

import com.example.maidmanager.leave.entity.LeaveRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveRecordRepository extends JpaRepository<LeaveRecord, UUID> {

    @Query("SELECT l FROM LeaveRecord l WHERE l.id = :id AND l.maid.owner.id = :ownerId")
    Optional<LeaveRecord> findByIdAndOwnerId(@Param("id") UUID id, @Param("ownerId") UUID ownerId);

    @Query("SELECT l FROM LeaveRecord l WHERE l.maid.id = :maidId AND l.maid.owner.id = :ownerId AND l.leaveDate = :leaveDate")
    Optional<LeaveRecord> findByMaidIdAndDate(@Param("maidId") UUID maidId,
                                              @Param("ownerId") UUID ownerId,
                                              @Param("leaveDate") LocalDate leaveDate);

    @Query("SELECT l FROM LeaveRecord l WHERE l.maid.id = :maidId AND l.maid.owner.id = :ownerId " +
           "AND l.leaveDate BETWEEN :fromDate AND :toDate ORDER BY l.leaveDate ASC")
    List<LeaveRecord> findAllByMaidIdAndDateRange(@Param("maidId") UUID maidId,
                                                  @Param("ownerId") UUID ownerId,
                                                  @Param("fromDate") LocalDate fromDate,
                                                  @Param("toDate") LocalDate toDate);
}
