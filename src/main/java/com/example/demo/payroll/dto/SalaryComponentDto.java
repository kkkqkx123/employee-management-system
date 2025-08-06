package com.example.demo.payroll.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryComponentDto {
    private Long id;
    
    @NotBlank(message = "Component name is required")
    private String componentName;
    
    @NotBlank(message = "Component type is required")
    private String componentType;
    
    @DecimalMin(value = "0.0", message = "Amount must be non-negative")
    private BigDecimal amount;
    
    @DecimalMin(value = "0.0", message = "Percentage must be non-negative")
    private BigDecimal percentage;
    
    private Boolean isTaxable;
    private Boolean isMandatory;
    private Integer calculationOrder;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}