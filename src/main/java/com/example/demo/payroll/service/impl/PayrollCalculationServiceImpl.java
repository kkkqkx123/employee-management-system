package com.example.demo.payroll.service.impl;

import com.example.demo.employee.entity.Employee;
import com.example.demo.employee.repository.EmployeeRepository;
import com.example.demo.payroll.dto.PayrollCalculationRequest;
import com.example.demo.payroll.entity.*;
import com.example.demo.payroll.repository.PayrollLedgerRepository;
import com.example.demo.payroll.repository.PayrollPeriodRepository;
import com.example.demo.payroll.repository.SalaryComponentRepository;
import com.example.demo.payroll.service.PayrollCalculationService;
import com.example.demo.payroll.exception.PayrollCalculationException;
import com.example.demo.payroll.exception.PayrollValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PayrollCalculationServiceImpl implements PayrollCalculationService {
    
    private final PayrollLedgerRepository payrollLedgerRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final SalaryComponentRepository salaryComponentRepository;
    private final EmployeeRepository employeeRepository;
    
    private static final BigDecimal OVERTIME_MULTIPLIER = new BigDecimal("1.5");
    private static final int DECIMAL_SCALE = 2;
    
    @Override
    public PayrollLedger calculatePayroll(PayrollCalculationRequest request) {
        log.info("Calculating payroll for employee {} in period {}", 
                request.getEmployeeId(), request.getPayrollPeriodId());
        
        validateCalculation(request);
        
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new PayrollValidationException("Employee not found: " + request.getEmployeeId()));
        
        PayrollPeriod period = payrollPeriodRepository.findById(request.getPayrollPeriodId())
                .orElseThrow(() -> new PayrollValidationException("Payroll period not found: " + request.getPayrollPeriodId()));
        
        // Check if payroll already exists for this employee and period
        payrollLedgerRepository.findByEmployeeIdAndPayrollPeriodId(
                request.getEmployeeId(), request.getPayrollPeriodId())
                .ifPresent(existing -> {
                    throw new PayrollValidationException("Payroll already exists for this employee and period");
                });
        
        PayrollLedger ledger = new PayrollLedger();
        ledger.setEmployeeId(request.getEmployeeId());
        ledger.setPayrollPeriodId(request.getPayrollPeriodId());
        ledger.setBaseSalary(request.getBaseSalary() != null ? request.getBaseSalary() : employee.getSalary());
        ledger.setOvertimeHours(request.getOvertimeHours() != null ? request.getOvertimeHours() : BigDecimal.ZERO);
        ledger.setBonusAmount(request.getBonusAmount() != null ? request.getBonusAmount() : BigDecimal.ZERO);
        ledger.setNotes(request.getNotes());
        ledger.setStatus(PayrollLedgerStatus.PENDING);
        
        // Calculate overtime pay
        if (ledger.getOvertimeHours().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal hourlyRate = calculateHourlyRate(ledger.getBaseSalary());
            ledger.setOvertimePay(calculateOvertimePay(ledger.getOvertimeHours(), hourlyRate));
        }
        
        // Calculate gross pay
        ledger.setGrossPay(ledger.getBaseSalary()
                .add(ledger.getOvertimePay())
                .add(ledger.getBonusAmount()));
        
        // Calculate deductions and taxes
        List<SalaryComponent> activeComponents = salaryComponentRepository.findAllActiveOrderByCalculationOrder();
        Map<Long, BigDecimal> overrides = request.getComponentOverrides() != null ?
                request.getComponentOverrides().stream()
                        .collect(Collectors.toMap(
                                PayrollCalculationRequest.ComponentOverride::getSalaryComponentId,
                                PayrollCalculationRequest.ComponentOverride::getAmount)) :
                Map.of();
        
        List<PayrollLedgerComponent> components = calculateComponents(ledger, activeComponents, overrides);
        
        // Calculate totals
        BigDecimal totalDeductions = components.stream()
                .filter(c -> "DEDUCTION".equals(c.getSalaryComponent().getComponentType()))
                .map(PayrollLedgerComponent::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalTaxes = components.stream()
                .filter(c -> "TAX".equals(c.getSalaryComponent().getComponentType()))
                .map(PayrollLedgerComponent::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        ledger.setTotalDeductions(totalDeductions);
        ledger.setTotalTaxes(totalTaxes);
        ledger.setNetPay(ledger.getGrossPay().subtract(totalDeductions).subtract(totalTaxes));
        ledger.setStatus(PayrollLedgerStatus.CALCULATED);
        
        // Save ledger first
        PayrollLedger savedLedger = payrollLedgerRepository.save(ledger);
        
        // Set ledger ID for components and save
        components.forEach(component -> component.setPayrollLedgerId(savedLedger.getId()));
        savedLedger.setComponents(components);
        
        log.info("Payroll calculated successfully for employee {} with net pay {}", 
                request.getEmployeeId(), savedLedger.getNetPay());
        
        return savedLedger;
    }
    
    @Override
    public List<PayrollLedger> calculatePayrollForPeriod(Long payrollPeriodId, List<Long> employeeIds) {
        log.info("Calculating payroll for {} employees in period {}", employeeIds.size(), payrollPeriodId);
        
        List<PayrollLedger> results = new ArrayList<>();
        
        for (Long employeeId : employeeIds) {
            try {
                Employee employee = employeeRepository.findById(employeeId)
                        .orElseThrow(() -> new PayrollValidationException("Employee not found: " + employeeId));
                
                PayrollCalculationRequest request = new PayrollCalculationRequest();
                request.setEmployeeId(employeeId);
                request.setPayrollPeriodId(payrollPeriodId);
                request.setBaseSalary(employee.getSalary());
                
                PayrollLedger ledger = calculatePayroll(request);
                results.add(ledger);
                
            } catch (Exception e) {
                log.error("Failed to calculate payroll for employee {}: {}", employeeId, e.getMessage());
                // Continue with other employees
            }
        }
        
        log.info("Calculated payroll for {} out of {} employees", results.size(), employeeIds.size());
        return results;
    }
    
    @Override
    public PayrollLedger recalculatePayroll(Long payrollLedgerId) {
        PayrollLedger existing = payrollLedgerRepository.findById(payrollLedgerId)
                .orElseThrow(() -> new PayrollValidationException("Payroll ledger not found: " + payrollLedgerId));
        
        if (existing.getStatus() == PayrollLedgerStatus.PAID) {
            throw new PayrollValidationException("Cannot recalculate paid payroll");
        }
        
        PayrollCalculationRequest request = new PayrollCalculationRequest();
        request.setEmployeeId(existing.getEmployeeId());
        request.setPayrollPeriodId(existing.getPayrollPeriodId());
        request.setBaseSalary(existing.getBaseSalary());
        request.setOvertimeHours(existing.getOvertimeHours());
        request.setBonusAmount(existing.getBonusAmount());
        
        // Delete existing and create new
        payrollLedgerRepository.delete(existing);
        return calculatePayroll(request);
    }
    
    @Override
    public BigDecimal calculateOvertimePay(BigDecimal overtimeHours, BigDecimal hourlyRate) {
        if (overtimeHours == null || hourlyRate == null) {
            return BigDecimal.ZERO;
        }
        return overtimeHours.multiply(hourlyRate).multiply(OVERTIME_MULTIPLIER)
                .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
    }
    
    @Override
    public BigDecimal calculateTaxes(BigDecimal grossPay, Long employeeId) {
        // Simplified tax calculation - in real implementation, this would use tax tables
        BigDecimal taxRate = new BigDecimal("0.20"); // 20% tax rate
        return grossPay.multiply(taxRate).setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
    }
    
    @Override
    public BigDecimal calculateDeductions(BigDecimal grossPay, Long employeeId, List<Long> componentIds) {
        List<SalaryComponent> deductionComponents = salaryComponentRepository.findAllById(componentIds)
                .stream()
                .filter(c -> "DEDUCTION".equals(c.getComponentType()))
                .collect(Collectors.toList());
        
        BigDecimal totalDeductions = BigDecimal.ZERO;
        
        for (SalaryComponent component : deductionComponents) {
            BigDecimal amount;
            if (component.getPercentage() != null) {
                amount = grossPay.multiply(component.getPercentage().divide(new BigDecimal("100")));
            } else {
                amount = component.getAmount();
            }
            totalDeductions = totalDeductions.add(amount);
        }
        
        return totalDeductions.setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
    }
    
    @Override
    public void validateCalculation(PayrollCalculationRequest request) {
        if (request.getEmployeeId() == null) {
            throw new PayrollValidationException("Employee ID is required");
        }
        if (request.getPayrollPeriodId() == null) {
            throw new PayrollValidationException("Payroll period ID is required");
        }
        if (request.getBaseSalary() != null && request.getBaseSalary().compareTo(BigDecimal.ZERO) < 0) {
            throw new PayrollValidationException("Base salary cannot be negative");
        }
        if (request.getOvertimeHours() != null && request.getOvertimeHours().compareTo(BigDecimal.ZERO) < 0) {
            throw new PayrollValidationException("Overtime hours cannot be negative");
        }
        if (request.getBonusAmount() != null && request.getBonusAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new PayrollValidationException("Bonus amount cannot be negative");
        }
    }
    
    private BigDecimal calculateHourlyRate(BigDecimal monthlySalary) {
        // Assuming 160 working hours per month (40 hours/week * 4 weeks)
        BigDecimal workingHoursPerMonth = new BigDecimal("160");
        return monthlySalary.divide(workingHoursPerMonth, DECIMAL_SCALE, RoundingMode.HALF_UP);
    }
    
    private List<PayrollLedgerComponent> calculateComponents(PayrollLedger ledger, 
                                                           List<SalaryComponent> components,
                                                           Map<Long, BigDecimal> overrides) {
        List<PayrollLedgerComponent> ledgerComponents = new ArrayList<>();
        BigDecimal calculationBase = ledger.getGrossPay();
        
        for (SalaryComponent component : components) {
            PayrollLedgerComponent ledgerComponent = new PayrollLedgerComponent();
            ledgerComponent.setSalaryComponentId(component.getId());
            ledgerComponent.setCalculationBase(calculationBase);
            
            BigDecimal amount;
            if (overrides.containsKey(component.getId())) {
                amount = overrides.get(component.getId());
                ledgerComponent.setNotes("Override applied");
            } else if (component.getPercentage() != null) {
                amount = calculationBase.multiply(component.getPercentage().divide(new BigDecimal("100")));
                ledgerComponent.setPercentageApplied(component.getPercentage());
            } else {
                amount = component.getAmount();
            }
            
            ledgerComponent.setAmount(amount.setScale(DECIMAL_SCALE, RoundingMode.HALF_UP));
            ledgerComponents.add(ledgerComponent);
        }
        
        return ledgerComponents;
    }
}