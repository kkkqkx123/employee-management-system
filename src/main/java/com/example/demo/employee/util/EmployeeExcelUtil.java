package com.example.demo.employee.util;

import com.example.demo.employee.dto.EmployeeDto;
import com.example.demo.employee.dto.EmployeeImportResult;
import com.example.demo.employee.entity.EmployeeStatus;
import com.example.demo.employee.entity.EmploymentType;
import com.example.demo.employee.entity.Gender;
import com.example.demo.employee.entity.MaritalStatus;
import com.example.demo.employee.entity.PayType;
import com.example.demo.employee.exception.EmployeeImportException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Utility class for Excel operations related to employee data
 */
public class EmployeeExcelUtil {
    
    // Excel column headers
    public static final String[] IMPORT_HEADERS = {
        "Employee Number", "First Name", "Last Name", "Email", "Phone", 
        "Mobile Phone", "Address", "City", "State", "Zip Code", "Country",
        "Date of Birth", "Gender", "Marital Status", "Nationality",
        "Department ID", "Position ID", "Manager ID", "Hire Date",
        "Status", "Employment Type", "Pay Type", "Salary", "Hourly Rate",
        "Bank Account", "Tax ID"
    };
    
    public static final String[] EXPORT_HEADERS = {
        "Employee Number", "First Name", "Last Name", "Full Name", "Email", 
        "Phone", "Mobile Phone", "Address", "City", "State", "Zip Code", "Country",
        "Date of Birth", "Gender", "Marital Status", "Nationality",
        "Department", "Position", "Manager", "Hire Date", "Termination Date",
        "Status", "Employment Type", "Pay Type", "Salary", "Hourly Rate",
        "Enabled", "Created At", "Updated At"
    };
    
    // Field mapping for import
    private static final Map<String, String> FIELD_MAPPING = new HashMap<>();
    static {
        FIELD_MAPPING.put("Employee Number", "employeeNumber");
        FIELD_MAPPING.put("First Name", "firstName");
        FIELD_MAPPING.put("Last Name", "lastName");
        FIELD_MAPPING.put("Email", "email");
        FIELD_MAPPING.put("Phone", "phone");
        FIELD_MAPPING.put("Mobile Phone", "mobilePhone");
        FIELD_MAPPING.put("Address", "address");
        FIELD_MAPPING.put("City", "city");
        FIELD_MAPPING.put("State", "state");
        FIELD_MAPPING.put("Zip Code", "zipCode");
        FIELD_MAPPING.put("Country", "country");
        FIELD_MAPPING.put("Date of Birth", "dateOfBirth");
        FIELD_MAPPING.put("Gender", "gender");
        FIELD_MAPPING.put("Marital Status", "maritalStatus");
        FIELD_MAPPING.put("Nationality", "nationality");
        FIELD_MAPPING.put("Department ID", "departmentId");
        FIELD_MAPPING.put("Position ID", "positionId");
        FIELD_MAPPING.put("Manager ID", "managerId");
        FIELD_MAPPING.put("Hire Date", "hireDate");
        FIELD_MAPPING.put("Status", "status");
        FIELD_MAPPING.put("Employment Type", "employmentType");
        FIELD_MAPPING.put("Pay Type", "payType");
        FIELD_MAPPING.put("Salary", "salary");
        FIELD_MAPPING.put("Hourly Rate", "hourlyRate");
        FIELD_MAPPING.put("Bank Account", "bankAccount");
        FIELD_MAPPING.put("Tax ID", "taxId");
    }
    
    // Date formatters
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("MM-dd-yyyy")
    }; 
   /**
     * Create Excel template for employee import
     */
    public static byte[] createImportTemplate(boolean includeExamples) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Employee Import Template");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            for (int i = 0; i < IMPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(IMPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }
            
            // Add example data if requested
            if (includeExamples) {
                addExampleData(sheet, workbook);
            }
            
            // Add data validation
            addDataValidation(sheet);
            
            // Auto-size columns
            for (int i = 0; i < IMPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new EmployeeImportException("Failed to create import template", e);
        }
    }
    
    /**
     * Export employees to Excel
     */
    public static byte[] exportToExcel(List<EmployeeDto> employees, Set<String> includeFields) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Employee Export");
            
            // Determine which headers to include
            List<String> headers = getExportHeaders(includeFields);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            for (int i = 0; i < employees.size(); i++) {
                Row row = sheet.createRow(i + 1);
                EmployeeDto employee = employees.get(i);
                
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.createCell(j);
                    setEmployeeCellValue(cell, employee, headers.get(j), dateStyle, currencyStyle);
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new EmployeeImportException("Failed to export employees to Excel", e);
        }
    } 
   /**
     * Parse Excel file and extract employee data
     */
    public static EmployeeImportResult parseExcelFile(InputStream inputStream, String fileName) {
        EmployeeImportResult result = EmployeeImportResult.builder()
                .build();
        
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Validate headers
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                result.addError(1, "Header", "", "Header row is missing");
                return result;
            }
            
            Map<String, Integer> headerMap = validateAndMapHeaders(headerRow, result);
            if (!result.getErrors().isEmpty()) {
                return result;
            }
            
            // Process data rows
            int totalRows = sheet.getLastRowNum();
            result.setTotalRecords(totalRows);
            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    result.setSkippedRecords(result.getSkippedRecords() + 1);
                    continue;
                }
                
                final int currentRowNumber = i + 1;
                try {
                    Map<String, Object> employeeData = parseEmployeeRow(row, headerMap, currentRowNumber, result);
                    if (employeeData != null && result.getErrors().stream()
                            .noneMatch(error -> error.getRowNumber() == currentRowNumber)) {
                        // Convert to EmployeeDto and add to result
                        // This would be handled by the service layer
                        result.setSuccessfulImports(result.getSuccessfulImports() + 1);
                    } else {
                        result.setFailedImports(result.getFailedImports() + 1);
                    }
                } catch (Exception e) {
                    result.addError(currentRowNumber, "Row", "", "Error processing row: " + e.getMessage());
                    result.setFailedImports(result.getFailedImports() + 1);
                }
            }
            
        } catch (IOException e) {
            throw new EmployeeImportException("Failed to parse Excel file: " + fileName, e);
        }
        
        result.generateSummary();
        return result;
    }
    
    /**
     * Validate and map Excel headers
     */
    private static Map<String, Integer> validateAndMapHeaders(Row headerRow, EmployeeImportResult result) {
        Map<String, Integer> headerMap = new HashMap<>();
        Set<String> requiredHeaders = Set.of("First Name", "Last Name", "Email", "Department ID", 
                                           "Position ID", "Hire Date", "Status");
        Set<String> foundHeaders = new HashSet<>();
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String headerValue = getCellValueAsString(cell).trim();
                if (FIELD_MAPPING.containsKey(headerValue)) {
                    headerMap.put(headerValue, i);
                    foundHeaders.add(headerValue);
                }
            }
        }
        
        // Check for missing required headers
        for (String requiredHeader : requiredHeaders) {
            if (!foundHeaders.contains(requiredHeader)) {
                result.addError(1, "Header", requiredHeader, "Required header is missing");
            }
        }
        
        return headerMap;
    }  
  /**
     * Parse employee data from Excel row
     */
    private static Map<String, Object> parseEmployeeRow(Row row, Map<String, Integer> headerMap, 
                                                       int rowNumber, EmployeeImportResult result) {
        Map<String, Object> employeeData = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
            String header = entry.getKey();
            int columnIndex = entry.getValue();
            Cell cell = row.getCell(columnIndex);
            
            try {
                Object value = parseCellValue(cell, header, rowNumber, result);
                if (value != null) {
                    employeeData.put(FIELD_MAPPING.get(header), value);
                }
            } catch (Exception e) {
                result.addError(rowNumber, header, getCellValueAsString(cell), 
                              "Error parsing value: " + e.getMessage());
            }
        }
        
        return employeeData;
    }
    
    /**
     * Parse cell value based on field type
     */
    private static Object parseCellValue(Cell cell, String header, int rowNumber, EmployeeImportResult result) {
        if (cell == null) {
            return null;
        }
        
        String cellValue = getCellValueAsString(cell).trim();
        if (cellValue.isEmpty()) {
            return null;
        }
        
        try {
            switch (header) {
                case "Department ID":
                case "Position ID":
                case "Manager ID":
                    return parseLongValue(cellValue);
                    
                case "Hire Date":
                case "Date of Birth":
                    return parseDate(cellValue);
                    
                case "Salary":
                case "Hourly Rate":
                    return parseBigDecimal(cellValue);
                    
                case "Status":
                    return parseEmployeeStatus(cellValue);
                    
                case "Employment Type":
                    return parseEmploymentType(cellValue);
                    
                case "Pay Type":
                    return parsePayType(cellValue);
                    
                case "Gender":
                    return parseGender(cellValue);
                    
                case "Marital Status":
                    return parseMaritalStatus(cellValue);
                    
                default:
                    return cellValue;
            }
        } catch (Exception e) {
            result.addError(rowNumber, header, cellValue, "Invalid value format: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get cell value as string
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }    
/**
     * Parse date from string with multiple format support
     */
    private static LocalDate parseDate(String dateString) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException ignored) {
                // Try next formatter
            }
        }
        throw new IllegalArgumentException("Unable to parse date: " + dateString);
    }
    
    /**
     * Parse Long value
     */
    private static Long parseLongValue(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + value);
        }
    }
    
    /**
     * Parse BigDecimal value
     */
    private static BigDecimal parseBigDecimal(String value) {
        try {
            // Remove currency symbols and commas
            String cleanValue = value.replaceAll("[,$]", "");
            return new BigDecimal(cleanValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid decimal format: " + value);
        }
    }
    
    /**
     * Parse EmployeeStatus enum
     */
    private static EmployeeStatus parseEmployeeStatus(String value) {
        try {
            return EmployeeStatus.valueOf(value.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid employee status: " + value);
        }
    }
    
    /**
     * Parse EmploymentType enum
     */
    private static EmploymentType parseEmploymentType(String value) {
        try {
            return EmploymentType.valueOf(value.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid employment type: " + value);
        }
    }
    
    /**
     * Parse PayType enum
     */
    private static PayType parsePayType(String value) {
        try {
            return PayType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid pay type: " + value);
        }
    }
    
    /**
     * Parse Gender enum
     */
    private static Gender parseGender(String value) {
        try {
            return Gender.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid gender: " + value);
        }
    }
    
    /**
     * Parse MaritalStatus enum
     */
    private static MaritalStatus parseMaritalStatus(String value) {
        try {
            return MaritalStatus.valueOf(value.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid marital status: " + value);
        }
    }  
  /**
     * Check if row is empty
     */
    private static boolean isEmptyRow(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !getCellValueAsString(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Create header cell style
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
    
    /**
     * Create date cell style
     */
    private static CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd"));
        return style;
    }
    
    /**
     * Create currency cell style
     */
    private static CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("$#,##0.00"));
        return style;
    }
    
    /**
     * Add example data to template
     */
    private static void addExampleData(Sheet sheet, Workbook workbook) {
        Row exampleRow = sheet.createRow(1);
        
        // Add example employee data
        String[] exampleData = {
            "EMP-001", "John", "Doe", "john.doe@company.com", "555-1234",
            "555-5678", "123 Main St", "New York", "NY", "10001", "USA",
            "1990-01-15", "MALE", "SINGLE", "American",
            "1", "1", "", "2023-01-15",
            "ACTIVE", "FULL_TIME", "SALARY", "75000", "",
            "", ""
        };
        
        for (int i = 0; i < exampleData.length && i < IMPORT_HEADERS.length; i++) {
            Cell cell = exampleRow.createCell(i);
            cell.setCellValue(exampleData[i]);
        }
    }
    
    /**
     * Add data validation to template
     */
    private static void addDataValidation(Sheet sheet) {
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        
        // Add validation for Status column
        int statusColumnIndex = Arrays.asList(IMPORT_HEADERS).indexOf("Status");
        if (statusColumnIndex >= 0) {
            String[] statusValues = Arrays.stream(EmployeeStatus.values())
                    .map(Enum::name)
                    .toArray(String[]::new);
            addDropdownValidation(sheet, validationHelper, statusColumnIndex, statusValues);
        }
        
        // Add validation for Gender column
        int genderColumnIndex = Arrays.asList(IMPORT_HEADERS).indexOf("Gender");
        if (genderColumnIndex >= 0) {
            String[] genderValues = Arrays.stream(Gender.values())
                    .map(Enum::name)
                    .toArray(String[]::new);
            addDropdownValidation(sheet, validationHelper, genderColumnIndex, genderValues);
        }
        
        // Add validation for Employment Type column
        int employmentTypeColumnIndex = Arrays.asList(IMPORT_HEADERS).indexOf("Employment Type");
        if (employmentTypeColumnIndex >= 0) {
            String[] employmentTypeValues = Arrays.stream(EmploymentType.values())
                    .map(Enum::name)
                    .toArray(String[]::new);
            addDropdownValidation(sheet, validationHelper, employmentTypeColumnIndex, employmentTypeValues);
        }
        
        // Add validation for Pay Type column
        int payTypeColumnIndex = Arrays.asList(IMPORT_HEADERS).indexOf("Pay Type");
        if (payTypeColumnIndex >= 0) {
            String[] payTypeValues = Arrays.stream(PayType.values())
                    .map(Enum::name)
                    .toArray(String[]::new);
            addDropdownValidation(sheet, validationHelper, payTypeColumnIndex, payTypeValues);
        }
    }  
  /**
     * Add dropdown validation to column
     */
    private static void addDropdownValidation(Sheet sheet, DataValidationHelper validationHelper, 
                                            int columnIndex, String[] values) {
        DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(values);
        CellRangeAddressList addressList = new CellRangeAddressList(1, 1000, columnIndex, columnIndex);
        DataValidation validation = validationHelper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }
    
    /**
     * Get export headers based on included fields
     */
    private static List<String> getExportHeaders(Set<String> includeFields) {
        if (includeFields == null || includeFields.isEmpty()) {
            return Arrays.asList(EXPORT_HEADERS);
        }
        
        List<String> headers = new ArrayList<>();
        for (String header : EXPORT_HEADERS) {
            String fieldName = getFieldNameFromHeader(header);
            if (includeFields.contains(fieldName)) {
                headers.add(header);
            }
        }
        return headers;
    }
    
    /**
     * Get field name from export header
     */
    private static String getFieldNameFromHeader(String header) {
        switch (header) {
            case "Employee Number": return "employeeNumber";
            case "First Name": return "firstName";
            case "Last Name": return "lastName";
            case "Full Name": return "fullName";
            case "Email": return "email";
            case "Phone": return "phone";
            case "Mobile Phone": return "mobilePhone";
            case "Address": return "address";
            case "City": return "city";
            case "State": return "state";
            case "Zip Code": return "zipCode";
            case "Country": return "country";
            case "Date of Birth": return "dateOfBirth";
            case "Gender": return "gender";
            case "Marital Status": return "maritalStatus";
            case "Nationality": return "nationality";
            case "Department": return "departmentName";
            case "Position": return "positionName";
            case "Manager": return "managerName";
            case "Hire Date": return "hireDate";
            case "Termination Date": return "terminationDate";
            case "Status": return "status";
            case "Employment Type": return "employmentType";
            case "Pay Type": return "payType";
            case "Salary": return "salary";
            case "Hourly Rate": return "hourlyRate";
            case "Enabled": return "enabled";
            case "Created At": return "createdAt";
            case "Updated At": return "updatedAt";
            default: return header.toLowerCase().replace(" ", "");
        }
    }
    
    /**
     * Set cell value for employee export
     */
    private static void setEmployeeCellValue(Cell cell, EmployeeDto employee, String header, 
                                           CellStyle dateStyle, CellStyle currencyStyle) {
        switch (header) {
            case "Employee Number":
                cell.setCellValue(employee.getEmployeeNumber());
                break;
            case "First Name":
                cell.setCellValue(employee.getFirstName());
                break;
            case "Last Name":
                cell.setCellValue(employee.getLastName());
                break;
            case "Full Name":
                cell.setCellValue(employee.getFullName());
                break;
            case "Email":
                cell.setCellValue(employee.getEmail());
                break;
            case "Phone":
                cell.setCellValue(employee.getPhone());
                break;
            case "Mobile Phone":
                cell.setCellValue(employee.getMobilePhone());
                break;
            case "Address":
                cell.setCellValue(employee.getAddress());
                break;
            case "City":
                cell.setCellValue(employee.getCity());
                break;
            case "State":
                cell.setCellValue(employee.getState());
                break;
            case "Zip Code":
                cell.setCellValue(employee.getZipCode());
                break;
            case "Country":
                cell.setCellValue(employee.getCountry());
                break;
            case "Date of Birth":
                cell.setCellValue(employee.getDateOfBirth());
                break;
            case "Gender":
                if (employee.getGender() != null) {
                    cell.setCellValue(employee.getGender().name());
                }
                break;
            case "Marital Status":
                if (employee.getMaritalStatus() != null) {
                    cell.setCellValue(employee.getMaritalStatus().name());
                }
                break;
            case "Nationality":
                cell.setCellValue(employee.getNationality());
                break;
            case "Department":
                cell.setCellValue(employee.getDepartmentName());
                break;
            case "Position":
                cell.setCellValue(employee.getPositionName());
                break;
            case "Manager":
                cell.setCellValue(employee.getManagerName());
                break;
            case "Hire Date":
                if (employee.getHireDate() != null) {
                    cell.setCellValue(employee.getHireDate().toString());
                    cell.setCellStyle(dateStyle);
                }
                break;
            case "Termination Date":
                if (employee.getTerminationDate() != null) {
                    cell.setCellValue(employee.getTerminationDate().toString());
                    cell.setCellStyle(dateStyle);
                }
                break;
            case "Status":
                if (employee.getStatus() != null) {
                    cell.setCellValue(employee.getStatus().name());
                }
                break;
            case "Employment Type":
                if (employee.getEmploymentType() != null) {
                    cell.setCellValue(employee.getEmploymentType().name());
                }
                break;
            case "Pay Type":
                if (employee.getPayType() != null) {
                    cell.setCellValue(employee.getPayType().name());
                }
                break;
            case "Salary":
                if (employee.getSalary() != null) {
                    cell.setCellValue(employee.getSalary().doubleValue());
                    cell.setCellStyle(currencyStyle);
                }
                break;
            case "Hourly Rate":
                if (employee.getHourlyRate() != null) {
                    cell.setCellValue(employee.getHourlyRate().doubleValue());
                    cell.setCellStyle(currencyStyle);
                }
                break;
            case "Enabled":
                cell.setCellValue(employee.isEnabled());
                break;
            case "Created At":
                if (employee.getCreatedAt() != null) {
                    cell.setCellValue(employee.getCreatedAt().toString());
                }
                break;
            case "Updated At":
                if (employee.getUpdatedAt() != null) {
                    cell.setCellValue(employee.getUpdatedAt().toString());
                }
                break;
            default:
                cell.setCellValue("");
        }
    }
    
    /**
     * Get field mapping for import
     */
    public static Map<String, String> getFieldMapping() {
        return new HashMap<>(FIELD_MAPPING);
    }
    
    /**
     * Get required fields for import
     */
    public static List<String> getRequiredFields() {
        return Arrays.asList("First Name", "Last Name", "Email", "Department ID", 
                           "Position ID", "Hire Date", "Status");
    }
    
    /**
     * Get optional fields for import
     */
    public static List<String> getOptionalFields() {
        List<String> allFields = Arrays.asList(IMPORT_HEADERS);
        List<String> requiredFields = getRequiredFields();
        return allFields.stream()
                .filter(field -> !requiredFields.contains(field))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}