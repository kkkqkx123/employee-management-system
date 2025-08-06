package com.example.demo.payroll.entity;

import com.example.demo.employee.entity.Employee;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payroll_ledgers", indexes = {
    @Index(name = "idx_payroll_employee_id", columnList = "employee_id"),
    @Index(name = "idx_payroll_period_id", columnList = "payroll_period_id"),
    @Index(name = "idx_payroll_status", columnList = "status"),
    @Index(name = "idx_payroll_pay_date", columnList = "pay_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PayrollLedger {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @NotNull
    @Column(name = "payroll_period_id", nullable = false)
    private Long payrollPeriodId;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "base_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalary;
    
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "gross_pay", precision = 15, scale = 2)
    private BigDecimal grossPay = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "total_deductions", precision = 15, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "total_taxes", precision = 15, scale = 2)
    private BigDecimal totalTaxes = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "net_pay", precision = 15, scale = 2)
    private BigDecimal netPay = BigDecimal.ZERO;
    
    @Column(name = "overtime_hours", precision = 8, scale = 2)
    private BigDecimal overtimeHours = BigDecimal.ZERO;
    
    @Column(name = "overtime_pay", precision = 15, scale = 2)
    private BigDecimal overtimePay = BigDecimal.ZERO;
    
    @Column(name = "bonus_amount", precision = 15, scale = 2)
    private BigDecimal bonusAmount = BigDecimal.ZERO;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PayrollLedgerStatus status = PayrollLedgerStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;
    
    @Column(name = "pay_date")
    private LocalDate payDate;
    
    @Column(name = "payment_reference", length = 100)
    private String paymentReference;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "paid_by")
    private Long paidBy;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_period_id", insertable = false, updatable = false)
    private PayrollPeriod payrollPeriod;
    
    @OneToMany(mappedBy = "payrollLedgerId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PayrollLedgerComponent> components = new ArrayList<>();
    
    // Helper methods
    public void calculateTotals() {
        this.grossPay = this.baseSalary.add(this.overtimePay).add(this.bonusAmount);
        this.netPay = this.grossPay.subtract(this.totalDeductions).subtract(this.totalTaxes);
    }
    
    public boolean isProcessed() {
        return this.status == PayrollLedgerStatus.CALCULATED || 
               this.status == PayrollLedgerStatus.APPROVED || 
               this.status == PayrollLedgerStatus.PAID;
    }
}