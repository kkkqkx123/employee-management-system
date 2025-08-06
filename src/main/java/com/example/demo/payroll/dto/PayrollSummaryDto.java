package com.example.demo.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollSummaryDto {
    private Long payrollPeriodId;
    private String periodName;
    private Long totalEmployees;
    private Long processedEmployees;
    private Long pendingEmployees;
    private Long approvedEmployees;
    private Long paidEmployees;
    private BigDecimal totalGrossPay;
    private BigDecimal totalDeductions;
    private BigDecimal totalTaxes;
    private BigDecimal totalNetPay;
    private BigDecimal averageNetPay;
    private BigDecimal totalOvertimePay;
    private BigDecimal totalBonuses;
}