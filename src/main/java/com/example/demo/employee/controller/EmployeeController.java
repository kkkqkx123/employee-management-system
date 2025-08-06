package com.example.demo.employee.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.employee.dto.*;
import com.example.demo.employee.entity.EmployeeStatus;
import com.example.demo.employee.service.EmployeeService;
import com.example.demo.employee.service.EmployeeImportService;
import com.example.demo.employee.service.EmployeeExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee Management", description = "APIs for managing employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeImportService employeeImportService;
    private final EmployeeExportService employeeExportService;

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_CREATE')")
    @Operation(summary = "Create a new employee", description = "Creates a new employee with comprehensive information")
    public ResponseEntity<ApiResponse<EmployeeDto>> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest createRequest) {
        log.info("Creating employee with email: {}", createRequest.getEmail());
        
        EmployeeDto createdEmployee = employeeService.createEmployee(createRequest);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(createdEmployee, "Employee created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
    @Operation(summary = "Update employee", description = "Updates an existing employee")
    public ResponseEntity<ApiResponse<EmployeeDto>> updateEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest updateRequest) {
        log.info("Updating employee with ID: {}", id);
        
        EmployeeDto updatedEmployee = employeeService.updateEmployee(id, updateRequest);
        
        return ResponseEntity.ok(ApiResponse.success(updatedEmployee, "Employee updated successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get employee by ID", description = "Retrieves an employee by their ID")
    public ResponseEntity<ApiResponse<EmployeeDto>> getEmployeeById(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        log.info("Getting employee with ID: {}", id);
        
        EmployeeDto employee = employeeService.getEmployeeById(id);
        
        return ResponseEntity.ok(ApiResponse.success(employee));
    }

    @GetMapping("/employee-number/{employeeNumber}")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get employee by employee number", description = "Retrieves an employee by their employee number")
    public ResponseEntity<ApiResponse<EmployeeDto>> getEmployeeByEmployeeNumber(
            @Parameter(description = "Employee number") @PathVariable String employeeNumber) {
        log.info("Getting employee with employee number: {}", employeeNumber);
        
        EmployeeDto employee = employeeService.getEmployeeByEmployeeNumber(employeeNumber);
        
        return ResponseEntity.ok(ApiResponse.success(employee));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get all employees", description = "Retrieves all employees with pagination")
    public ResponseEntity<ApiResponse<Page<EmployeeDto>>> getAllEmployees(Pageable pageable) {
        log.info("Getting all employees with pagination");
        
        Page<EmployeeDto> employees = employeeService.getAllEmployees(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get employees by department", description = "Retrieves employees in a specific department")
    public ResponseEntity<ApiResponse<Page<EmployeeDto>>> getEmployeesByDepartment(
            @Parameter(description = "Department ID") @PathVariable Long departmentId,
            Pageable pageable) {
        log.info("Getting employees for department: {}", departmentId);
        
        Page<EmployeeDto> employees = employeeService.getEmployeesByDepartmentId(departmentId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get employees by status", description = "Retrieves employees with a specific status")
    public ResponseEntity<ApiResponse<Page<EmployeeDto>>> getEmployeesByStatus(
            @Parameter(description = "Employee status") @PathVariable EmployeeStatus status,
            Pageable pageable) {
        log.info("Getting employees with status: {}", status);
        
        Page<EmployeeDto> employees = employeeService.getEmployeesByStatus(status, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Search employees", description = "Searches employees by name, email, or employee number")
    public ResponseEntity<ApiResponse<Page<EmployeeDto>>> searchEmployees(
            @Parameter(description = "Search term") @RequestParam String q,
            Pageable pageable) {
        log.info("Searching employees with term: {}", q);
        
        Page<EmployeeDto> employees = employeeService.searchEmployees(q, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_DELETE')")
    @Operation(summary = "Delete employee", description = "Deletes an employee")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        log.info("Deleting employee with ID: {}", id);
        
        employeeService.deleteEmployee(id);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Employee deleted successfully"));
    }

    @GetMapping("/exists/employee-number/{employeeNumber}")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Check if employee number exists", description = "Checks if an employee number is already in use")
    public ResponseEntity<ApiResponse<Boolean>> existsByEmployeeNumber(
            @Parameter(description = "Employee number") @PathVariable String employeeNumber) {
        boolean exists = employeeService.existsByEmployeeNumber(employeeNumber);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @GetMapping("/exists/email/{email}")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Check if email exists", description = "Checks if an email is already in use")
    public ResponseEntity<ApiResponse<Boolean>> existsByEmail(
            @Parameter(description = "Email address") @PathVariable String email) {
        boolean exists = employeeService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @PostMapping("/search/advanced")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Advanced search employees", description = "Searches employees with multiple criteria")
    public ResponseEntity<ApiResponse<Page<EmployeeDto>>> advancedSearchEmployees(
            @Valid @RequestBody EmployeeSearchCriteria criteria,
            Pageable pageable) {
        log.info("Advanced search employees with criteria");
        
        Page<EmployeeDto> employees = employeeService.searchEmployees(criteria, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    // Import/Export endpoints
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('EMPLOYEE_IMPORT')")
    @Operation(summary = "Import employees from Excel", description = "Imports employees from Excel file")
    public ResponseEntity<ApiResponse<EmployeeImportResult>> importEmployees(
            @Parameter(description = "Excel file containing employee data") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Import options") @RequestParam(required = false) Map<String, Object> options) {
        log.info("Importing employees from file: {}", file.getOriginalFilename());
        
        if (options == null) {
            options = new HashMap<>();
        }
        
        EmployeeImportResult result = employeeImportService.importFromExcel(file, options);
        
        return ResponseEntity.ok(ApiResponse.success(result, "Import completed"));
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('EMPLOYEE_IMPORT')")
    @Operation(summary = "Import employees from CSV", description = "Imports employees from CSV file")
    public ResponseEntity<ApiResponse<EmployeeImportResult>> importEmployeesFromCsv(
            @Parameter(description = "CSV file containing employee data") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Import options") @RequestParam(required = false) Map<String, Object> options) {
        log.info("Importing employees from CSV file: {}", file.getOriginalFilename());
        
        if (options == null) {
            options = new HashMap<>();
        }
        
        EmployeeImportResult result = employeeImportService.importFromCsv(file, options);
        
        return ResponseEntity.ok(ApiResponse.success(result, "CSV import completed"));
    }

    @PostMapping(value = "/import/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Validate import file", description = "Validates import file format and structure")
    public ResponseEntity<ApiResponse<EmployeeImportResult>> validateImportFile(
            @Parameter(description = "File to validate") @RequestParam("file") MultipartFile file) {
        log.info("Validating import file: {}", file.getOriginalFilename());
        
        EmployeeImportResult result = employeeImportService.validateImportFile(file);
        
        return ResponseEntity.ok(ApiResponse.success(result, "File validation completed"));
    }

    @PostMapping(value = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Preview import data", description = "Previews import data without actually importing")
    public ResponseEntity<ApiResponse<EmployeeImportResult>> previewImport(
            @Parameter(description = "File to preview") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Maximum rows to preview") @RequestParam(defaultValue = "10") int maxRows) {
        log.info("Previewing import file: {}", file.getOriginalFilename());
        
        EmployeeImportResult result = employeeImportService.previewImport(file, maxRows);
        
        return ResponseEntity.ok(ApiResponse.success(result, "Import preview completed"));
    }

    @GetMapping("/import/template")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Download import template", description = "Downloads Excel template for employee import")
    public ResponseEntity<byte[]> downloadImportTemplate(
            @Parameter(description = "Include example data") @RequestParam(defaultValue = "false") boolean includeExamples) {
        log.info("Downloading import template with examples: {}", includeExamples);
        
        byte[] template = employeeImportService.getImportTemplate(includeExamples);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "employee-import-template.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(template);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('EMPLOYEE_EXPORT')")
    @Operation(summary = "Export employees", description = "Exports employees based on criteria")
    public ResponseEntity<byte[]> exportEmployees(
            @Valid @RequestBody EmployeeExportRequest exportRequest) {
        log.info("Exporting employees to format: {}", exportRequest.getFormat());
        
        byte[] exportData;
        String contentType;
        String fileExtension;
        
        switch (exportRequest.getFormat()) {
            case EXCEL:
                exportData = employeeExportService.exportToExcel(exportRequest);
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                fileExtension = "xlsx";
                break;
            case CSV:
                exportData = employeeExportService.exportToCsv(exportRequest);
                contentType = "text/csv";
                fileExtension = "csv";
                break;
            case PDF:
                exportData = employeeExportService.exportToPdf(exportRequest);
                contentType = "application/pdf";
                fileExtension = "pdf";
                break;
            default:
                throw new IllegalArgumentException("Unsupported export format: " + exportRequest.getFormat());
        }
        
        String filename = employeeExportService.generateExportFilename(exportRequest);
        if (!filename.endsWith("." + fileExtension)) {
            filename += "." + fileExtension;
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
    }

    @PostMapping("/export/by-ids")
    @PreAuthorize("hasAuthority('EMPLOYEE_EXPORT')")
    @Operation(summary = "Export specific employees", description = "Exports specific employees by their IDs")
    public ResponseEntity<byte[]> exportEmployeesByIds(
            @Parameter(description = "Employee IDs to export") @RequestBody List<Long> employeeIds,
            @Valid @RequestBody EmployeeExportRequest exportRequest) {
        log.info("Exporting {} employees by IDs", employeeIds.size());
        
        byte[] exportData = employeeExportService.exportEmployeesByIds(employeeIds, exportRequest);
        
        String filename = employeeExportService.generateExportFilename(exportRequest);
        String contentType = getContentTypeForFormat(exportRequest.getFormat());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
    }

    @GetMapping("/import/info")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get import information", description = "Gets information about import capabilities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getImportInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("supportedFormats", employeeImportService.getSupportedFormats());
        info.put("maxFileSize", employeeImportService.getMaxFileSize());
        info.put("maxRecordCount", employeeImportService.getMaxRecordCount());
        info.put("fieldMapping", employeeImportService.getFieldMapping());
        info.put("requiredFields", employeeImportService.getRequiredFields());
        info.put("optionalFields", employeeImportService.getOptionalFields());
        
        return ResponseEntity.ok(ApiResponse.success(info));
    }

    @GetMapping("/export/info")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get export information", description = "Gets information about export capabilities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExportInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("supportedFormats", employeeExportService.getSupportedFormats());
        info.put("maxExportRecords", employeeExportService.getMaxExportRecords());
        
        return ResponseEntity.ok(ApiResponse.success(info));
    }

    private String getContentTypeForFormat(EmployeeExportRequest.ExportFormat format) {
        switch (format) {
            case EXCEL:
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case CSV:
                return "text/csv";
            case PDF:
                return "application/pdf";
            default:
                return "application/octet-stream";
        }
    }
}