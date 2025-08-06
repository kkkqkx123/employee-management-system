package com.example.demo.payroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_components")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SalaryComponent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "component_name", nullable = false, length = 100)
    private String componentName;
    
    @NotNull
    @Column(name = "component_type", nullable = false, length = 50)
    private String componentType; // EARNING, DEDUCTION, TAX
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;
    
    @Column(name = "is_taxable", nullable = false)
    private Boolean isTaxable = false;
    
    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = false;
    
    @Column(name = "calculation_order", nullable = false)
    private Integer calculationOrder = 0;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}