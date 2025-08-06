package com.example.demo.payroll.dto;

import com.example.demo.payroll.entity.PayrollLedgerStatus;
import com.example.demo.payroll.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollLedgerDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeNumber;
    private Long payrollPeriodId;
    private String payrollPeriodName;
    private BigDecimal baseSalary;
    private BigDecimal grossPay;
    private BigDecimal totalDeductions;
    private BigDecimal totalTaxes;
    private BigDecimal netPay;
    private BigDecimal overtimeHours;
    private BigDecimal overtimePay;
    private BigDecimal bonusAmount;
    private PayrollLedgerStatus status;
    private PaymentMethod paymentMethod;
    private LocalDate payDate;
    private String paymentReference;
    private String notes;
    private Long approvedBy;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private Long paidBy;
    private String paidByName;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PayrollLedgerComponentDto> components;
}