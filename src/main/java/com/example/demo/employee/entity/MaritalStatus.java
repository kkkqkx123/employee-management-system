package com.example.demo.employee.entity;

public enum MaritalStatus {
    SINGLE("Single"),
    MARRIED("Married"),
    DIVORCED("Divorced"),
    WIDOWED("Widowed"),
    SEPARATED("Separated"),
    DOMESTIC_PARTNERSHIP("Domestic Partnership");
    
    private final String displayName;
    
    MaritalStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}