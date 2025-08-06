package com.example.demo.department.exception;

import com.example.demo.common.exception.BusinessException;

public class DepartmentAlreadyExistsException extends BusinessException {
    
    public DepartmentAlreadyExistsException(String message) {
        super(message);
    }
    
    public DepartmentAlreadyExistsException(String message, Throwable cause) {
        super("DEPARTMENT_ALREADY_EXISTS", message, cause);
    }
    
    public static DepartmentAlreadyExistsException byName(String name) {
        return new DepartmentAlreadyExistsException(
            "Department with name '" + name + "' already exists"
        );
    }
    
    public static DepartmentAlreadyExistsException byCode(String code) {
        return new DepartmentAlreadyExistsException(
            "Department with code '" + code + "' already exists"
        );
    }
}