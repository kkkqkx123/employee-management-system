package com.example.demo.security.service.impl;

import com.example.demo.security.dto.ResourceDto;
import com.example.demo.security.dto.RoleDto;
import com.example.demo.security.entity.Role;
import com.example.demo.security.entity.Resource;
import com.example.demo.security.repository.RoleRepository;
import com.example.demo.security.repository.ResourceRepository;
import com.example.demo.security.service.RoleService;
import com.example.demo.security.exception.RoleNotFoundException;
import com.example.demo.security.exception.RoleAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Role Service Implementation
 * 
 * Implements role management operations including CRUD operations,
 * resource assignments, and permission management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final ResourceRepository resourceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        log.debug("Fetching all roles");

        List<Role> roles = roleRepository.findAll();

        return roles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleDto> getAllRoles(Pageable pageable) {
        log.debug("Fetching all roles with pagination: {}", pageable);

        Page<Role> roles = roleRepository.findAll(pageable);

        return roles.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDto getRoleById(Long id) {
        log.debug("Fetching role with ID: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + id));

        return convertToDto(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> searchRoles(String searchTerm) {
        log.debug("Searching roles with term: '{}'", searchTerm);

        List<Role> roles = roleRepository.findByNameContainingIgnoreCase(searchTerm);

        return roles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDto createRole(RoleDto roleDto) {
        log.debug("Creating new role with name: {}", roleDto.getName());

        // Check if role name already exists
        if (roleRepository.existsByName(roleDto.getName())) {
            throw new RoleAlreadyExistsException("Role already exists with name: " + roleDto.getName());
        }

        Role role = convertToEntity(roleDto);
        role.setActive(true); // New roles are active by default

        Role savedRole = roleRepository.save(role);

        log.info("Role created successfully with ID: {}", savedRole.getId());

        return convertToDto(savedRole);
    }

    @Override
    public RoleDto updateRole(Long id, RoleDto roleDto) {
        log.debug("Updating role with ID: {}", id);

        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + id));

        // Check if new name conflicts with existing role (excluding current role)
        if (!existingRole.getName().equals(roleDto.getName()) &&
                roleRepository.existsByName(roleDto.getName())) {
            throw new RoleAlreadyExistsException("Role already exists with name: " + roleDto.getName());
        }

        existingRole.setName(roleDto.getName());
        existingRole.setDescription(roleDto.getDescription());

        Role updatedRole = roleRepository.save(existingRole);

        log.info("Role updated successfully: {}", updatedRole.getName());

        return convertToDto(updatedRole);
    }

    @Override
    public void deleteRole(Long id) {
        log.debug("Deleting role with ID: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + id));

        // Clear all resource associations first
        role.getResources().clear();
        roleRepository.save(role);

        // Delete the role
        roleRepository.delete(role);

        log.info("Role deleted successfully with ID: {}", id);
    }

    @Override
    public void setRoleEnabled(Long id, boolean enabled) {
        log.debug("Setting role {} enabled status to: {}", id, enabled);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + id));

        role.setActive(enabled);
        roleRepository.save(role);

        String action = enabled ? "enabled" : "disabled";
        log.info("Role {} successfully {}", id, action);
    }

    @Override
    public void assignResourcesToRole(Long roleId, Set<Long> resourceIds) {
        log.debug("Assigning resources {} to role ID: {}", resourceIds, roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + roleId));

        for (Long resourceId : resourceIds) {
            Resource resource = resourceRepository.findById(resourceId)
                    .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));

            // Add resource to role if not already present
            role.getResources().add(resource);
        }

        roleRepository.save(role);

        log.info("Resources assigned successfully to role ID: {}", roleId);
    }

    @Override
    public void removeResourcesFromRole(Long roleId, Set<Long> resourceIds) {
        log.debug("Removing resources {} from role ID: {}", resourceIds, roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + roleId));

        for (Long resourceId : resourceIds) {
            Resource resource = resourceRepository.findById(resourceId).orElse(null);
            if (resource != null) {
                role.getResources().remove(resource);
            }
        }

        roleRepository.save(role);

        log.info("Resources removed successfully from role ID: {}", roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getRolePermissions(Long roleId) {
        log.debug("Fetching permissions for role ID: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + roleId));

        Set<String> permissions = role.getResources().stream()
                .map(Resource::getName)
                .collect(Collectors.toSet());

        log.debug("Retrieved {} permissions for role ID: {}", permissions.size(), roleId);

        return permissions;
    }

    /**
     * Convert Role entity to RoleDto
     */
/**
 * Convert Role entity to RoleDto
 */
private RoleDto convertToDto(Role role) {
    RoleDto dto = new RoleDto();
    dto.setId(role.getId());
    dto.setName(role.getName());
    dto.setDescription(role.getDescription());
    dto.setActive(role.getActive());
    dto.setCreatedAt(role.getCreatedAt());
    dto.setUpdatedAt(role.getUpdatedAt());

    // Load resources
    Set<ResourceDto> resourceDtos = role.getResources().stream()
            .map(this::convertToResourceDto)
            .collect(Collectors.toSet());
    dto.setResources(resourceDtos);

    return dto;
}

    /**
     * Convert Resource entity to ResourceDto
     */
    private ResourceDto convertToResourceDto(Resource resource) {
        ResourceDto resourceDto = new ResourceDto();
        resourceDto.setId(resource.getId());
        resourceDto.setName(resource.getName());
        resourceDto.setDescription(resource.getDescription());
        resourceDto.setActive(resource.getActive());
        resourceDto.setCreatedAt(resource.getCreatedAt());
        resourceDto.setUpdatedAt(resource.getUpdatedAt());

        return resourceDto;
    }


    /**
     * Convert RoleDto to Role entity
     */
    private Role convertToEntity(RoleDto dto) {
        Role role = new Role();
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setActive(dto.getActive() != null ? dto.getActive() : true);

        return role;
    }
}