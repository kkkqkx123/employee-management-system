package com.example.demo.employee.repository;

import com.example.demo.employee.entity.Employee;
import com.example.demo.employee.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    /**
     * Find employee by employee number
     */
    Optional<Employee> findByEmployeeNumber(String employeeNumber);
    
    /**
     * Find employee by email
     */
    Optional<Employee> findByEmail(String email);
    
    /**
     * Find employees by department ID
     */
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);
    
    /**
     * Find employees by department ID (list version)
     */
    List<Employee> findByDepartmentId(Long departmentId);
    
    /**
     * Find employees by position ID
     */
    Page<Employee> findByPositionId(Long positionId, Pageable pageable);
    
    /**
     * Find employees by manager ID
     */
    Page<Employee> findByManagerId(Long managerId, Pageable pageable);
    
    /**
     * Find employees by status
     */
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);
    
    /**
     * Find employees by hire date range
     */
    Page<Employee> findByHireDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Find employees by first name containing (case insensitive)
     */
    Page<Employee> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);
    
    /**
     * Find employees by last name containing (case insensitive)
     */
    Page<Employee> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);
    
    /**
     * Find employees by first name or last name containing (case insensitive)
     */
    Page<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName, Pageable pageable);
    
    /**
     * Find employees by email containing (case insensitive)
     */
    Page<Employee> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    
    /**
     * Find enabled employees
     */
    Page<Employee> findByEnabledTrue(Pageable pageable);
    
    /**
     * Check if employee number exists
     */
    boolean existsByEmployeeNumber(String employeeNumber);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Count employees by department ID
     */
    long countByDepartmentId(Long departmentId);
    
    /**
     * Count employees by position ID
     */
    long countByPositionId(Long positionId);
    
    /**
     * Count employees by status
     */
    long countByStatus(EmployeeStatus status);
    
    /**
     * Find employees by IDs
     */
    List<Employee> findByIdIn(List<Long> ids);
    
    /**
     * Search employees by name (first or last name)
     */
    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.employeeNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Employee> searchEmployees(@Param("searchTerm") String searchTerm, Pageable pageable);
}