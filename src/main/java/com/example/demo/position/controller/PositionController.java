package com.example.demo.position.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.position.dto.*;
import com.example.demo.position.enums.PositionCategory;
import com.example.demo.position.enums.PositionLevel;
import com.example.demo.position.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
@Slf4j
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<ApiResponse<PositionDto>> createPosition(@Valid @RequestBody PositionCreateRequest request) {
        log.info("Creating new position with code: {}", request.getCode());
        PositionDto createdPosition = positionService.createPosition(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdPosition, "Position created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<ApiResponse<PositionDto>> updatePosition(
            @PathVariable Long id,
            @Valid @RequestBody PositionUpdateRequest request) {
        log.info("Updating position with ID: {}", id);
        PositionDto updatedPosition = positionService.updatePosition(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedPosition, "Position updated successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PositionDto>> getPositionById(@PathVariable Long id) {
        log.info("Fetching position with ID: {}", id);
        PositionDto position = positionService.getPositionById(id);
        return ResponseEntity.ok(ApiResponse.success(position, "Position retrieved successfully"));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PositionDto>> getPositionByCode(@PathVariable String code) {
        log.info("Fetching position with code: {}", code);
        PositionDto position = positionService.getPositionByCode(code);
        return ResponseEntity.ok(ApiResponse.success(position, "Position retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Page<PositionDto>>> getAllPositions(
            @PageableDefault(size = 20, sort = "jobTitle") Pageable pageable) {
        log.info("Fetching all positions with pagination");
        Page<PositionDto> positions = positionService.getAllPositions(pageable);
        return ResponseEntity.ok(ApiResponse.success(positions, "Positions retrieved successfully"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Page<PositionDto>>> searchPositions(
            @ModelAttribute PositionSearchCriteria criteria,
            @PageableDefault(size = 20, sort = "jobTitle") Pageable pageable) {
        log.info("Searching positions with criteria: {}", criteria);
        Page<PositionDto> positions = positionService.searchPositions(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(positions, "Position search completed successfully"));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<PositionDto>>> getPositionsByDepartment(@PathVariable Long departmentId) {
        log.info("Fetching positions for department ID: {}", departmentId);
        List<PositionDto> positions = positionService.getPositionsByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success(positions, "Department positions retrieved successfully"));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<PositionDto>>> getPositionsByCategory(@PathVariable PositionCategory category) {
        log.info("Fetching positions for category: {}", category);
        List<PositionDto> positions = positionService.getPositionsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(positions, "Category positions retrieved successfully"));
    }

    @GetMapping("/level/{level}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<PositionDto>>> getPositionsByLevel(@PathVariable PositionLevel level) {
        log.info("Fetching positions for level: {}", level);
        List<PositionDto> positions = positionService.getPositionsByLevel(level);
        return ResponseEntity.ok(ApiResponse.success(positions, "Level positions retrieved successfully"));
    }

    @GetMapping("/enabled")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Page<PositionDto>>> getEnabledPositions(
            @PageableDefault(size = 20, sort = "jobTitle") Pageable pageable) {
        log.info("Fetching enabled positions");
        Page<PositionDto> positions = positionService.getEnabledPositions(pageable);
        return ResponseEntity.ok(ApiResponse.success(positions, "Enabled positions retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deletePosition(@PathVariable Long id) {
        log.info("Deleting position with ID: {}", id);
        positionService.deletePosition(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Position deleted successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<ApiResponse<PositionDto>> togglePositionStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        log.info("Toggling position status for ID: {} to {}", id, enabled);
        PositionDto updatedPosition = positionService.togglePositionStatus(id, enabled);
        return ResponseEntity.ok(ApiResponse.success(updatedPosition, "Position status updated successfully"));
    }

    @GetMapping("/exists/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE')")
    public ResponseEntity<ApiResponse<Boolean>> checkPositionCodeExists(@PathVariable String code) {
        log.info("Checking if position code exists: {}", code);
        boolean exists = positionService.existsByCode(code);
        return ResponseEntity.ok(ApiResponse.success(exists, "Position code check completed"));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<ApiResponse<PositionStatisticsDto>> getPositionStatistics() {
        log.info("Fetching position statistics");
        PositionStatisticsDto statistics = positionService.getPositionStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics, "Position statistics retrieved successfully"));
    }
}