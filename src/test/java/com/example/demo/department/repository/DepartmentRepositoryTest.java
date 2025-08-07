package com.example.demo.department.repository;

import com.example.demo.config.TestConfig;
import com.example.demo.department.entity.Department;
import com.example.demo.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for DepartmentRepository.
 * Tests custom query methods, hierarchical operations, and complex queries.
 */
@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class DepartmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department rootDepartment;
    private Department childDepartment;

    @BeforeEach
    void setUp() {
        // Create root department
        rootDepartment = TestDataBuilder.department()
                .withName("Engineering")
                .withCode("ENG")
                .withDescription("Engineering Department")
                .withLevel(1)
                .withDepPath("/ENG")
                .build();
        rootDepartment = entityManager.persistAndFlush(rootDepartment);

        // Create child department
        childDepartment = TestDataBuilder.department()
                .withName("Software Development")
                .withCode("SW")
                .withDescription("Software Development Team")
                .withParentId(rootDepartment.getId())
                .withLevel(2)
                .withDepPath("/ENG/SW")
                .build();
        childDepartment = entityManager.persistAndFlush(childDepartment);

        entityManager.clear();
    }

    @Test
    void findByName_ShouldReturnDepartment_WhenNameExists() {
        // When
        Optional<Department> result = departmentRepository.findByName("Engineering");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Engineering");
        assertThat(result.get().getCode()).isEqualTo("ENG");
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNameDoesNotExist() {
        // When
        Optional<Department> result = departmentRepository.findByName("NonExistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByCode_ShouldReturnDepartment_WhenCodeExists() {
        // When
        Optional<Department> result = departmentRepository.findByCode("ENG");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("ENG");
        assertThat(result.get().getName()).isEqualTo("Engineering");
    }

    @Test
    void findByParentIdOrderBySortOrder_ShouldReturnChildDepartments() {
        // Given
        Department anotherChild = TestDataBuilder.department()
                .withName("Quality Assurance")
                .withCode("QA")
                .withParentId(rootDepartment.getId())
                .withLevel(2)
                .withDepPath("/ENG/QA")
                .build();
        entityManager.persistAndFlush(anotherChild);

        // When
        List<Department> children = departmentRepository.findByParentIdOrderBySortOrder(rootDepartment.getId());

        // Then
        assertThat(children).hasSize(2);
        assertThat(children).extracting(Department::getName)
                .containsExactlyInAnyOrder("Software Development", "Quality Assurance");
    }

    @Test
    void findByParentIdIsNullOrderBySortOrder_ShouldReturnRootDepartments() {
        // Given
        Department anotherRoot = TestDataBuilder.department()
                .withName("Human Resources")
                .withCode("HR")
                .withLevel(1)
                .withDepPath("/HR")
                .build();
        entityManager.persistAndFlush(anotherRoot);

        // When
        List<Department> rootDepartments = departmentRepository.findByParentIdIsNullOrderBySortOrder();

        // Then
        assertThat(rootDepartments).hasSize(2);
        assertThat(rootDepartments).extracting(Department::getName)
                .containsExactlyInAnyOrder("Engineering", "Human Resources");
    }

    @Test
    void findByLevel_ShouldReturnDepartmentsAtSpecificLevel() {
        // When
        List<Department> level1Departments = departmentRepository.findByLevel(1);
        List<Department> level2Departments = departmentRepository.findByLevel(2);

        // Then
        assertThat(level1Departments).hasSize(1);
        assertThat(level1Departments.get(0).getName()).isEqualTo("Engineering");

        assertThat(level2Departments).hasSize(1);
        assertThat(level2Departments.get(0).getName()).isEqualTo("Software Development");
    }

    @Test
    void findByDepPathStartingWithOrderByDepPath_ShouldReturnSubtree() {
        // Given
        Department grandChild = TestDataBuilder.department()
                .withName("Frontend Team")
                .withCode("FE")
                .withParentId(childDepartment.getId())
                .withLevel(3)
                .withDepPath("/ENG/SW/FE")
                .build();
        entityManager.persistAndFlush(grandChild);

        // When
        List<Department> subtree = departmentRepository.findByDepPathStartingWithOrderByDepPath("/ENG");

        // Then
        assertThat(subtree).hasSize(3);
        assertThat(subtree).extracting(Department::getDepPath)
                .containsExactly("/ENG", "/ENG/SW", "/ENG/SW/FE");
    }

    @Test
    void findByEnabledTrueOrderByDepPath_ShouldReturnEnabledDepartments() {
        // Given
        Department disabledDept = TestDataBuilder.department()
                .withName("Disabled Department")
                .withCode("DIS")
                .withEnabled(false)
                .withLevel(1)
                .withDepPath("/DIS")
                .build();
        entityManager.persistAndFlush(disabledDept);

        // When
        List<Department> enabledDepartments = departmentRepository.findByEnabledTrueOrderByDepPath();

        // Then
        assertThat(enabledDepartments).hasSize(2);
        assertThat(enabledDepartments).extracting(Department::getName)
                .containsExactlyInAnyOrder("Engineering", "Software Development");
    }

    @Test
    void findByNameContainingIgnoreCaseOrderByName_ShouldReturnMatchingDepartments() {
        // Given
        Department devDept = TestDataBuilder.department()
                .withName("Mobile Development")
                .withCode("MOB")
                .withLevel(1)
                .withDepPath("/MOB")
                .build();
        entityManager.persistAndFlush(devDept);

        // When
        List<Department> result = departmentRepository.findByNameContainingIgnoreCaseOrderByName("development");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Department::getName)
                .containsExactly("Mobile Development", "Software Development");
    }

    @Test
    void existsByName_ShouldReturnTrue_WhenNameExists() {
        // When
        boolean exists = departmentRepository.existsByName("Engineering");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_ShouldReturnFalse_WhenNameDoesNotExist() {
        // When
        boolean exists = departmentRepository.existsByName("NonExistent");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByCode_ShouldReturnTrue_WhenCodeExists() {
        // When
        boolean exists = departmentRepository.existsByCode("ENG");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByParentId_ShouldReturnTrue_WhenDepartmentHasChildren() {
        // When
        boolean hasChildren = departmentRepository.existsByParentId(rootDepartment.getId());

        // Then
        assertThat(hasChildren).isTrue();
    }

    @Test
    void existsByParentId_ShouldReturnFalse_WhenDepartmentHasNoChildren() {
        // When
        boolean hasChildren = departmentRepository.existsByParentId(childDepartment.getId());

        // Then
        assertThat(hasChildren).isFalse();
    }

    @Test
    void countByParentId_ShouldReturnCorrectCount() {
        // Given
        Department anotherChild = TestDataBuilder.department()
                .withName("DevOps")
                .withCode("DEVOPS")
                .withParentId(rootDepartment.getId())
                .withLevel(2)
                .withDepPath("/ENG/DEVOPS")
                .build();
        entityManager.persistAndFlush(anotherChild);

        // When
        long childCount = departmentRepository.countByParentId(rootDepartment.getId());

        // Then
        assertThat(childCount).isEqualTo(2);
    }

    @Test
    void findByManagerId_ShouldReturnDepartmentsManagedByEmployee() {
        // Given
        Long managerId = 123L;
        rootDepartment.setManagerId(managerId);
        entityManager.persistAndFlush(rootDepartment);

        // When
        List<Department> managedDepartments = departmentRepository.findByManagerId(managerId);

        // Then
        assertThat(managedDepartments).hasSize(1);
        assertThat(managedDepartments.get(0).getName()).isEqualTo("Engineering");
    }

    @Test
    void findAllWithEmployeeCount_ShouldExecuteNativeQuery() {
        // When
        List<Department> departments = departmentRepository.findAllWithEmployeeCount();

        // Then
        assertThat(departments).isNotEmpty();
        // Note: Employee count will be 0 since we don't have employees in this test
    }

    @Test
    void findHierarchy_ShouldReturnDepartmentHierarchy() {
        // Given
        Department grandChild = TestDataBuilder.department()
                .withName("Backend Team")
                .withCode("BE")
                .withParentId(childDepartment.getId())
                .withLevel(3)
                .withDepPath("/ENG/SW/BE")
                .build();
        entityManager.persistAndFlush(grandChild);

        // When
        List<Department> hierarchy = departmentRepository.findHierarchy(rootDepartment.getId());

        // Then
        assertThat(hierarchy).hasSize(3);
        assertThat(hierarchy).extracting(Department::getName)
                .containsExactlyInAnyOrder("Engineering", "Software Development", "Backend Team");
    }

    @Test
    void customQueryPerformance_ShouldExecuteWithinReasonableTime() {
        // Given - Create multiple departments for performance testing
        for (int i = 1; i <= 10; i++) {
            Department dept = TestDataBuilder.department()
                    .withName("Department " + i)
                    .withCode("DEPT" + i)
                    .withLevel(1)
                    .withDepPath("/DEPT" + i)
                    .build();
            entityManager.persistAndFlush(dept);
        }

        long startTime = System.currentTimeMillis();

        // When
        List<Department> result = departmentRepository.findByEnabledTrueOrderByDepPath();

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Then
        assertThat(result).hasSizeGreaterThan(10);
        assertThat(executionTime).isLessThan(1000); // Should execute within 1 second
    }

    @Test
    void transactionRollback_ShouldNotPersistChanges() {
        // Given
        Long initialCount = departmentRepository.count();

        // When - Simulate transaction rollback
        try {
            Department newDept = TestDataBuilder.department()
                    .withName("Rollback Test")
                    .withCode("ROLLBACK")
                    .withLevel(1)
                    .withDepPath("/ROLLBACK")
                    .build();
            entityManager.persist(newDept);
            
            // Force an exception to trigger rollback
            throw new RuntimeException("Simulated exception");
        } catch (RuntimeException e) {
            // Expected exception
        }

        entityManager.clear();

        // Then
        Long finalCount = departmentRepository.count();
        assertThat(finalCount).isEqualTo(initialCount);
    }
}