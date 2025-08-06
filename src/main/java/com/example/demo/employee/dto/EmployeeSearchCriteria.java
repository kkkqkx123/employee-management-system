package com.example.demo.employee.dto;

import com.example.demo.employee.entity.EmployeeStatus;
import com.example.demo.employee.entity.EmploymentType;
import com.example.demo.employee.entity.Gender;
import com.example.demo.employee.entity.MaritalStatus;
import com.example.demo.employee.entity.PayType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for advanced employee search criteria
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSearchCriteria {
    
    // Basic search
    private String searchTerm; // Search in name, email, employee number
    
    // Personal information
    private String firstName;
    private String lastName;
    private String email;
    private String employeeNumber;
    private Gender gender;
    private MaritalStatus maritalStatus;
    private String nationality;
    
    // Contact information
    private String phone;
    private String city;
    private String state;
    private String country;
    
    // Employment details
    private List<Long> departmentIds;
    private List<Long> positionIds;
    private List<Long> managerIds;
    private List<EmployeeStatus> statuses;
    private List<EmploymentType> employmentTypes;
    private List<PayType> payTypes;
    
    // Date ranges
    private LocalDate hireDateFrom;
    private LocalDate hireDateTo;
    private LocalDate terminationDateFrom;
    private LocalDate terminationDateTo;
    private LocalDate dateOfBirthFrom;
    private LocalDate dateOfBirthTo;
    
    // Salary ranges
    private BigDecimal salaryFrom;
    private BigDecimal salaryTo;
    private BigDecimal hourlyRateFrom;
    private BigDecimal hourlyRateTo;
    
    // Status filters
    private Boolean enabled;
    private Boolean hasManager; // true = has manager, false = no manager, null = both
    private Boolean hasDirectReports; // true = has direct reports, false = no direct reports, null = both
    
    // Age range (calculated from date of birth)
    private Integer ageFrom;
    private Integer ageTo;
    
    // Years of service range
    private Integer yearsOfServiceFrom;
    private Integer yearsOfServiceTo;
    
    // Sorting options
    private String sortBy; // field name to sort by
    private String sortDirection; // ASC or DESC
    
    // Additional filters
    private List<String> excludeEmployeeNumbers; // Exclude specific employees
    private List<Long> includeEmployeeIds; // Include only specific employees
    
    /**
     * Check if any search criteria is specified
     */
    public boolean hasAnyCriteria() {
        return searchTerm != null || firstName != null || lastName != null || 
               email != null || employeeNumber != null || gender != null || 
               maritalStatus != null || nationality != null || phone != null || 
               city != null || state != null || country != null ||
               (departmentIds != null && !departmentIds.isEmpty()) ||
               (positionIds != null && !positionIds.isEmpty()) ||
               (managerIds != null && !managerIds.isEmpty()) ||
               (statuses != null && !statuses.isEmpty()) ||
               (employmentTypes != null && !employmentTypes.isEmpty()) ||
               (payTypes != null && !payTypes.isEmpty()) ||
               hireDateFrom != null || hireDateTo != null ||
               terminationDateFrom != null || terminationDateTo != null ||
               dateOfBirthFrom != null || dateOfBirthTo != null ||
               salaryFrom != null || salaryTo != null ||
               hourlyRateFrom != null || hourlyRateTo != null ||
               enabled != null || hasManager != null || hasDirectReports != null ||
               ageFrom != null || ageTo != null ||
               yearsOfServiceFrom != null || yearsOfServiceTo != null ||
               (excludeEmployeeNumbers != null && !excludeEmployeeNumbers.isEmpty()) ||
               (includeEmployeeIds != null && !includeEmployeeIds.isEmpty());
    }
    
    /**
     * Check if basic search term is specified
     */
    public boolean hasSearchTerm() {
        return searchTerm != null && !searchTerm.trim().isEmpty();
    }
    
    /**
     * Check if date range criteria is specified
     */
    public boolean hasDateRangeCriteria() {
        return hireDateFrom != null || hireDateTo != null ||
               terminationDateFrom != null || terminationDateTo != null ||
               dateOfBirthFrom != null || dateOfBirthTo != null;
    }
    
    /**
     * Check if salary range criteria is specified
     */
    public boolean hasSalaryRangeCriteria() {
        return salaryFrom != null || salaryTo != null ||
               hourlyRateFrom != null || hourlyRateTo != null;
    }
}