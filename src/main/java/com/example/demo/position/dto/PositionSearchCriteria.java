package com.example.demo.position.dto;

import com.example.demo.position.enums.EmploymentType;
import com.example.demo.position.enums.PositionCategory;
import com.example.demo.position.enums.PositionLevel;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionSearchCriteria {
    private String jobTitle;
    private String professionalTitle;
    private String code;
    private PositionCategory category;
    private Long departmentId;
    private PositionLevel level;
    private Boolean enabled;
    private BigDecimal minSalaryFrom;
    private BigDecimal minSalaryTo;
    private BigDecimal maxSalaryFrom;
    private BigDecimal maxSalaryTo;
    private String requiredSkills;
    private String requiredEducation;
    private Integer minExperience;
    private Integer maxExperience;
    private String workLocation;
    private EmploymentType employmentType;
    private Boolean isManagerial;
    private String sortBy = "jobTitle";
    private String sortDirection = "ASC";
}