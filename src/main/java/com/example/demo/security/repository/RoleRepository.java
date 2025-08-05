package com.example.demo.security.repository;

import com.example.demo.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find role by name
     * @param name Role name to search for
     * @return Optional Role
     */
    Optional<Role> findByName(String name);
    
    /**
     * Check if role name exists
     * @param name Role name to check
     * @return true if role name exists
     */
    boolean existsByName(String name);
    
    /**
     * Find all active roles
     * @return List of active roles
     */
    List<Role> findByActiveTrue();
    
    /**
     * Find roles by name containing search term (case insensitive)
     * @param name Role name search term
     * @return List of matching roles
     */
    List<Role> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find roles with their resources loaded (using JOIN FETCH for performance)
     * @return List of roles with resources
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.resources")
    List<Role> findRolesWithResources();
    
    /**
     * Find role by name with resources loaded
     * @param name Role name to search for
     * @return Optional Role with resources
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.resources WHERE r.name = :name")
    Optional<Role> findByNameWithResources(@Param("name") String name);
    
    /**
     * Find role by ID with resources loaded
     * @param id Role ID to search for
     * @return Optional Role with resources
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.resources WHERE r.id = :id")
    Optional<Role> findByIdWithResources(@Param("id") Long id);
    
    /**
     * Find roles assigned to a specific user
     * @param userId User ID to search for
     * @return List of roles assigned to the user
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find active roles with resources for permission checking
     * @return List of active roles with resources
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.resources res " +
           "WHERE r.active = true AND res.active = true")
    List<Role> findActiveRolesWithActiveResources();
    
    /**
     * Count users assigned to a role
     * @param roleId Role ID
     * @return Number of users assigned to the role
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    long countUsersByRoleId(@Param("roleId") Long roleId);
}