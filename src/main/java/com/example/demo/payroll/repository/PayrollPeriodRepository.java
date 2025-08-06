package com.example.demo.payroll.repository;

import com.example.demo.payroll.entity.PayrollPeriod;
import com.example.demo.payroll.entity.PayrollPeriodStatus;
import com.example.demo.payroll.entity.PayrollPeriodType;
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
public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> {
    
    List<PayrollPeriod> findByStatus(PayrollPeriodStatus status);
    
    List<PayrollPeriod> findByPeriodType(PayrollPeriodType periodType);
    
    List<PayrollPeriod> findByIsActiveTrue();
    
    Page<PayrollPeriod> findByIsActiveTrueOrderByStartDateDesc(Pageable pageable);
    
    @Query("SELECT pp FROM PayrollPeriod pp WHERE pp.startDate <= :date AND pp.endDate >= :date AND pp.isActive = true")
    Optional<PayrollPeriod> findCurrentPeriodForDate(@Param("date") LocalDate date);
    
    @Query("SELECT pp FROM PayrollPeriod pp WHERE pp.startDate BETWEEN :startDate AND :endDate")
    List<PayrollPeriod> findPeriodsInDateRange(@Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);
    
    @Query("SELECT pp FROM PayrollPeriod pp WHERE pp.status = 'OPEN' AND pp.isActive = true ORDER BY pp.startDate")
    List<PayrollPeriod> findOpenPeriods();
    
    @Query("SELECT pp FROM PayrollPeriod pp WHERE pp.endDate < :date AND pp.status != 'COMPLETED'")
    List<PayrollPeriod> findOverduePeriods(@Param("date") LocalDate date);
    
    boolean existsByPeriodNameIgnoreCase(String periodName);
    
    @Query("SELECT pp FROM PayrollPeriod pp WHERE pp.startDate <= :endDate AND pp.endDate >= :startDate")
    List<PayrollPeriod> findOverlappingPeriods(@Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);
}