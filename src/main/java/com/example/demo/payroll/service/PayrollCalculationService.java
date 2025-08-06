package com.example.demo.payroll.service;

import com.example.demo.payroll.dto.PayrollCalculationRequest;
import com.example.demo.payroll.entity.PayrollLedger;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for payroll calculations
 */
public interface PayrollCalculationService {
    
    /**
     * Calculate payroll for a single employee
     */
    PayrollLedger calculatePayroll(PayrollCalculationRequest request);
    
    /**
     * Calculate payroll for multiple employees in a period
     */
    List<PayrollLedger> calculatePayrollForPeriod(Long payrollPeriodId, List<Long> employeeIds);
    
    /**
     * Recalculate an existing payroll ledger
     */
    PayrollLedger recalculatePayroll(Long payrollLedgerId);
    
    /**
     * Calculate overtime pay based on hours and rate
     */
    BigDecimal calculateOvertimePay(BigDecimal overtimeHours, BigDecimal hourlyRate);
    
    /**
     * Calculate tax amount based on gross pay and tax rules
     */
    BigDecimal calculateTaxes(BigDecimal grossPay, Long employeeId);
    
    /**
     * Calculate total deductions for an employee
     */
    BigDecimal calculateDeductions(BigDecimal grossPay, Long employeeId, List<Long> componentIds);
    
    /**
     * Validate payroll calculation rules
     */
    void validateCalculation(PayrollCalculationRequest request);
}