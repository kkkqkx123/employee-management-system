package com.example.demo.security.repository;

import com.example.demo.security.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    
    /**
     * Find resource by URL and HTTP method
     * @param url Resource URL
     * @param method HTTP method
     * @return Optional Resource
     */
    Optional<Resource> findByUrlAndMethod(String url, String method);
    
    /**
     * Find all active resources
     * @return List of active resources
     */
    List<Resource> findByActiveTrue();
    
    /**
     * Find resources by category
     * @param category Resource category
     * @return List of resources in the category
     */
    List<Resource> findByCategory(String category);
    
    /**
     * Find resources by URL containing search term (case insensitive)
     * @param url URL search term
     * @return List of matching resources
     */
    List<Resource> findByUrlContainingIgnoreCase(String url);
    
    /**
     * Check if resource exists by URL and method
     * @param url Resource URL
     * @param method HTTP method
     * @return true if resource exists
     */
    boolean existsByUrlAndMethod(String url, String method);
    
    /**
     * Find resources by role ID
     * @param roleId Role ID
     * @return List of resources assigned to the role
     */
    @Query("SELECT res FROM Resource res JOIN res.roles r WHERE r.id = :roleId")
    List<Resource> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * Find resources by user ID (through roles)
     * @param userId User ID
     * @return List of resources accessible to the user
     */
    @Query("SELECT DISTINCT res FROM Resource res " +
           "JOIN res.roles r " +
           "JOIN r.users u " +
           "WHERE u.id = :userId AND res.active = true AND r.active = true")
    List<Resource> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find resources matching URL pattern for permission checking
     * @param urlPattern URL pattern to match
     * @param method HTTP method
     * @return List of matching resources
     */
    @Query("SELECT res FROM Resource res WHERE " +
           "res.active = true AND " +
           "res.method = :method AND " +
           "(:urlPattern LIKE CONCAT('%', res.url, '%') OR res.url LIKE CONCAT('%', :urlPattern, '%'))")
    List<Resource> findByUrlPatternAndMethod(@Param("urlPattern") String urlPattern, 
                                           @Param("method") String method);
    
    /**
     * Find all distinct categories
     * @return List of distinct resource categories
     */
    @Query("SELECT DISTINCT res.category FROM Resource res WHERE res.active = true")
    List<String> findDistinctCategories();
    
    /**
     * Find resources by name containing search term (case insensitive)
     * @param name Resource name search term
     * @return List of matching resources
     */
    List<Resource> findByNameContainingIgnoreCase(String name);
    
    /**
     * Count roles assigned to a resource
     * @param resourceId Resource ID
     * @return Number of roles assigned to the resource
     */
    @Query("SELECT COUNT(r) FROM Role r JOIN r.resources res WHERE res.id = :resourceId")
    long countRolesByResourceId(@Param("resourceId") Long resourceId);
}