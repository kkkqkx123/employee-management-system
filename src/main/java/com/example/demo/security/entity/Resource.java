package com.example.demo.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resources", indexes = {
    @Index(name = "idx_resource_url", columnList = "url"),
    @Index(name = "idx_resource_method", columnList = "method"),
    @Index(name = "idx_resource_category", columnList = "category"),
    @Index(name = "idx_resource_active", columnList = "active")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "url", nullable = false, length = 255)
    private String url;
    
    @Column(name = "method", nullable = false, length = 10)
    private String method;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    
    @Column(name = "active", nullable = false)
    private Boolean active = true;
    
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
    
    @ManyToMany(mappedBy = "resources", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();
}