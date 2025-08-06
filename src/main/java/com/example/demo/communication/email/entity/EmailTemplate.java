package com.example.demo.communication.email.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "email_templates", indexes = {
    @Index(name = "idx_emailtemplate_code", columnList = "code", unique = true),
    @Index(name = "idx_emailtemplate_category", columnList = "category")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EmailTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name; // Template name for identification
    
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code; // Unique template code
    
    @Column(name = "subject", nullable = false, length = 255)
    private String subject; // Email subject template
    
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // Email content template (HTML/Text)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 20)
    private TemplateType templateType; // Template type (e.g., HTML, TEXT, MIXED)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private TemplateCategory category; // WELCOME, NOTIFICATION, REMINDER, etc.
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Lob
    @Column(name = "variables", columnDefinition = "TEXT")
    private String variables; // JSON string of available template variables
    
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
    
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
}