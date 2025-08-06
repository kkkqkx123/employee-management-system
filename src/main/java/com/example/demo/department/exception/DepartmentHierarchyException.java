package com.example.demo.department.exception;

import com.example.demo.common.exception.BusinessException;

public class DepartmentHierarchyException extends BusinessException {
    
    public DepartmentHierarchyException(String message) {
        super(message);
    }
    
    public DepartmentHierarchyException(String message, Throwable cause) {
        super("DEPARTMENT_HIERARCHY_ERROR", message, cause);
    }
    
    public static DepartmentHierarchyException circularReference(Long departmentId, Long parentId) {
        return new DepartmentHierarchyException(
            "Moving department " + departmentId + " to parent " + parentId + " would create a circular reference"
        );
    }
    
    public static DepartmentHierarchyException hasChildren(Long departmentId) {
        return new DepartmentHierarchyException(
            "Cannot delete department " + departmentId + " because it has child departments"
        );
    }
    
    public static DepartmentHierarchyException invalidParent(Long parentId) {
        return new DepartmentHierarchyException(
            "Invalid parent department ID: " + parentId
        );
    }
}