package com.example.demo.department.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.department.dto.*;
import com.example.demo.department.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Department Management", description = "APIs for managing department hierarchy")
public class DepartmentController {
    
    private final DepartmentService departmentService;
    private final com.example.demo.employee.service.EmployeeService employeeService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('DEPARTMENT_CREATE')")
    @Operation(summary = "Create a new department", description = "Creates a new department in the hierarchy")
    public ResponseEntity<ApiResponse<DepartmentDto>> createDepartment(
            @Valid @RequestBody DepartmentCreateRequest request) {
        log.info("Creating department: {}", request.getName());
        
        DepartmentDto created = departmentService.createDepartment(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(created, "Department created successfully"));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_UPDATE')")
    @Operation(summary = "Update department", description = "Updates an existing department")
    public ResponseEntity<ApiResponse<DepartmentDto>> updateDepartment(
            @Parameter(description = "Department ID") @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateRequest request) {
        log.info("Updating department: {}", id);
        
        DepartmentDto updated = departmentService.updateDepartment(id, request);
        
        return ResponseEntity.ok(ApiResponse.success(updated, "Department updated successfully"));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Get department by ID", description = "Retrieves a department by its ID")
    public ResponseEntity<ApiResponse<DepartmentDto>> getDepartmentById(
            @Parameter(description = "Department ID") @PathVariable Long id) {
        log.info("Getting department: {}", id);
        
        DepartmentDto department = departmentService.getDepartmentById(id);
        
        return ResponseEntity.ok(ApiResponse.success(department));
    }
    
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Get department by code", description = "Retrieves a department by its code")
    public ResponseEntity<ApiResponse<DepartmentDto>> getDepartmentByCode(
            @Parameter(description = "Department code") @PathVariable String code) {
        log.info("Getting department by code: {}", code);
        
        DepartmentDto department = departmentService.getDepartmentByCode(code);
        
        return ResponseEntity.ok(ApiResponse.success(department));
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Get all departments", description = "Retrieves all departments as a flat list")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getAllDepartments() {
        log.info("Getting all departments");
        
        List<DepartmentDto> departments = departmentService.getAllDepartments();
        
        return ResponseEntity.ok(ApiResponse.success(departments));
    }
    
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Get department tree", description = "Retrieves the complete department hierarchy as a tree")
    public ResponseEntity<ApiResponse<List<DepartmentTreeDto>>> getDepartmentTree() {
        log.info("Getting department tree");
        
        List<DepartmentTreeDto> tree = departmentService.getDepartmentTree();
        
        return ResponseEntity.ok(ApiResponse.success(tree));
    }
    
    @GetMapping("/{id}/subtree")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Get department subtree", description = "Retrieves a department subtree starting from the specified department")
    public ResponseEntity<ApiResponse<DepartmentTreeDto>> getDepartmentSubtree(
            @Parameter(description = "Root department ID for subtree") @PathVariable Long id) {
        log.info("Getting department subtree for: {}", id);
        
        DepartmentTreeDto subtree = departmentService.getDepartmentSubtree(id);
        
        return ResponseEntity.ok(ApiResponse.success(subtree));
    }
    
    @GetMapping("/{id}/children")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Get child departments", description = "Retrieves direct child departments of the specified parent")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getChildDepartments(
            @Parameter(description = "Parent department ID") @PathVariable Long id) {
        log.info("Getting child departments for: {}", id);
        
        List<DepartmentDto> children = departmentService.getChildDepartments(id);
        
        return ResponseEntity.ok(ApiResponse.success(children));
    }
    
    @GetMapping("/root")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Get root departments", description = "Retrieves all root-level departments")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getRootDepartments() {
        log.info("Getting root departments");
        
        List<DepartmentDto> rootDepartments = departmentService.getChildDepartments(null);
        
        return ResponseEntity.ok(ApiResponse.success(rootDepartments));
    }
    
    @GetMapping("/level/{level}")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Get departments by level", description = "Retrieves all departments at the specified hierarchy level")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getDepartmentsByLevel(
            @Parameter(description = "Hierarchy level (0 for root)") @PathVariable Integer level) {
        log.info("Getting departments at level: {}", level);
        
        List<DepartmentDto> departments = departmentService.getDepartmentsByLevel(level);
        
        return ResponseEntity.ok(ApiResponse.success(departments));
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Search departments", description = "Searches departments by name")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> searchDepartments(
            @Parameter(description = "Search term") @RequestParam String q) {
        log.info("Searching departments with term: {}", q);
        
        List<DepartmentDto> departments = departmentService.searchDepartments(q);
        
        return ResponseEntity.ok(ApiResponse.success(departments));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT_DELETE')")
    @Operation(summary = "Delete department", description = "Deletes a department if it has no children or employees")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(
            @Parameter(description = "Department ID") @PathVariable Long id) {
        log.info("Deleting department: {}", id);
        
        departmentService.deleteDepartment(id);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Department deleted successfully"));
    }
    
    @PutMapping("/{id}/move")
    @PreAuthorize("hasAuthority('DEPARTMENT_UPDATE')")
    @Operation(summary = "Move department", description = "Moves a department to a new parent")
    public ResponseEntity<ApiResponse<Void>> moveDepartment(
            @Parameter(description = "Department ID to move") @PathVariable Long id,
            @Parameter(description = "New parent department ID (null for root)") @RequestParam(required = false) Long parentId) {
        log.info("Moving department {} to parent {}", id, parentId);
        
        departmentService.moveDepartment(id, parentId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Department moved successfully"));
    }
    
    @PutMapping("/{id}/enabled")
    @PreAuthorize("hasAuthority('DEPARTMENT_UPDATE')")
    @Operation(summary = "Enable/disable department", description = "Enables or disables a department")
    public ResponseEntity<ApiResponse<Void>> setDepartmentEnabled(
            @Parameter(description = "Department ID") @PathVariable Long id,
            @Parameter(description = "Enable/disable flag") @RequestParam boolean enabled) {
        log.info("Setting department {} enabled status to: {}", id, enabled);
        
        departmentService.setDepartmentEnabled(id, enabled);
        
        return ResponseEntity.ok(ApiResponse.success(null, 
            "Department " + (enabled ? "enabled" : "disabled") + " successfully"));
    }
    
    @PutMapping("/{id}/sort-order")
    @PreAuthorize("hasAuthority('DEPARTMENT_UPDATE')")
    @Operation(summary = "Update sort order", description = "Updates the sort order of a department")
    public ResponseEntity<ApiResponse<Void>> updateSortOrder(
            @Parameter(description = "Department ID") @PathVariable Long id,
            @Parameter(description = "New sort order") @RequestParam Integer sortOrder) {
        log.info("Updating sort order for department {} to: {}", id, sortOrder);
        
        departmentService.updateSortOrder(id, sortOrder);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Sort order updated successfully"));
    }
    
    @GetMapping("/{id}/path")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Get department path", description = "Gets the path from root to the specified department")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getDepartmentPath(
            @Parameter(description = "Department ID") @PathVariable Long id) {
        log.info("Getting path for department: {}", id);
        
        List<DepartmentDto> path = departmentService.getDepartmentPath(id);
        
        return ResponseEntity.ok(ApiResponse.success(path));
    }
    
    @GetMapping("/{id}/ancestors")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Get ancestor departments", description = "Gets all ancestor departments of the specified department")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getAncestorDepartments(
            @Parameter(description = "Department ID") @PathVariable Long id) {
        log.info("Getting ancestors for department: {}", id);
        
        List<DepartmentDto> ancestors = departmentService.getAncestorDepartments(id);
        
        return ResponseEntity.ok(ApiResponse.success(ancestors));
    }
    
    @GetMapping("/{id}/descendants")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Get descendant departments", description = "Gets all descendant departments of the specified department")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getDescendantDepartments(
            @Parameter(description = "Department ID") @PathVariable Long id) {
        log.info("Getting descendants for department: {}", id);
        
        List<DepartmentDto> descendants = departmentService.getDescendantDepartments(id);
        
        return ResponseEntity.ok(ApiResponse.success(descendants));
    }
    
    @GetMapping("/{id}/can-delete")
    @PreAuthorize("hasAuthority('DEPARTMENT_DELETE')")
    @Operation(summary = "Check if department can be deleted", description = "Checks if a department can be safely deleted")
    public ResponseEntity<ApiResponse<Boolean>> canDeleteDepartment(
            @Parameter(description = "Department ID") @PathVariable Long id) {
        log.info("Checking if department can be deleted: {}", id);
        
        boolean canDelete = departmentService.canDeleteDepartment(id);
        
        return ResponseEntity.ok(ApiResponse.success(canDelete));
    }
    
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Get department statistics", description = "Gets statistics for the specified department")
    public ResponseEntity<ApiResponse<DepartmentStatisticsDto>> getDepartmentStatistics(
            @Parameter(description = "Department ID") @PathVariable Long id) {
        log.info("Getting statistics for department: {}", id);
        
        DepartmentStatisticsDto statistics = departmentService.getDepartmentStatistics(id);
        
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
    
    @GetMapping("/{id}/employees")
    @PreAuthorize("hasAuthority('DEPARTMENT_READ')")
    @Operation(summary = "Get department employees", description = "Retrieves all employees in the specified department")
    public ResponseEntity<ApiResponse<List<com.example.demo.employee.dto.EmployeeDto>>> getDepartmentEmployees(
            @Parameter(description = "Department ID") @PathVariable Long id) {
        log.info("Getting employees for department: {}", id);
        
        List<com.example.demo.employee.dto.EmployeeDto> employees = employeeService.getEmployeesByDepartmentId(id);
        
        return ResponseEntity.ok(ApiResponse.success(employees));
    }
    
    @PostMapping("/rebuild-paths")
    @PreAuthorize("hasAuthority('DEPARTMENT_ADMIN')")
    @Operation(summary = "Rebuild department paths", description = "Maintenance operation to rebuild all department paths and levels")
    public ResponseEntity<ApiResponse<Void>> rebuildDepartmentPaths() {
        log.info("Rebuilding department paths");
        
        departmentService.rebuildDepartmentPaths();
        
        return ResponseEntity.ok(ApiResponse.success(null, "Department paths rebuilt successfully"));
    }
}