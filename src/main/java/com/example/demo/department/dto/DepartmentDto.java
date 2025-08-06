package com.example.demo.department.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import java.time.Instant;
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
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
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
    
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;
    
    private Long managerId;
    
    private String managerName; // Transient field for display
    
    private boolean enabled;
    
    private Instant createdAt;
    private Instant updatedAt;
    
    private Long createdBy;
    
    private Long updatedBy;
    
    private String createdByName; // Transient field for display
    
    private String updatedByName; // Transient field for display
    
    private List<DepartmentDto> children;
    
    private DepartmentDto parent;
    
    private Long employeeCount; // Number of employees in this department
}