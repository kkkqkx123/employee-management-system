package com.example.demo.department.repository;

import com.example.demo.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    /**
     * Find department by name
     * @param name Department name
     * @return Optional department
     */
    Optional<Department> findByName(String name);
    
    /**
     * Find departments by parent ID
     * @param parentId Parent department ID
     * @return List of child departments
     */
    List<Department> findByParentIdOrderBySortOrder(Long parentId);
    
    /**
     * Find root departments (parentId is null)
     * @return List of root departments
     */
    List<Department> findByParentIdIsNullOrderBySortOrder();
    
    /**
     * Find departments by level
     * @param level Hierarchy level
     * @return List of departments at specified level
     */
    List<Department> findByLevel(Integer level);
    
    /**
     * Find departments by path prefix (for subtree queries)
     * @param pathPrefix Path prefix to match
     * @return List of departments in subtree
     */
    List<Department> findByDepPathStartingWithOrderByDepPath(String pathPrefix);
    
    /**
     * Find enabled departments
     * @return List of enabled departments
     */
    List<Department> findByEnabledTrueOrderByDepPath();
    
    /**
     * Find departments by name containing (case insensitive)
     * @param name Name search term
     * @return List of matching departments
     */
    List<Department> findByNameContainingIgnoreCaseOrderByName(String name);
    
    /**
     * Find departments by code
     * @param code Department code
     * @return Optional department
     */
    Optional<Department> findByCode(String code);
    
    /**
     * Check if department name exists
     * @param name Department name
     * @return true if exists
     */
    boolean existsByName(String name);
    
    /**
     * Check if department code exists
     * @param code Department code
     * @return true if exists
     */
    boolean existsByCode(String code);
    
    /**
     * Check if department has children
     * @param parentId Parent department ID
     * @return true if has children
     */
    boolean existsByParentId(Long parentId);
    
    /**
     * Count departments by parent ID
     * @param parentId Parent department ID
     * @return Count of child departments
     */
    long countByParentId(Long parentId);
    
    /**
     * Find departments by manager ID
     * @param managerId Manager employee ID
     * @return List of departments managed by the employee
     */
    List<Department> findByManagerId(Long managerId);
    
    /**
     * Find all departments with employee count using native query
     * @return List of departments with employee count
     */
    @Query(value = """
        SELECT d.*, COALESCE(e.employee_count, 0) as employee_count
        FROM departments d
        LEFT JOIN (
            SELECT department_id, COUNT(*) as employee_count
            FROM employees
            WHERE status = 'ACTIVE'
            GROUP BY department_id
        ) e ON d.id = e.department_id
        ORDER BY d.dep_path
        """, nativeQuery = true)
    List<Department> findAllWithEmployeeCount();
    
    /**
     * Find department hierarchy using recursive CTE
     * @param rootId Root department ID
     * @return List of departments in hierarchy
     */
    @Query(value = """
        WITH RECURSIVE department_hierarchy AS (
            SELECT id, name, code, parent_id, dep_path, level, sort_order, enabled
            FROM departments
            WHERE id = :rootId
            UNION ALL
            SELECT d.id, d.name, d.code, d.parent_id, d.dep_path, d.level, d.sort_order, d.enabled
            FROM departments d
            INNER JOIN department_hierarchy dh ON d.parent_id = dh.id
        )
        SELECT * FROM department_hierarchy
        ORDER BY dep_path, sort_order
        """, nativeQuery = true)
    List<Department> findHierarchy(@Param("rootId") Long rootId);
}