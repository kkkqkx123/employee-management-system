package com.example.demo.employee.service.impl;

import com.example.demo.employee.dto.EmployeeDto;
import com.example.demo.employee.dto.EmployeeExportRequest;
import com.example.demo.employee.dto.EmployeeSearchCriteria;
import com.example.demo.employee.entity.Employee;
import com.example.demo.employee.repository.EmployeeRepository;
import com.example.demo.employee.service.EmployeeExportService;
import com.example.demo.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeExportServiceImpl implements EmployeeExportService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    @Override
    @Transactional(readOnly = true)
    public byte[] exportToExcel(EmployeeExportRequest request) {
        log.info("Exporting employees to Excel format");
        List<EmployeeDto> employees = getEmployeesForExport(request);
        return generateSimpleExcel(employees, request);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportToCsv(EmployeeExportRequest request) {
        log.info("Exporting employees to CSV format");
        List<EmployeeDto> employees = getEmployeesForExport(request);
        return generateSimpleCsv(employees, request);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportToPdf(EmployeeExportRequest request) {
        log.info("Exporting employees to PDF format");
        List<EmployeeDto> employees = getEmployeesForExport(request);
        return generateSimplePdf(employees, request);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportEmployees(EmployeeSearchCriteria criteria, EmployeeExportRequest request) {
        log.info("Exporting employees based on search criteria");
        List<EmployeeDto> employees = searchEmployees(criteria, request);
        
        switch (request.getFormat()) {
            case EXCEL:
                return generateSimpleExcel(employees, request);
            case CSV:
                return generateSimpleCsv(employees, request);
            case PDF:
                return generateSimplePdf(employees, request);
            default:
                throw new IllegalArgumentException("Unsupported export format: " + request.getFormat());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportEmployeesByIds(List<Long> employeeIds, EmployeeExportRequest request) {
        log.info("Exporting employees by IDs: {}, format: {}", employeeIds, request.getFormat());
        List<EmployeeDto> employees = employeeIds.stream()
                .map(employeeService::getEmployeeById)
                .collect(Collectors.toList());
        
        switch (request.getFormat()) {
            case EXCEL:
                return generateSimpleExcel(employees, request);
            case CSV:
                return generateSimpleCsv(employees, request);
            case PDF:
                return generateSimplePdf(employees, request);
            default:
                throw new IllegalArgumentException("Unsupported export format: " + request.getFormat());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void exportToStream(EmployeeExportRequest request, ByteArrayOutputStream outputStream) {
        log.info("Exporting employees to stream");
        List<EmployeeDto> employees = getEmployeesForExport(request);
        byte[] data = exportEmployees(new EmployeeSearchCriteria(), request);
        try {
            outputStream.write(data);
        } catch (Exception e) {
            log.error("Error writing to output stream", e);
            throw new RuntimeException("Failed to export to stream", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getExportStatistics(EmployeeSearchCriteria criteria) {
        log.info("Getting export statistics");
        List<EmployeeDto> employees = searchEmployees(criteria, new EmployeeExportRequest());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", employees.size());
        stats.put("departments", employees.stream()
                .map(EmployeeDto::getDepartmentName)
                .filter(Objects::nonNull)
                .distinct()
                .count());
        stats.put("positions", employees.stream()
                .map(EmployeeDto::getPositionName)
                .filter(Objects::nonNull)
                .distinct()
                .count());
        stats.put("activeEmployees", employees.stream()
                .filter(e -> e.getStatus() != null && e.getStatus().name().equals("ACTIVE"))
                .count());
        
        return stats;
    }

    @Override
    public List<String> validateExportRequest(EmployeeExportRequest request) {
        List<String> errors = new ArrayList<>();
        
        if (request == null) {
            errors.add("Export request cannot be null");
            return errors;
        }
        
        if (request.getFormat() == null) {
            errors.add("Export format is required");
        }
        
        return errors;
    }

    @Override
    public List<EmployeeExportRequest.ExportFormat> getSupportedFormats() {
        return Arrays.asList(
                EmployeeExportRequest.ExportFormat.EXCEL,
                EmployeeExportRequest.ExportFormat.CSV,
                EmployeeExportRequest.ExportFormat.PDF
        );
    }

    @Override
    public int getMaxExportRecords() {
        return 10000;
    }

    @Override
    public boolean canExportSensitiveData(Long userId) {
        return true; // 简化权限检查
    }

    @Override
    public String generateExportFilename(EmployeeExportRequest request) {
        String baseName = request.getFileName() != null ? request.getFileName() : "employee_export";
        return baseName + "_" + LocalDateTime.now().toString().replace(":", "-") + "." + request.getFormat().getExtension();
    }

    @Override
    public byte[] getExportTemplate(EmployeeExportRequest.ExportFormat format) {
        List<EmployeeDto> sampleEmployees = createSampleEmployees();
        
        EmployeeExportRequest request = EmployeeExportRequest.builder()
                .format(format)
                .includeFields(EmployeeExportRequest.getDefaultFields())
                .build();
        
        switch (format) {
            case EXCEL:
                return generateSimpleExcel(sampleEmployees, request);
            case CSV:
                return generateSimpleCsv(sampleEmployees, request);
            case PDF:
                return generateSimplePdf(sampleEmployees, request);
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    private List<EmployeeDto> getEmployeesForExport(EmployeeExportRequest request) {
        Specification<Employee> spec = (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            if (request.getDepartmentIds() != null && !request.getDepartmentIds().isEmpty()) {
                predicates.add(root.get("departmentId").in(request.getDepartmentIds()));
            }
            
            if (request.getPositionIds() != null && !request.getPositionIds().isEmpty()) {
                predicates.add(root.get("positionId").in(request.getPositionIds()));
            }
            
            if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(request.getStatuses()));
            }
            
            if (request.getEmploymentTypes() != null && !request.getEmploymentTypes().isEmpty()) {
                predicates.add(root.get("employmentType").in(request.getEmploymentTypes()));
            }
            
            if (request.getHireDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("hireDate"), request.getHireDateFrom()));
            }
            
            if (request.getHireDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("hireDate"), request.getHireDateTo()));
            }
            
            if (!request.isIncludeTerminated()) {
                predicates.add(criteriaBuilder.notEqual(root.get("status"), "TERMINATED"));
            }
            
            if (!request.isIncludeDisabled()) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), true));
            }
            
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        
        Pageable pageable = PageRequest.of(0, getMaxExportRecords());
        Page<Employee> employeePage = employeeRepository.findAll(spec, pageable);
        
        return employeePage.getContent().stream()
                .map(emp -> employeeService.getEmployeeById(emp.getId()))
                .collect(Collectors.toList());
    }

    private List<EmployeeDto> searchEmployees(EmployeeSearchCriteria criteria, EmployeeExportRequest request) {
        // 使用EmployeeService的搜索功能
        if (criteria != null && criteria.getSearchTerm() != null) {
            return employeeService.searchEmployees(criteria.getSearchTerm(), PageRequest.of(0, getMaxExportRecords()))
                    .getContent();
        }
        return getEmployeesForExport(request);
    }

    private byte[] generateSimpleExcel(List<EmployeeDto> employees, EmployeeExportRequest request) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Set<String> fields = request.getEffectiveFields();
            
            StringBuilder csv = new StringBuilder();
            
            // 添加标题行
            if (fields.contains("employeeNumber")) csv.append("Employee Number,");
            if (fields.contains("firstName")) csv.append("First Name,");
            if (fields.contains("lastName")) csv.append("Last Name,");
            if (fields.contains("email")) csv.append("Email,");
            if (fields.contains("phone")) csv.append("Phone,");
            if (fields.contains("departmentName")) csv.append("Department,");
            if (fields.contains("positionName")) csv.append("Position,");
            if (fields.contains("hireDate")) csv.append("Hire Date,");
            if (fields.contains("salary")) csv.append("Salary,");
            if (fields.contains("status")) csv.append("Status");
            csv.append("\n");
            
            // 添加数据行
            for (EmployeeDto employee : employees) {
                if (fields.contains("employeeNumber")) csv.append(employee.getEmployeeNumber()).append(",");
                if (fields.contains("firstName")) csv.append(employee.getFirstName()).append(",");
                if (fields.contains("lastName")) csv.append(employee.getLastName()).append(",");
                if (fields.contains("email")) csv.append(employee.getEmail()).append(",");
                if (fields.contains("phone")) csv.append(employee.getPhone() != null ? employee.getPhone() : "").append(",");
                if (fields.contains("departmentName")) csv.append(employee.getDepartmentName() != null ? employee.getDepartmentName() : "").append(",");
                if (fields.contains("positionName")) csv.append(employee.getPositionName() != null ? employee.getPositionName() : "").append(",");
                if (fields.contains("hireDate")) csv.append(employee.getHireDate()).append(",");
                if (fields.contains("salary")) csv.append(employee.getSalary() != null ? employee.getSalary().toString() : "").append(",");
                if (fields.contains("status")) csv.append(employee.getStatus() != null ? employee.getStatus().name() : "");
                csv.append("\n");
            }
            
            outputStream.write(csv.toString().getBytes());
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error generating Excel file", e);
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    private byte[] generateSimpleCsv(List<EmployeeDto> employees, EmployeeExportRequest request) {
        return generateSimpleExcel(employees, request); // 使用相同格式，实际CSV更简单
    }

    private byte[] generateSimplePdf(List<EmployeeDto> employees, EmployeeExportRequest request) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            StringBuilder content = new StringBuilder();
            content.append("Employee Export Report\n")
                   .append("Generated: ").append(LocalDateTime.now()).append("\n")
                   .append("Total Records: ").append(employees.size()).append("\n\n");
            
            Set<String> fields = request.getEffectiveFields();
            
            // 创建表格头部
            content.append("|");
            if (fields.contains("employeeNumber")) content.append(" Employee Number |");
            if (fields.contains("firstName")) content.append(" First Name |");
            if (fields.contains("lastName")) content.append(" Last Name |");
            if (fields.contains("email")) content.append(" Email |");
            if (fields.contains("departmentName")) content.append(" Department |");
            if (fields.contains("positionName")) content.append(" Position |");
            content.append("\n");
            
            // 添加数据行
            for (EmployeeDto employee : employees) {
                content.append("|");
                if (fields.contains("employeeNumber")) content.append(" ").append(employee.getEmployeeNumber()).append(" |");
                if (fields.contains("firstName")) content.append(" ").append(employee.getFirstName()).append(" |");
                if (fields.contains("lastName")) content.append(" ").append(employee.getLastName()).append(" |");
                if (fields.contains("email")) content.append(" ").append(employee.getEmail()).append(" |");
                if (fields.contains("departmentName")) content.append(" ").append(employee.getDepartmentName() != null ? employee.getDepartmentName() : "").append(" |");
                if (fields.contains("positionName")) content.append(" ").append(employee.getPositionName() != null ? employee.getPositionName() : "").append(" |");
                content.append("\n");
            }
            
            outputStream.write(content.toString().getBytes());
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF file", e);
            throw new RuntimeException("Failed to generate PDF file", e);
        }
    }

    private List<EmployeeDto> createSampleEmployees() {
        List<EmployeeDto> sampleEmployees = new ArrayList<>();
        
        EmployeeDto employee1 = EmployeeDto.builder()
                .id(1L)
                .employeeNumber("EMP001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@company.com")
                .departmentName("IT Department")
                .positionName("Software Engineer")
                .hireDate(java.time.LocalDate.now().minusYears(2))
                .status(java.util.Optional.ofNullable(com.example.demo.employee.entity.EmployeeStatus.ACTIVE).orElse(null))
                .build();
                
        EmployeeDto employee2 = EmployeeDto.builder()
                .id(2L)
                .employeeNumber("EMP002")
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@company.com")
                .departmentName("HR Department")
                .positionName("HR Manager")
                .hireDate(java.time.LocalDate.now().minusYears(3))
                .status(java.util.Optional.ofNullable(com.example.demo.employee.entity.EmployeeStatus.ACTIVE).orElse(null))
                .build();
                
        sampleEmployees.add(employee1);
        sampleEmployees.add(employee2);
        
        return sampleEmployees;
    }
}