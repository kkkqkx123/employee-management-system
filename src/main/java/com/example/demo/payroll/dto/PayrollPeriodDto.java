package com.example.demo.payroll.dto;

import com.example.demo.payroll.entity.PayrollPeriodStatus;
import com.example.demo.payroll.entity.PayrollPeriodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollPeriodDto {
    private Long id;
    
    @NotBlank(message = "Period name is required")
    private String periodName;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    @NotNull(message = "Period type is required")
    private PayrollPeriodType periodType;
    
    private PayrollPeriodStatus status;
    private LocalDate payDate;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long employeeCount;
    private Long processedCount;
}