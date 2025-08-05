package com.example.demo.common.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Utility class for custom validation operations.
 * 
 * Provides business-specific validation methods that complement
 * standard Bean Validation annotations.
 */
@UtilityClass
public class ValidationUtil {
    
    private static final Pattern EMPLOYEE_NUMBER_PATTERN = Pattern.compile("^EMP\\d{6}$");
    private static final Pattern DEPARTMENT_CODE_PATTERN = Pattern.compile("^DEPT\\d{4}$");
    private static final Pattern POSITION_CODE_PATTERN = Pattern.compile("^POS\\d{4}$");
    
    /**
     * Validates employee number format (EMP followed by 6 digits)
     */
    public static boolean isValidEmployeeNumber(String employeeNumber) {
        return StringUtil.isNotEmpty(employeeNumber) && 
               EMPLOYEE_NUMBER_PATTERN.matcher(employeeNumber).matches();
    }
    
    /**
     * Validates department code format (DEPT followed by 4 digits)
     */
    public static boolean isValidDepartmentCode(String departmentCode) {
        return StringUtil.isNotEmpty(departmentCode) && 
               DEPARTMENT_CODE_PATTERN.matcher(departmentCode).matches();
    }
    
    /**
     * Validates position code format (POS followed by 4 digits)
     */
    public static boolean isValidPositionCode(String positionCode) {
        return StringUtil.isNotEmpty(positionCode) && 
               POSITION_CODE_PATTERN.matcher(positionCode).matches();
    }
    
    /**
     * Validates that a date of birth is reasonable for an employee
     */
    public static boolean isValidDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return false;
        }
        
        LocalDate now = LocalDate.now();
        LocalDate minDate = now.minusYears(100); // Maximum age 100
        LocalDate maxDate = now.minusYears(16);  // Minimum age 16
        
        return !dateOfBirth.isBefore(minDate) && !dateOfBirth.isAfter(maxDate);
    }
    
    /**
     * Validates that a hire date is reasonable
     */
    public static boolean isValidHireDate(LocalDate hireDate) {
        if (hireDate == null) {
            return false;
        }
        
        LocalDate now = LocalDate.now();
        LocalDate minDate = now.minusYears(50); // Company founded max 50 years ago
        LocalDate maxDate = now.plusDays(30);   // Can be hired up to 30 days in future
        
        return !hireDate.isBefore(minDate) && !hireDate.isAfter(maxDate);
    }
    
    /**
     * Validates salary range
     */
    public static boolean isValidSalary(Double salary) {
        return salary != null && salary > 0 && salary <= 1000000; // Max salary 1M
    }
    
    /**
     * Validates that minimum salary is less than maximum salary
     */
    public static boolean isValidSalaryRange(Double minSalary, Double maxSalary) {
        if (minSalary == null || maxSalary == null) {
            return false;
        }
        return minSalary > 0 && maxSalary > 0 && minSalary <= maxSalary;
    }
    
    /**
     * Validates password strength
     */
    public static boolean isValidPassword(String password) {
        if (StringUtil.isEmpty(password) || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
    
    /**
     * Validates username format
     */
    public static boolean isValidUsername(String username) {
        if (StringUtil.isEmpty(username)) {
            return false;
        }
        
        // Username should be 3-20 characters, alphanumeric and underscore only
        Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
        return usernamePattern.matcher(username).matches();
    }
    
    /**
     * Validates that a string contains only letters and spaces
     */
    public static boolean isValidName(String name) {
        if (StringUtil.isEmpty(name)) {
            return false;
        }
        
        Pattern namePattern = Pattern.compile("^[a-zA-Z\\s]{2,50}$");
        return namePattern.matcher(name.trim()).matches();
    }
    
    /**
     * Validates postal code format (flexible for different countries)
     */
    public static boolean isValidPostalCode(String postalCode) {
        if (StringUtil.isEmpty(postalCode)) {
            return false;
        }
        
        // Basic validation - alphanumeric, spaces, hyphens, 3-10 characters
        Pattern postalPattern = Pattern.compile("^[a-zA-Z0-9\\s\\-]{3,10}$");
        return postalPattern.matcher(postalCode.trim()).matches();
    }
}