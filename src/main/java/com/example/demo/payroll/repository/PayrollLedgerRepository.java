package com.example.demo.payroll.repository;

import com.example.demo.payroll.entity.PayrollLedger;
import com.example.demo.payroll.entity.PayrollLedgerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollLedgerRepository extends JpaRepository<PayrollLedger, Long> {
    
    List<PayrollLedger> findByEmployeeId(Long employeeId);
    
    List<PayrollLedger> findByPayrollPeriodId(Long payrollPeriodId);
    
    List<PayrollLedger> findByStatus(PayrollLedgerStatus status);
    
    Optional<PayrollLedger> findByEmployeeIdAndPayrollPeriodId(Long employeeId, Long payrollPeriodId);
    
    Page<PayrollLedger> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId, Pageable pageable);
    
    @Query("SELECT pl FROM PayrollLedger pl WHERE pl.payDate BETWEEN :startDate AND :endDate")
    List<PayrollLedger> findByPayDateBetween(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    @Query("SELECT pl FROM PayrollLedger pl JOIN FETCH pl.employee e JOIN FETCH pl.payrollPeriod pp WHERE pl.status = :status")
    List<PayrollLedger> findByStatusWithEmployeeAndPeriod(@Param("status") PayrollLedgerStatus status);
    
    @Query("SELECT COUNT(pl) FROM PayrollLedger pl WHERE pl.status = :status")
    long countByStatus(@Param("status") PayrollLedgerStatus status);
    
    @Query("SELECT SUM(pl.netPay) FROM PayrollLedger pl WHERE pl.payrollPeriodId = :periodId AND pl.status = 'PAID'")
    Optional<java.math.BigDecimal> getTotalNetPayForPeriod(@Param("periodId") Long periodId);
    
    @Query("SELECT pl FROM PayrollLedger pl WHERE pl.employeeId IN :employeeIds AND pl.payrollPeriodId = :periodId")
    List<PayrollLedger> findByEmployeeIdsAndPeriod(@Param("employeeIds") List<Long> employeeIds, 
                                                  @Param("periodId") Long periodId);
}