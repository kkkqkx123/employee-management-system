package com.example.demo.employee.service.impl;

import com.example.demo.employee.dto.EmployeeCreateRequest;
import com.example.demo.employee.dto.EmployeeDto;
import com.example.demo.employee.dto.EmployeeSearchCriteria;
import com.example.demo.employee.dto.EmployeeUpdateRequest;
import com.example.demo.employee.entity.Employee;
import com.example.demo.employee.entity.EmployeeStatus;
import com.example.demo.employee.exception.EmployeeAlreadyExistsException;
import com.example.demo.employee.exception.EmployeeNotFoundException;
import com.example.demo.employee.repository.EmployeeRepository;
import com.example.demo.employee.service.EmployeeService;
import com.example.demo.department.entity.Department;
import com.example.demo.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public EmployeeDto createEmployee(EmployeeCreateRequest createRequest) {
        log.info("Creating employee with email: {}", createRequest.getEmail());

        // Validate unique constraints
        if (employeeRepository.existsByEmail(createRequest.getEmail())) {
            throw EmployeeAlreadyExistsException.byEmail(createRequest.getEmail());
        }

        // Validate department exists
        if (!departmentRepository.existsById(createRequest.getDepartmentId())) {
            throw new RuntimeException("Department not found with id: " + createRequest.getDepartmentId());
        }

        // Create employee entity
        Employee employee = new Employee();
        employee.setEmployeeNumber(generateEmployeeNumber());
        employee.setFirstName(createRequest.getFirstName());
        employee.setLastName(createRequest.getLastName());
        employee.setEmail(createRequest.getEmail());
        employee.setPhone(createRequest.getPhone());
        employee.setMobilePhone(createRequest.getMobilePhone());
        employee.setAddress(createRequest.getAddress());
        employee.setCity(createRequest.getCity());
        employee.setState(createRequest.getState());
        employee.setZipCode(createRequest.getZipCode());
        employee.setCountry(createRequest.getCountry());
        employee.setDateOfBirth(createRequest.getDateOfBirth());
        employee.setGender(createRequest.getGender());
        employee.setMaritalStatus(createRequest.getMaritalStatus());
        employee.setNationality(createRequest.getNationality());
        employee.setDepartmentId(createRequest.getDepartmentId());
        employee.setPositionId(createRequest.getPositionId());
        employee.setManagerId(createRequest.getManagerId());
        employee.setHireDate(createRequest.getHireDate());
        employee.setStatus(createRequest.getStatus());
        employee.setEmploymentType(createRequest.getEmploymentType());
        employee.setPayType(createRequest.getPayType());
        employee.setSalary(createRequest.getSalary());
        employee.setHourlyRate(createRequest.getHourlyRate());
        employee.setBankAccount(createRequest.getBankAccount());
        employee.setTaxId(createRequest.getTaxId());
        employee.setEnabled(createRequest.getEnabled());

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee created successfully with ID: {}", savedEmployee.getId());

        return convertToDto(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeUpdateRequest updateRequest) {
        log.info("Updating employee with ID: {}", id);

        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        // Check email uniqueness (excluding current employee)
        employeeRepository.findByEmail(updateRequest.getEmail())
                .ifPresent(employee -> {
                    if (!employee.getId().equals(id)) {
                        throw EmployeeAlreadyExistsException.byEmail(updateRequest.getEmail());
                    }
                });

        // Validate department exists
        if (!departmentRepository.existsById(updateRequest.getDepartmentId())) {
            throw new RuntimeException("Department not found with id: " + updateRequest.getDepartmentId());
        }

        // Update fields
        existingEmployee.setFirstName(updateRequest.getFirstName());
        existingEmployee.setLastName(updateRequest.getLastName());
        existingEmployee.setEmail(updateRequest.getEmail());
        existingEmployee.setPhone(updateRequest.getPhone());
        existingEmployee.setMobilePhone(updateRequest.getMobilePhone());
        existingEmployee.setAddress(updateRequest.getAddress());
        existingEmployee.setCity(updateRequest.getCity());
        existingEmployee.setState(updateRequest.getState());
        existingEmployee.setZipCode(updateRequest.getZipCode());
        existingEmployee.setCountry(updateRequest.getCountry());
        existingEmployee.setDateOfBirth(updateRequest.getDateOfBirth());
        existingEmployee.setGender(updateRequest.getGender());
        existingEmployee.setMaritalStatus(updateRequest.getMaritalStatus());
        existingEmployee.setNationality(updateRequest.getNationality());
        existingEmployee.setDepartmentId(updateRequest.getDepartmentId());
        existingEmployee.setPositionId(updateRequest.getPositionId());
        existingEmployee.setManagerId(updateRequest.getManagerId());
        existingEmployee.setHireDate(updateRequest.getHireDate());
        existingEmployee.setTerminationDate(updateRequest.getTerminationDate());
        existingEmployee.setStatus(updateRequest.getStatus());
        existingEmployee.setEmploymentType(updateRequest.getEmploymentType());
        existingEmployee.setPayType(updateRequest.getPayType());
        existingEmployee.setSalary(updateRequest.getSalary());
        existingEmployee.setHourlyRate(updateRequest.getHourlyRate());
        existingEmployee.setBankAccount(updateRequest.getBankAccount());
        existingEmployee.setTaxId(updateRequest.getTaxId());
        existingEmployee.setEnabled(updateRequest.getEnabled());

        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        log.info("Employee updated successfully with ID: {}", updatedEmployee.getId());

        return convertToDto(updatedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return convertToDto(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeByEmployeeNumber(String employeeNumber) {
        Employee employee = employeeRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> EmployeeNotFoundException.byEmployeeNumber(employeeNumber));
        return convertToDto(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDto> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDto> getEmployeesByDepartmentId(Long departmentId, Pageable pageable) {
        return employeeRepository.findByDepartmentId(departmentId, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> getEmployeesByDepartmentId(Long departmentId) {
        List<Employee> employees = employeeRepository.findByDepartmentId(departmentId);
        return employees.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDto> getEmployeesByStatus(EmployeeStatus status, Pageable pageable) {
        return employeeRepository.findByStatus(status, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDto> searchEmployees(String searchTerm, Pageable pageable) {
        return employeeRepository.searchEmployees(searchTerm, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDto> searchEmployees(EmployeeSearchCriteria criteria, Pageable pageable) {
        log.info("Searching employees with advanced criteria");

        // If no criteria specified, return all employees
        if (criteria == null || !criteria.hasAnyCriteria()) {
            return employeeRepository.findAll(pageable).map(this::convertToDto);
        }

        // If only basic search term is provided, use simple search
        if (criteria.hasSearchTerm() && !hasAdvancedCriteria(criteria)) {
            return searchEmployees(criteria.getSearchTerm(), pageable);
        }

        // Use advanced search with criteria
        return searchWithCriteria(criteria, pageable);
    }

    private boolean hasAdvancedCriteria(EmployeeSearchCriteria criteria) {
        return criteria.getDepartmentIds() != null && !criteria.getDepartmentIds().isEmpty() ||
                criteria.getPositionIds() != null && !criteria.getPositionIds().isEmpty() ||
                criteria.getManagerIds() != null && !criteria.getManagerIds().isEmpty() ||
                criteria.getStatuses() != null && !criteria.getStatuses().isEmpty() ||
                criteria.getEmploymentTypes() != null && !criteria.getEmploymentTypes().isEmpty() ||
                criteria.getPayTypes() != null && !criteria.getPayTypes().isEmpty() ||
                criteria.getHireDateFrom() != null || criteria.getHireDateTo() != null ||
                criteria.getTerminationDateFrom() != null || criteria.getTerminationDateTo() != null ||
                criteria.getSalaryFrom() != null || criteria.getSalaryTo() != null ||
                criteria.getHourlyRateFrom() != null || criteria.getHourlyRateTo() != null ||
                criteria.getEnabled() != null || criteria.getHasManager() != null ||
                criteria.getHasDirectReports() != null;
    }

    private Page<EmployeeDto> searchWithCriteria(EmployeeSearchCriteria criteria, Pageable pageable) {
        // For now, implement basic filtering. In a real application, you would use
        // JPA Criteria API or custom repository methods for complex queries

        Page<Employee> employees = employeeRepository.findAll(pageable);

        // Apply basic filters that can be handled by existing repository methods
        if (criteria.getDepartmentIds() != null && criteria.getDepartmentIds().size() == 1) {
            employees = employeeRepository.findByDepartmentId(criteria.getDepartmentIds().get(0), pageable);
        } else if (criteria.getStatuses() != null && criteria.getStatuses().size() == 1) {
            employees = employeeRepository.findByStatus(criteria.getStatuses().get(0), pageable);
        } else if (criteria.getHireDateFrom() != null && criteria.getHireDateTo() != null) {
            employees = employeeRepository.findByHireDateBetween(criteria.getHireDateFrom(), criteria.getHireDateTo(),
                    pageable);
        } else if (criteria.hasSearchTerm()) {
            // Use the existing search method for basic text search
            return searchEmployees(criteria.getSearchTerm(), pageable);
        }

        return employees.map(this::convertToDto);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        employeeRepository.deleteById(id);
        log.info("Employee deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmployeeNumber(String employeeNumber) {
        return employeeRepository.existsByEmployeeNumber(employeeNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return employeeRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public long countEmployeesByDepartmentId(Long departmentId) {
        return employeeRepository.countByDepartmentId(departmentId);
    }

    private String generateEmployeeNumber() {
        String prefix = "EMP";
        String suffix;
        String employeeNumber;

        do {
            suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            employeeNumber = prefix + "-" + suffix;
        } while (employeeRepository.existsByEmployeeNumber(employeeNumber));

        return employeeNumber;
    }

    private EmployeeDto convertToDto(Employee employee) {
        EmployeeDto dto = EmployeeDto.builder()
                .id(employee.getId())
                .employeeNumber(employee.getEmployeeNumber())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .mobilePhone(employee.getMobilePhone())
                .address(employee.getAddress())
                .city(employee.getCity())
                .state(employee.getState())
                .zipCode(employee.getZipCode())
                .country(employee.getCountry())
                .dateOfBirth(employee.getDateOfBirth()) // Already decrypted by converter
                .gender(employee.getGender())
                .maritalStatus(employee.getMaritalStatus())
                .nationality(employee.getNationality())
                .departmentId(employee.getDepartmentId())
                .positionId(employee.getPositionId())
                .managerId(employee.getManagerId())
                .hireDate(employee.getHireDate())
                .terminationDate(employee.getTerminationDate())
                .status(employee.getStatus())
                .employmentType(employee.getEmploymentType())
                .payType(employee.getPayType())
                .salary(employee.getSalary())
                .hourlyRate(employee.getHourlyRate())
                .enabled(employee.isEnabled())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .fullName(employee.getFullName())
                .build();

        // Mask sensitive data
        if (employee.getBankAccount() != null && employee.getBankAccount().length() > 4) {
            dto.setBankAccount("****" + employee.getBankAccount().substring(employee.getBankAccount().length() - 4));
        }
        if (employee.getTaxId() != null && employee.getTaxId().length() > 4) {
            dto.setTaxId("****" + employee.getTaxId().substring(employee.getTaxId().length() - 4));
        }

        // Set department name if available
        if (employee.getDepartment() != null) {
            dto.setDepartmentName(employee.getDepartment().getName());
        }

        // Set manager name if available
        if (employee.getManager() != null) {
            dto.setManagerName(employee.getManager().getFullName());
        }

        return dto;
    }
}