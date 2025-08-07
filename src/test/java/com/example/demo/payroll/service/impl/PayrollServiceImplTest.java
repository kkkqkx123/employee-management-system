package com.example.demo.payroll.service.impl;

import com.example.demo.payroll.dto.PayrollCalculationRequest;
import com.example.demo.payroll.dto.PayrollLedgerDto;
import com.example.demo.payroll.dto.PayrollPeriodDto;
import com.example.demo.payroll.entity.*;
import com.example.demo.payroll.exception.PayrollNotFoundException;
import com.example.demo.payroll.exception.PayrollPeriodException;
import com.example.demo.payroll.exception.PayrollValidationException;
import com.example.demo.payroll.repository.PayrollLedgerRepository;
import com.example.demo.payroll.repository.PayrollPeriodRepository;
import com.example.demo.payroll.service.PayrollCalculationService;
import com.example.demo.employee.entity.Employee;
import com.example.demo.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PayrollServiceImpl using Mockito.
 * Tests business logic, validation, and exception handling.
 */
@ExtendWith(MockitoExtension.class)
class PayrollServiceImplTest {

    @Mock
    private PayrollLedgerRepository payrollLedgerRepository;

    @Mock
    private PayrollPeriodRepository payrollPeriodRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PayrollCalculationService payrollCalculationService;

    @InjectMocks
    private PayrollServiceImpl payrollService;

    private PayrollPeriod payrollPeriod;
    private PayrollLedger payrollLedger;
    private Employee employee;
    private PayrollCalculationRequest calculationRequest;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeNumber("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");

        payrollPeriod = new PayrollPeriod();
        payrollPeriod.setId(1L);
        payrollPeriod.setPeriodType(PayrollPeriodType.MONTHLY);
        payrollPeriod.setStartDate(LocalDate.of(2024, 1, 1));
        payrollPeriod.setEndDate(LocalDate.of(2024, 1, 31));
        payrollPeriod.setStatus(PayrollPeriodStatus.OPEN);

        payrollLedger = new PayrollLedger();
        payrollLedger.setId(1L);
        payrollLedger.setEmployee(employee);
        payrollLedger.setPayrollPeriod(payrollPeriod);
        payrollLedger.setGrossPay(BigDecimal.valueOf(5000));
        payrollLedger.setNetPay(BigDecimal.valueOf(4000));
        payrollLedger.setStatus(PayrollLedgerStatus.PENDING);

        calculationRequest = new PayrollCalculationRequest();
        calculationRequest.setEmployeeId(1L);
        calculationRequest.setPayrollPeriodId(1L);
    }

    @Test
    void createPayrollPeriod_shouldCreatePeriodSuccessfully() {
        // Given
        PayrollPeriodDto payrollPeriodDto = new PayrollPeriodDto();
        payrollPeriodDto.setPeriodName("Feb 2024");
        payrollPeriodDto.setStartDate(LocalDate.of(2024, 2, 1));
        payrollPeriodDto.setEndDate(LocalDate.of(2024, 2, 29));
        payrollPeriodDto.setPeriodType(PayrollPeriodType.MONTHLY);
        payrollPeriodDto.setStatus(PayrollPeriodStatus.OPEN);

        when(payrollPeriodRepository.existsByPeriodNameIgnoreCase("Feb 2024")).thenReturn(false);
        when(payrollPeriodRepository.save(any(PayrollPeriod.class))).thenReturn(payrollPeriod);

        // When
        PayrollPeriodDto result = payrollService.createPayrollPeriod(payrollPeriodDto);

        // Then
        assertThat(result).isNotNull();
        verify(payrollPeriodRepository).save(any(PayrollPeriod.class));
    }

    @Test
    void createPayrollPeriod_shouldThrowExceptionWhenPeriodExists() {
        // Given
        PayrollPeriodDto payrollPeriodDto = new PayrollPeriodDto();
        payrollPeriodDto.setPeriodName("Jan 2024");
        when(payrollPeriodRepository.existsByPeriodNameIgnoreCase("Jan 2024")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> payrollService.createPayrollPeriod(payrollPeriodDto))
                .isInstanceOf(PayrollPeriodException.class)
                .hasMessageContaining("already exists");

        verify(payrollPeriodRepository, never()).save(any(PayrollPeriod.class));
    }

    @Test
    void createPayrollLedger_shouldCalculateAndCreateSuccessfully() {
        // Given
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(payrollCalculationService.calculatePayroll(calculationRequest)).thenReturn(payrollLedger);
        when(payrollLedgerRepository.save(any(PayrollLedger.class))).thenReturn(payrollLedger);

        // When
        PayrollLedgerDto result = payrollService.createPayrollLedger(calculationRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGrossPay()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        verify(payrollLedgerRepository).save(any(PayrollLedger.class));
    }

    @Test
    void createPayrollLedger_shouldThrowExceptionWhenEmployeeNotFound() {
        // Given
        when(employeeRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> payrollService.createPayrollLedger(calculationRequest))
                .isInstanceOf(PayrollNotFoundException.class)
                .hasMessageContaining("Employee not found");

        verify(payrollLedgerRepository, never()).save(any(PayrollLedger.class));
    }

    @Test
    void createPayrollLedger_shouldThrowExceptionWhenPayrollPeriodNotFound() {
        // Given
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> payrollService.createPayrollLedger(calculationRequest))
                .isInstanceOf(PayrollPeriodException.class)
                .hasMessageContaining("Payroll period not found");

        verify(payrollLedgerRepository, never()).save(any(PayrollLedger.class));
    }

    @Test
    void createPayrollLedger_shouldThrowExceptionWhenPayrollAlreadyExists() {
        // Given
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(payrollPeriod));
        when(payrollLedgerRepository.findByEmployeeIdAndPayrollPeriodId(1L, 1L)).thenReturn(Optional.of(payrollLedger));

        // When & Then
        assertThatThrownBy(() -> payrollService.createPayrollLedger(calculationRequest))
                .isInstanceOf(PayrollValidationException.class)
                .hasMessageContaining("Payroll already exists");

        verify(payrollLedgerRepository, never()).save(any(PayrollLedger.class));
    }

    @Test
    void getPayrollLedgerById_shouldReturnLedgerWhenExists() {
        // Given
        when(payrollLedgerRepository.findById(1L)).thenReturn(Optional.of(payrollLedger));

        // When
        PayrollLedgerDto result = payrollService.getPayrollLedgerById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(payrollLedger.getId());
        assertThat(result.getGrossPay()).isEqualTo(payrollLedger.getGrossPay());
    }

    @Test
    void getPayrollLedgerById_shouldThrowExceptionWhenNotExists() {
        // Given
        when(payrollLedgerRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> payrollService.getPayrollLedgerById(1L))
                .isInstanceOf(PayrollNotFoundException.class)
                .hasMessageContaining("Payroll ledger not found with id: 1");
    }

    @Test
    void approvePayroll_shouldApprovePayrollSuccessfully() {
        // Given
        payrollLedger.setStatus(PayrollLedgerStatus.CALCULATED);
        when(payrollLedgerRepository.findById(1L)).thenReturn(Optional.of(payrollLedger));
        when(payrollLedgerRepository.save(any(PayrollLedger.class))).thenReturn(payrollLedger);

        // When
        payrollService.approvePayroll(1L, "Approved");

        // Then
        verify(payrollLedgerRepository).save(argThat(ledger ->
                ledger.getStatus() == PayrollLedgerStatus.APPROVED));
    }

    @Test
    void approvePayroll_shouldThrowExceptionWhenNotCalculated() {
        // Given
        payrollLedger.setStatus(PayrollLedgerStatus.PENDING);
        when(payrollLedgerRepository.findById(1L)).thenReturn(Optional.of(payrollLedger));

        // When & Then
        assertThatThrownBy(() -> payrollService.approvePayroll(1L, "Test"))
                .isInstanceOf(PayrollValidationException.class)
                .hasMessageContaining("Payroll can only be approved from CALCULATED status");

        verify(payrollLedgerRepository, never()).save(any(PayrollLedger.class));
    }

    @Test
    void markAsPaid_shouldMarkPayrollAsPaidSuccessfully() {
        // Given
        payrollLedger.setStatus(PayrollLedgerStatus.APPROVED);
        when(payrollLedgerRepository.findById(1L)).thenReturn(Optional.of(payrollLedger));
        when(payrollLedgerRepository.save(any(PayrollLedger.class))).thenReturn(payrollLedger);

        // When
        payrollService.markAsPaid(1L, "REF123");

        // Then
        verify(payrollLedgerRepository).save(argThat(ledger ->
                ledger.getStatus() == PayrollLedgerStatus.PAID && ledger.getPaymentReference().equals("REF123")));
    }

    @Test
    void markAsPaid_shouldThrowExceptionWhenNotApproved() {
        // Given
        payrollLedger.setStatus(PayrollLedgerStatus.CALCULATED);
        when(payrollLedgerRepository.findById(1L)).thenReturn(Optional.of(payrollLedger));

        // When & Then
        assertThatThrownBy(() -> payrollService.markAsPaid(1L, "REF123"))
                .isInstanceOf(PayrollValidationException.class)
                .hasMessageContaining("Payroll can only be marked as paid from APPROVED status");

        verify(payrollLedgerRepository, never()).save(any(PayrollLedger.class));
    }

    @Test
    void getPayrollsByPeriod_shouldReturnPayrollsForPeriod() {
        // Given
        List<PayrollLedger> ledgers = Arrays.asList(payrollLedger);
        when(payrollLedgerRepository.findByPayrollPeriodId(1L)).thenReturn(ledgers);

        // When
        List<PayrollLedgerDto> result = payrollService.getPayrollLedgersByPeriod(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(payrollLedger.getId());
    }

    @Test
    void getPayrollsByEmployee_shouldReturnPayrollsForEmployee() {
        // Given
        List<PayrollLedger> ledgers = Arrays.asList(payrollLedger);
        when(payrollLedgerRepository.findByEmployeeId(1L)).thenReturn(ledgers);

        // When
        List<PayrollLedgerDto> result = payrollService.getPayrollLedgersByEmployee(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(payrollLedger.getId());
    }

    @Test
    void closePayrollPeriod_shouldClosePeriodSuccessfully() {
        // Given
        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(payrollPeriod));
        when(payrollPeriodRepository.save(any(PayrollPeriod.class))).thenReturn(payrollPeriod);

        // When
        payrollService.closePayrollPeriod(1L);

        // Then
        verify(payrollPeriodRepository).save(argThat(period ->
                period.getStatus() == PayrollPeriodStatus.CLOSED));
    }

    @Test
    void closePayrollPeriod_shouldThrowExceptionWhenNotAllPayrollsProcessed() {
        // Given
        payrollLedger.setStatus(PayrollLedgerStatus.APPROVED);
        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(payrollPeriod));
        when(payrollLedgerRepository.findByPayrollPeriodId(1L)).thenReturn(List.of(payrollLedger));

        // When & Then
        assertThatThrownBy(() -> payrollService.closePayrollPeriod(1L))
                .isInstanceOf(PayrollPeriodException.class)
                .hasMessageContaining("Cannot close period with unprocessed payrolls");

        verify(payrollPeriodRepository, never()).save(any(PayrollPeriod.class));
    }
}