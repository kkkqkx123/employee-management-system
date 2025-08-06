package com.example.demo.security.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.security.dto.RoleDto;
import com.example.demo.security.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Role Management Controller
 * 
 * Handles role management operations including CRUD operations and resource assignments.
 * All endpoints require authentication and appropriate permissions.
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Management", description = "Role CRUD operations and resource management")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * Get all roles
     * 
     * @return List of all roles
     */
    @GetMapping
    @Operation(
        summary = "Get all roles",
        description = "Retrieve list of all roles. Requires ROLE_READ permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Roles retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAllRoles() {
        
        log.info("Fetching all roles");
        
        List<RoleDto> roles = roleService.getAllRoles();
        
        log.info("Retrieved {} roles", roles.size());
        
        return ResponseEntity.ok(
            ApiResponse.success(roles, "Roles retrieved successfully")
        );
    }
    
    /**
     * Get all roles with pagination
     * 
     * @param pageable Pagination parameters
     * @return Paginated list of roles
     */
    @GetMapping("/paginated")
    @Operation(
        summary = "Get all roles with pagination",
        description = "Retrieve paginated list of all roles. Requires ROLE_READ permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Roles retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<Page<RoleDto>>> getAllRolesPaginated(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Fetching all roles with pagination: {}", pageable);
        
        Page<RoleDto> roles = roleService.getAllRoles(pageable);
        
        log.info("Retrieved {} roles", roles.getTotalElements());
        
        return ResponseEntity.ok(
            ApiResponse.success(roles, "Roles retrieved successfully")
        );
    }    

    /**
     * Get role by ID
     * 
     * @param id Role ID
     * @return Role details
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get role by ID",
        description = "Retrieve role details by ID. Requires ROLE_READ permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Role found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Role not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<RoleDto>> getRoleById(
            @Parameter(description = "Role ID", required = true)
            @PathVariable Long id) {
        
        log.info("Fetching role with ID: {}", id);
        
        RoleDto role = roleService.getRoleById(id);
        
        log.info("Retrieved role: {}", role.getName());
        
        return ResponseEntity.ok(
            ApiResponse.success(role, "Role retrieved successfully")
        );
    }
    
    /**
     * Search roles by name
     * 
     * @param searchTerm Search term for role name
     * @return List of matching roles
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search roles",
        description = "Search roles by name. Requires ROLE_READ permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Search completed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<List<RoleDto>>> searchRoles(
            @Parameter(description = "Search term", required = true)
            @RequestParam String searchTerm) {
        
        log.info("Searching roles with term: '{}'", searchTerm);
        
        List<RoleDto> roles = roleService.searchRoles(searchTerm);
        
        log.info("Found {} roles matching search term", roles.size());
        
        return ResponseEntity.ok(
            ApiResponse.success(roles, "Search completed successfully")
        );
    }
    
    /**
     * Create new role
     * 
     * @param roleDto Role creation data
     * @return Created role details
     */
    @PostMapping
    @Operation(
        summary = "Create new role",
        description = "Create a new role. Requires ROLE_CREATE permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "Role created successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid role data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", 
            description = "Role name already exists"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<ApiResponse<RoleDto>> createRole(
            @Valid @RequestBody RoleDto roleDto) {
        
        log.info("Creating new role with name: {}", roleDto.getName());
        
        RoleDto createdRole = roleService.createRole(roleDto);
        
        log.info("Role created successfully with ID: {}", createdRole.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(createdRole, "Role created successfully")
        );
    }  
  
    /**
     * Update existing role
     * 
     * @param id Role ID
     * @param roleDto Updated role data
     * @return Updated role details
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update role",
        description = "Update existing role details. Requires ROLE_UPDATE permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Role updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid role data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Role not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<RoleDto>> updateRole(
            @Parameter(description = "Role ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody RoleDto roleDto) {
        
        log.info("Updating role with ID: {}", id);
        
        RoleDto updatedRole = roleService.updateRole(id, roleDto);
        
        log.info("Role updated successfully: {}", updatedRole.getName());
        
        return ResponseEntity.ok(
            ApiResponse.success(updatedRole, "Role updated successfully")
        );
    }
    
    /**
     * Delete role
     * 
     * @param id Role ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete role",
        description = "Delete role. Requires ROLE_DELETE permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Role deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Role not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", 
            description = "Role is in use and cannot be deleted"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @Parameter(description = "Role ID", required = true)
            @PathVariable Long id) {
        
        log.info("Deleting role with ID: {}", id);
        
        roleService.deleteRole(id);
        
        log.info("Role deleted successfully with ID: {}", id);
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "Role deleted successfully")
        );
    }
    
    /**
     * Enable or disable role
     * 
     * @param id Role ID
     * @param enabled Enable/disable flag
     * @return Success message
     */
    @PatchMapping("/{id}/enabled")
    @Operation(
        summary = "Enable/disable role",
        description = "Enable or disable role. Requires ROLE_UPDATE permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Role status updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Role not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> setRoleEnabled(
            @Parameter(description = "Role ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Enable/disable flag", required = true)
            @RequestParam boolean enabled) {
        
        log.info("Setting role {} enabled status to: {}", id, enabled);
        
        roleService.setRoleEnabled(id, enabled);
        
        String action = enabled ? "enabled" : "disabled";
        log.info("Role {} successfully {}", id, action);
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "Role " + action + " successfully")
        );
    }  
  
    /**
     * Assign resources to role
     * 
     * @param id Role ID
     * @param resourceIds Set of resource IDs to assign
     * @return Success message
     */
    @PostMapping("/{id}/resources")
    @Operation(
        summary = "Assign resources to role",
        description = "Assign one or more resources to role. Requires RESOURCE_ASSIGN permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Resources assigned successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Role or resource not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('RESOURCE_ASSIGN')")
    public ResponseEntity<ApiResponse<Void>> assignResourcesToRole(
            @Parameter(description = "Role ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Resource IDs to assign", required = true)
            @RequestBody Set<Long> resourceIds) {
        
        log.info("Assigning resources {} to role ID: {}", resourceIds, id);
        
        roleService.assignResourcesToRole(id, resourceIds);
        
        log.info("Resources assigned successfully to role ID: {}", id);
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "Resources assigned successfully")
        );
    }
    
    /**
     * Remove resources from role
     * 
     * @param id Role ID
     * @param resourceIds Set of resource IDs to remove
     * @return Success message
     */
    @DeleteMapping("/{id}/resources")
    @Operation(
        summary = "Remove resources from role",
        description = "Remove one or more resources from role. Requires RESOURCE_ASSIGN permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Resources removed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Role not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('RESOURCE_ASSIGN')")
    public ResponseEntity<ApiResponse<Void>> removeResourcesFromRole(
            @Parameter(description = "Role ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Resource IDs to remove", required = true)
            @RequestBody Set<Long> resourceIds) {
        
        log.info("Removing resources {} from role ID: {}", resourceIds, id);
        
        roleService.removeResourcesFromRole(id, resourceIds);
        
        log.info("Resources removed successfully from role ID: {}", id);
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "Resources removed successfully")
        );
    }
    
    /**
     * Get role permissions
     * 
     * @param id Role ID
     * @return Set of role permissions
     */
    @GetMapping("/{id}/permissions")
    @Operation(
        summary = "Get role permissions",
        description = "Retrieve all permissions for role. Requires ROLE_READ permission."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Role permissions retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Role not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Insufficient permissions"
        )
    })
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<Set<String>>> getRolePermissions(
            @Parameter(description = "Role ID", required = true)
            @PathVariable Long id) {
        
        log.info("Fetching permissions for role ID: {}", id);
        
        Set<String> permissions = roleService.getRolePermissions(id);
        
        log.info("Retrieved {} permissions for role ID: {}", permissions.size(), id);
        
        return ResponseEntity.ok(
            ApiResponse.success(permissions, "Role permissions retrieved successfully")
        );
    }
}