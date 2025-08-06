package com.example.demo.position.dto;

import com.example.demo.position.enums.EmploymentType;
import com.example.demo.position.enums.PositionCategory;
import com.example.demo.position.enums.PositionLevel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PositionDto {
    private Long id;
    private String jobTitle;
    private String professionalTitle;
    private String code;
    private String description;
    private String requirements;
    private String responsibilities;
    private PositionCategory category;
    private String salaryGrade;
    private Long departmentId;
    private String departmentName;
    private PositionLevel level;
    private Boolean enabled;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private String requiredSkills;
    private String requiredEducation;
    private Integer requiredExperience;
    private String benefits;
    private String workLocation;
    private EmploymentType employmentType;
    private Boolean isManagerial;
    private Instant createdAt;
    private Instant updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer employeeCount;
}