package com.example.demo.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollLedgerComponentDto {
    private Long id;
    private Long payrollLedgerId;
    private Long salaryComponentId;
    private String componentName;
    private String componentType;
    private BigDecimal amount;
    private BigDecimal calculationBase;
    private BigDecimal percentageApplied;
    private String notes;
}