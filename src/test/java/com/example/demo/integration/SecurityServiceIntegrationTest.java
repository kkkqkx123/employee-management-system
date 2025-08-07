package com.example.demo.integration;

import com.example.demo.config.TestConfig;
import com.example.demo.security.dto.LoginRequest;
import com.example.demo.security.dto.LoginResponse;
import com.example.demo.security.dto.UserCreateRequest;
import com.example.demo.security.dto.UserDto;
import com.example.demo.security.dto.UserUpdateRequest;
import com.example.demo.security.entity.Role;
import com.example.demo.security.entity.User;
import com.example.demo.security.exception.AuthenticationException;
import com.example.demo.security.exception.UserAlreadyExistsException;
import com.example.demo.security.repository.RoleRepository;
import com.example.demo.security.repository.UserRepository;
import com.example.demo.security.service.AuthenticationService;
import com.example.demo.security.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for Security Services.
 * Tests authentication and user management with actual database interactions.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class SecurityServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role userRole;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create a basic user role
        userRole = new Role();
        userRole.setName("USER");
        userRole.setDescription("Basic user role");
        userRole = roleRepository.save(userRole);
    }

    @Test
    void createUser_shouldPersistToDatabase() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setRoleIds(Set.of(userRole.getId()));

        // When
        UserDto result = userService.createUser(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();

        // Verify persistence
        User saved = userRepository.findById(result.getId()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("testuser");
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getFirstName()).isEqualTo("Test");
        assertThat(saved.getLastName()).isEqualTo("User");
        assertThat(passwordEncoder.matches("password123", saved.getPassword())).isTrue();
        assertThat(saved.getRoles()).hasSize(1);
        assertThat(saved.getRoles().iterator().next().getName()).isEqualTo("USER");
    }

    @Test
    void createUser_shouldThrowExceptionWhenUsernameExists() {
        // Given
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword("encodedPassword");
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setEnabled(true);
        existingUser.setAccountLocked(false);
        userRepository.save(existingUser);

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("existinguser"); // Same username
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setRoleIds(Set.of(userRole.getId()));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void authenticate_shouldReturnTokenForValidCredentials() {
        // Given
        User user = new User();
        user.setUsername("authuser");
        user.setEmail("auth@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFirstName("Auth");
        user.setLastName("User");
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setLoginAttempts(0);
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("authuser");
        loginRequest.setPassword("password123");

        // When
        LoginResponse result = authenticationService.authenticate(loginRequest);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getUser().getUsername()).isEqualTo("authuser");

        // Verify last login date was updated
        User updatedUser = userRepository.findById(user.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getLastLogin()).isNotNull();
        assertThat(updatedUser.getLoginAttempts()).isEqualTo(0);
    }

    @Test
    void authenticate_shouldIncrementFailedAttemptsForInvalidPassword() {
        // Given
        User user = new User();
        user.setUsername("failuser");
        user.setEmail("fail@example.com");
        user.setPassword(passwordEncoder.encode("correctpassword"));
        user.setFirstName("Fail");
        user.setLastName("User");
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setLoginAttempts(0);
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("failuser");
        loginRequest.setPassword("wrongpassword");

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(AuthenticationException.class);

        // Verify failed attempts were incremented
        User updatedUser = userRepository.findById(user.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getLoginAttempts()).isEqualTo(1);
    }

    @Test
    void authenticate_shouldLockAccountAfterMaxFailedAttempts() {
        // Given
        User user = new User();
        user.setUsername("lockuser");
        user.setEmail("lock@example.com");
        user.setPassword(passwordEncoder.encode("correctpassword"));
        user.setFirstName("Lock");
        user.setLastName("User");
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setLoginAttempts(4); // One more failure will lock
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("lockuser");
        loginRequest.setPassword("wrongpassword");

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(AuthenticationException.class);

        // Verify account was locked
        User updatedUser = userRepository.findById(user.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getLoginAttempts()).isEqualTo(5);
        assertThat(updatedUser.getAccountLocked()).isTrue();
    }

    @Test
    void getUserByUsername_shouldReturnFromDatabase() {
        // Given
        User user = new User();
        user.setUsername("getuser");
        user.setEmail("get@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("Get");
        user.setLastName("User");
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);

        // When
        UserDto result = userService.getUserByUsername("getuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getUsername()).isEqualTo("getuser");
        assertThat(result.getEmail()).isEqualTo("get@example.com");
        assertThat(result.getFirstName()).isEqualTo("Get");
        assertThat(result.getLastName()).isEqualTo("User");
    }

    @Test
    void updateUser_shouldPersistChanges() {
        // Given
        User user = new User();
        user.setUsername("updateuser");
        user.setEmail("update@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("Original");
        user.setLastName("Name");
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("updated@example.com");
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");
        updateRequest.setRoleIds(Set.of(userRole.getId()));

        // When
        UserDto result = userService.updateUser(user.getId(), updateRequest);

        // Then
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getFirstName()).isEqualTo("Updated");

        // Verify persistence
        User updated = userRepository.findById(user.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.getFirstName()).isEqualTo("Updated");
        assertThat(updated.getLastName()).isEqualTo("Name");
    }

    @Test
    void setUserEnabled_shouldDisableUser() {
        // Given
        User user = new User();
        user.setUsername("deactivateuser");
        user.setEmail("deactivate@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("Deactivate");
        user.setLastName("User");
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);

        // When
        userService.setUserEnabled(user.getId(), false);

        // Then
        User deactivated = userRepository.findById(user.getId()).orElse(null);
        assertThat(deactivated).isNotNull();
        assertThat(deactivated.getEnabled()).isFalse();
    }

    @Test
    void setUserEnabled_shouldEnableUser() {
        // Given
        User user = new User();
        user.setUsername("activateuser");
        user.setEmail("activate@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("Activate");
        user.setLastName("User");
        user.setEnabled(false); // Start disabled
        user.setAccountLocked(false);
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);

        // When
        userService.setUserEnabled(user.getId(), true);

        // Then
        User activated = userRepository.findById(user.getId()).orElse(null);
        assertThat(activated).isNotNull();
        assertThat(activated.getEnabled()).isTrue();
    }
}