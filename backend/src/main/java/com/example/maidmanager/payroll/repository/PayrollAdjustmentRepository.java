package com.example.maidmanager.payroll.repository;

import com.example.maidmanager.payroll.entity.PayrollAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollAdjustmentRepository extends JpaRepository<PayrollAdjustment, UUID> {

    @Query("SELECT a FROM PayrollAdjustment a WHERE a.id = :id AND a.payrollRun.owner.id = :ownerId")
    Optional<PayrollAdjustment> findByIdAndOwnerId(@Param("id") UUID id, @Param("ownerId") UUID ownerId);

    @Query("SELECT a FROM PayrollAdjustment a WHERE a.payrollRun.id = :payrollRunId AND a.payrollRun.owner.id = :ownerId ORDER BY a.adjustmentDate ASC")
    List<PayrollAdjustment> findAllByPayrollRunIdAndOwnerId(@Param("payrollRunId") UUID payrollRunId, @Param("ownerId") UUID ownerId);
}
