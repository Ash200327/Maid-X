package com.example.maidmanager.payroll.repository;

import com.example.maidmanager.payroll.entity.PayrollCalculationSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollCalculationSnapshotRepository extends JpaRepository<PayrollCalculationSnapshot, UUID> {

    @Query("SELECT s FROM PayrollCalculationSnapshot s WHERE s.payrollRun.id = :payrollRunId AND s.payrollRun.owner.id = :ownerId")
    Optional<PayrollCalculationSnapshot> findByPayrollRunIdAndOwnerId(@Param("payrollRunId") UUID payrollRunId,
                                                                      @Param("ownerId") UUID ownerId);
}
