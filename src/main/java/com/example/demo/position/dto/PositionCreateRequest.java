package com.example.demo.position.dto;

import com.example.demo.position.enums.EmploymentType;
import com.example.demo.position.enums.PositionCategory;
import com.example.demo.position.enums.PositionLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionCreateRequest {
    @NotBlank(message = "Job title is required")
    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;

    @Size(max = 100, message = "Professional title must not exceed 100 characters")
    private String professionalTitle;

    @NotBlank(message = "Position code is required")
    @Size(max = 20, message = "Position code must not exceed 20 characters")
    private String code;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 2000, message = "Requirements must not exceed 2000 characters")
    private String requirements;

    @Size(max = 2000, message = "Responsibilities must not exceed 2000 characters")
    private String responsibilities;

    @NotNull(message = "Position category is required")
    private PositionCategory category;

    @Size(max = 10, message = "Salary grade must not exceed 10 characters")
    private String salaryGrade;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Position level is required")
    private PositionLevel level;

    private Boolean enabled = true;

    private BigDecimal minSalary;

    private BigDecimal maxSalary;

    @Size(max = 1000, message = "Required skills must not exceed 1000 characters")
    private String requiredSkills;

    @Size(max = 500, message = "Required education must not exceed 500 characters")
    private String requiredEducation;

    private Integer requiredExperience;

    @Size(max = 1000, message = "Benefits must not exceed 1000 characters")
    private String benefits;

    @Size(max = 255, message = "Work location must not exceed 255 characters")
    private String workLocation;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    private Boolean isManagerial = false;
}