package com.example.demo.department.exception;

import com.example.demo.common.exception.BusinessException;

public class DepartmentInUseException extends BusinessException {
    
    public DepartmentInUseException(String message) {
        super(message);
    }
    
    public DepartmentInUseException(String message, Throwable cause) {
        super("DEPARTMENT_IN_USE", message, cause);
    }
    
    public static DepartmentInUseException hasEmployees(Long departmentId, long employeeCount) {
        return new DepartmentInUseException(
            "Cannot delete department " + departmentId + " because it has " + employeeCount + " employees"
        );
    }
    
    public static DepartmentInUseException hasPositions(Long departmentId, long positionCount) {
        return new DepartmentInUseException(
            "Cannot delete department " + departmentId + " because it has " + positionCount + " positions"
        );
    }
}