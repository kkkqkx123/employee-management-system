package com.example.demo.employee.service.impl;

import com.example.demo.employee.dto.EmployeeDto;
import com.example.demo.employee.dto.EmployeeImportResult;
import com.example.demo.employee.entity.Employee;
import com.example.demo.employee.repository.EmployeeRepository;
import com.example.demo.employee.service.EmployeeImportService;
import com.example.demo.employee.util.EmployeeExcelUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeImportServiceImpl implements EmployeeImportService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public EmployeeImportResult importFromExcel(MultipartFile file, Map<String, Object> options) {
        log.info("Importing employees from Excel file: {}", file.getOriginalFilename());
        try {
            return EmployeeExcelUtil.importEmployeesFromExcel(file.getInputStream(), file.getOriginalFilename(), options);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }
    }

    @Override
    @Transactional
    public EmployeeImportResult importFromExcel(InputStream inputStream, String fileName, Map<String, Object> options) {
        log.info("Importing employees from Excel input stream: {}", fileName);
        return EmployeeExcelUtil.importEmployeesFromExcel(inputStream, fileName, options);
    }

    @Override
    @Transactional
    public EmployeeImportResult importFromCsv(MultipartFile file, Map<String, Object> options) {
        log.info("Importing employees from CSV file: {}", file.getOriginalFilename());
        try {
            return EmployeeExcelUtil.importEmployeesFromCsv(file.getInputStream(), file.getOriginalFilename(), options);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file", e);
        }
    }

    @Override
    @Transactional
    public EmployeeImportResult importFromCsv(InputStream inputStream, String fileName, Map<String, Object> options) {
        log.info("Importing employees from CSV input stream: {}", fileName);
        return EmployeeExcelUtil.importEmployeesFromCsv(inputStream, fileName, options);
    }

    @Override
    public EmployeeImportResult validateImportFile(MultipartFile file) {
        log.info("Validating import file: {}", file.getOriginalFilename());
        return importFromExcel(file, Map.of("validateOnly", true));
    }

    @Override
    public byte[] getImportTemplate(boolean includeExamples) {
        log.info("Generating import template, include examples: {}", includeExamples);
        return EmployeeExcelUtil.createImportTemplate(includeExamples);
    }

    @Override
    public List<String> getSupportedFormats() {
        return List.of("xlsx", "xls", "csv");
    }

    @Override
    public long getMaxFileSize() {
        return 10 * 1024 * 1024; // 10MB
    }

    @Override
    public int getMaxRecordCount() {
        return 10000; // 最多10000条记录
    }

    @Override
    public EmployeeImportResult previewImport(MultipartFile file, int maxRows) {
        log.info("Previewing import file: {}, max rows: {}", file.getOriginalFilename(), maxRows);
        Map<String, Object> options = Map.of("previewOnly", true, "maxRows", maxRows);
        return importFromExcel(file, options);
    }

    @Override
    public Map<String, String> getFieldMapping() {
        return Map.of(
            "员工编号", "employeeNumber",
            "姓名", "fullName",
            "邮箱", "email",
            "电话", "phone",
            "手机号", "mobilePhone",
            "地址", "address",
            "城市", "city",
            "州/省", "state",
            "邮编", "zipCode",
            "国家", "country",
            "出生日期", "dateOfBirth",
            "性别", "gender",
            "婚姻状况", "maritalStatus",
            "国籍", "nationality",
            "部门ID", "departmentId",
            "职位ID", "positionId",
            "直属上司ID", "managerId",
            "入职日期", "hireDate",
            "离职日期", "terminationDate",
            "状态", "status",
            "雇佣类型", "employmentType",
            "薪资类型", "payType",
            "薪资", "salary",
            "时薪", "hourlyRate",
            "银行账户", "bankAccount",
            "税号", "taxId"
        );
    }

    @Override
    public List<String> getRequiredFields() {
        return List.of("employeeNumber", "firstName", "lastName", "email", "departmentId", "hireDate");
    }

    @Override
    public List<String> getOptionalFields() {
        return List.of("phone", "mobilePhone", "address", "city", "state", "zipCode", "country", 
                      "dateOfBirth", "gender", "maritalStatus", "nationality", "positionId", 
                      "managerId", "terminationDate", "employmentType", "payType", "salary", 
                      "hourlyRate", "bankAccount", "taxId");
    }
}