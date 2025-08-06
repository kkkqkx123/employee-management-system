package com.example.demo.employee.exception;

/**
 * Exception thrown when employee import operations fail
 */
public class EmployeeImportException extends RuntimeException {
    
    private final String fileName;
    private final int rowNumber;
    private final String field;
    
    public EmployeeImportException(String message) {
        super(message);
        this.fileName = null;
        this.rowNumber = -1;
        this.field = null;
    }
    
    public EmployeeImportException(String message, Throwable cause) {
        super(message, cause);
        this.fileName = null;
        this.rowNumber = -1;
        this.field = null;
    }
    
    public EmployeeImportException(String message, String fileName) {
        super(message);
        this.fileName = fileName;
        this.rowNumber = -1;
        this.field = null;
    }
    
    public EmployeeImportException(String message, String fileName, int rowNumber) {
        super(message);
        this.fileName = fileName;
        this.rowNumber = rowNumber;
        this.field = null;
    }
    
    public EmployeeImportException(String message, String fileName, int rowNumber, String field) {
        super(message);
        this.fileName = fileName;
        this.rowNumber = rowNumber;
        this.field = field;
    }
    
    public EmployeeImportException(String message, String fileName, int rowNumber, String field, Throwable cause) {
        super(message, cause);
        this.fileName = fileName;
        this.rowNumber = rowNumber;
        this.field = field;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public int getRowNumber() {
        return rowNumber;
    }
    
    public String getField() {
        return field;
    }
    
    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder(super.getMessage());
        
        if (fileName != null) {
            message.append(" (File: ").append(fileName);
            
            if (rowNumber > 0) {
                message.append(", Row: ").append(rowNumber);
            }
            
            if (field != null) {
                message.append(", Field: ").append(field);
            }
            
            message.append(")");
        }
        
        return message.toString();
    }
}