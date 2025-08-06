package com.example.demo.employee.exception;

import com.example.demo.common.exception.BusinessException;

public class EmployeeAlreadyExistsException extends BusinessException {
    
    public EmployeeAlreadyExistsException(String message) {
        super(message);
    }
    
    public static EmployeeAlreadyExistsException byEmployeeNumber(String employeeNumber) {
        return new EmployeeAlreadyExistsException("Employee already exists with employee number: " + employeeNumber);
    }
    
    public static EmployeeAlreadyExistsException byEmail(String email) {
        return new EmployeeAlreadyExistsException("Employee already exists with email: " + email);
    }
}