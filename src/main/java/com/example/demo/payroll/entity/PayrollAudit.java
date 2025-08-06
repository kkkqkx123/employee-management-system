package com.example.demo.payroll.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_audits", indexes = {
    @Index(name = "idx_payroll_audit_ledger_id", columnList = "payroll_ledger_id"),
    @Index(name = "idx_payroll_audit_action", columnList = "action"),
    @Index(name = "idx_payroll_audit_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PayrollAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "payroll_ledger_id", nullable = false)
    private Long payrollLedgerId;
    
    @NotNull
    @Column(name = "action", nullable = false, length = 50)
    private String action; // CREATED, CALCULATED, APPROVED, PAID, REJECTED, CANCELLED
    
    @Column(name = "old_status", length = 50)
    private String oldStatus;
    
    @Column(name = "new_status", length = 50)
    private String newStatus;
    
    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes; // JSON string of changes
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @NotNull
    @Column(name = "performed_by", nullable = false)
    private Long performedBy;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_ledger_id", insertable = false, updatable = false)
    private PayrollLedger payrollLedger;
}