# Position and Title Management Implementation

## Overview
This document provides detailed implementation specifications for the Position and Title Management module. This module handles job positions, professional titles, position hierarchies, and position-related CRUD operations.

## Package Structure
```
com.example.demo.position/
├── entity/
│   └── Position.java
├── repository/
│   └── PositionRepository.java
├── service/
│   ├── PositionService.java
│   └── impl/
│       └── PositionServiceImpl.java
├── controller/
│   └── PositionController.java
├── dto/
│   ├── PositionDto.java
│   ├── PositionCreateRequest.java
│   ├── PositionUpdateRequest.java
│   └── PositionSearchCriteria.java
└── exception/
    ├── PositionNotFoundException.java
    ├── PositionAlreadyExistsException.java
    └── PositionInUseException.java
```

## Entity Class

### Position Entity
```java
package com.example.demo.position.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("positions")
public class Position {
    @Id
    private Long id;
    
    @Indexed
    private String jobTitle; // Job title (e.g., "Software Engineer")
    
    @Indexed
    private String professionalTitle; // Professional title (e.g., "Senior", "Lead", "Manager")
    
    private String code; // Position code for identification
    
    private String description;
    
    @Indexed
    private Long departmentId; // Department this position belongs to
    
    @Indexed
    private String level; // Position level (ENTRY, JUNIOR, SENIOR, LEAD, MANAGER, DIRECTOR, VP, C_LEVEL)
    
    @Indexed
    private String category; // Position category (TECHNICAL, MANAGEMENT, SALES, HR, FINANCE, etc.)
    
    private BigDecimal minSalary; // Minimum salary for this position
    
    private BigDecimal maxSalary; // Maximum salary for this position
    
    private String salaryGrade; // Salary grade/band
    
    private String requiredSkills; // Comma-separated required skills
    
    private String preferredSkills; // Comma-separated preferred skills
    
    private String requiredEducation; // Required education level
    
    private String requiredExperience; // Required years of experience
    
    private String responsibilities; // Job responsibilities
    
    private String requirements; // Job requirements
    
    private String benefits; // Position benefits
    
    private String workLocation; // OFFICE, REMOTE, HYBRID
    
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT, INTERN
    
    private boolean isManagerial; // Whether this is a managerial position
    
    private boolean isActive; // Whether position is currently active/available
    
    private boolean enabled;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    
    private Long updatedBy;
    
    // Transient fields for display purposes (not stored in Redis)
    private transient String departmentName;
    private transient Long employeeCount; // Number of employees in this position
    private transient String fullTitle; // jobTitle + professionalTitle
}
```

## Repository Interface

### PositionRepository
```java
package com.example.demo.position.repository;

import com.example.demo.position.entity.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends CrudRepository<Position, Long> {
    
    /**
     * Find position by job title and professional title
     * @param jobTitle Job title
     * @param professionalTitle Professional title
     * @return Optional position
     */
    Optional<Position> findByJobTitleAndProfessionalTitle(String jobTitle, String professionalTitle);
    
    /**
     * Find position by code
     * @param code Position code
     * @return Optional position
     */
    Optional<Position> findByCode(String code);
    
    /**
     * Find positions by department ID
     * @param departmentId Department ID
     * @return List of positions
     */
    List<Position> findByDepartmentIdOrderByJobTitle(Long departmentId);
    
    /**
     * Find positions by department ID with pagination
     * @param departmentId Department ID
     * @param pageable Pagination parameters
     * @return Page of positions
     */
    Page<Position> findByDepartmentId(Long departmentId, Pageable pageable);
    
    /**
     * Find positions by level
     * @param level Position level
     * @return List of positions
     */
    List<Position> findByLevelOrderByJobTitle(String level);
    
    /**
     * Find positions by category
     * @param category Position category
     * @return List of positions
     */
    List<Position> findByCategoryOrderByJobTitle(String category);
    
    /**
     * Find positions by job title containing (case insensitive)
     * @param jobTitle Job title search term
     * @param pageable Pagination parameters
     * @return Page of positions
     */
    Page<Position> findByJobTitleContainingIgnoreCase(String jobTitle, Pageable pageable);
    
    /**
     * Find positions by professional title containing (case insensitive)
     * @param professionalTitle Professional title search term
     * @param pageable Pagination parameters
     * @return Page of positions
     */
    Page<Position> findByProfessionalTitleContainingIgnoreCase(String professionalTitle, Pageable pageable);
    
    /**
     * Find positions by job title or professional title containing (case insensitive)
     * @param jobTitle Job title search term
     * @param professionalTitle Professional title search term
     * @param pageable Pagination parameters
     * @return Page of positions
     */
    Page<Position> findByJobTitleContainingIgnoreCaseOrProfessionalTitleContainingIgnoreCase(
        String jobTitle, String professionalTitle, Pageable pageable);
    
    /**
     * Find enabled positions
     * @param pageable Pagination parameters
     * @return Page of enabled positions
     */
    Page<Position> findByEnabledTrue(Pageable pageable);
    
    /**
     * Find active positions
     * @param pageable Pagination parameters
     * @return Page of active positions
     */
    Page<Position> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Find managerial positions
     * @param pageable Pagination parameters
     * @return Page of managerial positions
     */
    Page<Position> findByIsManagerialTrue(Pageable pageable);
    
    /**
     * Find positions by employment type
     * @param employmentType Employment type
     * @param pageable Pagination parameters
     * @return Page of positions
     */
    Page<Position> findByEmploymentType(String employmentType, Pageable pageable);
    
    /**
     * Find positions by work location
     * @param workLocation Work location
     * @param pageable Pagination parameters
     * @return Page of positions
     */
    Page<Position> findByWorkLocation(String workLocation, Pageable pageable);
    
    /**
     * Check if position exists by job title and professional title
     * @param jobTitle Job title
     * @param professionalTitle Professional title
     * @return true if exists
     */
    boolean existsByJobTitleAndProfessionalTitle(String jobTitle, String professionalTitle);
    
    /**
     * Check if position code exists
     * @param code Position code
     * @return true if exists
     */
    boolean existsByCode(String code);
    
    /**
     * Count positions by department ID
     * @param departmentId Department ID
     * @return Count of positions
     */
    long countByDepartmentId(Long departmentId);
    
    /**
     * Count positions by level
     * @param level Position level
     * @return Count of positions
     */
    long countByLevel(String level);
    
    /**
     * Count positions by category
     * @param category Position category
     * @return Count of positions
     */
    long countByCategory(String category);
    
    /**
     * Find positions by multiple criteria (for advanced search)
     * @param departmentId Department ID (optional)
     * @param level Position level (optional)
     * @param category Position category (optional)
     * @param isManagerial Managerial flag (optional)
     * @param pageable Pagination parameters
     * @return Page of positions matching criteria
     */
    Page<Position> findByDepartmentIdAndLevelAndCategoryAndIsManagerial(
        Long departmentId, String level, String category, Boolean isManagerial, Pageable pageable);
}
```