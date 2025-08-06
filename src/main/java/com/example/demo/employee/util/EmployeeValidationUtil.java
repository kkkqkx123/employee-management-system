package com.example.demo.employee.util;

import com.example.demo.employee.dto.EmployeeCreateRequest;
import com.example.demo.employee.dto.EmployeeUpdateRequest;
import com.example.demo.employee.entity.Employee;
import com.example.demo.employee.entity.EmployeeStatus;
import com.example.demo.employee.entity.PayType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for employee data validation
 */
public class EmployeeValidationUtil {
    
    // Regex patterns for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9\\s\\-\\(\\)]{7,20}$"
    );
    
    private static final Pattern EMPLOYEE_NUMBER_PATTERN = Pattern.compile(
        "^[A-Z0-9\\-]{3,20}$"
    );
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z\\s\\-\\.]{2,50}$"
    );
    
    // Constants
    private static final int MIN_AGE = 16;
    private static final int MAX_AGE = 100;
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;
    private static final BigDecimal MIN_SALARY = new BigDecimal("0.01");
    private static final BigDecimal MAX_SALARY = new BigDecimal("10000000.00");
    private static final BigDecimal MIN_HOURLY_RATE = new BigDecimal("0.01");
    private static final BigDecimal MAX_HOURLY_RATE = new BigDecimal("1000.00");
    
    /**
     * Validate employee create request
     */
    public static List<String> validateCreateRequest(EmployeeCreateRequest request) {
        List<String> errors = new ArrayList<>();
        
        // Validate required fields
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            errors.add("First name is required");
        } else if (!isValidName(request.getFirstName())) {
            errors.add("First name contains invalid characters or is too long/short");
        }
        
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            errors.add("Last name is required");
        } else if (!isValidName(request.getLastName())) {
            errors.add("Last name contains invalid characters or is too long/short");
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            errors.add("Email is required");
        } else if (!isValidEmail(request.getEmail())) {
            errors.add("Email format is invalid");
        }
        
        if (request.getDepartmentId() == null) {
            errors.add("Department is required");
        }
        
        if (request.getPositionId() == null) {
            errors.add("Position is required");
        }
        
        if (request.getHireDate() == null) {
            errors.add("Hire date is required");
        } else if (request.getHireDate().isAfter(LocalDate.now())) {
            errors.add("Hire date cannot be in the future");
        }
        
        if (request.getStatus() == null) {
            errors.add("Employee status is required");
        }
        
        if (request.getPayType() == null) {
            errors.add("Pay type is required");
        }
        
        // Validate optional fields
        validateOptionalFields(request.getPhone(), request.getMobilePhone(), 
                             request.getDateOfBirth(), request.getSalary(), 
                             request.getHourlyRate(), request.getPayType(), errors);
        
        return errors;
    }
    
    /**
     * Validate employee update request
     */
    public static List<String> validateUpdateRequest(EmployeeUpdateRequest request) {
        List<String> errors = new ArrayList<>();
        
        // Validate required fields (all fields in update request should be non-null if provided)
        if (request.getFirstName() != null && !isValidName(request.getFirstName())) {
            errors.add("First name contains invalid characters or is too long/short");
        }
        
        if (request.getLastName() != null && !isValidName(request.getLastName())) {
            errors.add("Last name contains invalid characters or is too long/short");
        }
        
        if (request.getEmail() != null && !isValidEmail(request.getEmail())) {
            errors.add("Email format is invalid");
        }
        
        if (request.getHireDate() != null && request.getHireDate().isAfter(LocalDate.now())) {
            errors.add("Hire date cannot be in the future");
        }
        
        // Validate termination date
        if (request.getTerminationDate() != null && request.getHireDate() != null) {
            if (request.getTerminationDate().isBefore(request.getHireDate())) {
                errors.add("Termination date cannot be before hire date");
            }
        }
        
        // Validate status consistency
        if (request.getStatus() == EmployeeStatus.TERMINATED && request.getTerminationDate() == null) {
            errors.add("Termination date is required when status is TERMINATED");
        }
        
        if (request.getStatus() != EmployeeStatus.TERMINATED && request.getTerminationDate() != null) {
            errors.add("Termination date should only be set when status is TERMINATED");
        }
        
        // Validate optional fields
        validateOptionalFields(request.getPhone(), request.getMobilePhone(), 
                             request.getDateOfBirth(), request.getSalary(), 
                             request.getHourlyRate(), null, errors);
        
        return errors;
    }
    
    /**
     * Validate employee entity
     */
    public static List<String> validateEmployee(Employee employee) {
        List<String> errors = new ArrayList<>();
        
        if (employee == null) {
            errors.add("Employee cannot be null");
            return errors;
        }
        
        // Validate business rules
        if (employee.getManagerId() != null && employee.getManagerId().equals(employee.getId())) {
            errors.add("Employee cannot be their own manager");
        }
        
        if (employee.getStatus() == EmployeeStatus.TERMINATED && employee.getTerminationDate() == null) {
            errors.add("Terminated employee must have termination date");
        }
        
        if (employee.getPayType() == PayType.SALARY && employee.getSalary() == null) {
            errors.add("Salaried employee must have salary amount");
        }
        
        if (employee.getPayType() == PayType.HOURLY && employee.getHourlyRate() == null) {
            errors.add("Hourly employee must have hourly rate");
        }
        
        return errors;
    }
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate phone number format
     */
    public static boolean isValidPhone(String phone) {
        return phone == null || phone.trim().isEmpty() || PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * Validate employee number format
     */
    public static boolean isValidEmployeeNumber(String employeeNumber) {
        return employeeNumber != null && EMPLOYEE_NUMBER_PATTERN.matcher(employeeNumber.trim()).matches();
    }
    
    /**
     * Validate name format
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.length() >= MIN_NAME_LENGTH && 
               trimmed.length() <= MAX_NAME_LENGTH && 
               NAME_PATTERN.matcher(trimmed).matches();
    }
    
    /**
     * Validate date of birth
     */
    public static boolean isValidDateOfBirth(String dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            return true; // Optional field
        }
        
        try {
            LocalDate birthDate = LocalDate.parse(dateOfBirth);
            LocalDate now = LocalDate.now();
            
            if (birthDate.isAfter(now)) {
                return false; // Cannot be in the future
            }
            
            int age = Period.between(birthDate, now).getYears();
            return age >= MIN_AGE && age <= MAX_AGE;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validate salary amount
     */
    public static boolean isValidSalary(BigDecimal salary) {
        return salary == null || 
               (salary.compareTo(MIN_SALARY) >= 0 && salary.compareTo(MAX_SALARY) <= 0);
    }
    
    /**
     * Validate hourly rate
     */
    public static boolean isValidHourlyRate(BigDecimal hourlyRate) {
        return hourlyRate == null || 
               (hourlyRate.compareTo(MIN_HOURLY_RATE) >= 0 && hourlyRate.compareTo(MAX_HOURLY_RATE) <= 0);
    }
    
    /**
     * Check if employee can be deleted
     */
    public static boolean canDeleteEmployee(Employee employee) {
        if (employee == null) {
            return false;
        }
        
        // Cannot delete if employee has direct reports
        return employee.getDirectReports() == null || employee.getDirectReports().isEmpty();
    }
    
    /**
     * Validate manager assignment
     */
    public static boolean isValidManagerAssignment(Long employeeId, Long managerId) {
        if (managerId == null) {
            return true; // No manager is valid (e.g., CEO)
        }
        
        return !managerId.equals(employeeId); // Cannot be self-manager
    }
    
    /**
     * Validate optional fields helper method
     */
    private static void validateOptionalFields(String phone, String mobilePhone, String dateOfBirth, 
                                             BigDecimal salary, BigDecimal hourlyRate, PayType payType, 
                                             List<String> errors) {
        if (!isValidPhone(phone)) {
            errors.add("Phone number format is invalid");
        }
        
        if (!isValidPhone(mobilePhone)) {
            errors.add("Mobile phone number format is invalid");
        }
        
        if (!isValidDateOfBirth(dateOfBirth)) {
            errors.add("Date of birth is invalid or employee age is outside allowed range");
        }
        
        if (!isValidSalary(salary)) {
            errors.add("Salary amount is outside allowed range");
        }
        
        if (!isValidHourlyRate(hourlyRate)) {
            errors.add("Hourly rate is outside allowed range");
        }
        
        // Validate pay type consistency
        if (payType == PayType.SALARY && salary == null) {
            errors.add("Salary is required for salaried employees");
        }
        
        if (payType == PayType.HOURLY && hourlyRate == null) {
            errors.add("Hourly rate is required for hourly employees");
        }
    }
}