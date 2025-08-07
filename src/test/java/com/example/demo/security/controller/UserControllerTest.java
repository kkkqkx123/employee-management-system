package com.example.demo.security.controller;

import com.example.demo.security.dto.UserCreateRequest;
import com.example.demo.security.dto.UserDto;
import com.example.demo.security.dto.UserUpdateRequest;
import com.example.demo.security.service.UserService;
import com.example.demo.security.exception.UserNotFoundException;
import com.example.demo.security.exception.UserAlreadyExistsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController
 * Tests user management endpoints with MockMvc and security annotations
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto userDto;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .roles(Set.of())
                .build();

        createRequest = UserCreateRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .firstName("New")
                .lastName("User")
                .password("password123")
                .build();

        updateRequest = UserUpdateRequest.builder()
                .firstName("Updated")
                .lastName("User")
                .email("updated@example.com")
                .build();
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void getAllUsers_WithValidPermission_ShouldReturnPagedUsers() throws Exception {
        // Given
        Page<UserDto> userPage = new PageImpl<>(List.of(userDto), PageRequest.of(0, 20), 1);
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].username").value("testuser"))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(userService).getAllUsers(any(Pageable.class));
    }

    @Test
    @WithMockUser(authorities = "WRONG_PERMISSION")
    void getAllUsers_WithoutPermission_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAllUsers(any(Pageable.class));
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void getUserById_WithValidId_ShouldReturnUser() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(userDto);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void getUserById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenThrow(new UserNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(999L);
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void searchUsers_WithSearchTerm_ShouldReturnMatchingUsers() throws Exception {
        // Given
        Page<UserDto> userPage = new PageImpl<>(List.of(userDto), PageRequest.of(0, 20), 1);
        when(userService.searchUsers(eq("test"), any(Pageable.class))).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/api/users/search")
                        .param("searchTerm", "test")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].username").value("testuser"));

        verify(userService).searchUsers(eq("test"), any(Pageable.class));
    }

    @Test
    @WithMockUser(authorities = "USER_CREATE")
    void createUser_WithValidData_ShouldReturnCreatedUser() throws Exception {
        // Given
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(userDto);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.message").value("User created successfully"));

        verify(userService).createUser(any(UserCreateRequest.class));
    }

    @Test
    @WithMockUser(authorities = "USER_CREATE")
    void createUser_WithDuplicateUsername_ShouldReturnConflict() throws Exception {
        // Given
        when(userService.createUser(any(UserCreateRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Username already exists"));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());

        verify(userService).createUser(any(UserCreateRequest.class));
    }

    @Test
    @WithMockUser(authorities = "USER_CREATE")
    void createUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        UserCreateRequest invalidRequest = UserCreateRequest.builder()
                .username("") // Invalid: empty username
                .email("invalid-email") // Invalid: malformed email
                .password("123") // Invalid: too short
                .build();

        // When & Then
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(UserCreateRequest.class));
    }

    @Test
    @WithMockUser(authorities = "USER_UPDATE")
    void updateUser_WithValidData_ShouldReturnUpdatedUser() throws Exception {
        // Given
        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .enabled(true)
                .roles(Set.of())
                .build();
        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Updated"))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"));

        verify(userService).updateUser(eq(1L), any(UserUpdateRequest.class));
    }

    @Test
    @WithMockUser(authorities = "USER_DELETE")
    void deleteUser_WithValidId_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(authorities = "USER_DELETE")
    void deleteUser_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        doThrow(new UserNotFoundException("User not found")).when(userService).deleteUser(999L);

        // When & Then
        mockMvc.perform(delete("/api/users/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(999L);
    }

    @Test
    @WithMockUser(authorities = "USER_UPDATE")
    void setUserEnabled_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(userService).setUserEnabled(1L, false);

        // When & Then
        mockMvc.perform(patch("/api/users/1/enabled")
                        .with(csrf())
                        .param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User disabled successfully"));

        verify(userService).setUserEnabled(1L, false);
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void getUserRoles_WithValidId_ShouldReturnUserWithRoles() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(userDto);

        // When & Then
        mockMvc.perform(get("/api/users/1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ASSIGN")
    void assignRolesToUser_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        Set<Long> roleIds = Set.of(1L, 2L);
        doNothing().when(userService).assignRolesToUser(1L, roleIds);

        // When & Then
        mockMvc.perform(post("/api/users/1/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Roles assigned successfully"));

        verify(userService).assignRolesToUser(1L, roleIds);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ASSIGN")
    void removeRolesFromUser_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        Set<Long> roleIds = Set.of(1L, 2L);
        doNothing().when(userService).removeRolesFromUser(1L, roleIds);

        // When & Then
        mockMvc.perform(delete("/api/users/1/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Roles removed successfully"));

        verify(userService).removeRolesFromUser(1L, roleIds);
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void getUserPermissions_WithValidId_ShouldReturnPermissions() throws Exception {
        // Given
        Set<String> permissions = Set.of("USER_READ", "USER_WRITE");
        when(userService.getUserPermissions(1L)).thenReturn(permissions);

        // When & Then
        mockMvc.perform(get("/api/users/1/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(userService).getUserPermissions(1L);
    }

    @Test
    @WithMockUser(authorities = "USER_UPDATE")
    void changePassword_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(userService).changePassword(1L, "oldPassword", "newPassword");

        // When & Then
        mockMvc.perform(patch("/api/users/1/password")
                        .with(csrf())
                        .param("oldPassword", "oldPassword")
                        .param("newPassword", "newPassword"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(userService).changePassword(1L, "oldPassword", "newPassword");
    }

    @Test
    @WithMockUser(authorities = "PASSWORD_RESET")
    void resetPassword_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(userService).resetPassword(1L, "newPassword");

        // When & Then
        mockMvc.perform(patch("/api/users/1/password/reset")
                        .with(csrf())
                        .param("newPassword", "newPassword"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successfully"));

        verify(userService).resetPassword(1L, "newPassword");
    }
}