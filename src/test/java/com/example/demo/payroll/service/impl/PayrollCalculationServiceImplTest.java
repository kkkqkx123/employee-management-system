package com.example.demo.payroll.service.impl;

import com.example.demo.payroll.dto.PayrollCalculationRequest;
import com.example.demo.payroll.entity.*;
import com.example.demo.payroll.repository.SalaryComponentRepository;
import com.example.demo.employee.entity.Employee;
import com.example.demo.employee.entity.PayType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PayrollCalculationServiceImpl using Mockito.
 * Tests payroll calculation logic and component processing.
 */
@ExtendWith(MockitoExtension.class)
class PayrollCalculationServiceImplTest {

    @Mock
    private SalaryComponentRepository salaryComponentRepository;

    @InjectMocks
    private PayrollCalculationServiceImpl payrollCalculationService;

    private Employee employee;
    private PayrollPeriod payrollPeriod;
    private PayrollCalculationRequest calculationRequest;
    private List<SalaryComponent> salaryComponents;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeNumber("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setSalary(BigDecimal.valueOf(60000));
        employee.setPayType(PayType.SALARY);

        payrollPeriod = new PayrollPeriod();
        payrollPeriod.setId(1L);
        payrollPeriod.setPeriodType(PayrollPeriodType.MONTHLY);
        payrollPeriod.setStartDate(LocalDate.of(2024, 1, 1));
        payrollPeriod.setEndDate(LocalDate.of(2024, 1, 31));

        calculationRequest = new PayrollCalculationRequest();
        calculationRequest.setEmployeeId(1L);
        calculationRequest.setPayrollPeriodId(1L);

        // Setup salary components
        SalaryComponent basicSalary = new SalaryComponent();
        basicSalary.setId(1L);
        basicSalary.setComponentName("Basic Salary");
        basicSalary.setComponentType("EARNING");
        basicSalary.setIsTaxable(true);
        basicSalary.setAmount(BigDecimal.valueOf(5000));

        SalaryComponent allowance = new SalaryComponent();
        allowance.setId(2L);
        allowance.setComponentName("Housing Allowance");
        allowance.setComponentType("EARNING");
        allowance.setIsTaxable(false);
        allowance.setAmount(BigDecimal.valueOf(1000));

        SalaryComponent tax = new SalaryComponent();
        tax.setId(3L);
        tax.setComponentName("Income Tax");
        tax.setComponentType("DEDUCTION");
        tax.setIsTaxable(false);
        tax.setAmount(BigDecimal.valueOf(750));

        salaryComponents = Arrays.asList(basicSalary, allowance, tax);
    }

    @Test
    void calculatePayroll_shouldCalculateCorrectGrossAndNetPay() {
        // Given
        when(salaryComponentRepository.findAllActiveOrderByCalculationOrder()).thenReturn(salaryComponents);

        // When
        PayrollLedger result = payrollCalculationService.calculatePayroll(calculationRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGrossPay()).isEqualByComparingTo(BigDecimal.valueOf(6000)); // 5000 + 1000
        assertThat(result.getNetPay()).isEqualByComparingTo(BigDecimal.valueOf(5250)); // 6000 - 750
        assertThat(result.getTotalDeductions()).isEqualByComparingTo(BigDecimal.valueOf(750));
    }

    @Test
    void calculatePayroll_shouldCreateCorrectLedgerComponents() {
        // Given
        when(salaryComponentRepository.findAllActiveOrderByCalculationOrder()).thenReturn(salaryComponents);

        // When
        PayrollLedger result = payrollCalculationService.calculatePayroll(calculationRequest);

        // Then
        assertThat(result.getComponents()).hasSize(3);

        // Check basic salary component
        PayrollLedgerComponent basicComponent = result.getComponents().stream()
                .filter(c -> c.getSalaryComponent().getComponentName().equals("Basic Salary"))
                .findFirst()
                .orElse(null);
        assertThat(basicComponent).isNotNull();
        assertThat(basicComponent.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));

        // Check housing allowance component
        PayrollLedgerComponent housingComponent = result.getComponents().stream()
                .filter(c -> c.getSalaryComponent().getComponentName().equals("Housing Allowance"))
                .findFirst()
                .orElse(null);
        assertThat(housingComponent).isNotNull();
        assertThat(housingComponent.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));

        // Check tax component
        PayrollLedgerComponent taxComponent = result.getComponents().stream()
                .filter(c -> c.getSalaryComponent().getComponentName().equals("Income Tax"))
                .findFirst()
                .orElse(null);
        assertThat(taxComponent).isNotNull();
        assertThat(taxComponent.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(750));
    }

    @Test
    void calculatePayroll_shouldHandleNoComponents() {
        // Given
        when(salaryComponentRepository.findAllActiveOrderByCalculationOrder()).thenReturn(Arrays.asList());

        // When
        PayrollLedger result = payrollCalculationService.calculatePayroll(calculationRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGrossPay()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getNetPay()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalDeductions()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getComponents()).isEmpty();
    }

    @Test
    void calculatePayroll_shouldHandleOnlyEarnings() {
        // Given
        List<SalaryComponent> earningsOnly = Arrays.asList(
                salaryComponents.get(0), // Basic salary
                salaryComponents.get(1)  // Housing allowance
        );
        when(salaryComponentRepository.findAllActiveOrderByCalculationOrder()).thenReturn(earningsOnly);

        // When
        PayrollLedger result = payrollCalculationService.calculatePayroll(calculationRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGrossPay()).isEqualByComparingTo(BigDecimal.valueOf(6000));
        assertThat(result.getNetPay()).isEqualByComparingTo(BigDecimal.valueOf(6000)); // No deductions
        assertThat(result.getTotalDeductions()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculatePayroll_shouldHandleOnlyDeductions() {
        // Given
        List<SalaryComponent> deductionsOnly = Arrays.asList(
                salaryComponents.get(2) // Tax
        );
        when(salaryComponentRepository.findAllActiveOrderByCalculationOrder()).thenReturn(deductionsOnly);

        // When
        PayrollLedger result = payrollCalculationService.calculatePayroll(calculationRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGrossPay()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getNetPay()).isEqualByComparingTo(BigDecimal.valueOf(-750)); // Negative net pay
        assertThat(result.getTotalDeductions()).isEqualByComparingTo(BigDecimal.valueOf(750));
    }

    @Test
    void calculateTaxableAmount_shouldCalculateCorrectTaxableAmount() {
        // Given
        when(salaryComponentRepository.findAllActiveOrderByCalculationOrder()).thenReturn(salaryComponents);

        // When
        PayrollLedger result = payrollCalculationService.calculatePayroll(calculationRequest);

        // Then
        // Only basic salary (5000) is taxable, housing allowance (1000) is not taxable
        BigDecimal expectedTaxableAmount = BigDecimal.valueOf(5000);
        assertThat(result.getBaseSalary()).isEqualByComparingTo(expectedTaxableAmount);
    }

    @Test
    void calculatePayroll_shouldSetCorrectStatus() {
        // Given
        when(salaryComponentRepository.findAllActiveOrderByCalculationOrder()).thenReturn(salaryComponents);

        // When
        PayrollLedger result = payrollCalculationService.calculatePayroll(calculationRequest);

        // Then
        assertThat(result.getStatus()).isEqualTo(PayrollLedgerStatus.PENDING);
    }

    @Test
    void calculatePayroll_shouldSetCorrectPaymentMethod() {
        // Given
        when(salaryComponentRepository.findAllActiveOrderByCalculationOrder()).thenReturn(salaryComponents);

        // When
        PayrollLedger result = payrollCalculationService.calculatePayroll(calculationRequest);

        // Then
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
    }
}