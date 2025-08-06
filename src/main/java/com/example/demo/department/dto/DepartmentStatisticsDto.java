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