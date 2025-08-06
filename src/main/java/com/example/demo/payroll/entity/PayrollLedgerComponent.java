package com.example.demo.payroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_ledger_components", indexes = {
    @Index(name = "idx_payroll_ledger_component_ledger_id", columnList = "payroll_ledger_id"),
    @Index(name = "idx_payroll_ledger_component_salary_component_id", columnList = "salary_component_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PayrollLedgerComponent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "payroll_ledger_id", nullable = false)
    private Long payrollLedgerId;
    
    @NotNull
    @Column(name = "salary_component_id", nullable = false)
    private Long salaryComponentId;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "calculation_base", precision = 15, scale = 2)
    private BigDecimal calculationBase;
    
    @Column(name = "percentage_applied", precision = 5, scale = 2)
    private BigDecimal percentageApplied;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_ledger_id", insertable = false, updatable = false)
    private PayrollLedger payrollLedger;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_component_id", insertable = false, updatable = false)
    private SalaryComponent salaryComponent;
}