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

### Enums

#### PositionCategory Enum
```java
package com.example.demo.position.enums;

public enum PositionCategory {
    TECHNICAL,
    MANAGEMENT,
    ADMINISTRATIVE,
    SALES,
    HR,
    FINANCE,
    MARKETING,
    OPERATIONS,
    SUPPORT,
    OTHER
}
```

#### PositionLevel Enum
```java
package com.example.demo.position.enums;

public enum PositionLevel {
    JUNIOR,
    MID,
    SENIOR,
    LEAD,
    MANAGER,
    DIRECTOR,
    VP,
    EXECUTIVE
}
```

#### EmploymentType Enum
```java
package com.example.demo.position.enums;

public enum EmploymentType {
    FULL_TIME,
    PART_TIME,
    CONTRACT,
    INTERNSHIP,
    TEMPORARY
}
```
### Position Entity
```java
package com.example.demo.position.entity;

import com.example.demo.department.entity.Department;
import com.example.demo.employee.entity.Employee;
import com.example.demo.position.enums.EmploymentType;
import com.example.demo.position.enums.PositionCategory;
import com.example.demo.position.enums.PositionLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "positions", indexes = {
    @Index(name = "idx_position_job_title", columnList = "job_title"),
    @Index(name = "idx_position_code", columnList = "code"),
    @Index(name = "idx_position_department_id", columnList = "department_id"),
    @Index(name = "idx_position_level", columnList = "level"),
    @Index(name = "idx_position_enabled", columnList = "enabled"),
    @Index(name = "idx_position_category", columnList = "category")
})
@EntityListeners(AuditingEntityListener.class)
@Data
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Job title is required")
    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;

    @Column(name = "professional_title", length = 100)
    private String professionalTitle;

    @NotBlank(message = "Position code is required")
    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "requirements", length = 2000)
    private String requirements;

    @Column(name = "responsibilities", length = 2000)
    private String responsibilities;

    @NotNull(message = "Position category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private PositionCategory category = PositionCategory.TECHNICAL;

    @Column(name = "salary_grade", length = 10)
    private String salaryGrade;

    @NotNull(message = "Department is required")
    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @NotNull(message = "Position level is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private PositionLevel level = PositionLevel.JUNIOR;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "min_salary", precision = 12, scale = 2)
    private BigDecimal minSalary;

    @Column(name = "max_salary", precision = 12, scale = 2)
    private BigDecimal maxSalary;

    @Column(name = "required_skills", length = 1000)
    private String requiredSkills;

    @Column(name = "required_education", length = 500)
    private String requiredEducation;

    @Column(name = "required_experience")
    private Integer requiredExperience;

    @Column(name = "benefits", length = 1000)
    private String benefits;

    @Column(name = "work_location", length = 255)
    private String workLocation;

    @NotNull(message = "Employment type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20)
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    @Column(name = "is_managerial", nullable = false)
    private Boolean isManagerial = false;

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
    @JoinColumn(name = "department_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_position_department"))
    private Department department;

    @OneToMany(mappedBy = "position", fetch = FetchType.LAZY)
    private Set<Employee> employees = new HashSet<>();
}
```

## Repository Interface

### PositionRepository
```java
package com.example.demo.position.repository;

import com.example.demo.position.entity.Position;
import com.example.demo.position.enums.PositionCategory;
import com.example.demo.position.enums.PositionLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long>, JpaSpecificationExecutor<Position> {

    /**
     * Find a position by its unique code.
     *
     * @param code The position code.
     * @return An Optional containing the found position or empty if not found.
     */
    Optional<Position> findByCode(String code);

    /**
     * Check if a position with the given code exists.
     *
     * @param code The position code.
     * @return true if a position with the code exists, false otherwise.
     */
    boolean existsByCode(String code);

    /**
     * Find all positions within a specific department.
     *
     * @param departmentId The ID of the department.
     * @return A list of positions in the specified department.
     */
    List<Position> findByDepartmentId(Long departmentId);

    /**
     * Find all positions matching a specific level.
     *
     * @param level The position level.
     * @return A list of positions with the specified level.
     */
    List<Position> findByLevel(PositionLevel level);

    /**
     * Find all positions belonging to a specific category.
     *
     * @param category The position category.
     * @return A list of positions in the specified category.
     */
    List<Position> findByCategory(PositionCategory category);

    /**
     * Find all enabled positions with pagination.
     *
     * @param pageable Pagination information.
     * @return A Page of enabled positions.
     */
    Page<Position> findByEnabledTrue(Pageable pageable);

    /**
     * Search for positions by job title.
     *
     * @param jobTitle The job title to search for (case-insensitive).
     * @param pageable Pagination information.
     * @return A Page of positions matching the job title.
     */
    Page<Position> findByJobTitleContainingIgnoreCase(String jobTitle, Pageable pageable);

    /**
     * Custom query to find positions by department and filter by job title.
     *
     * @param departmentId The ID of the department.
     * @param jobTitle     A part of the job title to search for.
     * @return A list of matching positions.
     */
    @Query("SELECT p FROM Position p WHERE p.departmentId = :departmentId AND lower(p.jobTitle) LIKE lower(concat('%', :jobTitle, '%'))")
    List<Position> findByDepartmentAndJobTitle(@Param("departmentId") Long departmentId, @Param("jobTitle") String jobTitle);
}
```