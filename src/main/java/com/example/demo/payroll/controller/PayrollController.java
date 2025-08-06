package com.example.demo.payroll.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.common.dto.PageResponse;
import com.example.demo.payroll.dto.*;
import com.example.demo.payroll.entity.PayrollLedgerStatus;
import com.example.demo.payroll.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Payroll Management
 * Provides endpoints for payroll ledgers, periods, salary components, and reporting
 */
@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payroll Management", description = "APIs for managing payroll operations")
@SecurityRequirement(name = "bearerAuth")
public class PayrollController {

    private final PayrollService payrollService;

    // ===== PAYROLL LEDGER ENDPOINTS =====

    @Operation(summary = "Get all payroll ledgers with pagination and filtering")
    @GetMapping("/ledgers")
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    public ResponseEntity<ApiResponse<PageResponse<PayrollLedgerDto>>> getPayrollLedgers(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long payrollPeriodId,
            @RequestParam(required = false) PayrollLedgerStatus status) {
        
        log.info("Fetching payroll ledgers with filters - employeeId: {}, periodId: {}, status: {}", 
                employeeId, payrollPeriodId, status);
        
        PageResponse<PayrollLedgerDto> ledgers;
        
        if (employeeId != null) {
            ledgers = payrollService.getPayrollLedgersByEmployeeWithPaging(employeeId, pageable);
        } else {
            ledgers = payrollService.getPayrollLedgers(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(ledgers));
    }

    @Operation(summary = "Get payroll ledger by ID")
    @GetMapping("/ledgers/{id}")
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    public ResponseEntity<ApiResponse<PayrollLedgerDto>> getPayrollLedgerById(
            @Parameter(description = "Payroll ledger ID") @PathVariable Long id) {
        
        log.info("Fetching payroll ledger with ID: {}", id);
        PayrollLedgerDto ledger = payrollService.getPayrollLedgerById(id);
        return ResponseEntity.ok(ApiResponse.success(ledger));
    }

    @Operation(summary = "Get payroll ledgers by employee")
    @GetMapping("/ledgers/employee/{employeeId}")
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    public ResponseEntity<ApiResponse<List<PayrollLedgerDto>>> getPayrollLedgersByEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {
        
        log.info("Fetching payroll ledgers for employee: {}", employeeId);
        List<PayrollLedgerDto> ledgers = payrollService.getPayrollLedgersByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success(ledgers));
    }

    @Operation(summary = "Get payroll ledgers by period")
    @GetMapping("/ledgers/period/{periodId}")
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    public ResponseEntity<ApiResponse<List<PayrollLedgerDto>>> getPayrollLedgersByPeriod(
            @Parameter(description = "Payroll period ID") @PathVariable Long periodId) {
        
        log.info("Fetching payroll ledgers for period: {}", periodId);
        List<PayrollLedgerDto> ledgers = payrollService.getPayrollLedgersByPeriod(periodId);
        return ResponseEntity.ok(ApiResponse.success(ledgers));
    }

    @Operation(summary = "Get payroll ledgers by status")
    @GetMapping("/ledgers/status/{status}")
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    public ResponseEntity<ApiResponse<List<PayrollLedgerDto>>> getPayrollLedgersByStatus(
            @Parameter(description = "Payroll status") @PathVariable PayrollLedgerStatus status) {
        
        log.info("Fetching payroll ledgers with status: {}", status);
        List<PayrollLedgerDto> ledgers = payrollService.getPayrollLedgersByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(ledgers));
    }

    @Operation(summary = "Create new payroll ledger")
    @PostMapping("/ledgers")
    @PreAuthorize("hasAuthority('PAYROLL_CREATE')")
    public ResponseEntity<ApiResponse<PayrollLedgerDto>> createPayrollLedger(
            @Valid @RequestBody PayrollCalculationRequest request) {
        
        log.info("Creating payroll ledger for employee: {}, period: {}", 
                request.getEmployeeId(), request.getPayrollPeriodId());
        
        PayrollLedgerDto ledger = payrollService.createPayrollLedger(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ledger, "Payroll ledger created successfully"));
    }

    @Operation(summary = "Update payroll ledger")
    @PutMapping("/ledgers/{id}")
    @PreAuthorize("hasAuthority('PAYROLL_UPDATE')")
    public ResponseEntity<ApiResponse<PayrollLedgerDto>> updatePayrollLedger(
            @Parameter(description = "Payroll ledger ID") @PathVariable Long id,
            @Valid @RequestBody PayrollLedgerDto payrollLedgerDto) {
        
        log.info("Updating payroll ledger with ID: {}", id);
        PayrollLedgerDto updatedLedger = payrollService.updatePayrollLedger(id, payrollLedgerDto);
        return ResponseEntity.ok(ApiResponse.success(updatedLedger, "Payroll ledger updated successfully"));
    }

    @Operation(summary = "Delete payroll ledger")
    @DeleteMapping("/ledgers/{id}")
    @PreAuthorize("hasAuthority('PAYROLL_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deletePayrollLedger(
            @Parameter(description = "Payroll ledger ID") @PathVariable Long id) {
        
        log.info("Deleting payroll ledger with ID: {}", id);
        payrollService.deletePayrollLedger(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Payroll ledger deleted successfully"));
    }

    // ===== PAYROLL CALCULATION ENDPOINTS =====

    @Operation(summary = "Calculate payroll for single employee")
    @PostMapping("/calculate")
    @PreAuthorize("hasAuthority('PAYROLL_CALCULATE')")
    public ResponseEntity<ApiResponse<PayrollLedgerDto>> calculatePayroll(
            @Valid @RequestBody PayrollCalculationRequest request) {
        
        log.info("Calculating payroll for employee: {}, period: {}", 
                request.getEmployeeId(), request.getPayrollPeriodId());
        
        PayrollLedgerDto ledger = payrollService.createPayrollLedger(request);
        return ResponseEntity.ok(ApiResponse.success(ledger, "Payroll calculated successfully"));
    }

    @Operation(summary = "Process payroll for entire period")
    @PostMapping("/periods/{periodId}/process")
    @PreAuthorize("hasAuthority('PAYROLL_PROCESS')")
    public ResponseEntity<ApiResponse<List<PayrollLedgerDto>>> processPayrollForPeriod(
            @Parameter(description = "Payroll period ID") @PathVariable Long periodId,
            @RequestBody(required = false) List<Long> employeeIds) {
        
        log.info("Processing payroll for period: {}, employees: {}", periodId, employeeIds);
        List<PayrollLedgerDto> ledgers = payrollService.processPayrollForPeriod(periodId, employeeIds);
        return ResponseEntity.ok(ApiResponse.success(ledgers, "Payroll processed successfully"));
    }

    // ===== PAYROLL APPROVAL ENDPOINTS =====

    @Operation(summary = "Approve payroll ledger")
    @PostMapping("/ledgers/{id}/approve")
    @PreAuthorize("hasAuthority('PAYROLL_APPROVE')")
    public ResponseEntity<ApiResponse<PayrollLedgerDto>> approvePayroll(
            @Parameter(description = "Payroll ledger ID") @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        
        log.info("Approving payroll ledger with ID: {}, reason: {}", id, reason);
        PayrollLedgerDto ledger = payrollService.approvePayroll(id, reason);
        return ResponseEntity.ok(ApiResponse.success(ledger, "Payroll approved successfully"));
    }

    @Operation(summary = "Reject payroll ledger")
    @PostMapping("/ledgers/{id}/reject")
    @PreAuthorize("hasAuthority('PAYROLL_APPROVE')")
    public ResponseEntity<ApiResponse<PayrollLedgerDto>> rejectPayroll(
            @Parameter(description = "Payroll ledger ID") @PathVariable Long id,
            @RequestParam String reason) {
        
        log.info("Rejecting payroll ledger with ID: {}, reason: {}", id, reason);
        PayrollLedgerDto ledger = payrollService.rejectPayroll(id, reason);
        return ResponseEntity.ok(ApiResponse.success(ledger, "Payroll rejected successfully"));
    }

    @Operation(summary = "Mark payroll as paid")
    @PostMapping("/ledgers/{id}/paid")
    @PreAuthorize("hasAuthority('PAYROLL_PAY')")
    public ResponseEntity<ApiResponse<PayrollLedgerDto>> markAsPaid(
            @Parameter(description = "Payroll ledger ID") @PathVariable Long id,
            @RequestParam String paymentReference) {
        
        log.info("Marking payroll ledger as paid with ID: {}, reference: {}", id, paymentReference);
        PayrollLedgerDto ledger = payrollService.markAsPaid(id, paymentReference);
        return ResponseEntity.ok(ApiResponse.success(ledger, "Payroll marked as paid successfully"));
    }

    // ===== PAYROLL PERIOD ENDPOINTS =====

    @Operation(summary = "Get all payroll periods")
    @GetMapping("/periods")
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    public ResponseEntity<ApiResponse<PageResponse<PayrollPeriodDto>>> getPayrollPeriods(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Fetching payroll periods");
        PageResponse<PayrollPeriodDto> periods = payrollService.getPayrollPeriods(pageable);
        return ResponseEntity.ok(ApiResponse.success(periods));
    }

    @Operation(summary = "Get active payroll periods")
    @GetMapping("/periods/active")
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    public ResponseEntity<ApiResponse<List<PayrollPeriodDto>>> getActivePayrollPeriods() {
        
        log.info("Fetching active payroll periods");
        List<PayrollPeriodDto> periods = payrollService.getActivePayrollPeriods();
        return ResponseEntity.ok(ApiResponse.success(periods));
    }

    @Operation(summary = "Get current payroll period")
    @GetMapping("/periods/current")
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    public ResponseEntity<ApiResponse<PayrollPeriodDto>> getCurrentPayrollPeriod() {
        
        log.info("Fetching current payroll period");
        PayrollPeriodDto period = payrollService.getCurrentPayrollPeriod();
        return ResponseEntity.ok(ApiResponse.success(period));
    }

    @Operation(summary = "Get payroll period by ID")
    @GetMapping("/periods/{id}")
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    public ResponseEntity<ApiResponse<PayrollPeriodDto>> getPayrollPeriodById(
            @Parameter(description = "Payroll period ID") @PathVariable Long id) {
        
        log.info("Fetching payroll period with ID: {}", id);
        PayrollPeriodDto period = payrollService.getPayrollPeriodById(id);
        return ResponseEntity.ok(ApiResponse.success(period));
    }

    @Operation(summary = "Create new payroll period")
    @PostMapping("/periods")
    @PreAuthorize("hasAuthority('PAYROLL_ADMIN')")
    public ResponseEntity<ApiResponse<PayrollPeriodDto>> createPayrollPeriod(
            @Valid @RequestBody PayrollPeriodDto payrollPeriodDto) {
        
        log.info("Creating payroll period: {}", payrollPeriodDto.getPeriodName());
        PayrollPeriodDto period = payrollService.createPayrollPeriod(payrollPeriodDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(period, "Payroll period created successfully"));
    }

    @Operation(summary = "Update payroll period")
    @PutMapping("/periods/{id}")
    @PreAuthorize("hasAuthority('PAYROLL_ADMIN')")
    public ResponseEntity<ApiResponse<PayrollPeriodDto>> updatePayrollPeriod(
            @Parameter(description = "Payroll period ID") @PathVariable Long id,
            @Valid @RequestBody PayrollPeriodDto payrollPeriodDto) {
        
        log.info("Updating payroll period with ID: {}", id);
        PayrollPeriodDto period = payrollService.updatePayrollPeriod(id, payrollPeriodDto);
        return ResponseEntity.ok(ApiResponse.success(period, "Payroll period updated successfully"));
    }

    @Operation(summary = "Close payroll period")
    @PostMapping("/periods/{id}/close")
    @PreAuthorize("hasAuthority('PAYROLL_ADMIN')")
    public ResponseEntity<ApiResponse<PayrollPeriodDto>> closePayrollPeriod(
            @Parameter(description = "Payroll period ID") @PathVariable Long id) {
        
        log.info("Closing payroll period with ID: {}", id);
        PayrollPeriodDto period = payrollService.closePayrollPeriod(id);
        return ResponseEntity.ok(ApiResponse.success(period, "Payroll period closed successfully"));
    }

    @Operation(summary = "Delete payroll period")
    @DeleteMapping("/periods/{id}")
    @PreAuthorize("hasAuthority('PAYROLL_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePayrollPeriod(
            @Parameter(description = "Payroll period ID") @PathVariable Long id) {
        
        log.info("Deleting payroll period with ID: {}", id);
        payrollService.deletePayrollPeriod(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Payroll period deleted successfully"));
    }

    // ===== SALARY COMPONENT ENDPOINTS =====

    @Operation(summary = "Get all salary components")
    @GetMapping("/components")
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    public ResponseEntity<ApiResponse<PageResponse<SalaryComponentDto>>> getSalaryComponents(
            @PageableDefault(size = 50) Pageable pageable,
            @RequestParam(required = false) String componentType) {
        
        log.info("Fetching salary components with type filter: {}", componentType);
        PageResponse<SalaryComponentDto> components;
        
        if (componentType != null) {
            List<SalaryComponentDto> componentList = payrollService.getSalaryComponentsByType(componentType);
            components = new PageResponse<>(componentList, pageable, componentList.size());
        } else {
            components = payrollService.getSalaryComponents(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(components));
    }

    @Operation(summary = "Get active salary components")
    @GetMapping("/components/active")
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    public ResponseEntity<ApiResponse<List<SalaryComponentDto>>> getActiveSalaryComponents() {
        
        log.info("Fetching active salary components");
        List<SalaryComponentDto> components = payrollService.getActiveSalaryComponents();
        return ResponseEntity.ok(ApiResponse.success(components));
    }

    @Operation(summary = "Get salary component by ID")
    @GetMapping("/components/{id}")
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    public ResponseEntity<ApiResponse<SalaryComponentDto>> getSalaryComponentById(
            @Parameter(description = "Salary component ID") @PathVariable Long id) {
        
        log.info("Fetching salary component with ID: {}", id);
        SalaryComponentDto component = payrollService.getSalaryComponentById(id);
        return ResponseEntity.ok(ApiResponse.success(component));
    }

    @Operation(summary = "Create new salary component")
    @PostMapping("/components")
    @PreAuthorize("hasAuthority('PAYROLL_ADMIN')")
    public ResponseEntity<ApiResponse<SalaryComponentDto>> createSalaryComponent(
            @Valid @RequestBody SalaryComponentDto salaryComponentDto) {
        
        log.info("Creating salary component: {}", salaryComponentDto.getComponentName());
        SalaryComponentDto component = payrollService.createSalaryComponent(salaryComponentDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(component, "Salary component created successfully"));
    }

    @Operation(summary = "Update salary component")
    @PutMapping("/components/{id}")
    @PreAuthorize("hasAuthority('PAYROLL_ADMIN')")
    public ResponseEntity<ApiResponse<SalaryComponentDto>> updateSalaryComponent(
            @Parameter(description = "Salary component ID") @PathVariable Long id,
            @Valid @RequestBody SalaryComponentDto salaryComponentDto) {
        
        log.info("Updating salary component with ID: {}", id);
        SalaryComponentDto component = payrollService.updateSalaryComponent(id, salaryComponentDto);
        return ResponseEntity.ok(ApiResponse.success(component, "Salary component updated successfully"));
    }

    @Operation(summary = "Delete salary component")
    @DeleteMapping("/components/{id}")
    @PreAuthorize("hasAuthority('PAYROLL_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSalaryComponent(
            @Parameter(description = "Salary component ID") @PathVariable Long id) {
        
        log.info("Deleting salary component with ID: {}", id);
        payrollService.deleteSalaryComponent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Salary component deleted successfully"));
    }

    // ===== REPORTING ENDPOINTS =====

    @Operation(summary = "Get payroll summary for period")
    @GetMapping("/reports/summary/period/{periodId}")
    @PreAuthorize("hasAuthority('PAYROLL_REPORT')")
    public ResponseEntity<ApiResponse<PayrollSummaryDto>> getPayrollSummary(
            @Parameter(description = "Payroll period ID") @PathVariable Long periodId) {
        
        log.info("Generating payroll summary for period: {}", periodId);
        PayrollSummaryDto summary = payrollService.getPayrollSummary(periodId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @Operation(summary = "Get payroll summary by date range")
    @GetMapping("/reports/summary/daterange")
    @PreAuthorize("hasAuthority('PAYROLL_REPORT')")
    public ResponseEntity<ApiResponse<List<PayrollSummaryDto>>> getPayrollSummaryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Generating payroll summary for date range: {} to {}", startDate, endDate);
        List<PayrollSummaryDto> summaries = payrollService.getPayrollSummaryByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summaries));
    }

    @Operation(summary = "Generate payroll report")
    @PostMapping("/reports/generate")
    @PreAuthorize("hasAuthority('PAYROLL_REPORT')")
    public ResponseEntity<byte[]> generatePayrollReport(
            @Valid @RequestBody PayrollReportRequest request) {
        
        log.info("Generating payroll report for request: {}", request);
        byte[] reportData = payrollService.generatePayrollReport(request);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "payroll_report.pdf");
        headers.setContentLength(reportData.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(reportData);
    }

    // ===== AUDIT ENDPOINTS =====

    @Operation(summary = "Get payroll audit history")
    @GetMapping("/ledgers/{id}/audit")
    @PreAuthorize("hasAuthority('PAYROLL_AUDIT')")
    public ResponseEntity<ApiResponse<List<Object>>> getPayrollAuditHistory(
            @Parameter(description = "Payroll ledger ID") @PathVariable Long id) {
        
        log.info("Fetching audit history for payroll ledger: {}", id);
        // This would be implemented in the service layer
        return ResponseEntity.ok(ApiResponse.success(List.of(), "Audit history retrieved successfully"));
    }
}