package com.example.demo.employee.service;

import com.example.demo.employee.dto.EmployeeImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Service interface for employee import operations
 */
public interface EmployeeImportService {
    
    /**
     * Import employees from Excel file
     * @param file Excel file containing employee data
     * @param options Import options and configurations
     * @return Import result with success/failure details
     */
    EmployeeImportResult importFromExcel(MultipartFile file, Map<String, Object> options);
    
    /**
     * Import employees from Excel input stream
     * @param inputStream Excel file input stream
     * @param fileName Original file name
     * @param options Import options and configurations
     * @return Import result with success/failure details
     */
    EmployeeImportResult importFromExcel(InputStream inputStream, String fileName, Map<String, Object> options);
    
    /**
     * Import employees from CSV file
     * @param file CSV file containing employee data
     * @param options Import options and configurations
     * @return Import result with success/failure details
     */
    EmployeeImportResult importFromCsv(MultipartFile file, Map<String, Object> options);
    
    /**
     * Import employees from CSV input stream
     * @param inputStream CSV file input stream
     * @param fileName Original file name
     * @param options Import options and configurations
     * @return Import result with success/failure details
     */
    EmployeeImportResult importFromCsv(InputStream inputStream, String fileName, Map<String, Object> options);
    
    /**
     * Validate import file format and structure
     * @param file File to validate
     * @return Validation result with errors if any
     */
    EmployeeImportResult validateImportFile(MultipartFile file);
    
    /**
     * Get import template as Excel file
     * @param includeExamples Whether to include example data
     * @return Excel template as byte array
     */
    byte[] getImportTemplate(boolean includeExamples);
    
    /**
     * Get supported file formats for import
     * @return List of supported file extensions
     */
    List<String> getSupportedFormats();
    
    /**
     * Get maximum file size allowed for import
     * @return Maximum file size in bytes
     */
    long getMaxFileSize();
    
    /**
     * Get maximum number of records allowed per import
     * @return Maximum record count
     */
    int getMaxRecordCount();
    
    /**
     * Preview import data without actually importing
     * @param file File to preview
     * @param maxRows Maximum rows to preview
     * @return Preview of import data
     */
    EmployeeImportResult previewImport(MultipartFile file, int maxRows);
    
    /**
     * Get field mapping for import columns
     * @return Map of column names to entity field names
     */
    Map<String, String> getFieldMapping();
    
    /**
     * Get required fields for import
     * @return List of required field names
     */
    List<String> getRequiredFields();
    
    /**
     * Get optional fields for import
     * @return List of optional field names
     */
    List<String> getOptionalFields();
}