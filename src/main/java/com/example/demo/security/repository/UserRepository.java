package com.example.demo.security.repository;

import com.example.demo.security.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     * @param username Username to search for
     * @return Optional User
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     * @param email Email to search for
     * @return Optional User
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists
     * @param username Username to check
     * @return true if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     * @param email Email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find all enabled users
     * @return List of enabled users
     */
    List<User> findByEnabledTrue();
    
    /**
     * Find enabled users with pagination
     * @param pageable Pagination parameters
     * @return Page of enabled users
     */
    Page<User> findByEnabledTrue(Pageable pageable);
    
    /**
     * Find users by username containing search term (case insensitive)
     * @param username Username search term
     * @return List of matching users
     */
    List<User> findByUsernameContainingIgnoreCase(String username);
    
    /**
     * Find users by first name or last name containing search term (case insensitive)
     * @param firstName First name search term
     * @param lastName Last name search term
     * @return List of matching users
     */
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName);
    
    /**
     * Find users with their roles loaded (using JOIN FETCH for performance)
     * @return List of users with roles
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles")
    List<User> findUsersWithRoles();
    
    /**
     * Find user by username with roles loaded
     * @param username Username to search for
     * @return Optional User with roles
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
    
    /**
     * Find users by role name
     * @param roleName Role name to filter by
     * @return List of users with the specified role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    /**
     * Search users by multiple criteria
     * @param searchTerm Search term for username, first name, or last name
     * @param pageable Pagination parameters
     * @return Page of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
}