package com.example.demo.payroll.repository;

import com.example.demo.payroll.entity.PayrollAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PayrollAuditRepository extends JpaRepository<PayrollAudit, Long> {
    
    List<PayrollAudit> findByPayrollLedgerId(Long payrollLedgerId);
    
    Page<PayrollAudit> findByPayrollLedgerIdOrderByCreatedAtDesc(Long payrollLedgerId, Pageable pageable);
    
    List<PayrollAudit> findByAction(String action);
    
    List<PayrollAudit> findByPerformedBy(Long performedBy);
    
    @Query("SELECT pa FROM PayrollAudit pa WHERE pa.createdAt BETWEEN :startDate AND :endDate ORDER BY pa.createdAt DESC")
    List<PayrollAudit> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT pa FROM PayrollAudit pa WHERE pa.payrollLedgerId = :ledgerId AND pa.action = :action ORDER BY pa.createdAt DESC")
    List<PayrollAudit> findByPayrollLedgerIdAndAction(@Param("ledgerId") Long payrollLedgerId, 
                                                     @Param("action") String action);
    
    @Query("SELECT COUNT(pa) FROM PayrollAudit pa WHERE pa.action = :action AND pa.createdAt >= :fromDate")
    long countByActionSince(@Param("action") String action, @Param("fromDate") LocalDateTime fromDate);
}