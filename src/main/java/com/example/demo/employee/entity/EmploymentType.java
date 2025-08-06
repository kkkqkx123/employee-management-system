package com.example.demo.employee.entity;

public enum EmploymentType {
    FULL_TIME("Full Time"),
    PART_TIME("Part Time"),
    CONTRACT("Contract"),
    TEMPORARY("Temporary"),
    INTERN("Intern"),
    CONSULTANT("Consultant");
    
    private final String displayName;
    
    EmploymentType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}