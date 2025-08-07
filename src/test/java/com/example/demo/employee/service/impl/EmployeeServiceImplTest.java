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
import com.example.demo.department.entity.Department;
import com.example.demo.department.repository.DepartmentRepository;
import com.example.demo.position.entity.Position;
import com.example.demo.position.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeServiceImpl using Mockito.
 * Tests business logic, validation, and exception handling.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PositionRepository positionRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;
    private Department department;
    private Position position;
    private EmployeeCreateRequest createRequest;
    private EmployeeUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("IT Department");

        position = new Position();
        position.setId(1L);
        position.setJobTitle("Software Developer");

        employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeNumber("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setHireDate(LocalDate.now());

        createRequest = new EmployeeCreateRequest();
        createRequest.setFirstName("Jane");
        createRequest.setLastName("Smith");
        createRequest.setEmail("jane.smith@example.com");
        createRequest.setDepartmentId(1L);
        createRequest.setPositionId(1L);
        createRequest.setHireDate(LocalDate.now());

        updateRequest = new EmployeeUpdateRequest();
        updateRequest.setFirstName("John Updated");
        updateRequest.setLastName("Doe Updated");
        updateRequest.setEmail("john.updated@example.com");
    }

    @Test
    void createEmployee_shouldCreateEmployeeSuccessfully() {
        // Given
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
        when(departmentRepository.findById(createRequest.getDepartmentId())).thenReturn(Optional.of(department));
        when(positionRepository.findById(createRequest.getPositionId())).thenReturn(Optional.of(position));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // When
        EmployeeDto result = employeeService.createEmployee(createRequest);

        // Then
        assertThat(result).isNotNull();
        // Can't assert employee number as it's randomly generated
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void createEmployee_shouldThrowExceptionWhenEmailExists() {
        // Given
        when(employeeRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> employeeService.createEmployee(createRequest))
                .isInstanceOf(EmployeeAlreadyExistsException.class)
                .hasMessageContaining("Employee with email " + createRequest.getEmail() + " already exists");

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void getEmployeeById_shouldReturnEmployeeWhenExists() {
        // Given
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // When
        EmployeeDto result = employeeService.getEmployeeById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(employee.getId());
        assertThat(result.getEmployeeNumber()).isEqualTo(employee.getEmployeeNumber());
    }

    @Test
    void getEmployeeById_shouldThrowExceptionWhenNotExists() {
        // Given
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> employeeService.getEmployeeById(1L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("Employee not found with id: 1");
    }

    @Test
    void updateEmployee_shouldUpdateEmployeeSuccessfully() {
        // Given
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findByEmail(updateRequest.getEmail())).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // When
        EmployeeDto result = employeeService.updateEmployee(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void updateEmployee_shouldThrowExceptionWhenEmailExistsForOtherEmployee() {
        // Given
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Employee otherEmployee = new Employee();
        otherEmployee.setId(2L);
        otherEmployee.setEmail(updateRequest.getEmail());
        when(employeeRepository.findByEmail(updateRequest.getEmail())).thenReturn(Optional.of(otherEmployee));

        // When & Then
        assertThatThrownBy(() -> employeeService.updateEmployee(1L, updateRequest))
                .isInstanceOf(EmployeeAlreadyExistsException.class)
                .hasMessageContaining("Employee with email " + updateRequest.getEmail() + " already exists");

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_shouldDeleteEmployeeSuccessfully() {
        // Given
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // When
        employeeService.deleteEmployee(1L);

        // Then
        verify(employeeRepository).delete(employee);
    }

    @Test
    void deleteEmployee_shouldThrowExceptionWhenNotExists() {
        // Given
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> employeeService.deleteEmployee(1L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("Employee not found with id: 1");

        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    @Test
    void getAllEmployees_shouldReturnPagedEmployees() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(employee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        // When
        Page<EmployeeDto> result = employeeService.getAllEmployees(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(employee.getId());
    }

    @Test
    void searchEmployees_shouldReturnFilteredEmployees() {
        // Given
        EmployeeSearchCriteria criteria = new EmployeeSearchCriteria();
        criteria.setFirstName("John");
        criteria.setDepartmentIds(List.of(1L));

        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> employees = Arrays.asList(employee);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findByDepartmentId(1L, pageable))
            .thenReturn(employeePage);

        // When
        Page<EmployeeDto> result = employeeService.searchEmployees(criteria, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(employeeRepository).findByDepartmentId(1L, pageable);
    }

    @Test
    void getEmployeeByEmployeeNumber_shouldReturnEmployeeWhenExists() {
        // Given
        when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));

        // When
        EmployeeDto result = employeeService.getEmployeeByEmployeeNumber("EMP001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmployeeNumber()).isEqualTo("EMP001");
    }

    @Test
    void getEmployeeByEmployeeNumber_shouldThrowExceptionWhenNotExists() {
        // Given
        when(employeeRepository.findByEmployeeNumber("EMP999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> employeeService.getEmployeeByEmployeeNumber("EMP999"))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("Employee not found with employee number: EMP999");
    }

}