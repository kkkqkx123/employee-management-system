package com.example.demo.employee.service;

import com.example.demo.employee.dto.EmployeeCreateRequest;
import com.example.demo.employee.dto.EmployeeDto;
import com.example.demo.employee.dto.EmployeeSearchCriteria;
import com.example.demo.employee.dto.EmployeeUpdateRequest;
import com.example.demo.employee.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {
    
    /**
     * Create a new employee
     */
    EmployeeDto createEmployee(EmployeeCreateRequest createRequest);
    
    /**
     * Update an existing employee
     */
    EmployeeDto updateEmployee(Long id, EmployeeUpdateRequest updateRequest);
    
    /**
     * Get employee by ID
     */
    EmployeeDto getEmployeeById(Long id);
    
    /**
     * Get employee by employee number
     */
    EmployeeDto getEmployeeByEmployeeNumber(String employeeNumber);
    
    /**
     * Get all employees with pagination
     */
    Page<EmployeeDto> getAllEmployees(Pageable pageable);
    
    /**
     * Get employees by department ID
     */
    Page<EmployeeDto> getEmployeesByDepartmentId(Long departmentId, Pageable pageable);
    
    /**
     * Get employees by department ID (list version for department service)
     */
    List<EmployeeDto> getEmployeesByDepartmentId(Long departmentId);
    
    /**
     * Get employees by status
     */
    Page<EmployeeDto> getEmployeesByStatus(EmployeeStatus status, Pageable pageable);
    
    /**
     * Search employees by term
     */
    Page<EmployeeDto> searchEmployees(String searchTerm, Pageable pageable);
    
    /**
     * Advanced search employees with multiple criteria
     */
    Page<EmployeeDto> searchEmployees(EmployeeSearchCriteria criteria, Pageable pageable);
    
    /**
     * Delete employee by ID
     */
    void deleteEmployee(Long id);
    
    /**
     * Check if employee exists by employee number
     */
    boolean existsByEmployeeNumber(String employeeNumber);
    
    /**
     * Check if employee exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Count employees by department ID
     */
    long countEmployeesByDepartmentId(Long departmentId);
}