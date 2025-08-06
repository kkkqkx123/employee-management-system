package com.example.demo.position.repository;

import com.example.demo.position.entity.Position;
import com.example.demo.position.enums.PositionCategory;
import com.example.demo.position.enums.PositionLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long>, JpaSpecificationExecutor<Position> {

    /**
     * Find a position by its unique code.
     *
     * @param code The position code.
     * @return An Optional containing the found position or empty if not found.
     */
    Optional<Position> findByCode(String code);

    /**
     * Check if a position with the given code exists.
     *
     * @param code The position code.
     * @return true if a position with the code exists, false otherwise.
     */
    boolean existsByCode(String code);

    /**
     * Find all positions within a specific department.
     *
     * @param departmentId The ID of the department.
     * @return A list of positions in the specified department.
     */
    List<Position> findByDepartmentId(Long departmentId);

    /**
     * Find all positions matching a specific level.
     *
     * @param level The position level.
     * @return A list of positions with the specified level.
     */
    List<Position> findByLevel(PositionLevel level);

    /**
     * Find all positions belonging to a specific category.
     *
     * @param category The position category.
     * @return A list of positions in the specified category.
     */
    List<Position> findByCategory(PositionCategory category);

    /**
     * Find all enabled positions with pagination.
     *
     * @param pageable Pagination information.
     * @return A Page of enabled positions.
     */
    Page<Position> findByEnabledTrue(Pageable pageable);

    /**
     * Search for positions by job title.
     *
     * @param jobTitle The job title to search for (case-insensitive).
     * @param pageable Pagination information.
     * @return A Page of positions matching the job title.
     */
    Page<Position> findByJobTitleContainingIgnoreCase(String jobTitle, Pageable pageable);

    /**
     * Custom query to find positions by department and filter by job title.
     *
     * @param departmentId The ID of the department.
     * @param jobTitle     A part of the job title to search for.
     * @return A list of matching positions.
     */
    @Query("SELECT p FROM Position p WHERE p.departmentId = :departmentId AND lower(p.jobTitle) LIKE lower(concat('%', :jobTitle, '%'))")
    List<Position> findByDepartmentAndJobTitle(@Param("departmentId") Long departmentId, @Param("jobTitle") String jobTitle);
}