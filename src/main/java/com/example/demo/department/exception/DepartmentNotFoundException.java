package com.example.demo.department.exception;

import com.example.demo.common.exception.BusinessException;

public class DepartmentNotFoundException extends BusinessException {
    
    public DepartmentNotFoundException(String message) {
        super(message);
    }
    
    public DepartmentNotFoundException(String message, Throwable cause) {
        super("DEPARTMENT_NOT_FOUND", message, cause);
    }
    
    public DepartmentNotFoundException(Long departmentId) {
        super("Department not found with ID: " + departmentId);
    }
    
    public DepartmentNotFoundException(String field, String value) {
        super("Department not found with " + field + ": " + value);
    }
}