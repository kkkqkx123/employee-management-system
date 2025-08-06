package com.example.demo.payroll.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollCalculationRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    
    @NotNull(message = "Payroll period ID is required")
    private Long payrollPeriodId;
    
    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", message = "Base salary must be non-negative")
    private BigDecimal baseSalary;
    
    @DecimalMin(value = "0.0", message = "Overtime hours must be non-negative")
    private BigDecimal overtimeHours;
    
    @DecimalMin(value = "0.0", message = "Bonus amount must be non-negative")
    private BigDecimal bonusAmount;
    
    @Valid
    private List<ComponentOverride> componentOverrides;
    
    private String notes;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentOverride {
        @NotNull(message = "Salary component ID is required")
        private Long salaryComponentId;
        
        @NotNull(message = "Override amount is required")
        @DecimalMin(value = "0.0", message = "Override amount must be non-negative")
        private BigDecimal amount;
        
        private String reason;
    }
}