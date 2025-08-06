package com.example.demo.employee.entity;

public enum PayType {
    SALARY("Salary"),
    HOURLY("Hourly");
    
    private final String displayName;
    
    PayType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}