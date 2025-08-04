# Employee Management Implementation

## Overview
This document provides detailed implementation specifications for the Employee Management module. This module handles comprehensive employee CRUD operations, search functionality, pagination, Excel import/export, and batch operations.

## Package Structure
```
com.example.demo.employee/
├── entity/
│   ├── Employee.java
│   └── EmployeeStatus.java
├── repository/
│   └── EmployeeRepository.java
├── service/
│   ├── EmployeeService.java
│   ├── EmployeeImportService.java
│   ├── EmployeeExportService.java
│   └── impl/
│       ├── EmployeeServiceImpl.java
│       ├── EmployeeImportServiceImpl.java
│       └── EmployeeExportServiceImpl.java
├── controller/
│   └── EmployeeController.java
├── dto/
│   ├── EmployeeDto.java
│   ├── EmployeeCreateRequest.java
│   ├── EmployeeUpdateRequest.java
│   ├── EmployeeSearchCriteria.java
│   ├── EmployeeImportResult.java
│   └── EmployeeExportRequest.java
├── util/
│   ├── EmployeeExcelUtil.java
│   └── EmployeeValidationUtil.java
└── exception/
    ├── EmployeeNotFoundException.java
    ├── EmployeeAlreadyExistsException.java
    ├── EmployeeImportException.java
    └── EmployeeExportException.java
```

## Entity Classes

### EmployeeStatus Enum
```java
package com.example.demo.employee.entity;

public enum EmployeeStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    TERMINATED("Terminated"),
    ON_LEAVE("On Leave"),
    PROBATION("Probation"),
    SUSPENDED("Suspended");
    
    private final String displayName;
    
    EmployeeStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

### Employee Entity
```java
package com.example.demo.employee.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("employees")
public class Employee {
    @Id
    private Long id;
    
    @Indexed
    private String employeeNumber; // Unique employee identifier
    
    @Indexed
    private String firstName;
    
    @Indexed
    private String lastName;
    
    @Indexed
    private String email;
    
    private String phone;
    
    private String mobilePhone;
    
    private String address;
    
    private String city;
    
    private String state;
    
    private String zipCode;
    
    private String country;
    
    private LocalDate dateOfBirth;
    
    private String gender; // MALE, FEMALE, OTHER
    
    private String maritalStatus; // SINGLE, MARRIED, DIVORCED, WIDOWED
    
    private String nationality;
    
    private String emergencyContactName;
    
    private String emergencyContactPhone;
    
    private String emergencyContactRelation;
    
    @Indexed
    private Long departmentId;
    
    @Indexed
    private Long positionId;
    
    @Indexed
    private Long managerId; // Direct manager employee ID
    
    private LocalDate hireDate;
    
    private LocalDate terminationDate;
    
    @Indexed
    private EmployeeStatus status;
    
    private BigDecimal salary;
    
    private String salaryGrade;
    
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT, INTERN
    
    private String workLocation; // OFFICE, REMOTE, HYBRID
    
    private String skills; // Comma-separated skills
    
    private String education;
    
    private String certifications;
    
    private String notes;
    
    private String profileImageUrl;
    
    private boolean enabled;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    
    private Long updatedBy;
    
    // Transient fields for display purposes (not stored in Redis)
    private transient String departmentName;
    private transient String positionName;
    private transient String managerName;
    private transient String fullName; // firstName + lastName
}
```## Repos
itory Interface

### EmployeeRepository
```java
package com.example.demo.employee.repository;

import com.example.demo.employee.entity.Employee;
import com.example.demo.employee.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends CrudRepository<Employee, Long> {
    
    /**
     * Find employee by employee number
     * @param employeeNumber Unique employee number
     * @return Optional employee
     */
    Optional<Employee> findByEmployeeNumber(String employeeNumber);
    
    /**
     * Find employee by email
     * @param email Employee email
     * @return Optional employee
     */
    Optional<Employee> findByEmail(String email);
    
    /**
     * Find employees by department ID
     * @param departmentId Department ID
     * @param pageable Pagination parameters
     * @return Page of employees
     */
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);
    
    /**
     * Find employees by position ID
     * @param positionId Position ID
     * @param pageable Pagination parameters
     * @return Page of employees
     */
    Page<Employee> findByPositionId(Long positionId, Pageable pageable);
    
    /**
     * Find employees by manager ID
     * @param managerId Manager employee ID
     * @param pageable Pagination parameters
     * @return Page of employees
     */
    Page<Employee> findByManagerId(Long managerId, Pageable pageable);
    
    /**
     * Find employees by status
     * @param status Employee status
     * @param pageable Pagination parameters
     * @return Page of employees
     */
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);
    
    /**
     * Find employees by hire date range
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination parameters
     * @return Page of employees
     */
    Page<Employee> findByHireDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Find employees by first name containing (case insensitive)
     * @param firstName First name search term
     * @param pageable Pagination parameters
     * @return Page of employees
     */
    Page<Employee> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);
    
    /**
     * Find employees by last name containing (case insensitive)
     * @param lastName Last name search term
     * @param pageable Pagination parameters
     * @return Page of employees
     */
    Page<Employee> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);
    
    /**
     * Find employees by first name or last name containing (case insensitive)
     * @param firstName First name search term
     * @param lastName Last name search term
     * @param pageable Pagination parameters
     * @return Page of employees
     */
    Page<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName, Pageable pageable);
    
    /**
     * Find employees by email containing (case insensitive)
     * @param email Email search term
     * @param pageable Pagination parameters
     * @return Page of employees
     */
    Page<Employee> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    
    /**
     * Find enabled employees
     * @param pageable Pagination parameters
     * @return Page of enabled employees
     */
    Page<Employee> findByEnabledTrue(Pageable pageable);
    
    /**
     * Find employees by multiple criteria (for advanced search)
     * @param departmentId Department ID (optional)
     * @param positionId Position ID (optional)
     * @param status Employee status (optional)
     * @param pageable Pagination parameters
     * @return Page of employees matching criteria
     */
    Page<Employee> findByDepartmentIdAndPositionIdAndStatus(
        Long departmentId, Long positionId, EmployeeStatus status, Pageable pageable);
    
    /**
     * Check if employee number exists
     * @param employeeNumber Employee number
     * @return true if exists
     */
    boolean existsByEmployeeNumber(String employeeNumber);
    
    /**
     * Check if email exists
     * @param email Employee email
     * @return true if exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Count employees by department ID
     * @param departmentId Department ID
     * @return Count of employees
     */
    long countByDepartmentId(Long departmentId);
    
    /**
     * Count employees by position ID
     * @param positionId Position ID
     * @return Count of employees
     */
    long countByPositionId(Long positionId);
    
    /**
     * Count employees by status
     * @param status Employee status
     * @return Count of employees
     */
    long countByStatus(EmployeeStatus status);
    
    /**
     * Find employees by IDs
     * @param ids List of employee IDs
     * @return List of employees
     */
    List<Employee> findByIdIn(List<Long> ids);
    
    /**
     * Find employees hired in date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of employees
     */
    List<Employee> findByHireDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find employees with birthdays in date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of employees
     */
    List<Employee> findByDateOfBirthBetween(LocalDate startDate, LocalDate endDate);
}
```#
# DTO Classes

### EmployeeDto
```java
package com.example.demo.employee.dto;

import com.example.demo.employee.entity.EmployeeStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    
    private Long id;
    
    @NotBlank(message = "Employee number is required")
    @Size(max = 20, message = "Employee number must not exceed 20 characters")
    private String employeeNumber;
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{10,20}$", message = "Phone number format is invalid")
    private String phone;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{10,20}$", message = "Mobile phone number format is invalid")
    private String mobilePhone;
    
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;
    
    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;
    
    @Size(max = 10, message = "Zip code must not exceed 10 characters")
    private String zipCode;
    
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;
    
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @Pattern(regexp = "MALE|FEMALE|OTHER", message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;
    
    @Pattern(regexp = "SINGLE|MARRIED|DIVORCED|WIDOWED", message = "Invalid marital status")
    private String maritalStatus;
    
    @Size(max = 50, message = "Nationality must not exceed 50 characters")
    private String nationality;
    
    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    private String emergencyContactName;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{10,20}$", message = "Emergency contact phone format is invalid")
    private String emergencyContactPhone;
    
    @Size(max = 50, message = "Emergency contact relation must not exceed 50 characters")
    private String emergencyContactRelation;
    
    @NotNull(message = "Department is required")
    private Long departmentId;
    
    private String departmentName; // Transient field for display
    
    @NotNull(message = "Position is required")
    private Long positionId;
    
    private String positionName; // Transient field for display
    
    private Long managerId;
    
    private String managerName; // Transient field for display
    
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
    
    private LocalDate terminationDate;
    
    @NotNull(message = "Employee status is required")
    private EmployeeStatus status;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be positive")
    @Digits(integer = 10, fraction = 2, message = "Salary format is invalid")
    private BigDecimal salary;
    
    @Size(max = 20, message = "Salary grade must not exceed 20 characters")
    private String salaryGrade;
    
    @Pattern(regexp = "FULL_TIME|PART_TIME|CONTRACT|INTERN", message = "Invalid employment type")
    private String employmentType;
    
    @Pattern(regexp = "OFFICE|REMOTE|HYBRID", message = "Invalid work location")
    private String workLocation;
    
    @Size(max = 500, message = "Skills must not exceed 500 characters")
    private String skills;
    
    @Size(max = 500, message = "Education must not exceed 500 characters")
    private String education;
    
    @Size(max = 500, message = "Certifications must not exceed 500 characters")
    private String certifications;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    private String profileImageUrl;
    
    private boolean enabled;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    
    private Long updatedBy;
    
    private String createdByName; // Transient field for display
    
    private String updatedByName; // Transient field for display
    
    private String fullName; // firstName + lastName
    
    private Integer age; // Calculated from dateOfBirth
    
    private Integer yearsOfService; // Calculated from hireDate
}
```

### EmployeeCreateRequest
```java
package com.example.demo.employee.dto;

import com.example.demo.employee.entity.EmployeeStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateRequest {
    
    @NotBlank(message = "Employee number is required")
    @Size(max = 20, message = "Employee number must not exceed 20 characters")
    private String employeeNumber;
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{10,20}$", message = "Phone number format is invalid")
    private String phone;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{10,20}$", message = "Mobile phone number format is invalid")
    private String mobilePhone;
    
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;
    
    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;
    
    @Size(max = 10, message = "Zip code must not exceed 10 characters")
    private String zipCode;
    
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;
    
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @Pattern(regexp = "MALE|FEMALE|OTHER", message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;
    
    @Pattern(regexp = "SINGLE|MARRIED|DIVORCED|WIDOWED", message = "Invalid marital status")
    private String maritalStatus;
    
    @Size(max = 50, message = "Nationality must not exceed 50 characters")
    private String nationality;
    
    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    private String emergencyContactName;
    
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{10,20}$", message = "Emergency contact phone format is invalid")
    private String emergencyContactPhone;
    
    @Size(max = 50, message = "Emergency contact relation must not exceed 50 characters")
    private String emergencyContactRelation;
    
    @NotNull(message = "Department is required")
    private Long departmentId;
    
    @NotNull(message = "Position is required")
    private Long positionId;
    
    private Long managerId;
    
    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
    
    @NotNull(message = "Employee status is required")
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be positive")
    @Digits(integer = 10, fraction = 2, message = "Salary format is invalid")
    private BigDecimal salary;
    
    @Size(max = 20, message = "Salary grade must not exceed 20 characters")
    private String salaryGrade;
    
    @Pattern(regexp = "FULL_TIME|PART_TIME|CONTRACT|INTERN", message = "Invalid employment type")
    private String employmentType = "FULL_TIME";
    
    @Pattern(regexp = "OFFICE|REMOTE|HYBRID", message = "Invalid work location")
    private String workLocation = "OFFICE";
    
    @Size(max = 500, message = "Skills must not exceed 500 characters")
    private String skills;
    
    @Size(max = 500, message = "Education must not exceed 500 characters")
    private String education;
    
    @Size(max = 500, message = "Certifications must not exceed 500 characters")
    private String certifications;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    private String profileImageUrl;
    
    private boolean enabled = true;
}
```