package com.example.demo.employee.service.impl;

import com.example.demo.employee.dto.EmployeeExportRequest;
import com.example.demo.employee.dto.EmployeeSearchCriteria;
import com.example.demo.employee.entity.Employee;
import com.example.demo.employee.entity.EmployeeStatus;
import com.example.demo.employee.entity.EmploymentType;
import com.example.demo.employee.repository.EmployeeRepository;
import com.example.demo.employee.service.EmployeeExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeExportServiceImpl implements EmployeeExportService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] exportToExcel(EmployeeExportRequest request) {
        log.info("Exporting employees to Excel format");
        List<Employee> employees = getEmployeesForExport(request);
        return EmployeeExcelUtil.exportEmployeesToExcel(employees, request);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportToCsv(EmployeeExportRequest request) {
        log.info("Exporting employees to CSV format");
        List<Employee> employees = getEmployeesForExport(request);
        return EmployeeExcelUtil.exportEmployeesToCsv(employees, request);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportToPdf(EmployeeExportRequest request) {
        log.info("Exporting employees to PDF format");
        List<Employee> employees = getEmployeesForExport(request);
        return EmployeeExcelUtil.exportEmployeesToPdf(employees, request);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportEmployees(EmployeeSearchCriteria criteria, EmployeeExportRequest request) {
        log.info("Exporting employees based on search criteria");
        List<Employee> employees = findEmployeesByCriteria(criteria);
        
        switch (request.getFormat()) {
            case EXCEL:
                return exportToExcel(request);
            case CSV:
                return exportToCsv(request);
            case PDF:
                return exportToPdf(request);
            default:
                throw new IllegalArgumentException("Unsupported export format: " + request.getFormat());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportEmployeesByIds(List<Long> employeeIds, EmployeeExportRequest request) {
        log.info("Exporting employees by IDs: {}", employeeIds);
        List<Employee> employees = employeeRepository.findAllById(employeeIds);
        
        switch (request.getFormat()) {
            case EXCEL:
                return EmployeeExcelUtil.exportEmployeesToExcel(employees, request);
            case CSV:
                return EmployeeExcelUtil.exportEmployeesToCsv(employees, request);
            case PDF:
                return EmployeeExcelUtil.exportEmployeesToPdf(employees, request);
            default:
                throw new IllegalArgumentException("Unsupported export format: " + request.getFormat());
        }
    }

    @Override
    public void exportToStream(EmployeeExportRequest request, ByteArrayOutputStream outputStream) {
        log.info("Exporting employees to stream");
        byte[] data = exportEmployeesByIds(request.getEffectiveFields() != null ? new ArrayList<>() : new ArrayList<>(), request);
        try {
            outputStream.write(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write export data to stream", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getExportStatistics(EmployeeSearchCriteria criteria) {
        log.info("Getting export statistics");
        List<Employee> employees = findEmployeesByCriteria(criteria);
        
        return Map.of(
            "totalRecords", employees.size(),
            "filteredRecords", employees.size(),
            "departments", employees.stream()
                .map(emp -> emp.getDepartment() != null ? emp.getDepartment().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .count(),
            "positions", employees.stream()
                .map(emp -> emp.getPosition() != null ? emp.getPosition().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .count(),
            "managers", employees.stream()
                .map(emp -> emp.getManager() != null ? emp.getManager().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .count()
        );
    }

    @Override
    public List<String> validateExportRequest(EmployeeExportRequest request) {
        List<String> errors = new ArrayList<>();
        
        if (request.getFormat() == null) {
            errors.add("Export format is required");
        }
        
        return errors;
    }

    @Override
    public List<EmployeeExportRequest.ExportFormat> getSupportedFormats() {
        return List.of(
            EmployeeExportRequest.ExportFormat.EXCEL,
            EmployeeExportRequest.ExportFormat.CSV,
            EmployeeExportRequest.ExportFormat.PDF
        );
    }

    @Override
    public int getMaxExportRecords() {
        return 50000; // 最多导出50000条记录
    }

    @Override
    public boolean canExportSensitiveData(Long userId) {
        // 这里可以添加权限检查逻辑
        // 暂时返回true，表示所有用户都可以导出敏感数据
        return true;
    }

    @Override
    public String generateExportFilename(EmployeeExportRequest request) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "employees_" + timestamp + "." + request.getFormat().getExtension();
    }

    @Override
    public byte[] getExportTemplate(EmployeeExportRequest.ExportFormat format) {
        log.info("Getting export template for format: {}", format);
        return new byte[0]; // 简化实现
    }

    private List<Employee> getEmployeesForExport(EmployeeExportRequest request) {
        Specification<Employee> spec = Specification.where(null);

        if (request.getDepartmentIds() != null && !request.getDepartmentIds().isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("department").get("id").in(request.getDepartmentIds()));
        }

        if (request.getPositionIds() != null && !request.getPositionIds().isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("position").get("id").in(request.getPositionIds()));
        }

        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("status").in(request.getStatuses()));
        }

        if (request.getEmploymentTypes() != null && !request.getEmploymentTypes().isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("employmentType").in(request.getEmploymentTypes()));
        }

        if (request.getHireDateFrom() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("hireDate"), request.getHireDateFrom()));
        }

        if (request.getHireDateTo() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("hireDate"), request.getHireDateTo()));
        }

        if (!request.isIncludeTerminated()) {
            spec = spec.and((root, query, cb) -> cb.notEqual(root.get("status"), EmployeeStatus.TERMINATED));
        }

        if (!request.isIncludeDisabled()) {
            spec = spec.and((root, query, cb) -> cb.notEqual(root.get("status"), EmployeeStatus.INACTIVE));
        }

        return employeeRepository.findAll(spec);
    }

    private List<Employee> findEmployeesByCriteria(EmployeeSearchCriteria criteria) {
        if (criteria == null) {
            return employeeRepository.findAll();
        }
        
        // 这里可以添加基于criteria的复杂查询
        // 暂时返回所有员工
        return employeeRepository.findAll();
    }
}