package com.example.demo.position.service;

import com.example.demo.position.dto.PositionCreateRequest;
import com.example.demo.position.dto.PositionDto;
import com.example.demo.position.dto.PositionSearchCriteria;
import com.example.demo.position.dto.PositionStatisticsDto;
import com.example.demo.position.dto.PositionUpdateRequest;
import com.example.demo.position.entity.Position;
import com.example.demo.position.enums.PositionCategory;
import com.example.demo.position.enums.PositionLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PositionService {
    
    /**
     * Create a new position.
     *
     * @param request The position creation request.
     * @return The created position DTO.
     */
    PositionDto createPosition(PositionCreateRequest request);

    /**
     * Update an existing position.
     *
     * @param id The position ID.
     * @param request The position update request.
     * @return The updated position DTO.
     */
    PositionDto updatePosition(Long id, PositionUpdateRequest request);

    /**
     * Get a position by ID.
     *
     * @param id The position ID.
     * @return The position DTO.
     */
    PositionDto getPositionById(Long id);

    /**
     * Get a position by code.
     *
     * @param code The position code.
     * @return The position DTO.
     */
    PositionDto getPositionByCode(String code);

    /**
     * Get all positions with pagination.
     *
     * @param pageable Pagination information.
     * @return A page of position DTOs.
     */
    Page<PositionDto> getAllPositions(Pageable pageable);

    /**
     * Search positions based on criteria.
     *
     * @param criteria The search criteria.
     * @param pageable Pagination information.
     * @return A page of position DTOs matching the criteria.
     */
    Page<PositionDto> searchPositions(PositionSearchCriteria criteria, Pageable pageable);

    /**
     * Get positions by department.
     *
     * @param departmentId The department ID.
     * @return A list of position DTOs in the department.
     */
    List<PositionDto> getPositionsByDepartment(Long departmentId);

    /**
     * Get positions by category.
     *
     * @param category The position category.
     * @return A list of position DTOs in the category.
     */
    List<PositionDto> getPositionsByCategory(PositionCategory category);

    /**
     * Get positions by level.
     *
     * @param level The position level.
     * @return A list of position DTOs at the level.
     */
    List<PositionDto> getPositionsByLevel(PositionLevel level);

    /**
     * Get only enabled positions.
     *
     * @param pageable Pagination information.
     * @return A page of enabled position DTOs.
     */
    Page<PositionDto> getEnabledPositions(Pageable pageable);

    /**
     * Delete a position by ID.
     *
     * @param id The position ID.
     */
    void deletePosition(Long id);

    /**
     * Enable or disable a position.
     *
     * @param id The position ID.
     * @param enabled Whether to enable or disable the position.
     * @return The updated position DTO.
     */
    PositionDto togglePositionStatus(Long id, boolean enabled);

    /**
     * Check if a position code exists.
     *
     * @param code The position code.
     * @return true if the code exists, false otherwise.
     */
    boolean existsByCode(String code);

    /**
     * Get position statistics.
     *
     * @return Position statistics.
     */
    PositionStatisticsDto getPositionStatistics();

    /**
     * Convert entity to DTO.
     *
     * @param position The position entity.
     * @return The position DTO.
     */
    PositionDto convertToDto(Position position);

    /**
     * Convert DTO to entity.
     *
     * @param dto The position DTO.
     * @return The position entity.
     */
    Position convertToEntity(PositionDto dto);
}