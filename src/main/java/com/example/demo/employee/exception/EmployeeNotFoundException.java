package com.example.demo.employee.exception;

import com.example.demo.common.exception.BusinessException;

public class EmployeeNotFoundException extends BusinessException {
    
    public EmployeeNotFoundException(String message) {
        super(message);
    }
    
    public EmployeeNotFoundException(Long id) {
        super("Employee not found with id: " + id);
    }
    
    public static EmployeeNotFoundException byEmployeeNumber(String employeeNumber) {
        return new EmployeeNotFoundException("Employee not found with employee number: " + employeeNumber);
    }
    
    public static EmployeeNotFoundException byEmail(String email) {
        return new EmployeeNotFoundException("Employee not found with email: " + email);
    }
}