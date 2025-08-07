package com.example.demo.util;

import com.example.demo.department.entity.Department;
import com.example.demo.employee.entity.Employee;
import com.example.demo.employee.entity.EmployeeStatus;
import com.example.demo.employee.entity.EmploymentType;
import com.example.demo.employee.entity.Gender;
import com.example.demo.employee.entity.MaritalStatus;
import com.example.demo.employee.entity.PayType;
import com.example.demo.position.entity.Position;
import com.example.demo.position.enums.PositionCategory;
import com.example.demo.position.enums.PositionLevel;
import com.example.demo.security.entity.Role;
import com.example.demo.security.entity.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;

/**
 * Test data builder utility for creating test entities.
 * Provides fluent API for building test data with sensible defaults.
 */
public class TestDataBuilder {

    /**
     * Builder for User entities
     */
    public static class UserBuilder {
        private User user = new User();

        public UserBuilder() {
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setPassword("$2a$10$testhashedpassword");
            user.setFirstName("Test");
            user.setLastName("User");
            user.setEnabled(true);
            user.setAccountLocked(false);
            user.setPasswordExpired(false);
            user.setPasswordChangeRequired(false);
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            user.setRoles(new HashSet<>());
        }

        public UserBuilder withUsername(String username) {
            user.setUsername(username);
            return this;
        }

        public UserBuilder withEmail(String email) {
            user.setEmail(email);
            return this;
        }

        public UserBuilder withPassword(String password) {
            user.setPassword(password);
            return this;
        }

        public UserBuilder withFirstName(String firstName) {
            user.setFirstName(firstName);
            return this;
        }

        public UserBuilder withLastName(String lastName) {
            user.setLastName(lastName);
            return this;
        }

        public UserBuilder withEnabled(boolean enabled) {
            user.setEnabled(enabled);
            return this;
        }

        public UserBuilder withRole(Role role) {
            user.getRoles().add(role);
            return this;
        }

        public UserBuilder withCreatedBy(Long createdBy) {
            user.setCreatedBy(createdBy);
            return this;
        }

        public User build() {
            return user;
        }
    }

    /**
     * Builder for Role entities
     */
    public static class RoleBuilder {
        private Role role = new Role();

        public RoleBuilder() {
            role.setName("ROLE_USER");
            role.setDescription("Default user role");
            role.setActive(true);
            role.setCreatedAt(Instant.now());
            role.setUpdatedAt(Instant.now());
        }

        public RoleBuilder withName(String name) {
            role.setName(name);
            return this;
        }

        public RoleBuilder withDescription(String description) {
            role.setDescription(description);
            return this;
        }

        public RoleBuilder withActive(boolean active) {
            role.setActive(active);
            return this;
        }

        public Role build() {
            return role;
        }
    }

    /**
     * Builder for Department entities
     */
    public static class DepartmentBuilder {
        private Department department = new Department();

        public DepartmentBuilder() {
            department.setName("Test Department");
            department.setCode("TEST");
            department.setDescription("Test department description");
            department.setLevel(1);
            department.setDepPath("/TEST");
            department.setSortOrder(1);
            department.setEnabled(true);
            department.setIsParent(false);
            department.setLocation("Test Location");
            department.setCreatedAt(Instant.now());
            department.setUpdatedAt(Instant.now());
        }

        public DepartmentBuilder withName(String name) {
            department.setName(name);
            return this;
        }

        public DepartmentBuilder withCode(String code) {
            department.setCode(code);
            return this;
        }

        public DepartmentBuilder withDescription(String description) {
            department.setDescription(description);
            return this;
        }

        public DepartmentBuilder withParentId(Long parentId) {
            department.setParentId(parentId);
            return this;
        }

        public DepartmentBuilder withLevel(Integer level) {
            department.setLevel(level);
            return this;
        }

        public DepartmentBuilder withDepPath(String depPath) {
            department.setDepPath(depPath);
            return this;
        }

        public DepartmentBuilder withManagerId(Long managerId) {
            department.setManagerId(managerId);
            return this;
        }

        public DepartmentBuilder withEnabled(boolean enabled) {
            department.setEnabled(enabled);
            return this;
        }

        public DepartmentBuilder withIsParent(boolean isParent) {
            department.setIsParent(isParent);
            return this;
        }

        public DepartmentBuilder withLocation(String location) {
            department.setLocation(location);
            return this;
        }

        public Department build() {
            return department;
        }
    }

    /**
     * Builder for Position entities
     */
    public static class PositionBuilder {
        private Position position = new Position();

        public PositionBuilder() {
            position.setJobTitle("Test Position");
            position.setCode("TEST_POS");
            position.setDescription("Test position description");
            position.setCategory(PositionCategory.TECHNICAL);
            position.setLevel(PositionLevel.JUNIOR);
            position.setEmploymentType(com.example.demo.position.enums.EmploymentType.FULL_TIME);
            position.setMinSalary(BigDecimal.valueOf(50000));
            position.setMaxSalary(BigDecimal.valueOf(80000));
            position.setEnabled(true);
            position.setCreatedAt(Instant.now());
            position.setUpdatedAt(Instant.now());
        }

        public PositionBuilder withJobTitle(String title) {
            position.setJobTitle(title);
            return this;
        }

        public PositionBuilder withCode(String code) {
            position.setCode(code);
            return this;
        }

        public PositionBuilder withCategory(PositionCategory category) {
            position.setCategory(category);
            return this;
        }

        public PositionBuilder withLevel(PositionLevel level) {
            position.setLevel(level);
            return this;
        }

        public PositionBuilder withDepartmentId(Long departmentId) {
            position.setDepartmentId(departmentId);
            return this;
        }

        public PositionBuilder withEnabled(boolean enabled) {
            position.setEnabled(enabled);
            return this;
        }

        public PositionBuilder withProfessionalTitle(String professionalTitle) {
            position.setProfessionalTitle(professionalTitle);
            return this;
        }

        public PositionBuilder withRequirements(String requirements) {
            position.setRequirements(requirements);
            return this;
        }

        public PositionBuilder withResponsibilities(String responsibilities) {
            position.setResponsibilities(responsibilities);
            return this;
        }

        public PositionBuilder withSalaryGrade(String salaryGrade) {
            position.setSalaryGrade(salaryGrade);
            return this;
        }

        public PositionBuilder withIsManagerial(boolean isManagerial) {
            position.setIsManagerial(isManagerial);
            return this;
        }

        public Position build() {
            return position;
        }
    }

    /**
     * Builder for Employee entities
     */
    public static class EmployeeBuilder {
        private Employee employee = new Employee();

        public EmployeeBuilder() {
            employee.setEmployeeNumber("EMP001");
            employee.setFirstName("John");
            employee.setLastName("Doe");
            employee.setEmail("john.doe@example.com");
            employee.setPhone("123-456-7890");
            employee.setDateOfBirth("1990-01-01"); // Stored as encrypted string
            employee.setGender(Gender.MALE);
            employee.setMaritalStatus(MaritalStatus.SINGLE);
            employee.setAddress("123 Test Street");
            employee.setCity("Test City");
            employee.setState("Test State");
            employee.setZipCode("12345");
            employee.setCountry("Test Country");
            employee.setHireDate(LocalDate.now());
            employee.setEmploymentType(EmploymentType.FULL_TIME);
            employee.setStatus(EmployeeStatus.ACTIVE);
            employee.setPayType(PayType.SALARY);
            employee.setSalary(BigDecimal.valueOf(60000));
            employee.setEnabled(true);
            employee.setCreatedAt(Instant.now());
            employee.setUpdatedAt(Instant.now());
        }

        public EmployeeBuilder withEmployeeNumber(String employeeNumber) {
            employee.setEmployeeNumber(employeeNumber);
            return this;
        }

        public EmployeeBuilder withFirstName(String firstName) {
            employee.setFirstName(firstName);
            return this;
        }

        public EmployeeBuilder withLastName(String lastName) {
            employee.setLastName(lastName);
            return this;
        }

        public EmployeeBuilder withEmail(String email) {
            employee.setEmail(email);
            return this;
        }

        public EmployeeBuilder withPhone(String phone) {
            employee.setPhone(phone);
            return this;
        }

        public EmployeeBuilder withDateOfBirth(String dateOfBirth) {
            employee.setDateOfBirth(dateOfBirth);
            return this;
        }

        public EmployeeBuilder withDepartmentId(Long departmentId) {
            employee.setDepartmentId(departmentId);
            return this;
        }

        public EmployeeBuilder withPositionId(Long positionId) {
            employee.setPositionId(positionId);
            return this;
        }

        public EmployeeBuilder withManagerId(Long managerId) {
            employee.setManagerId(managerId);
            return this;
        }

        public EmployeeBuilder withStatus(EmployeeStatus status) {
            employee.setStatus(status);
            return this;
        }

        public EmployeeBuilder withSalary(BigDecimal salary) {
            employee.setSalary(salary);
            return this;
        }

        public EmployeeBuilder withEnabled(boolean enabled) {
            employee.setEnabled(enabled);
            return this;
        }

        public Employee build() {
            return employee;
        }
    }

    // Static factory methods
    public static UserBuilder user() {
        return new UserBuilder();
    }

    public static RoleBuilder role() {
        return new RoleBuilder();
    }

    public static DepartmentBuilder department() {
        return new DepartmentBuilder();
    }

    public static PositionBuilder position() {
        return new PositionBuilder();
    }

    public static EmployeeBuilder employee() {
        return new EmployeeBuilder();
    }
}