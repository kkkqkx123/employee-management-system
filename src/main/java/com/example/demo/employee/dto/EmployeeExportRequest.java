package com.example.demo.employee.dto;

import com.example.demo.employee.entity.EmployeeStatus;
import com.example.demo.employee.entity.EmploymentType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * DTO for employee export request parameters
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeExportRequest {
    
    // Export format
    @Builder.Default
    private ExportFormat format = ExportFormat.EXCEL;
    
    // File name (without extension)
    private String fileName;
    
    // Fields to include in export
    private Set<String> includeFields;
    
    // Fields to exclude from export
    private Set<String> excludeFields;
    
    // Filter criteria
    private List<Long> departmentIds;
    private List<Long> positionIds;
    private List<EmployeeStatus> statuses;
    private List<EmploymentType> employmentTypes;
    
    // Date range filters
    private LocalDate hireDateFrom;
    private LocalDate hireDateTo;
    
    // Include sensitive data (requires special permission)
    @Builder.Default
    private boolean includeSensitiveData = false;
    
    // Include terminated employees
    @Builder.Default
    private boolean includeTerminated = false;
    
    // Include disabled employees
    @Builder.Default
    private boolean includeDisabled = false;
    
    // Sort options
    @Builder.Default
    private String sortBy = "lastName";
    @Builder.Default
    private String sortDirection = "ASC";
    
    // Template options
    @Builder.Default
    private boolean includeHeaders = true;
    @Builder.Default
    private boolean includeMetadata = true;
    
    // Localization
    @Builder.Default
    private String locale = "en_US";
    
    public enum ExportFormat {
        EXCEL("xlsx"),
        CSV("csv"),
        PDF("pdf");
        
        private final String extension;
        
        ExportFormat(String extension) {
            this.extension = extension;
        }
        
        public String getExtension() {
            return extension;
        }
    }
    
    /**
     * Get default field set for export
     */
    public static Set<String> getDefaultFields() {
        return Set.of(
            "employeeNumber", "firstName", "lastName", "email", 
            "phone", "departmentName", "positionName", "managerName",
            "hireDate", "status", "employmentType", "salary"
        );
    }
    
    /**
     * Get sensitive fields that require special permission
     */
    public static Set<String> getSensitiveFields() {
        return Set.of(
            "dateOfBirth", "bankAccount", "taxId", "address", 
            "city", "state", "zipCode", "country"
        );
    }
    
    /**
     * Check if request includes sensitive data
     */
    public boolean requestsIncludeSensitiveData() {
        if (!includeSensitiveData) return false;
        
        if (includeFields != null) {
            return includeFields.stream().anyMatch(getSensitiveFields()::contains);
        }
        
        if (excludeFields != null) {
            return getSensitiveFields().stream().anyMatch(field -> !excludeFields.contains(field));
        }
        
        return true; // Default includes sensitive data if flag is set
    }
    
    /**
     * Get effective fields to export
     */
    public Set<String> getEffectiveFields() {
        Set<String> fields = includeFields != null ? includeFields : getDefaultFields();
        
        if (excludeFields != null) {
            fields.removeAll(excludeFields);
        }
        
        if (!includeSensitiveData) {
            fields.removeAll(getSensitiveFields());
        }
        
        return fields;
    }
}