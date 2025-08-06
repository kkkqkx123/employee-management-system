package com.example.demo.department.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentUpdateRequest {
    
    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Min(value = 0, message = "Sort order must be non-negative")
    private Integer sortOrder;
    
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;
    
    private Long managerId;
    
    private boolean enabled;
}