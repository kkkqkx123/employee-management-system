package com.example.demo.security.repository;

import com.example.demo.config.TestConfig;
import com.example.demo.security.entity.Role;
import com.example.demo.security.entity.User;
import com.example.demo.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for UserRepository.
 * Tests custom query methods, pagination, and sorting functionality.
 */
@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // Create test role
        testRole = TestDataBuilder.role()
                .withName("ROLE_TEST")
                .withDescription("Test role")
                .build();
        testRole = entityManager.persistAndFlush(testRole);

        // Create test user
        testUser = TestDataBuilder.user()
                .withUsername("testuser")
                .withEmail("test@example.com")
                .withFirstName("Test")
                .withLastName("User")
                .withRole(testRole)
                .build();
        testUser = entityManager.persistAndFlush(testUser);

        entityManager.clear();
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        // When
        Optional<User> result = userRepository.findByUsername("testuser");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // When
        Optional<User> result = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        // When
        Optional<User> result = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenUsernameExists() {
        // When
        boolean exists = userRepository.existsByUsername("testuser");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenUsernameDoesNotExist() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void findByEnabledTrue_ShouldReturnEnabledUsers() {
        // Given
        User disabledUser = TestDataBuilder.user()
                .withUsername("disabled")
                .withEmail("disabled@example.com")
                .withEnabled(false)
                .build();
        entityManager.persistAndFlush(disabledUser);

        // When
        List<User> enabledUsers = userRepository.findByEnabledTrue();

        // Then
        assertThat(enabledUsers).hasSize(1);
        assertThat(enabledUsers.get(0).getUsername()).isEqualTo("testuser");
        assertThat(enabledUsers.get(0).getEnabled()).isTrue();
    }

    @Test
    void findByEnabledTrueWithPagination_ShouldReturnPagedResults() {
        // Given
        for (int i = 1; i <= 5; i++) {
            User user = TestDataBuilder.user()
                    .withUsername("user" + i)
                    .withEmail("user" + i + "@example.com")
                    .build();
            entityManager.persistAndFlush(user);
        }

        Pageable pageable = PageRequest.of(0, 3);

        // When
        Page<User> result = userRepository.findByEnabledTrue(pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(6); // 5 new + 1 original
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void findByUsernameContainingIgnoreCase_ShouldReturnMatchingUsers() {
        // Given
        User user1 = TestDataBuilder.user()
                .withUsername("TestUser1")
                .withEmail("testuser1@example.com")
                .build();
        User user2 = TestDataBuilder.user()
                .withUsername("AnotherUser")
                .withEmail("another@example.com")
                .build();
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        // When
        List<User> result = userRepository.findByUsernameContainingIgnoreCase("test");

        // Then
        assertThat(result).hasSize(2); // testuser and TestUser1
        assertThat(result).extracting(User::getUsername)
                .containsExactlyInAnyOrder("testuser", "TestUser1");
    }

    @Test
    void findByFirstNameOrLastNameContainingIgnoreCase_ShouldReturnMatchingUsers() {
        // Given
        User user1 = TestDataBuilder.user()
                .withUsername("user1")
                .withEmail("user1@example.com")
                .withFirstName("John")
                .withLastName("Smith")
                .build();
        entityManager.persistAndFlush(user1);

        // When
        List<User> result = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("john", "john");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void findUsersWithRoles_ShouldReturnUsersWithRolesLoaded() {
        // When
        List<User> result = userRepository.findUsersWithRoles();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getRoles()).isNotEmpty();
        assertThat(result.get(0).getRoles()).contains(testRole);
    }

    @Test
    void findByUsernameWithRoles_ShouldReturnUserWithRoles() {
        // When
        Optional<User> result = userRepository.findByUsernameWithRoles("testuser");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getRoles()).isNotEmpty();
        assertThat(result.get().getRoles()).contains(testRole);
    }

    @Test
    void findByRoleName_ShouldReturnUsersWithSpecificRole() {
        // When
        List<User> result = userRepository.findByRoleName("ROLE_TEST");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    void searchUsers_ShouldReturnMatchingUsers() {
        // Given
        User user1 = TestDataBuilder.user()
                .withUsername("john.doe")
                .withEmail("john.doe@example.com")
                .withFirstName("John")
                .withLastName("Doe")
                .build();
        entityManager.persistAndFlush(user1);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.searchUsers("john", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void searchUsers_ShouldSearchInMultipleFields() {
        // Given
        User user1 = TestDataBuilder.user()
                .withUsername("jane.smith")
                .withEmail("jane.smith@company.com")
                .withFirstName("Jane")
                .withLastName("Smith")
                .build();
        entityManager.persistAndFlush(user1);

        Pageable pageable = PageRequest.of(0, 10);

        // When - Search by email domain
        Page<User> result = userRepository.searchUsers("company", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).contains("company.com");
    }

    @Test
    void transactionRollback_ShouldNotPersistChanges() {
        // Given
        Long initialCount = userRepository.count();

        // When - Simulate transaction rollback
        try {
            User newUser = TestDataBuilder.user()
                    .withUsername("rollback_test")
                    .withEmail("rollback@example.com")
                    .build();
            entityManager.persist(newUser);
            
            // Force an exception to trigger rollback
            throw new RuntimeException("Simulated exception");
        } catch (RuntimeException e) {
            // Expected exception
        }

        entityManager.clear();

        // Then
        Long finalCount = userRepository.count();
        assertThat(finalCount).isEqualTo(initialCount);
    }
}