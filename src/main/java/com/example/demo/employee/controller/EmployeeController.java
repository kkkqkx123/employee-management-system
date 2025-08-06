package com.example.demo.employee.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.employee.dto.EmployeeCreateRequest;
import com.example.demo.employee.dto.EmployeeDto;
import com.example.demo.employee.dto.EmployeeUpdateRequest;
import com.example.demo.employee.entity.EmployeeStatus;
import com.example.demo.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee Management", description = "APIs for managing employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_CREATE')")
    @Operation(summary = "Create a new employee", description = "Creates a new employee with comprehensive information")
    public ResponseEntity<ApiResponse<EmployeeDto>> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest createRequest) {
        log.info("Creating employee with email: {}", createRequest.getEmail());
        
        EmployeeDto createdEmployee = employeeService.createEmployee(createRequest);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(createdEmployee, "Employee created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
    @Operation(summary = "Update employee", description = "Updates an existing employee")
    public ResponseEntity<ApiResponse<EmployeeDto>> updateEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest updateRequest) {
        log.info("Updating employee with ID: {}", id);
        
        EmployeeDto updatedEmployee = employeeService.updateEmployee(id, updateRequest);
        
        return ResponseEntity.ok(ApiResponse.success(updatedEmployee, "Employee updated successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get employee by ID", description = "Retrieves an employee by their ID")
    public ResponseEntity<ApiResponse<EmployeeDto>> getEmployeeById(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        log.info("Getting employee with ID: {}", id);
        
        EmployeeDto employee = employeeService.getEmployeeById(id);
        
        return ResponseEntity.ok(ApiResponse.success(employee));
    }

    @GetMapping("/employee-number/{employeeNumber}")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get employee by employee number", description = "Retrieves an employee by their employee number")
    public ResponseEntity<ApiResponse<EmployeeDto>> getEmployeeByEmployeeNumber(
            @Parameter(description = "Employee number") @PathVariable String employeeNumber) {
        log.info("Getting employee with employee number: {}", employeeNumber);
        
        EmployeeDto employee = employeeService.getEmployeeByEmployeeNumber(employeeNumber);
        
        return ResponseEntity.ok(ApiResponse.success(employee));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get all employees", description = "Retrieves all employees with pagination")
    public ResponseEntity<ApiResponse<Page<EmployeeDto>>> getAllEmployees(Pageable pageable) {
        log.info("Getting all employees with pagination");
        
        Page<EmployeeDto> employees = employeeService.getAllEmployees(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get employees by department", description = "Retrieves employees in a specific department")
    public ResponseEntity<ApiResponse<Page<EmployeeDto>>> getEmployeesByDepartment(
            @Parameter(description = "Department ID") @PathVariable Long departmentId,
            Pageable pageable) {
        log.info("Getting employees for department: {}", departmentId);
        
        Page<EmployeeDto> employees = employeeService.getEmployeesByDepartmentId(departmentId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get employees by status", description = "Retrieves employees with a specific status")
    public ResponseEntity<ApiResponse<Page<EmployeeDto>>> getEmployeesByStatus(
            @Parameter(description = "Employee status") @PathVariable EmployeeStatus status,
            Pageable pageable) {
        log.info("Getting employees with status: {}", status);
        
        Page<EmployeeDto> employees = employeeService.getEmployeesByStatus(status, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Search employees", description = "Searches employees by name, email, or employee number")
    public ResponseEntity<ApiResponse<Page<EmployeeDto>>> searchEmployees(
            @Parameter(description = "Search term") @RequestParam String q,
            Pageable pageable) {
        log.info("Searching employees with term: {}", q);
        
        Page<EmployeeDto> employees = employeeService.searchEmployees(q, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_DELETE')")
    @Operation(summary = "Delete employee", description = "Deletes an employee")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        log.info("Deleting employee with ID: {}", id);
        
        employeeService.deleteEmployee(id);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Employee deleted successfully"));
    }

    @GetMapping("/exists/employee-number/{employeeNumber}")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Check if employee number exists", description = "Checks if an employee number is already in use")
    public ResponseEntity<ApiResponse<Boolean>> existsByEmployeeNumber(
            @Parameter(description = "Employee number") @PathVariable String employeeNumber) {
        boolean exists = employeeService.existsByEmployeeNumber(employeeNumber);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @GetMapping("/exists/email/{email}")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Check if email exists", description = "Checks if an email is already in use")
    public ResponseEntity<ApiResponse<Boolean>> existsByEmail(
            @Parameter(description = "Email address") @PathVariable String email) {
        boolean exists = employeeService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}