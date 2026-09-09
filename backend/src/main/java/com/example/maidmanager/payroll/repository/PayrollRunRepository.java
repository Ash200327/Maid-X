package com.example.maidmanager.payroll.repository;

import com.example.maidmanager.common.enums.PayrollStatus;
import com.example.maidmanager.payroll.entity.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, UUID> {

    @Query("SELECT p FROM PayrollRun p WHERE p.id = :id AND p.owner.id = :ownerId")
    Optional<PayrollRun> findByIdAndOwnerId(@Param("id") UUID id, @Param("ownerId") UUID ownerId);

    @Query("SELECT p FROM PayrollRun p WHERE p.maid.id = :maidId AND p.owner.id = :ownerId AND p.payrollMonth = :payrollMonth")
    Optional<PayrollRun> findByMaidIdAndMonth(@Param("maidId") UUID maidId,
                                              @Param("ownerId") UUID ownerId,
                                              @Param("payrollMonth") LocalDate payrollMonth);

    @Query("SELECT p FROM PayrollRun p WHERE p.owner.id = :ownerId AND p.payrollMonth = :payrollMonth ORDER BY p.maid.name ASC")
    List<PayrollRun> findAllByOwnerIdAndMonth(@Param("ownerId") UUID ownerId,
                                              @Param("payrollMonth") LocalDate payrollMonth);

    @Query("SELECT p FROM PayrollRun p WHERE p.owner.id = :ownerId AND p.payrollMonth = :payrollMonth AND p.status = :status")
    List<PayrollRun> findAllByOwnerIdAndMonthAndStatus(@Param("ownerId") UUID ownerId,
                                                       @Param("payrollMonth") LocalDate payrollMonth,
                                                       @Param("status") PayrollStatus status);
}
