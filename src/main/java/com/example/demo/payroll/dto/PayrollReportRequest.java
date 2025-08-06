package com.example.demo.payroll.dto;

import com.example.demo.payroll.entity.PayrollLedgerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollReportRequest {
    private Long payrollPeriodId;
    private List<Long> employeeIds;
    private List<Long> departmentIds;
    private PayrollLedgerStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reportType; // SUMMARY, DETAILED, TAX_REPORT, DEPARTMENT_SUMMARY
    private String format; // PDF, EXCEL, CSV
}