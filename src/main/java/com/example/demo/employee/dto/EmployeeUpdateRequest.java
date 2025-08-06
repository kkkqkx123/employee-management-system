package com.example.demo.employee.dto;

import com.example.demo.employee.entity.EmployeeStatus;
import com.example.demo.employee.entity.EmploymentType;
import com.example.demo.employee.entity.Gender;
import com.example.demo.employee.entity.MaritalStatus;
import com.example.demo.employee.entity.PayType;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateRequest {

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

    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{7,20}$", message = "Phone number format is invalid")
    private String phone;

    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{7,20}$", message = "Mobile phone number format is invalid")
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

    private String dateOfBirth; // Will be encrypted by service

    private Gender gender;
    
    private MaritalStatus maritalStatus;

    @Size(max = 50, message = "Nationality must not exceed 50 characters")
    private String nationality;

    @NotNull(message = "Department is required")
    private Long departmentId;

    private Long positionId;

    private Long managerId;

    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
    
    private LocalDate terminationDate;

    @NotNull(message = "Employee status is required")
    private EmployeeStatus status;
    
    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    @NotNull(message = "Pay type is required")
    private PayType payType;

    @DecimalMin(value = "0.0", inclusive = true, message = "Salary must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Salary format is invalid")
    private BigDecimal salary;

    @DecimalMin(value = "0.0", inclusive = true, message = "Hourly rate must be non-negative")
    @Digits(integer = 6, fraction = 2, message = "Hourly rate format is invalid")
    private BigDecimal hourlyRate;

    private String bankAccount; // Will be encrypted by service
    private String taxId; // Will be encrypted by service

    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
}