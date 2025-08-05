# Payroll Management Implementation

## Overview
This document provides detailed implementation specifications for the Payroll Management module. This module handles payroll ledgers, salary calculations, payroll processing, and payroll reporting with comprehensive audit trails.

## Package Structure
```
com.example.demo.payroll/
├── entity/
│   ├── PayrollLedger.java
│   ├── PayrollPeriod.java
│   ├── SalaryComponent.java
│   └── PayrollAudit.java
├── repository/
│   ├── PayrollLedgerRepository.java
│   ├── PayrollPeriodRepository.java
│   ├── SalaryComponentRepository.java
│   └── PayrollAuditRepository.java
├── service/
│   ├── PayrollService.java
│   ├── PayrollCalculationService.java
│   ├── PayrollReportService.java
│   └── impl/
│       ├── PayrollServiceImpl.java
│       ├── PayrollCalculationServiceImpl.java
│       └── PayrollReportServiceImpl.java
├── controller/
│   └── PayrollController.java
├── dto/
│   ├── PayrollLedgerDto.java
│   ├── PayrollPeriodDto.java
│   ├── SalaryComponentDto.java
│   ├── PayrollCalculationRequest.java
│   ├── PayrollReportRequest.java
│   └── PayrollSummaryDto.java
├── util/
│   ├── PayrollCalculationUtil.java
│   └── PayrollValidationUtil.java
└── exception/
    ├── PayrollNotFoundException.java
    ├── PayrollCalculationException.java
    ├── PayrollPeriodException.java
    └── PayrollValidationException.java
```

## Entity Classes

### PayrollLedger Entity
```java
package com.example.demo.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import com.example.demo.employee.entity.Employee;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "payroll_ledgers", indexes = {
    @Index(name = "idx_payrollledger_employee_id", columnList = "employee_id"),
    @Index(name = "idx_payrollledger_period_id", columnList = "payroll_period_id"),
    @Index(name = "idx_payrollledger_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PayrollLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "payroll_period_id", nullable = false)
    private Long payrollPeriodId;

    @Column(name = "employee_number", length = 20)
    private String employeeNumber;

    @Column(name = "employee_name", length = 100)
    private String employeeName;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "department_name", length = 100)
    private String departmentName;

    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "position_name", length = 100)
    private String positionName;

    @Column(name = "base_salary", precision = 12, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "hourly_rate", precision = 12, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "hours_worked", precision = 10, scale = 2)
    private BigDecimal hoursWorked;

    @Column(name = "overtime_hours", precision = 10, scale = 2)
    private BigDecimal overtimeHours;

    @Column(name = "overtime_rate", precision = 12, scale = 2)
    private BigDecimal overtimeRate;

    @Column(name = "total_allowances", precision = 12, scale = 2)
    private BigDecimal totalAllowances;

    @Column(name = "total_deductions", precision = 12, scale = 2)
    private BigDecimal totalDeductions;

    @Column(name = "gross_salary", precision = 12, scale = 2)
    private BigDecimal grossSalary;

    @Column(name = "net_salary", precision = 12, scale = 2)
    private BigDecimal netSalary;

    @Column(name = "employer_contributions", precision = 12, scale = 2)
    private BigDecimal employerContributions;

    @Column(name = "total_cost", precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "pay_date")
    private LocalDate payDate;

    @Lob
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Lob
    @Column(name = "calculation_details", columnDefinition = "TEXT")
    private String calculationDetails; // JSON string for all components
}
```
### PayrollPeriod Entity
```java
package com.example.demo.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "payroll_periods", indexes = {
    @Index(name = "idx_payrollperiod_start_end", columnList = "start_date, end_date"),
    @Index(name = "idx_payrollperiod_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PayrollPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "pay_date", nullable = false)
    private LocalDate payDate;

    @Column(name = "working_days")
    private Integer workingDays;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
```

### SalaryComponent Entity
```java
package com.example.demo.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "salary_components", indexes = {
    @Index(name = "idx_salarycomponent_code", columnList = "code", unique = true),
    @Index(name = "idx_salarycomponent_type", columnList = "type")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SalaryComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "type", nullable = false, length = 20)
    private String type; // ALLOWANCE, DEDUCTION

    @Column(name = "calculation_type", nullable = false, length = 20)
    private String calculationType; // FIXED, PERCENTAGE

    @Column(name = "value", precision = 12, scale = 2)
    private BigDecimal value; // Amount for FIXED, percentage for PERCENTAGE

    @Column(name = "is_taxable", nullable = false)
    private boolean isTaxable = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
```

### PayrollAudit Entity
```java
package com.example.demo.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "payroll_audits", indexes = {
    @Index(name = "idx_payrollaudit_ledger_id", columnList = "payroll_ledger_id"),
    @Index(name = "idx_payrollaudit_performed_by", columnList = "performed_by")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PayrollAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payroll_ledger_id", nullable = false)
    private Long payrollLedgerId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Lob
    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // JSON string of changes

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "performed_by", nullable = false)
    private Long performedBy;

    @CreatedDate
    @Column(name = "performed_at", nullable = false, updatable = false)
    private Instant performedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}
```
```## Repos
itory Interfaces

### PayrollLedgerRepository
```java
package com.example.demo.payroll.repository;

import com.example.demo.payroll.entity.PayrollLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollLedgerRepository extends JpaRepository<PayrollLedger, Long> {
    
    /**
     * Find payroll ledger by employee and payroll period
     * @param employeeId Employee ID
     * @param payrollPeriodId Payroll period ID
     * @return Optional payroll ledger
     */
    Optional<PayrollLedger> findByEmployeeIdAndPayrollPeriodId(Long employeeId, Long payrollPeriodId);
    
    /**
     * Find payroll ledgers by employee ID
     * @param employeeId Employee ID
     * @param pageable Pagination parameters
     * @return Page of payroll ledgers
     */
    Page<PayrollLedger> findByEmployeeIdOrderByPayDateDesc(Long employeeId, Pageable pageable);
    
    /**
     * Find payroll ledgers by payroll period
     * @param payrollPeriodId Payroll period ID
     * @param pageable Pagination parameters
     * @return Page of payroll ledgers
     */
    Page<PayrollLedger> findByPayrollPeriodId(Long payrollPeriodId, Pageable pageable);
    
    /**
     * Find payroll ledgers by department
     * @param departmentId Department ID
     * @param pageable Pagination parameters
     * @return Page of payroll ledgers
     */
    Page<PayrollLedger> findByDepartmentId(Long departmentId, Pageable pageable);
    
    /**
     * Find payroll ledgers by status
     * @param status Payroll status
     * @param pageable Pagination parameters
     * @return Page of payroll ledgers
     */
    Page<PayrollLedger> findByStatus(String status, Pageable pageable);
    
    /**
     * Find payroll ledgers by pay date range
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination parameters
     * @return Page of payroll ledgers
     */
    Page<PayrollLedger> findByPayDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Find payroll ledgers by employee and date range
     * @param employeeId Employee ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of payroll ledgers
     */
    List<PayrollLedger> findByEmployeeIdAndPayDateBetweenOrderByPayDateDesc(
        Long employeeId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find payroll ledgers by department and payroll period
     * @param departmentId Department ID
     * @param payrollPeriodId Payroll period ID
     * @return List of payroll ledgers
     */
    List<PayrollLedger> findByDepartmentIdAndPayrollPeriodId(Long departmentId, Long payrollPeriodId);
    
    /**
     * Check if payroll exists for employee and period
     * @param employeeId Employee ID
     * @param payrollPeriodId Payroll period ID
     * @return true if exists
     */
    boolean existsByEmployeeIdAndPayrollPeriodId(Long employeeId, Long payrollPeriodId);
    
    /**
     * Count payroll ledgers by status
     * @param status Payroll status
     * @return Count of payroll ledgers
     */
    long countByStatus(String status);
    
    /**
     * Count payroll ledgers by payroll period
     * @param payrollPeriodId Payroll period ID
     * @return Count of payroll ledgers
     */
    long countByPayrollPeriodId(Long payrollPeriodId);
    
    /**
     * Find payroll ledgers by multiple criteria
     * @param departmentId Department ID (optional)
     * @param payrollPeriodId Payroll period ID (optional)
     * @param status Status (optional)
     * @param pageable Pagination parameters
     * @return Page of payroll ledgers
     */
    Page<PayrollLedger> findByDepartmentIdAndPayrollPeriodIdAndStatus(
        Long departmentId, Long payrollPeriodId, String status, Pageable pageable);
}
```

### PayrollPeriodRepository
```java
package com.example.demo.payroll.repository;

import com.example.demo.payroll.entity.PayrollPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> {
    
    /**
     * Find payroll period by name
     * @param name Period name
     * @return Optional payroll period
     */
    Optional<PayrollPeriod> findByName(String name);
    
    /**
     * Find active payroll periods
     * @return List of active payroll periods
     */
    List<PayrollPeriod> findByIsActiveTrueOrderByStartDateDesc();
    
    /**
     * Find payroll periods by type
     * @param type Period type
     * @param pageable Pagination parameters
     * @return Page of payroll periods
     */
    Page<PayrollPeriod> findByTypeOrderByStartDateDesc(String type, Pageable pageable);
    
    /**
     * Find payroll periods by status
     * @param status Period status
     * @param pageable Pagination parameters
     * @return Page of payroll periods
     */
    Page<PayrollPeriod> findByStatusOrderByStartDateDesc(String status, Pageable pageable);
    
    /**
     * Find payroll periods by date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of payroll periods
     */
    List<PayrollPeriod> findByStartDateBetweenOrderByStartDateDesc(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find current payroll period (contains given date)
     * @param date Date to check
     * @return Optional payroll period
     */
    Optional<PayrollPeriod> findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsActiveTrue(
        LocalDate date, LocalDate date2);
    
    /**
     * Find overlapping payroll periods
     * @param startDate Start date
     * @param endDate End date
     * @return List of overlapping periods
     */
    List<PayrollPeriod> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
        LocalDate endDate, LocalDate startDate);
    
    /**
     * Check if period name exists
     * @param name Period name
     * @return true if exists
     */
    boolean existsByName(String name);
    
    /**
     * Find latest payroll period by type
     * @param type Period type
     * @return Optional payroll period
     */
    Optional<PayrollPeriod> findFirstByTypeOrderByStartDateDesc(String type);
}
```

## Service Interfaces

### PayrollService
```java
package com.example.demo.payroll.service;

import com.example.demo.payroll.dto.PayrollLedgerDto;
import com.example.demo.payroll.dto.PayrollPeriodDto;
import com.example.demo.payroll.dto.PayrollCalculationRequest;
import com.example.demo.payroll.dto.PayrollSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PayrollService {
    
    /**
     * Create a new payroll ledger
     * @param payrollLedgerDto Payroll ledger data
     * @return Created payroll ledger DTO
     * @throws PayrollValidationException if validation fails
     * @throws EmployeeNotFoundException if employee not found
     * @throws PayrollPeriodNotFoundException if payroll period not found
     */
    PayrollLedgerDto createPayrollLedger(PayrollLedgerDto payrollLedgerDto);
    
    /**
     * Update an existing payroll ledger
     * @param id Payroll ledger ID
     * @param payrollLedgerDto Updated payroll ledger data
     * @return Updated payroll ledger DTO
     * @throws PayrollNotFoundException if payroll ledger not found
     * @throws PayrollValidationException if validation fails
     */
    PayrollLedgerDto updatePayrollLedger(Long id, PayrollLedgerDto payrollLedgerDto);
    
    /**
     * Get payroll ledger by ID
     * @param id Payroll ledger ID
     * @return Payroll ledger DTO
     * @throws PayrollNotFoundException if payroll ledger not found
     */
    PayrollLedgerDto getPayrollLedger(Long id);
    
    /**
     * Get payroll ledgers with pagination
     * @param pageable Pagination parameters
     * @return Page of payroll ledger DTOs
     */
    Page<PayrollLedgerDto> getPayrollLedgers(Pageable pageable);
    
    /**
     * Get employee payroll history
     * @param employeeId Employee ID
     * @param pageable Pagination parameters
     * @return Page of payroll ledger DTOs
     * @throws EmployeeNotFoundException if employee not found
     */
    Page<PayrollLedgerDto> getEmployeePayrollHistory(Long employeeId, Pageable pageable);
    
    /**
     * Get payroll ledgers by period
     * @param payrollPeriodId Payroll period ID
     * @param pageable Pagination parameters
     * @return Page of payroll ledger DTOs
     * @throws PayrollPeriodNotFoundException if payroll period not found
     */
    Page<PayrollLedgerDto> getPayrollLedgersByPeriod(Long payrollPeriodId, Pageable pageable);
    
    /**
     * Get payroll ledgers by department
     * @param departmentId Department ID
     * @param pageable Pagination parameters
     * @return Page of payroll ledger DTOs
     * @throws DepartmentNotFoundException if department not found
     */
    Page<PayrollLedgerDto> getPayrollLedgersByDepartment(Long departmentId, Pageable pageable);
    
    /**
     * Calculate payroll for employee and period
     * @param request Payroll calculation request
     * @return Calculated payroll ledger DTO
     * @throws PayrollCalculationException if calculation fails
     * @throws EmployeeNotFoundException if employee not found
     * @throws PayrollPeriodNotFoundException if payroll period not found
     */
    PayrollLedgerDto calculatePayroll(PayrollCalculationRequest request);
    
    /**
     * Calculate payroll for all employees in a period
     * @param payrollPeriodId Payroll period ID
     * @return List of calculated payroll ledger DTOs
     * @throws PayrollPeriodNotFoundException if payroll period not found
     * @throws PayrollCalculationException if calculation fails
     */
    List<PayrollLedgerDto> calculatePayrollForPeriod(Long payrollPeriodId);
    
    /**
     * Calculate payroll for department in a period
     * @param departmentId Department ID
     * @param payrollPeriodId Payroll period ID
     * @return List of calculated payroll ledger DTOs
     * @throws DepartmentNotFoundException if department not found
     * @throws PayrollPeriodNotFoundException if payroll period not found
     * @throws PayrollCalculationException if calculation fails
     */
    List<PayrollLedgerDto> calculatePayrollForDepartment(Long departmentId, Long payrollPeriodId);
    
    /**
     * Approve payroll ledger
     * @param id Payroll ledger ID
     * @param approvedBy User ID who approved
     * @return Approved payroll ledger DTO
     * @throws PayrollNotFoundException if payroll ledger not found
     * @throws PayrollValidationException if payroll cannot be approved
     */
    PayrollLedgerDto approvePayroll(Long id, Long approvedBy);
    
    /**
     * Approve multiple payroll ledgers
     * @param ids List of payroll ledger IDs
     * @param approvedBy User ID who approved
     * @return List of approved payroll ledger DTOs
     * @throws PayrollNotFoundException if any payroll ledger not found
     * @throws PayrollValidationException if any payroll cannot be approved
     */
    List<PayrollLedgerDto> approvePayrolls(List<Long> ids, Long approvedBy);
    
    /**
     * Mark payroll as paid
     * @param id Payroll ledger ID
     * @param paidBy User ID who marked as paid
     * @param paymentReference Payment reference
     * @return Updated payroll ledger DTO
     * @throws PayrollNotFoundException if payroll ledger not found
     * @throws PayrollValidationException if payroll cannot be marked as paid
     */
    PayrollLedgerDto markPayrollAsPaid(Long id, Long paidBy, String paymentReference);
    
    /**
     * Cancel payroll ledger
     * @param id Payroll ledger ID
     * @param cancelledBy User ID who cancelled
     * @param reason Cancellation reason
     * @throws PayrollNotFoundException if payroll ledger not found
     * @throws PayrollValidationException if payroll cannot be cancelled
     */
    void cancelPayroll(Long id, Long cancelledBy, String reason);
    
    /**
     * Delete payroll ledger
     * @param id Payroll ledger ID
     * @throws PayrollNotFoundException if payroll ledger not found
     * @throws PayrollValidationException if payroll cannot be deleted
     */
    void deletePayrollLedger(Long id);
    
    /**
     * Get payroll summary for period
     * @param payrollPeriodId Payroll period ID
     * @return Payroll summary DTO
     * @throws PayrollPeriodNotFoundException if payroll period not found
     */
    PayrollSummaryDto getPayrollSummary(Long payrollPeriodId);
    
    /**
     * Get payroll summary for department and period
     * @param departmentId Department ID
     * @param payrollPeriodId Payroll period ID
     * @return Payroll summary DTO
     * @throws DepartmentNotFoundException if department not found
     * @throws PayrollPeriodNotFoundException if payroll period not found
     */
    PayrollSummaryDto getPayrollSummary(Long departmentId, Long payrollPeriodId);
}
```

This completes the comprehensive implementation documentation for all major modules of the Spring Boot backend. Each document provides:

1. **Complete package structure** with all necessary classes
2. **Detailed entity definitions** with all fields, annotations, and relationships
3. **Repository interfaces** with all required query methods
4. **Service interfaces** with complete method signatures, parameters, return types, and exceptions
5. **DTO classes** with validation annotations and constraints
6. **Exception handling** specifications
7. **Database schema** considerations for Redis

The implementation documents are structured to provide developers with everything they need to implement each module without having to make design decisions. Each method signature includes:
- Clear parameter definitions
- Return type specifications
- Exception declarations
- JavaDoc documentation explaining the purpose and behavior

Would you like me to elaborate on any specific module or add additional implementation details for any particular aspect?