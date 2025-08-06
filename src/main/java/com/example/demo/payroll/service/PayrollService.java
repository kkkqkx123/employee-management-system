package com.example.demo.payroll.service;

import com.example.demo.common.dto.PageResponse;
import com.example.demo.payroll.dto.*;
import com.example.demo.payroll.entity.PayrollLedgerStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for payroll management
 */
public interface PayrollService {
    
    // Payroll Ledger Operations
    PayrollLedgerDto createPayrollLedger(PayrollCalculationRequest request);
    PayrollLedgerDto updatePayrollLedger(Long id, PayrollLedgerDto payrollLedgerDto);
    PayrollLedgerDto getPayrollLedgerById(Long id);
    void deletePayrollLedger(Long id);
    
    // Payroll Ledger Queries
    PageResponse<PayrollLedgerDto> getPayrollLedgers(Pageable pageable);
    List<PayrollLedgerDto> getPayrollLedgersByEmployee(Long employeeId);
    List<PayrollLedgerDto> getPayrollLedgersByPeriod(Long payrollPeriodId);
    List<PayrollLedgerDto> getPayrollLedgersByStatus(PayrollLedgerStatus status);
    PageResponse<PayrollLedgerDto> getPayrollLedgersByEmployeeWithPaging(Long employeeId, Pageable pageable);
    
    // Payroll Processing
    List<PayrollLedgerDto> processPayrollForPeriod(Long payrollPeriodId, List<Long> employeeIds);
    PayrollLedgerDto approvePayroll(Long payrollLedgerId, String reason);
    PayrollLedgerDto rejectPayroll(Long payrollLedgerId, String reason);
    PayrollLedgerDto markAsPaid(Long payrollLedgerId, String paymentReference);
    
    // Payroll Period Operations
    PayrollPeriodDto createPayrollPeriod(PayrollPeriodDto payrollPeriodDto);
    PayrollPeriodDto updatePayrollPeriod(Long id, PayrollPeriodDto payrollPeriodDto);
    PayrollPeriodDto getPayrollPeriodById(Long id);
    void deletePayrollPeriod(Long id);
    PageResponse<PayrollPeriodDto> getPayrollPeriods(Pageable pageable);
    List<PayrollPeriodDto> getActivePayrollPeriods();
    PayrollPeriodDto getCurrentPayrollPeriod();
    PayrollPeriodDto closePayrollPeriod(Long id);
    
    // Salary Component Operations
    SalaryComponentDto createSalaryComponent(SalaryComponentDto salaryComponentDto);
    SalaryComponentDto updateSalaryComponent(Long id, SalaryComponentDto salaryComponentDto);
    SalaryComponentDto getSalaryComponentById(Long id);
    void deleteSalaryComponent(Long id);
    PageResponse<SalaryComponentDto> getSalaryComponents(Pageable pageable);
    List<SalaryComponentDto> getActiveSalaryComponents();
    List<SalaryComponentDto> getSalaryComponentsByType(String componentType);
    
    // Reporting and Analytics
    PayrollSummaryDto getPayrollSummary(Long payrollPeriodId);
    List<PayrollSummaryDto> getPayrollSummaryByDateRange(LocalDate startDate, LocalDate endDate);
    byte[] generatePayrollReport(PayrollReportRequest request);
}