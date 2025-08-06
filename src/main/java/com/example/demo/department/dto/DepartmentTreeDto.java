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