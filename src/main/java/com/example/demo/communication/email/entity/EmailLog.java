package com.example.demo.communication.email.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "email_logs", indexes = {
    @Index(name = "idx_emaillog_to_email", columnList = "to_email"),
    @Index(name = "idx_emaillog_status", columnList = "status"),
    @Index(name = "idx_emaillog_template_code", columnList = "template_code"),
    @Index(name = "idx_emaillog_sent_by", columnList = "sent_by")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "to_email", nullable = false)
    private String toEmail;
    
    @Column(name = "cc_emails", length = 1000)
    private String ccEmails; // Comma-separated CC emails
    
    @Column(name = "bcc_emails", length = 1000)
    private String bccEmails; // Comma-separated BCC emails
    
    @Column(name = "subject", nullable = false)
    private String subject;
    
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "template_code", length = 50)
    private String templateCode; // Template used (if any)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmailStatus status; // PENDING, SENT, FAILED, BOUNCED
    
    @Column(name = "error_message", length = 2000)
    private String errorMessage; // Error details if failed
    
    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "sent_at")
    private Instant sentAt;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "sent_by")
    private Long sentBy; // User who sent the email
    
    @Column(name = "message_id", length = 255)
    private String messageId; // Email provider message ID
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private EmailPriority priority; // HIGH, NORMAL, LOW
}