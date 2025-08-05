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

### PayType Enum
```java
package com.example.demo.employee.entity;

public enum PayType {
    SALARIED,
    HOURLY
}
```

### Employee Entity
### Employee Entity
```java
package com.example.demo.employee.entity;

import com.example.demo.config.security.EncryptedStringConverter;
import com.example.demo.department.entity.Department;
import com.example.demo.position.entity.Position;
import com.example.demo.payroll.entity.PayrollLedger;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees",
    indexes = {
        @Index(name = "idx_employee_number", columnList = "employee_number", unique = true),
        @Index(name = "idx_employee_email", columnList = "email", unique = true),
        @Index(name = "idx_employee_department_id", columnList = "department_id"),
        @Index(name = "idx_employee_position_id", columnList = "position_id"),
        @Index(name = "idx_employee_status", columnList = "status")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_number", nullable = false, unique = true, length = 20)
    private String employeeNumber;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "mobile_phone", length = 20)
    private String mobilePhone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(name = "country", length = 100)
    private String country;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "date_of_birth_encrypted")
    private String dateOfBirth; // Encrypted

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 20)
    private MaritalStatus maritalStatus;

    @Column(name = "nationality", length = 50)
    private String nationality;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "position_id", nullable = false)
    private Long positionId;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20)
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", nullable = false, length = 10)
    private PayType payType = PayType.SALARY;

    @Column(name = "salary", precision = 12, scale = 2)
    private BigDecimal salary;

    @Column(name = "hourly_rate", precision = 8, scale = 2)
    private BigDecimal hourlyRate;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "bank_account_encrypted")
    private String bankAccount; // Encrypted

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "tax_id_encrypted")
    private String taxId; // Encrypted

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", insertable = false, updatable = false)
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", insertable = false, updatable = false)
    private Employee manager;

    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private Set<Employee> directReports = new HashSet<>();

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<PayrollLedger> payrollLedgers = new HashSet<>();

    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
```
itory Interface
### EmployeeRepository
```java
package com.example.demo.employee.repository;

import com.example.demo.employee.entity.Employee;
import com.example.demo.employee.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
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

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.Instant;
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
  
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;
  
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;
  
    @Size(max = 20, message = "Zip code must not exceed 20 characters")
    private String zipCode;
  
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;
  
    // Sensitive fields are strings in DTO, service layer handles encryption/decryption
    private String dateOfBirth;
  
    private String gender;
  
    private String maritalStatus;
  
    @Size(max = 50, message = "Nationality must not exceed 50 characters")
    private String nationality;
  
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
  
    private String employmentType;
  
    private String payType;
  
    @DecimalMin(value = "0.0", inclusive = true, message = "Salary must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Salary format is invalid")
    private BigDecimal salary;
  
    @DecimalMin(value = "0.0", inclusive = true, message = "Hourly rate must be non-negative")
    @Digits(integer = 6, fraction = 2, message = "Hourly rate format is invalid")
    private BigDecimal hourlyRate;
  
    // Sensitive fields are strings in DTO
    private String bankAccount;
    private String taxId;
  
    private boolean enabled;
  
    private Instant createdAt;
  
    private Instant updatedAt;
  
    private String fullName; // firstName + lastName
}
```

### EmployeeCreateRequest
```java
package com.example.demo.employee.dto;

import lombok.Data;

@Data
public class EmployeeCreateRequest {
    // To simplify the design and reduce redundancy, the EmployeeDto will be used for
    // create and update operations. The @Valid annotation in the controller will
    // ensure the DTO's constraints are enforced. This avoids maintaining a separate
    // but nearly identical DTO for creation requests.
}
```

## Service Implementation

### EmployeeServiceImpl
```java
package com.example.demo.employee.service.impl;

import com.example.demo.employee.dto.EmployeeDto;
import com.example.demo.employee.entity.Employee;
import com.example.demo.employee.exception.EmployeeNotFoundException;
import com.example.demo.employee.exception.SalaryValidationException;
import com.example.demo.employee.repository.EmployeeRepository;
import com.example.demo.employee.service.EmployeeService;
import com.example.demo.position.entity.Position;
import com.example.demo.position.repository.PositionRepository;
import com.example.demo.util.EncryptionService; // Assume this service exists for PII
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final ModelMapper modelMapper;
    private final EncryptionService encryptionService;

    @Override
    @Transactional
    public EmployeeDto createEmployee(EmployeeDto employeeDto) {
        // 1. Validate salary against the position's defined range
        validateSalary(employeeDto.getPositionId(), employeeDto.getSalary());

        Employee employee = modelMapper.map(employeeDto, Employee.class);
      
        // 2. Encrypt sensitive data before saving
        encryptSensitiveData(employee, employeeDto);

        Employee savedEmployee = employeeRepository.save(employee);
        return convertToDto(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));

        // 1. Validate salary against the position's defined range
        validateSalary(employeeDto.getPositionId(), employeeDto.getSalary());

        // Map non-sensitive fields from DTO to entity
        modelMapper.map(employeeDto, existingEmployee);
      
        // 2. Encrypt sensitive data before saving
        encryptSensitiveData(existingEmployee, employeeDto);

        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        return convertToDto(updatedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));
        // 3. Decrypt sensitive data when retrieving
        return convertToDto(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDto> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    private void validateSalary(Long positionId, BigDecimal salary) {
        if (positionId == null || salary == null) {
            return; // Cannot validate if position or salary is not provided
        }
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Position not found with id: " + positionId));

        if (position.getMinSalary() != null && salary.compareTo(position.getMinSalary()) < 0) {
            throw new SalaryValidationException("Salary is below the minimum for this position.");
        }
        if (position.getMaxSalary() != null && salary.compareTo(position.getMaxSalary()) > 0) {
            throw new SalaryValidationException("Salary is above the maximum for this position.");
        }
    }

    private void encryptSensitiveData(Employee employee, EmployeeDto dto) {
        if (dto.getDateOfBirth() != null) {
            employee.setDateOfBirth(encryptionService.encrypt(dto.getDateOfBirth()));
        }
        if (dto.getBankAccount() != null) {
            employee.setBankAccount(encryptionService.encrypt(dto.getBankAccount()));
        }
        if (dto.getTaxId() != null) {
            employee.setTaxId(encryptionService.encrypt(dto.getTaxId()));
        }
    }

    private EmployeeDto convertToDto(Employee employee) {
        EmployeeDto dto = modelMapper.map(employee, EmployeeDto.class);
      
        // Decrypt sensitive data for display
        if (employee.getDateOfBirth() != null) {
            dto.setDateOfBirth(encryptionService.decrypt(employee.getDateOfBirth()));
        }
        if (employee.getBankAccount() != null) {
            // For security, bank account might be masked or omitted in general DTOs
            dto.setBankAccount("****" + encryptionService.decrypt(employee.getBankAccount()).substring(4));
        }
        if (employee.getTaxId() != null) {
            dto.setTaxId(encryptionService.decrypt(employee.getTaxId()));
        }
      
        // Populate transient fields for display purposes
        if (employee.getDepartment() != null) {
            dto.setDepartmentName(employee.getDepartment().getName());
        }
        if (employee.getPosition() != null) {
            dto.setPositionName(employee.getPosition().getJobTitle());
        }
        dto.setFullName(employee.getFullName());
      
        return dto;
    }
}
```

## Controller Implementation

### EmployeeController
```java
package com.example.demo.employee.controller;

import com.example.demo.employee.dto.EmployeeDto;
import com.example.demo.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_CREATE')")
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeDto employeeDto) {
        EmployeeDto createdEmployee = employeeService.createEmployee(employeeDto);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        EmployeeDto employeeDto = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employeeDto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(Pageable pageable) {
        Page<EmployeeDto> employees = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDto employeeDto) {
        EmployeeDto updatedEmployee = employeeService.updateEmployee(id, employeeDto);
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_DELETE')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
```