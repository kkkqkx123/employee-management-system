package com.example.demo.security.service.impl;

import com.example.demo.security.dto.RoleDto;
import com.example.demo.security.entity.Role;
import com.example.demo.security.entity.Resource;
import com.example.demo.security.exception.RoleAlreadyExistsException;
import com.example.demo.security.exception.RoleNotFoundException;
import com.example.demo.security.repository.RoleRepository;
import com.example.demo.security.repository.ResourceRepository;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoleServiceImpl using Mockito.
 * Tests role management, validation, and resource assignment.
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role role;
    private Resource resource;
    private RoleDto roleDto;

    @BeforeEach
    void setUp() {
        resource = new Resource();
        resource.setId(1L);
        resource.setName("USER_READ");
        resource.setDescription("Read user information");

        role = new Role();
        role.setId(1L);
        role.setName("ADMIN");
        role.setDescription("Administrator role");
        role.setResources(new HashSet<>(Arrays.asList(resource)));

        roleDto = new RoleDto();
        roleDto.setName("USER");
        roleDto.setDescription("Regular user role");
    }

    @Test
    void createRole_shouldCreateRoleSuccessfully() {
        // Given
        when(roleRepository.existsByName("USER")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        // When
        RoleDto result = roleService.createRole(roleDto);

        // Then
        assertThat(result).isNotNull();
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void createRole_shouldThrowExceptionWhenRoleNameExists() {
        // Given
        when(roleRepository.existsByName("USER")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> roleService.createRole(roleDto))
                .isInstanceOf(RoleAlreadyExistsException.class)
                .hasMessageContaining("Role name already exists");

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void getRoleById_shouldReturnRoleWhenExists() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        // When
        RoleDto result = roleService.getRoleById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(role.getName());
        assertThat(result.getDescription()).isEqualTo(role.getDescription());
    }

    @Test
    void getRoleById_shouldThrowExceptionWhenNotExists() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roleService.getRoleById(1L))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessageContaining("Role not found with id: 1");
    }

    @Test
    void updateRole_shouldUpdateRoleSuccessfully() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.existsByName("USER")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        // When
        RoleDto result = roleService.updateRole(1L, roleDto);

        // Then
        assertThat(result).isNotNull();
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void updateRole_shouldThrowExceptionWhenNameExistsForOtherRole() {
        // Given
        role.setName("ADMIN");
        roleDto.setName("USER");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.existsByName("USER")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> roleService.updateRole(1L, roleDto))
                .isInstanceOf(RoleAlreadyExistsException.class)
                .hasMessageContaining("Role already exists with name: USER");

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void deleteRole_shouldDeleteRoleSuccessfully() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        // When
        roleService.deleteRole(1L);

        // Then
        verify(roleRepository).delete(role);
    }


    @Test
    void getAllRoles_shouldReturnPagedRoles() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Role> roles = Arrays.asList(role);
        Page<Role> rolePage = new PageImpl<>(roles, pageable, 1);
        when(roleRepository.findAll(pageable)).thenReturn(rolePage);

        // When
        Page<RoleDto> result = roleService.getAllRoles(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(role.getName());
    }


    @Test
    void assignResourcesToRole_shouldAssignResourcesSuccessfully() {
        // Given
        Set<Long> resourceIds = new HashSet<>(Arrays.asList(1L, 2L));
        Resource resource2 = new Resource();
        resource2.setId(2L);
        resource2.setName("USER_WRITE");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
        when(resourceRepository.findById(2L)).thenReturn(Optional.of(resource2));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        // When
        roleService.assignResourcesToRole(1L, resourceIds);

        // Then
        verify(roleRepository).save(argThat(r -> r.getResources().size() == 2));
    }

    @Test
    void removeResourcesFromRole_shouldRemoveResourcesSuccessfully() {
        // Given
        Set<Long> resourceIds = new HashSet<>(Arrays.asList(1L));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        // When
        roleService.removeResourcesFromRole(1L, resourceIds);

        // Then
        verify(roleRepository).save(argThat(r -> r.getResources().isEmpty()));
    }

    @Test
    void getRolePermissions_shouldReturnRolePermissions() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        // When
        Set<String> result = roleService.getRolePermissions(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next()).isEqualTo("USER_READ");
    }

    @Test
    void searchRoles_shouldReturnFilteredRoles() {
        // Given
        String searchTerm = "ADMIN";
        List<Role> roles = Arrays.asList(role);
        when(roleRepository.findByNameContainingIgnoreCase(searchTerm))
                .thenReturn(roles);

        // When
        List<RoleDto> result = roleService.searchRoles(searchTerm);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("ADMIN");
    }
}