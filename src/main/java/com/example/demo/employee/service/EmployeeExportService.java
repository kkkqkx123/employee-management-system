package com.example.demo.employee.service;

import com.example.demo.employee.dto.EmployeeExportRequest;
import com.example.demo.employee.dto.EmployeeSearchCriteria;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * Service interface for employee export operations
 */
public interface EmployeeExportService {
    
    /**
     * Export employees to Excel format
     * @param request Export request with criteria and options
     * @return Excel file as byte array
     */
    byte[] exportToExcel(EmployeeExportRequest request);
    
    /**
     * Export employees to CSV format
     * @param request Export request with criteria and options
     * @return CSV file as byte array
     */
    byte[] exportToCsv(EmployeeExportRequest request);
    
    /**
     * Export employees to PDF format
     * @param request Export request with criteria and options
     * @return PDF file as byte array
     */
    byte[] exportToPdf(EmployeeExportRequest request);
    
    /**
     * Export employees based on search criteria
     * @param criteria Search criteria to filter employees
     * @param request Export request with format and options
     * @return Exported file as byte array
     */
    byte[] exportEmployees(EmployeeSearchCriteria criteria, EmployeeExportRequest request);
    
    /**
     * Export specific employees by IDs
     * @param employeeIds List of employee IDs to export
     * @param request Export request with format and options
     * @return Exported file as byte array
     */
    byte[] exportEmployeesByIds(List<Long> employeeIds, EmployeeExportRequest request);
    
    /**
     * Export employees to output stream
     * @param request Export request with criteria and options
     * @param outputStream Output stream to write to
     */
    void exportToStream(EmployeeExportRequest request, ByteArrayOutputStream outputStream);
    
    /**
     * Get export statistics
     * @param criteria Search criteria to filter employees
     * @return Map with export statistics
     */
    Map<String, Object> getExportStatistics(EmployeeSearchCriteria criteria);
    
    /**
     * Validate export request
     * @param request Export request to validate
     * @return List of validation errors, empty if valid
     */
    List<String> validateExportRequest(EmployeeExportRequest request);
    
    /**
     * Get supported export formats
     * @return List of supported export formats
     */
    List<EmployeeExportRequest.ExportFormat> getSupportedFormats();
    
    /**
     * Get maximum number of records allowed per export
     * @return Maximum record count
     */
    int getMaxExportRecords();
    
    /**
     * Check if user has permission to export sensitive data
     * @param userId User ID requesting export
     * @return true if user can export sensitive data
     */
    boolean canExportSensitiveData(Long userId);
    
    /**
     * Generate export filename
     * @param request Export request
     * @return Generated filename with extension
     */
    String generateExportFilename(EmployeeExportRequest request);
    
    /**
     * Get export template for specific format
     * @param format Export format
     * @return Template as byte array
     */
    byte[] getExportTemplate(EmployeeExportRequest.ExportFormat format);
}