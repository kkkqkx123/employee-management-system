package com.example.demo.integration;

import com.example.demo.config.TestConfig;
import com.example.demo.department.dto.DepartmentCreateRequest;
import com.example.demo.department.dto.DepartmentDto;
import com.example.demo.department.entity.Department;
import com.example.demo.department.exception.DepartmentNotFoundException;
import com.example.demo.department.repository.DepartmentRepository;
import com.example.demo.department.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.example.demo.department.dto.DepartmentTreeDto;
import com.example.demo.department.dto.DepartmentUpdateRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for DepartmentService.
 * Tests service layer with actual database interactions.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class DepartmentServiceIntegrationTest {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department parentDepartment;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        departmentRepository.deleteAll();
        
        // Create parent department
        parentDepartment = new Department();
        parentDepartment.setName("Engineering");
        parentDepartment.setCode("ENG");
        parentDepartment.setDescription("Engineering Department");
        parentDepartment = departmentRepository.save(parentDepartment);
    }

    @Test
    void createDepartment_shouldPersistToDatabase() {
        // Given
        DepartmentCreateRequest request = new DepartmentCreateRequest();
        request.setName("Software Development");
        request.setCode("SD");
        request.setDescription("Software Development Team");
        request.setParentId(parentDepartment.getId());

        // When
        DepartmentDto result = departmentService.createDepartment(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        
        // Verify persistence
        Department saved = departmentRepository.findById(result.getId()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Software Development");
        assertThat(saved.getCode()).isEqualTo("SD");
        assertThat(saved.getParent().getId()).isEqualTo(parentDepartment.getId());
    }

    @Test
    void getDepartmentById_shouldReturnFromDatabase() {
        // Given
        Department department = new Department();
        department.setName("Quality Assurance");
        department.setCode("QA");
        department.setDescription("Quality Assurance Team");
        department.setParent(parentDepartment);
        department = departmentRepository.save(department);

        // When
        DepartmentDto result = departmentService.getDepartmentById(department.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(department.getId());
        assertThat(result.getName()).isEqualTo("Quality Assurance");
        assertThat(result.getParentId()).isEqualTo(parentDepartment.getId());
    }

    @Test
    void getDepartmentById_shouldThrowExceptionWhenNotFound() {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        assertThatThrownBy(() -> departmentService.getDepartmentById(nonExistentId))
                .isInstanceOf(DepartmentNotFoundException.class);
    }

    @Test
    void getAllDepartments_shouldReturnAllResults() {
        // Given
        Department dept1 = new Department();
        dept1.setName("Development");
        dept1.setCode("DEV");
        dept1.setDescription("Development Team");
        departmentRepository.save(dept1);

        Department dept2 = new Department();
        dept2.setName("Testing");
        dept2.setCode("TEST");
        dept2.setDescription("Testing Team");
        departmentRepository.save(dept2);

        // When
        var result = departmentService.getAllDepartments();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSizeGreaterThanOrEqualTo(3); // Including parent department
    }

    @Test
    void updateDepartment_shouldPersistChanges() {
        // Given
        Department department = new Department();
        department.setName("Original Name");
        department.setCode("ORIG");
        department.setDescription("Original Description");
        department = departmentRepository.save(department);

        DepartmentUpdateRequest updateRequest = new DepartmentUpdateRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setDescription("Updated Description");

        // When
        DepartmentDto result = departmentService.updateDepartment(department.getId(), updateRequest);

        // Then
        assertThat(result.getName()).isEqualTo("Updated Name");
        
        // Verify persistence
        Department updated = departmentRepository.findById(department.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void deleteDepartment_shouldRemoveFromDatabase() {
        // Given
        Department department = new Department();
        department.setName("To Be Deleted");
        department.setCode("DEL");
        department.setDescription("Department to be deleted");
        department = departmentRepository.save(department);
        Long departmentId = department.getId();

        // When
        departmentService.deleteDepartment(departmentId);

        // Then
        assertThat(departmentRepository.findById(departmentId)).isEmpty();
    }

    @Test
    void getDepartmentTree_shouldReturnCorrectStructure() {
        // Given
        Department childDept = new Department();
        childDept.setName("Frontend Team");
        childDept.setCode("FE");
        childDept.setDescription("Frontend Development Team");
        childDept.setParent(parentDepartment);
        departmentRepository.save(childDept);

        Department grandChildDept = new Department();
        grandChildDept.setName("React Team");
        grandChildDept.setCode("REACT");
        grandChildDept.setDescription("React Development Team");
        grandChildDept.setParent(childDept);
        departmentRepository.save(grandChildDept);

        // When
        var hierarchy = departmentService.getDepartmentTree();

        // Then
        assertThat(hierarchy).isNotNull().isNotEmpty();
        // Verify that the hierarchy contains our departments
        boolean foundParent = hierarchy.stream()
                .anyMatch(dept -> dept.getName().equals("Engineering"));
        assertThat(foundParent).isTrue();
    }

    @Test
    void searchDepartments_shouldReturnMatchingResults() {
        // Given
        Department dept1 = new Department();
        dept1.setName("Software Engineering");
        dept1.setCode("SE");
        dept1.setDescription("Software Engineering Team");
        departmentRepository.save(dept1);

        Department dept2 = new Department();
        dept2.setName("Hardware Engineering");
        dept2.setCode("HE");
        dept2.setDescription("Hardware Engineering Team");
        departmentRepository.save(dept2);

        Department dept3 = new Department();
        dept3.setName("Marketing");
        dept3.setCode("MKT");
        dept3.setDescription("Marketing Department");
        departmentRepository.save(dept3);

        // When
        var result = departmentService.searchDepartments("Engineering");

        // Then
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result)
                .allMatch(dept -> dept.getName().contains("Engineering") || dept.getDescription().contains("Engineering"));
    }
}