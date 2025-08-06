package com.example.demo.employee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.ArrayList;

/**
 * DTO representing the result of an employee import operation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeImportResult {
    
    private int totalRecords;
    private int successfulImports;
    private int failedImports;
    private int skippedRecords;
    
    @Builder.Default
    private List<EmployeeImportError> errors = new ArrayList<>();
    
    @Builder.Default
    private List<EmployeeDto> importedEmployees = new ArrayList<>();
    
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    
    private String importSummary;
    private long processingTimeMs;
    
    /**
     * Add an import error
     */
    public void addError(int rowNumber, String field, String value, String errorMessage) {
        errors.add(EmployeeImportError.builder()
                .rowNumber(rowNumber)
                .field(field)
                .value(value)
                .errorMessage(errorMessage)
                .build());
    }
    
    /**
     * Add a warning message
     */
    public void addWarning(String warning) {
        warnings.add(warning);
    }
    
    /**
     * Add an imported employee
     */
    public void addImportedEmployee(EmployeeDto employee) {
        importedEmployees.add(employee);
    }
    
    /**
     * Check if import was successful (no errors)
     */
    public boolean isSuccessful() {
        return errors.isEmpty();
    }
    
    /**
     * Check if import has warnings
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    /**
     * Get success rate as percentage
     */
    public double getSuccessRate() {
        if (totalRecords == 0) return 0.0;
        return (double) successfulImports / totalRecords * 100.0;
    }
    
    /**
     * Generate import summary
     */
    public void generateSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Import completed: ");
        summary.append(successfulImports).append(" successful, ");
        summary.append(failedImports).append(" failed, ");
        summary.append(skippedRecords).append(" skipped ");
        summary.append("out of ").append(totalRecords).append(" total records.");
        
        if (hasWarnings()) {
            summary.append(" ").append(warnings.size()).append(" warnings generated.");
        }
        
        this.importSummary = summary.toString();
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeeImportError {
        private int rowNumber;
        private String field;
        private String value;
        private String errorMessage;
        
        @Override
        public String toString() {
            return String.format("Row %d, Field '%s', Value '%s': %s", 
                    rowNumber, field, value, errorMessage);
        }
    }
}