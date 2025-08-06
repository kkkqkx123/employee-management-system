package com.example.demo.department.entity;

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
@Table(name = "departments", indexes = {
    @Index(name = "idx_department_name", columnList = "name"),
    @Index(name = "idx_department_code", columnList = "code"),
    @Index(name = "idx_department_parent_id", columnList = "parent_id"),
    @Index(name = "idx_department_dep_path", columnList = "dep_path"),
    @Index(name = "idx_department_enabled", columnList = "enabled"),
    @Index(name = "idx_department_manager_id", columnList = "manager_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "location", length = 255)
    private String location;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "dep_path", length = 500)
    private String depPath;
    
    @Column(name = "is_parent", nullable = false)
    private Boolean isParent = false;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "level")
    private Integer level = 0;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "manager_id")
    private Long managerId;
    
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Department parent;
    
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<Department> children = new HashSet<>();
    
    // Employee relationship is defined in the Employee entity
    
    @Transient
    private Long employeeCount;
}