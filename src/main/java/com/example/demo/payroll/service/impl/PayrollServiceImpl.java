package com.example.demo.payroll.service.impl;

import com.example.demo.common.dto.PageResponse;
import com.example.demo.payroll.dto.*;
import com.example.demo.payroll.entity.*;
import com.example.demo.payroll.exception.*;
import com.example.demo.payroll.repository.*;
import com.example.demo.payroll.service.PayrollCalculationService;
import com.example.demo.payroll.service.PayrollService;
import com.example.demo.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PayrollServiceImpl implements PayrollService {

    private final PayrollLedgerRepository payrollLedgerRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final SalaryComponentRepository salaryComponentRepository;
    private final PayrollAuditRepository payrollAuditRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollCalculationService payrollCalculationService;

    // Payroll Ledger Operations
    @Override
    public PayrollLedgerDto createPayrollLedger(PayrollCalculationRequest request) {
        log.info("Creating payroll ledger for employee: {}", request.getEmployeeId());
        
        // Validate employee exists
        if (!employeeRepository.existsById(request.getEmployeeId())) {
            throw new PayrollNotFoundException("Employee not found with id: " + request.getEmployeeId());
        }
        
        // Calculate payroll
        PayrollLedger ledger = payrollCalculationService.calculatePayroll(request);
        PayrollLedger savedLedger = payrollLedgerRepository.save(ledger);
        
        // Create audit record
        createAuditRecord(savedLedger.getId(), "CREATED", "Payroll ledger created");
        
        return convertToDto(savedLedger);
    }

    @Override
    @CacheEvict(value = "payrollLedgers", key = "#id")
    public PayrollLedgerDto updatePayrollLedger(Long id, PayrollLedgerDto payrollLedgerDto) {
        log.info("Updating payroll ledger: {}", id);
        
        PayrollLedger existingLedger = payrollLedgerRepository.findById(id)
            .orElseThrow(() -> new PayrollNotFoundException("Payroll ledger not found with id: " + id));
        
        // Update fields
        existingLedger.setBaseSalary(payrollLedgerDto.getBaseSalary());
        existingLedger.setOvertimePay(payrollLedgerDto.getOvertimePay());
        existingLedger.setTotalDeductions(payrollLedgerDto.getTotalDeductions());
        existingLedger.setTotalTaxes(payrollLedgerDto.getTotalTaxes());
        existingLedger.setNetPay(payrollLedgerDto.getNetPay());
        existingLedger.setUpdatedAt(LocalDateTime.now());
        
        PayrollLedger savedLedger = payrollLedgerRepository.save(existingLedger);
        
        // Create audit record
        createAuditRecord(savedLedger.getId(), "UPDATED", "Payroll ledger updated");
        
        return convertToDto(savedLedger);
    }

    @Override
    @Cacheable(value = "payrollLedgers", key = "#id")
    public PayrollLedgerDto getPayrollLedgerById(Long id) {
        log.info("Fetching payroll ledger: {}", id);
        
        PayrollLedger ledger = payrollLedgerRepository.findById(id)
            .orElseThrow(() -> new PayrollNotFoundException("Payroll ledger not found with id: " + id));
        
        return convertToDto(ledger);
    }

    @Override
    @CacheEvict(value = "payrollLedgers", key = "#id")
    public void deletePayrollLedger(Long id) {
        log.info("Deleting payroll ledger: {}", id);
        
        if (!payrollLedgerRepository.existsById(id)) {
            throw new PayrollNotFoundException("Payroll ledger not found with id: " + id);
        }
        
        // Create audit record before deletion
        createAuditRecord(id, "DELETED", "Payroll ledger deleted");
        
        payrollLedgerRepository.deleteById(id);
    }

    // Payroll Ledger Queries
    @Override
    public PageResponse<PayrollLedgerDto> getPayrollLedgers(Pageable pageable) {
        log.info("Fetching payroll ledgers with pagination");
        
        Page<PayrollLedger> ledgerPage = payrollLedgerRepository.findAll(pageable);
        List<PayrollLedgerDto> ledgerDtos = ledgerPage.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        
        return PageResponse.of(
            ledgerDtos,
            ledgerPage.getNumber(),
            ledgerPage.getSize(),
            ledgerPage.getTotalElements(),
            ledgerPage.getTotalPages()
        );
    }

    @Override
    public List<PayrollLedgerDto> getPayrollLedgersByEmployee(Long employeeId) {
        log.info("Fetching payroll ledgers for employee: {}", employeeId);
        
        List<PayrollLedger> ledgers = payrollLedgerRepository.findByEmployeeId(employeeId);
        return ledgers.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<PayrollLedgerDto> getPayrollLedgersByPeriod(Long payrollPeriodId) {
        log.info("Fetching payroll ledgers for period: {}", payrollPeriodId);
        
        List<PayrollLedger> ledgers = payrollLedgerRepository.findByPayrollPeriodId(payrollPeriodId);
        return ledgers.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<PayrollLedgerDto> getPayrollLedgersByStatus(PayrollLedgerStatus status) {
        log.info("Fetching payroll ledgers with status: {}", status);
        
        List<PayrollLedger> ledgers = payrollLedgerRepository.findByStatus(status);
        return ledgers.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    public PageResponse<PayrollLedgerDto> getPayrollLedgersByEmployeeWithPaging(Long employeeId, Pageable pageable) {
        log.info("Fetching payroll ledgers for employee: {} with pagination", employeeId);
        
        Page<PayrollLedger> ledgerPage = payrollLedgerRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId, pageable);
        List<PayrollLedgerDto> ledgerDtos = ledgerPage.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        
        return PageResponse.of(
            ledgerDtos,
            ledgerPage.getNumber(),
            ledgerPage.getSize(),
            ledgerPage.getTotalElements(),
            ledgerPage.getTotalPages()
        );
    }

    // Payroll Processing
    @Override
    public List<PayrollLedgerDto> processPayrollForPeriod(Long payrollPeriodId, List<Long> employeeIds) {
        log.info("Processing payroll for period: {} and employees: {}", payrollPeriodId, employeeIds);
        
        // Validate payroll period exists
        PayrollPeriod period = payrollPeriodRepository.findById(payrollPeriodId)
            .orElseThrow(() -> new PayrollPeriodException("Payroll period not found with id: " + payrollPeriodId));
        
        List<PayrollLedger> ledgers = payrollCalculationService.calculatePayrollForPeriod(payrollPeriodId, employeeIds);
        List<PayrollLedger> savedLedgers = payrollLedgerRepository.saveAll(ledgers);
        
        // Create audit records
        savedLedgers.forEach(ledger -> 
            createAuditRecord(ledger.getId(), "PROCESSED", "Payroll processed for period: " + payrollPeriodId));
        
        return savedLedgers.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "payrollLedgers", key = "#payrollLedgerId")
    public PayrollLedgerDto approvePayroll(Long payrollLedgerId, String reason) {
        log.info("Approving payroll ledger: {}", payrollLedgerId);
        
        PayrollLedger ledger = payrollLedgerRepository.findById(payrollLedgerId)
            .orElseThrow(() -> new PayrollNotFoundException("Payroll ledger not found with id: " + payrollLedgerId));
        
        if (ledger.getStatus() != PayrollLedgerStatus.CALCULATED) {
            throw new PayrollValidationException("Payroll can only be approved from CALCULATED status");
        }
        
        ledger.setStatus(PayrollLedgerStatus.APPROVED);
        ledger.setUpdatedAt(LocalDateTime.now());
        
        PayrollLedger savedLedger = payrollLedgerRepository.save(ledger);
        
        // Create audit record
        createAuditRecord(savedLedger.getId(), "APPROVED", reason != null ? reason : "Payroll approved");
        
        return convertToDto(savedLedger);
    }

    @Override
    @CacheEvict(value = "payrollLedgers", key = "#payrollLedgerId")
    public PayrollLedgerDto rejectPayroll(Long payrollLedgerId, String reason) {
        log.info("Rejecting payroll ledger: {}", payrollLedgerId);
        
        PayrollLedger ledger = payrollLedgerRepository.findById(payrollLedgerId)
            .orElseThrow(() -> new PayrollNotFoundException("Payroll ledger not found with id: " + payrollLedgerId));
        
        if (ledger.getStatus() != PayrollLedgerStatus.CALCULATED) {
            throw new PayrollValidationException("Payroll can only be rejected from CALCULATED status");
        }
        
        ledger.setStatus(PayrollLedgerStatus.REJECTED);
        ledger.setUpdatedAt(LocalDateTime.now());
        
        PayrollLedger savedLedger = payrollLedgerRepository.save(ledger);
        
        // Create audit record
        createAuditRecord(savedLedger.getId(), "REJECTED", reason != null ? reason : "Payroll rejected");
        
        return convertToDto(savedLedger);
    }

    @Override
    @CacheEvict(value = "payrollLedgers", key = "#payrollLedgerId")
    public PayrollLedgerDto markAsPaid(Long payrollLedgerId, String paymentReference) {
        log.info("Marking payroll ledger as paid: {}", payrollLedgerId);
        
        PayrollLedger ledger = payrollLedgerRepository.findById(payrollLedgerId)
            .orElseThrow(() -> new PayrollNotFoundException("Payroll ledger not found with id: " + payrollLedgerId));
        
        if (ledger.getStatus() != PayrollLedgerStatus.APPROVED) {
            throw new PayrollValidationException("Payroll can only be marked as paid from APPROVED status");
        }
        
        ledger.setStatus(PayrollLedgerStatus.PAID);
        ledger.setPaymentReference(paymentReference);
        ledger.setUpdatedAt(LocalDateTime.now());
        
        PayrollLedger savedLedger = payrollLedgerRepository.save(ledger);
        
        // Create audit record
        createAuditRecord(savedLedger.getId(), "PAID", "Payroll marked as paid. Reference: " + paymentReference);
        
        return convertToDto(savedLedger);
    }

    // Payroll Period Operations
    @Override
    public PayrollPeriodDto createPayrollPeriod(PayrollPeriodDto payrollPeriodDto) {
        log.info("Creating payroll period: {}", payrollPeriodDto.getPeriodName());
        
        PayrollPeriod period = convertToEntity(payrollPeriodDto);
        period.setCreatedAt(LocalDateTime.now());
        period.setUpdatedAt(LocalDateTime.now());
        
        PayrollPeriod savedPeriod = payrollPeriodRepository.save(period);
        return convertToDto(savedPeriod);
    }

    @Override
    public PayrollPeriodDto updatePayrollPeriod(Long id, PayrollPeriodDto payrollPeriodDto) {
        log.info("Updating payroll period: {}", id);
        
        PayrollPeriod existingPeriod = payrollPeriodRepository.findById(id)
            .orElseThrow(() -> new PayrollPeriodException("Payroll period not found with id: " + id));
        
        existingPeriod.setPeriodName(payrollPeriodDto.getPeriodName());
        existingPeriod.setStartDate(payrollPeriodDto.getStartDate());
        existingPeriod.setEndDate(payrollPeriodDto.getEndDate());
        existingPeriod.setStatus(payrollPeriodDto.getStatus());
        existingPeriod.setUpdatedAt(LocalDateTime.now());
        
        PayrollPeriod savedPeriod = payrollPeriodRepository.save(existingPeriod);
        return convertToDto(savedPeriod);
    }

    @Override
    public PayrollPeriodDto getPayrollPeriodById(Long id) {
        log.info("Fetching payroll period: {}", id);
        
        PayrollPeriod period = payrollPeriodRepository.findById(id)
            .orElseThrow(() -> new PayrollPeriodException("Payroll period not found with id: " + id));
        
        return convertToDto(period);
    }

    @Override
    public void deletePayrollPeriod(Long id) {
        log.info("Deleting payroll period: {}", id);
        
        if (!payrollPeriodRepository.existsById(id)) {
            throw new PayrollPeriodException("Payroll period not found with id: " + id);
        }
        
        payrollPeriodRepository.deleteById(id);
    }

    @Override
    public PageResponse<PayrollPeriodDto> getPayrollPeriods(Pageable pageable) {
        log.info("Fetching payroll periods with pagination");
        
        Page<PayrollPeriod> periodPage = payrollPeriodRepository.findAll(pageable);
        List<PayrollPeriodDto> periodDtos = periodPage.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        
        return PageResponse.of(
            periodDtos,
            periodPage.getNumber(),
            periodPage.getSize(),
            periodPage.getTotalElements(),
            periodPage.getTotalPages()
        );
    }

    @Override
    public List<PayrollPeriodDto> getActivePayrollPeriods() {
        log.info("Fetching active payroll periods");
        
        List<PayrollPeriod> periods = payrollPeriodRepository.findByStatus(PayrollPeriodStatus.OPEN);
        return periods.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    public PayrollPeriodDto getCurrentPayrollPeriod() {
        log.info("Fetching current payroll period");
        
        LocalDate today = LocalDate.now();
        PayrollPeriod period = payrollPeriodRepository.findCurrentPeriodForDate(today)
            .orElseThrow(() -> new PayrollPeriodException("No current payroll period found"));
        
        return convertToDto(period);
    }

    @Override
    public PayrollPeriodDto closePayrollPeriod(Long id) {
        log.info("Closing payroll period: {}", id);
        
        PayrollPeriod period = payrollPeriodRepository.findById(id)
            .orElseThrow(() -> new PayrollPeriodException("Payroll period not found with id: " + id));
        
        period.setStatus(PayrollPeriodStatus.CLOSED);
        period.setUpdatedAt(LocalDateTime.now());
        
        PayrollPeriod savedPeriod = payrollPeriodRepository.save(period);
        return convertToDto(savedPeriod);
    }

    // Salary Component Operations
    @Override
    public SalaryComponentDto createSalaryComponent(SalaryComponentDto salaryComponentDto) {
        log.info("Creating salary component: {}", salaryComponentDto.getComponentName());
        
        SalaryComponent component = convertToEntity(salaryComponentDto);
        component.setCreatedAt(LocalDateTime.now());
        component.setUpdatedAt(LocalDateTime.now());
        
        SalaryComponent savedComponent = salaryComponentRepository.save(component);
        return convertToDto(savedComponent);
    }

    @Override
    public SalaryComponentDto updateSalaryComponent(Long id, SalaryComponentDto salaryComponentDto) {
        log.info("Updating salary component: {}", id);
        
        SalaryComponent existingComponent = salaryComponentRepository.findById(id)
            .orElseThrow(() -> new PayrollNotFoundException("Salary component not found with id: " + id));
        
        existingComponent.setComponentName(salaryComponentDto.getComponentName());
        existingComponent.setComponentType(salaryComponentDto.getComponentType());
        existingComponent.setComponentType(salaryComponentDto.getComponentType());
        existingComponent.setAmount(salaryComponentDto.getAmount());
        existingComponent.setIsActive(salaryComponentDto.getIsActive());
        existingComponent.setUpdatedAt(LocalDateTime.now());
        
        SalaryComponent savedComponent = salaryComponentRepository.save(existingComponent);
        return convertToDto(savedComponent);
    }

    @Override
    public SalaryComponentDto getSalaryComponentById(Long id) {
        log.info("Fetching salary component: {}", id);
        
        SalaryComponent component = salaryComponentRepository.findById(id)
            .orElseThrow(() -> new PayrollNotFoundException("Salary component not found with id: " + id));
        
        return convertToDto(component);
    }

    @Override
    public void deleteSalaryComponent(Long id) {
        log.info("Deleting salary component: {}", id);
        
        if (!salaryComponentRepository.existsById(id)) {
            throw new PayrollNotFoundException("Salary component not found with id: " + id);
        }
        
        salaryComponentRepository.deleteById(id);
    }

    @Override
    public PageResponse<SalaryComponentDto> getSalaryComponents(Pageable pageable) {
        log.info("Fetching salary components with pagination");
        
        Page<SalaryComponent> componentPage = salaryComponentRepository.findAll(pageable);
        List<SalaryComponentDto> componentDtos = componentPage.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        
        return PageResponse.of(
            componentDtos,
            componentPage.getNumber(),
            componentPage.getSize(),
            componentPage.getTotalElements(),
            componentPage.getTotalPages()
        );
    }

    @Override
    public List<SalaryComponentDto> getActiveSalaryComponents() {
        log.info("Fetching active salary components");
        
        List<SalaryComponent> components = salaryComponentRepository.findByIsActiveTrue();
        return components.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<SalaryComponentDto> getSalaryComponentsByType(String componentType) {
        log.info("Fetching salary components by type: {}", componentType);
        
        List<SalaryComponent> components = salaryComponentRepository.findByComponentType(componentType);
        return components.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    // Reporting and Analytics
    @Override
    public PayrollSummaryDto getPayrollSummary(Long payrollPeriodId) {
        log.info("Generating payroll summary for period: {}", payrollPeriodId);
        
        // Implementation would aggregate payroll data for the period
        // This is a simplified version
        PayrollSummaryDto summary = new PayrollSummaryDto();
        summary.setPayrollPeriodId(payrollPeriodId);
        // Add actual summary calculations here
        
        return summary;
    }

    @Override
    public List<PayrollSummaryDto> getPayrollSummaryByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Generating payroll summary for date range: {} to {}", startDate, endDate);
        
        // Implementation would aggregate payroll data for the date range
        // This is a simplified version
        return List.of();
    }

    @Override
    public byte[] generatePayrollReport(PayrollReportRequest request) {
        log.info("Generating payroll report for request: {}", request);
        
        // Implementation would generate actual report (PDF, Excel, etc.)
        // This is a placeholder
        return new byte[0];
    }

    // Helper methods
    private PayrollLedgerDto convertToDto(PayrollLedger ledger) {
        PayrollLedgerDto dto = new PayrollLedgerDto();
        dto.setId(ledger.getId());
        dto.setEmployeeId(ledger.getEmployeeId());
        if (ledger.getEmployee() != null) {
            dto.setEmployeeName(ledger.getEmployee().getFullName());
            dto.setEmployeeNumber(ledger.getEmployee().getEmployeeNumber());
        }
        dto.setPayrollPeriodId(ledger.getPayrollPeriodId());
        if (ledger.getPayrollPeriod() != null) {
            dto.setPayrollPeriodName(ledger.getPayrollPeriod().getPeriodName());
        }
        dto.setBaseSalary(ledger.getBaseSalary());
        dto.setGrossPay(ledger.getGrossPay());
        dto.setTotalDeductions(ledger.getTotalDeductions());
        dto.setTotalTaxes(ledger.getTotalTaxes());
        dto.setNetPay(ledger.getNetPay());
        dto.setOvertimeHours(ledger.getOvertimeHours());
        dto.setOvertimePay(ledger.getOvertimePay());
        dto.setBonusAmount(ledger.getBonusAmount());
        dto.setStatus(ledger.getStatus());
        dto.setPaymentMethod(ledger.getPaymentMethod());
        dto.setPayDate(ledger.getPayDate());
        dto.setPaymentReference(ledger.getPaymentReference());
        dto.setNotes(ledger.getNotes());
        dto.setApprovedBy(ledger.getApprovedBy());
        // approvedByName would require a separate lookup from a user service
        dto.setApprovedAt(ledger.getApprovedAt());
        dto.setPaidBy(ledger.getPaidBy());
        // paidByName would require a separate lookup from a user service
        dto.setPaidAt(ledger.getPaidAt());
        dto.setCreatedAt(ledger.getCreatedAt());
        dto.setUpdatedAt(ledger.getUpdatedAt());
        
        // Convert components if they exist
        if (ledger.getComponents() != null) {
            List<PayrollLedgerComponentDto> componentDtos = ledger.getComponents().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            dto.setComponents(componentDtos);
        }
        
        return dto;
    }

    private PayrollPeriodDto convertToDto(PayrollPeriod period) {
        PayrollPeriodDto dto = new PayrollPeriodDto();
        dto.setId(period.getId());
        dto.setPeriodName(period.getPeriodName());
        dto.setStartDate(period.getStartDate());
        dto.setEndDate(period.getEndDate());
        dto.setStatus(period.getStatus());
        dto.setCreatedAt(period.getCreatedAt());
        dto.setUpdatedAt(period.getUpdatedAt());
        return dto;
    }

    private PayrollPeriod convertToEntity(PayrollPeriodDto dto) {
        PayrollPeriod period = new PayrollPeriod();
        period.setPeriodName(dto.getPeriodName());
        period.setStartDate(dto.getStartDate());
        period.setEndDate(dto.getEndDate());
        period.setStatus(dto.getStatus());
        return period;
    }

    private SalaryComponentDto convertToDto(SalaryComponent component) {
        SalaryComponentDto dto = new SalaryComponentDto();
        dto.setId(component.getId());
        dto.setComponentName(component.getComponentName());
        dto.setComponentType(component.getComponentType());
        dto.setAmount(component.getAmount());
        dto.setIsActive(component.getIsActive());
        dto.setCreatedAt(component.getCreatedAt());
        dto.setUpdatedAt(component.getUpdatedAt());
        return dto;
    }

    private SalaryComponent convertToEntity(SalaryComponentDto dto) {
        SalaryComponent component = new SalaryComponent();
        component.setComponentName(dto.getComponentName());
        component.setComponentType(dto.getComponentType());
        component.setAmount(dto.getAmount());
        component.setIsActive(dto.getIsActive());
        return component;
    }

    private void createAuditRecord(Long payrollLedgerId, String action, String description) {
        PayrollAudit audit = new PayrollAudit();
        audit.setPayrollLedgerId(payrollLedgerId);
        audit.setAction(action);
        audit.setReason(description);
        audit.setCreatedAt(LocalDateTime.now());
        // audit.setUserId would be set from security context
        payrollAuditRepository.save(audit);
    }
    
    private PayrollLedgerComponentDto convertToDto(PayrollLedgerComponent component) {
        PayrollLedgerComponentDto dto = new PayrollLedgerComponentDto();
        dto.setId(component.getId());
        dto.setPayrollLedgerId(component.getPayrollLedgerId());
        dto.setSalaryComponentId(component.getSalaryComponentId());
        if (component.getSalaryComponent() != null) {
            dto.setComponentName(component.getSalaryComponent().getComponentName());
            dto.setComponentType(component.getSalaryComponent().getComponentType());
        }
        dto.setAmount(component.getAmount());
        dto.setCalculationBase(component.getCalculationBase());
        dto.setPercentageApplied(component.getPercentageApplied());
        dto.setNotes(component.getNotes());
        return dto;
    }
}