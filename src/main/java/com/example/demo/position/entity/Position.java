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