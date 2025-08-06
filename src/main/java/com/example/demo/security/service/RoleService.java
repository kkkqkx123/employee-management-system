package com.example.demo.security.service;

import com.example.demo.security.dto.RoleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * Role Service Interface
 * 
 * Defines operations for role management including CRUD operations,
 * resource assignments, and permission management.
 */
public interface RoleService {
    
    /**
     * Get all roles
     * 
     * @return List of all roles
     */
    List<RoleDto> getAllRoles();
    
    /**
     * Get all roles with pagination
     * 
     * @param pageable Pagination parameters
     * @return Paginated list of roles
     */
    Page<RoleDto> getAllRoles(Pageable pageable);
    
    /**
     * Get role by ID
     * 
     * @param id Role ID
     * @return Role details
     */
    RoleDto getRoleById(Long id);
    
    /**
     * Search roles by name
     * 
     * @param searchTerm Search term for role name
     * @return List of matching roles
     */
    List<RoleDto> searchRoles(String searchTerm);
    
    /**
     * Create new role
     * 
     * @param roleDto Role creation data
     * @return Created role details
     */
    RoleDto createRole(RoleDto roleDto);
    
    /**
     * Update existing role
     * 
     * @param id Role ID
     * @param roleDto Updated role data
     * @return Updated role details
     */
    RoleDto updateRole(Long id, RoleDto roleDto);
    
    /**
     * Delete role
     * 
     * @param id Role ID
     */
    void deleteRole(Long id);
    
    /**
     * Enable or disable role
     * 
     * @param id Role ID
     * @param enabled Enable/disable flag
     */
    void setRoleEnabled(Long id, boolean enabled);
    
    /**
     * Assign resources to role
     * 
     * @param roleId Role ID
     * @param resourceIds Set of resource IDs to assign
     */
    void assignResourcesToRole(Long roleId, Set<Long> resourceIds);
    
    /**
     * Remove resources from role
     * 
     * @param roleId Role ID
     * @param resourceIds Set of resource IDs to remove
     */
    void removeResourcesFromRole(Long roleId, Set<Long> resourceIds);
    
    /**
     * Get role permissions
     * 
     * @param roleId Role ID
     * @return Set of role permissions
     */
    Set<String> getRolePermissions(Long roleId);
}