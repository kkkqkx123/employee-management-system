package com.example.demo.employee.exception;

/**
 * Exception thrown when employee export operations fail
 */
public class EmployeeExportException extends RuntimeException {
    
    private final String exportFormat;
    private final int recordCount;
    
    public EmployeeExportException(String message) {
        super(message);
        this.exportFormat = null;
        this.recordCount = -1;
    }
    
    public EmployeeExportException(String message, Throwable cause) {
        super(message, cause);
        this.exportFormat = null;
        this.recordCount = -1;
    }
    
    public EmployeeExportException(String message, String exportFormat) {
        super(message);
        this.exportFormat = exportFormat;
        this.recordCount = -1;
    }
    
    public EmployeeExportException(String message, String exportFormat, int recordCount) {
        super(message);
        this.exportFormat = exportFormat;
        this.recordCount = recordCount;
    }
    
    public EmployeeExportException(String message, String exportFormat, int recordCount, Throwable cause) {
        super(message, cause);
        this.exportFormat = exportFormat;
        this.recordCount = recordCount;
    }
    
    public String getExportFormat() {
        return exportFormat;
    }
    
    public int getRecordCount() {
        return recordCount;
    }
    
    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder(super.getMessage());
        
        if (exportFormat != null) {
            message.append(" (Format: ").append(exportFormat);
            
            if (recordCount > 0) {
                message.append(", Records: ").append(recordCount);
            }
            
            message.append(")");
        }
        
        return message.toString();
    }
}