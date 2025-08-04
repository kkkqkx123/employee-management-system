# Department Management Implementation

## Overview
This document provides detailed implementation specifications for the Department Management module. This module handles hierarchical department structure, department CRUD operations, and department tree queries with recursive support.

## Package Structure
```
com.example.demo.department/
├── entity/
│   └── Department.java
├── repository/
│   └── DepartmentRepository.java
├── service/
│   ├── DepartmentService.java
│   └── impl/
│       └── DepartmentServiceImpl.java
├── controller/
│   └── DepartmentController.java
├── dto/
│   ├── DepartmentDto.java
│   ├── DepartmentTreeDto.java
│   └── DepartmentCreateRequest.java
└── exception/
    ├── DepartmentNotFoundException.java
    ├── DepartmentHierarchyException.java
    └── DepartmentInUseException.java
```

## Entity Class

### Department Entity
```java
package com.example.demo.department.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("departments")
public class Department {
    @Id
    private Long id;
    
    @Indexed
    private String name;
    
    private String description;
    
    @Indexed
    private Long parentId;
    
    @Indexed
    private String depPath; // Hierarchical path like "/1/2/3"
    
    @Indexed
    private Boolean isParent;
    
    private Integer level; // Depth level in hierarchy (0 for root)
    
    private Integer sortOrder; // Display order within same level
    
    private String code; // Department code for identification
    
    private String location;
    
    private Long managerId; // Employee ID of department manager
    
    private boolean enabled;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    
    private Long updatedBy;
    
    // Transient fields for tree operations (not stored in Redis)
    private transient List<Department> children;
    private transient Department parent;
    private transient Set<Long> employeeIds; // IDs of employees in this department
}
```

## Repository Interface

### DepartmentRepository
```java
package com.example.demo.department.repository;

import com.example.demo.department.entity.Department;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends CrudRepository<Department, Long> {
    
    /**
     * Find department by name
     * @param name Department name
     * @return Optional department
     */
    Optional<Department> findByName(String name);
    
    /**
     * Find departments by parent ID
     * @param parentId Parent department ID
     * @return List of child departments
     */
    List<Department> findByParentIdOrderBySortOrder(Long parentId);
    
    /**
     * Find root departments (parentId is null)
     * @return List of root departments
     */
    List<Department> findByParentIdIsNullOrderBySortOrder();
    
    /**
     * Find departments by level
     * @param level Hierarchy level
     * @return List of departments at specified level
     */
    List<Department> findByLevel(Integer level);
    
    /**
     * Find departments by path prefix (for subtree queries)
     * @param pathPrefix Path prefix to match
     * @return List of departments in subtree
     */
    List<Department> findByDepPathStartingWithOrderByDepPath(String pathPrefix);
    
    /**
     * Find enabled departments
     * @return List of enabled departments
     */
    List<Department> findByEnabledTrueOrderByDepPath();
    
    /**
     * Find departments by name containing (case insensitive)
     * @param name Name search term
     * @return List of matching departments
     */
    List<Department> findByNameContainingIgnoreCaseOrderByName(String name);
    
    /**
     * Find departments by code
     * @param code Department code
     * @return Optional department
     */
    Optional<Department> findByCode(String code);
    
    /**
     * Check if department name exists
     * @param name Department name
     * @return true if exists
     */
    boolean existsByName(String name);
    
    /**
     * Check if department code exists
     * @param code Department code
     * @return true if exists
     */
    boolean existsByCode(String code);
    
    /**
     * Check if department has children
     * @param parentId Parent department ID
     * @return true if has children
     */
    boolean existsByParentId(Long parentId);
    
    /**
     * Count departments by parent ID
     * @param parentId Parent department ID
     * @return Count of child departments
     */
    long countByParentId(Long parentId);
    
    /**
     * Find departments by manager ID
     * @param managerId Manager employee ID
     * @return List of departments managed by the employee
     */
    List<Department> findByManagerId(Long managerId);
}
```## DTO 
Classes

### DepartmentDto
```java
package com.example.demo.department.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDto {
    
    private Long id;
    
    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    private Long parentId;
    
    private String depPath;
    
    private Boolean isParent;
    
    @Min(value = 0, message = "Level must be non-negative")
    private Integer level;
    
    @Min(value = 0, message = "Sort order must be non-negative")
    private Integer sortOrder;
    
    @Size(max = 20, message = "Department code must not exceed 20 characters")
    private String code;
    
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;
    
    private Long managerId;
    
    private String managerName; // Transient field for display
    
    private boolean enabled;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    
    private Long updatedBy;
    
    private String createdByName; // Transient field for display
    
    private String updatedByName; // Transient field for display
    
    private List<DepartmentDto> children;
    
    private DepartmentDto parent;
    
    private Long employeeCount; // Number of employees in this department
}
```

### DepartmentTreeDto
```java
package com.example.demo.department.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentTreeDto {
    
    private Long id;
    
    private String name;
    
    private String code;
    
    private Long parentId;
    
    private Integer level;
    
    private Integer sortOrder;
    
    private boolean enabled;
    
    private boolean hasChildren;
    
    private Long employeeCount;
    
    private String managerName;
    
    private List<DepartmentTreeDto> children;
    
    // Additional fields for tree display
    private boolean expanded; // For UI tree expansion state
    
    private boolean selectable; // Whether this node can be selected
    
    private String icon; // Icon for tree node display
}
```

### DepartmentCreateRequest
```java
package com.example.demo.department.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentCreateRequest {
    
    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    private Long parentId;
    
    @Min(value = 0, message = "Sort order must be non-negative")
    private Integer sortOrder;
    
    @Size(max = 20, message = "Department code must not exceed 20 characters")
    private String code;
    
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;
    
    private Long managerId;
    
    private boolean enabled = true;
}
```

### DepartmentUpdateRequest
```java
package com.example.demo.department.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentUpdateRequest {
    
    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @Min(value = 0, message = "Sort order must be non-negative")
    private Integer sortOrder;
    
    @Size(max = 20, message = "Department code must not exceed 20 characters")
    private String code;
    
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;
    
    private Long managerId;
    
    private boolean enabled;
}
```#
# Service Interface and Implementation

### DepartmentService Interface
```java
package com.example.demo.department.service;

import com.example.demo.department.dto.DepartmentDto;
import com.example.demo.department.dto.DepartmentTreeDto;
import com.example.demo.department.dto.DepartmentCreateRequest;
import com.example.demo.department.dto.DepartmentUpdateRequest;

import java.util.List;

public interface DepartmentService {
    
    /**
     * Create a new department
     * @param request Department creation request
     * @return Created department DTO
     * @throws DepartmentAlreadyExistsException if name or code already exists
     * @throws DepartmentNotFoundException if parent department not found
     */
    DepartmentDto createDepartment(DepartmentCreateRequest request);
    
    /**
     * Update an existing department
     * @param id Department ID
     * @param request Department update request
     * @return Updated department DTO
     * @throws DepartmentNotFoundException if department not found
     * @throws DepartmentAlreadyExistsException if name or code conflicts
     */
    DepartmentDto updateDepartment(Long id, DepartmentUpdateRequest request);
    
    /**
     * Get department by ID
     * @param id Department ID
     * @return Department DTO
     * @throws DepartmentNotFoundException if department not found
     */
    DepartmentDto getDepartmentById(Long id);
    
    /**
     * Get department by code
     * @param code Department code
     * @return Department DTO
     * @throws DepartmentNotFoundException if department not found
     */
    DepartmentDto getDepartmentByCode(String code);
    
    /**
     * Get all departments as flat list
     * @return List of department DTOs
     */
    List<DepartmentDto> getAllDepartments();
    
    /**
     * Get department tree structure
     * @return List of root department tree DTOs with children
     */
    List<DepartmentTreeDto> getDepartmentTree();
    
    /**
     * Get department subtree starting from specified department
     * @param departmentId Root department ID for subtree
     * @return Department tree DTO with children
     * @throws DepartmentNotFoundException if department not found
     */
    DepartmentTreeDto getDepartmentSubtree(Long departmentId);
    
    /**
     * Get child departments of specified parent
     * @param parentId Parent department ID (null for root departments)
     * @return List of child department DTOs
     */
    List<DepartmentDto> getChildDepartments(Long parentId);
    
    /**
     * Get departments by level in hierarchy
     * @param level Hierarchy level (0 for root)
     * @return List of department DTOs at specified level
     */
    List<DepartmentDto> getDepartmentsByLevel(Integer level);
    
    /**
     * Search departments by name
     * @param searchTerm Search term for department name
     * @return List of matching department DTOs
     */
    List<DepartmentDto> searchDepartments(String searchTerm);
    
    /**
     * Delete department by ID
     * @param id Department ID
     * @throws DepartmentNotFoundException if department not found
     * @throws DepartmentHierarchyException if department has children
     * @throws DepartmentInUseException if department has employees
     */
    void deleteDepartment(Long id);
    
    /**
     * Move department to new parent
     * @param departmentId Department ID to move
     * @param newParentId New parent department ID (null for root)
     * @throws DepartmentNotFoundException if department or parent not found
     * @throws DepartmentHierarchyException if move would create circular reference
     */
    void moveDepartment(Long departmentId, Long newParentId);
    
    /**
     * Enable or disable department
     * @param id Department ID
     * @param enabled Enable/disable flag
     * @throws DepartmentNotFoundException if department not found
     */
    void setDepartmentEnabled(Long id, boolean enabled);
    
    /**
     * Update department sort order
     * @param id Department ID
     * @param sortOrder New sort order
     * @throws DepartmentNotFoundException if department not found
     */
    void updateSortOrder(Long id, Integer sortOrder);
    
    /**
     * Get department path from root to specified department
     * @param departmentId Department ID
     * @return List of department DTOs representing path from root
     * @throws DepartmentNotFoundException if department not found
     */
    List<DepartmentDto> getDepartmentPath(Long departmentId);
    
    /**
     * Get all ancestor departments of specified department
     * @param departmentId Department ID
     * @return List of ancestor department DTOs
     * @throws DepartmentNotFoundException if department not found
     */
    List<DepartmentDto> getAncestorDepartments(Long departmentId);
    
    /**
     * Get all descendant departments of specified department
     * @param departmentId Department ID
     * @return List of descendant department DTOs
     * @throws DepartmentNotFoundException if department not found
     */
    List<DepartmentDto> getDescendantDepartments(Long departmentId);
    
    /**
     * Check if department can be deleted
     * @param id Department ID
     * @return true if department can be safely deleted
     * @throws DepartmentNotFoundException if department not found
     */
    boolean canDeleteDepartment(Long id);
    
    /**
     * Get department statistics
     * @param departmentId Department ID
     * @return Department statistics including employee count, child count, etc.
     * @throws DepartmentNotFoundException if department not found
     */
    DepartmentStatisticsDto getDepartmentStatistics(Long departmentId);
    
    /**
     * Rebuild department paths (maintenance operation)
     * This method recalculates all department paths and levels
     */
    void rebuildDepartmentPaths();
}
```

### DepartmentStatisticsDto
```java
package com.example.demo.department.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentStatisticsDto {
    
    private Long departmentId;
    
    private String departmentName;
    
    private Long directEmployeeCount; // Employees directly in this department
    
    private Long totalEmployeeCount; // Employees in this department and all subdepartments
    
    private Long directChildCount; // Direct child departments
    
    private Long totalChildCount; // All descendant departments
    
    private Integer maxDepth; // Maximum depth of subdepartments
    
    private boolean hasManager;
    
    private String managerName;
}
```