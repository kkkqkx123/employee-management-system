package com.example.demo.department.service;

import com.example.demo.department.dto.DepartmentDto;
import com.example.demo.department.dto.DepartmentTreeDto;
import com.example.demo.department.dto.DepartmentCreateRequest;
import com.example.demo.department.dto.DepartmentUpdateRequest;
import com.example.demo.department.dto.DepartmentStatisticsDto;

import java.util.List;

public interface DepartmentService {
    
    /**
     * Create a new department
     * @param request Department creation request
     * @return Created department DTO
     * @throws DepartmentAlreadyExistsException if name or code already exists
     * @throws DepartmentNotFoundException if parent department not found
     */
    DepartmentDto createDepartment(DepartmentCreateRequest request);
    
    /**
     * Update an existing department
     * @param id Department ID
     * @param request Department update request
     * @return Updated department DTO
     * @throws DepartmentNotFoundException if department not found
     * @throws DepartmentAlreadyExistsException if name or code conflicts
     */
    DepartmentDto updateDepartment(Long id, DepartmentUpdateRequest request);
    
    /**
     * Get department by ID
     * @param id Department ID
     * @return Department DTO
     * @throws DepartmentNotFoundException if department not found
     */
    DepartmentDto getDepartmentById(Long id);
    
    /**
     * Get department by code
     * @param code Department code
     * @return Department DTO
     * @throws DepartmentNotFoundException if department not found
     */
    DepartmentDto getDepartmentByCode(String code);
    
    /**
     * Get all departments as flat list
     * @return List of department DTOs
     */
    List<DepartmentDto> getAllDepartments();
    
    /**
     * Get department tree structure
     * @return List of root department tree DTOs with children
     */
    List<DepartmentTreeDto> getDepartmentTree();
    
    /**
     * Get department subtree starting from specified department
     * @param departmentId Root department ID for subtree
     * @return Department tree DTO with children
     * @throws DepartmentNotFoundException if department not found
     */
    DepartmentTreeDto getDepartmentSubtree(Long departmentId);
    
    /**
     * Get child departments of specified parent
     * @param parentId Parent department ID (null for root departments)
     * @return List of child department DTOs
     */
    List<DepartmentDto> getChildDepartments(Long parentId);
    
    /**
     * Get departments by level in hierarchy
     * @param level Hierarchy level (0 for root)
     * @return List of department DTOs at specified level
     */
    List<DepartmentDto> getDepartmentsByLevel(Integer level);
    
    /**
     * Search departments by name
     * @param searchTerm Search term for department name
     * @return List of matching department DTOs
     */
    List<DepartmentDto> searchDepartments(String searchTerm);
    
    /**
     * Delete department by ID
     * @param id Department ID
     * @throws DepartmentNotFoundException if department not found
     * @throws DepartmentHierarchyException if department has children
     * @throws DepartmentInUseException if department has employees
     */
    void deleteDepartment(Long id);
    
    /**
     * Move department to new parent
     * @param departmentId Department ID to move
     * @param newParentId New parent department ID (null for root)
     * @throws DepartmentNotFoundException if department or parent not found
     * @throws DepartmentHierarchyException if move would create circular reference
     */
    void moveDepartment(Long departmentId, Long newParentId);
    
    /**
     * Enable or disable department
     * @param id Department ID
     * @param enabled Enable/disable flag
     * @throws DepartmentNotFoundException if department not found
     */
    void setDepartmentEnabled(Long id, boolean enabled);
    
    /**
     * Update department sort order
     * @param id Department ID
     * @param sortOrder New sort order
     * @throws DepartmentNotFoundException if department not found
     */
    void updateSortOrder(Long id, Integer sortOrder);
    
    /**
     * Get department path from root to specified department
     * @param departmentId Department ID
     * @return List of department DTOs representing path from root
     * @throws DepartmentNotFoundException if department not found
     */
    List<DepartmentDto> getDepartmentPath(Long departmentId);
    
    /**
     * Get all ancestor departments of specified department
     * @param departmentId Department ID
     * @return List of ancestor department DTOs
     * @throws DepartmentNotFoundException if department not found
     */
    List<DepartmentDto> getAncestorDepartments(Long departmentId);
    
    /**
     * Get all descendant departments of specified department
     * @param departmentId Department ID
     * @return List of descendant department DTOs
     * @throws DepartmentNotFoundException if department not found
     */
    List<DepartmentDto> getDescendantDepartments(Long departmentId);
    
    /**
     * Check if department can be deleted
     * @param id Department ID
     * @return true if department can be safely deleted
     * @throws DepartmentNotFoundException if department not found
     */
    boolean canDeleteDepartment(Long id);
    
    /**
     * Get department statistics
     * @param departmentId Department ID
     * @return Department statistics including employee count, child count, etc.
     * @throws DepartmentNotFoundException if department not found
     */
    DepartmentStatisticsDto getDepartmentStatistics(Long departmentId);
    
    /**
     * Rebuild department paths (maintenance operation)
     * This method recalculates all department paths and levels
     */
    void rebuildDepartmentPaths();
}