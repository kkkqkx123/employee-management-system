package com.example.demo.employee.entity;

public enum EmployeeStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    TERMINATED("Terminated"),
    ON_LEAVE("On Leave"),
    PROBATION("Probation"),
    SUSPENDED("Suspended");
    
    private final String displayName;
    
    EmployeeStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}