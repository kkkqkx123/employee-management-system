package com.example.demo.department.service.impl;

import com.example.demo.department.dto.*;
import com.example.demo.department.entity.Department;
import com.example.demo.department.exception.*;
import com.example.demo.department.repository.DepartmentRepository;
import com.example.demo.department.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DepartmentServiceImpl implements DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    
    @Override
    @CacheEvict(value = {"departments", "departmentTree"}, allEntries = true)
    public DepartmentDto createDepartment(DepartmentCreateRequest request) {
        log.info("Creating department with name: {}", request.getName());
        
        // Validate unique constraints
        if (departmentRepository.existsByName(request.getName())) {
            throw DepartmentAlreadyExistsException.byName(request.getName());
        }
        
        if (departmentRepository.existsByCode(request.getCode())) {
            throw DepartmentAlreadyExistsException.byCode(request.getCode());
        }
        
        // Validate parent department if specified
        if (request.getParentId() != null) {
            if (!departmentRepository.existsById(request.getParentId())) {
                throw new DepartmentNotFoundException(request.getParentId());
            }
        }
        
        Department department = new Department();
        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setDescription(request.getDescription());
        department.setParentId(request.getParentId());
        department.setLocation(request.getLocation());
        department.setManagerId(request.getManagerId());
        department.setEnabled(request.isEnabled());
        department.setSortOrder(request.getSortOrder());
        
        Department saved = departmentRepository.save(department);
        log.info("Department created successfully with ID: {}", saved.getId());
        
        return convertToDto(saved);
    }
    
    @Override
    @CacheEvict(value = {"departments", "departmentTree"}, allEntries = true)
    public DepartmentDto updateDepartment(Long id, DepartmentUpdateRequest request) {
        log.info("Updating department with ID: {}", id);
        
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new DepartmentNotFoundException(id));
        
        // Validate unique constraints (excluding current department)
        departmentRepository.findByName(request.getName())
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw DepartmentAlreadyExistsException.byName(request.getName());
                }
            });
        
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setLocation(request.getLocation());
        department.setManagerId(request.getManagerId());
        department.setEnabled(request.isEnabled());
        department.setSortOrder(request.getSortOrder());
        
        Department updated = departmentRepository.save(department);
        log.info("Department updated successfully with ID: {}", updated.getId());
        
        return convertToDto(updated);
    }
    
    @Override
    @Cacheable(value = "departments", key = "#id")
    public DepartmentDto getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new DepartmentNotFoundException(id));
        return convertToDto(department);
    }
    
    @Override
    @Cacheable(value = "departments", key = "#code")
    public DepartmentDto getDepartmentByCode(String code) {
        Department department = departmentRepository.findByCode(code)
            .orElseThrow(() -> new DepartmentNotFoundException("code", code));
        return convertToDto(department);
    }
    
    @Override
    @Cacheable(value = "departments", key = "'all'")
    public List<DepartmentDto> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        return departments.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "departmentTree", key = "'tree'")
    public List<DepartmentTreeDto> getDepartmentTree() {
        List<Department> rootDepartments = departmentRepository.findByParentIdIsNullOrderBySortOrder();
        return rootDepartments.stream()
            .map(this::buildDepartmentTree)
            .collect(Collectors.toList());
    }
    
    @Override
    public DepartmentTreeDto getDepartmentSubtree(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        return buildDepartmentTree(department);
    }
    
    @Override
    public List<DepartmentDto> getChildDepartments(Long parentId) {
        List<Department> children = departmentRepository.findByParentIdOrderBySortOrder(parentId);
        return children.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<DepartmentDto> getDepartmentsByLevel(Integer level) {
        List<Department> departments = departmentRepository.findByLevel(level);
        return departments.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<DepartmentDto> searchDepartments(String searchTerm) {
        List<Department> departments = departmentRepository.findByNameContainingIgnoreCaseOrderByName(searchTerm);
        return departments.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Override
    @CacheEvict(value = {"departments", "departmentTree"}, allEntries = true)
    public void deleteDepartment(Long id) {
        log.info("Deleting department with ID: {}", id);
        
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new DepartmentNotFoundException(id));
        
        // Check if department has children
        if (departmentRepository.existsByParentId(id)) {
            throw DepartmentHierarchyException.hasChildren(id);
        }
        
        // Check if department has employees (this would require Employee entity)
        // For now, we'll skip this check as Employee entity is not yet implemented
        
        departmentRepository.delete(department);
        log.info("Department deleted successfully with ID: {}", id);
    }
    
    @Override
    @CacheEvict(value = {"departments", "departmentTree"}, allEntries = true)
    public void moveDepartment(Long departmentId, Long newParentId) {
        log.info("Moving department {} to new parent {}", departmentId, newParentId);
        
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        
        // Validate new parent exists if specified
        if (newParentId != null) {
            if (!departmentRepository.existsById(newParentId)) {
                throw new DepartmentNotFoundException(newParentId);
            }
            
            // Check for circular reference
            if (wouldCreateCircularReference(departmentId, newParentId)) {
                throw DepartmentHierarchyException.circularReference(departmentId, newParentId);
            }
        }
        
        department.setParentId(newParentId);
        departmentRepository.save(department);
        
        log.info("Department moved successfully");
    }
    
    @Override
    @CacheEvict(value = {"departments", "departmentTree"}, allEntries = true)
    public void setDepartmentEnabled(Long id, boolean enabled) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new DepartmentNotFoundException(id));
        
        department.setEnabled(enabled);
        departmentRepository.save(department);
        
        log.info("Department {} enabled status set to: {}", id, enabled);
    }
    
    @Override
    @CacheEvict(value = {"departments", "departmentTree"}, allEntries = true)
    public void updateSortOrder(Long id, Integer sortOrder) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new DepartmentNotFoundException(id));
        
        department.setSortOrder(sortOrder);
        departmentRepository.save(department);
        
        log.info("Department {} sort order updated to: {}", id, sortOrder);
    }
    
    @Override
    public List<DepartmentDto> getDepartmentPath(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        
        List<DepartmentDto> path = new ArrayList<>();
        Department current = department;
        
        while (current != null) {
            path.add(0, convertToDto(current)); // Add to beginning to maintain order
            if (current.getParentId() != null) {
                current = departmentRepository.findById(current.getParentId()).orElse(null);
            } else {
                current = null;
            }
        }
        
        return path;
    }
    
    @Override
    public List<DepartmentDto> getAncestorDepartments(Long departmentId) {
        List<DepartmentDto> path = getDepartmentPath(departmentId);
        // Remove the department itself, keep only ancestors
        if (!path.isEmpty()) {
            path.remove(path.size() - 1);
        }
        return path;
    }
    
    @Override
    public List<DepartmentDto> getDescendantDepartments(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        
        String pathPrefix = department.getDepPath() + "/";
        List<Department> descendants = departmentRepository.findByDepPathStartingWithOrderByDepPath(pathPrefix);
        
        return descendants.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean canDeleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new DepartmentNotFoundException(id);
        }
        
        // Check if has children
        if (departmentRepository.existsByParentId(id)) {
            return false;
        }
        
        // Check if has employees (would require Employee entity)
        // For now, assume it can be deleted if no children
        return true;
    }
    
    @Override
    public DepartmentStatisticsDto getDepartmentStatistics(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        
        long directChildCount = departmentRepository.countByParentId(departmentId);
        List<DepartmentDto> descendants = getDescendantDepartments(departmentId);
        
        return DepartmentStatisticsDto.builder()
            .departmentId(departmentId)
            .departmentName(department.getName())
            .directEmployeeCount(0L) // Would require Employee entity
            .totalEmployeeCount(0L) // Would require Employee entity
            .directChildCount(directChildCount)
            .totalChildCount((long) descendants.size())
            .maxDepth(calculateMaxDepth(departmentId))
            .hasManager(department.getManagerId() != null)
            .managerName(null) // Would require Employee entity
            .build();
    }
    
    @Override
    @CacheEvict(value = {"departments", "departmentTree"}, allEntries = true)
    public void rebuildDepartmentPaths() {
        log.info("Rebuilding department paths");
        
        // This would typically be done by the database trigger
        // But we can implement it here for maintenance purposes
        List<Department> rootDepartments = departmentRepository.findByParentIdIsNullOrderBySortOrder();
        
        for (Department root : rootDepartments) {
            rebuildPathsRecursively(root, null, 0);
        }
        
        log.info("Department paths rebuilt successfully");
    }
    
    private void rebuildPathsRecursively(Department department, String parentPath, int level) {
        String newPath = (parentPath == null) ? "/" + department.getCode() : parentPath + "/" + department.getCode();
        department.setDepPath(newPath);
        department.setLevel(level);
        
        boolean hasChildren = departmentRepository.existsByParentId(department.getId());
        department.setIsParent(hasChildren);
        
        departmentRepository.save(department);
        
        // Process children
        List<Department> children = departmentRepository.findByParentIdOrderBySortOrder(department.getId());
        for (Department child : children) {
            rebuildPathsRecursively(child, newPath, level + 1);
        }
    }
    
    private boolean wouldCreateCircularReference(Long departmentId, Long newParentId) {
        if (newParentId == null) {
            return false;
        }
        
        if (departmentId.equals(newParentId)) {
            return true;
        }
        
        // Check if newParentId is a descendant of departmentId
        List<DepartmentDto> descendants = getDescendantDepartments(departmentId);
        return descendants.stream()
            .anyMatch(dept -> dept.getId().equals(newParentId));
    }
    
    private Integer calculateMaxDepth(Long departmentId) {
        List<DepartmentDto> descendants = getDescendantDepartments(departmentId);
        if (descendants.isEmpty()) {
            return 0;
        }
        
        Department department = departmentRepository.findById(departmentId).orElse(null);
        if (department == null) {
            return 0;
        }
        
        int currentLevel = department.getLevel();
        return descendants.stream()
            .mapToInt(dept -> dept.getLevel() - currentLevel)
            .max()
            .orElse(0);
    }
    
    private DepartmentTreeDto buildDepartmentTree(Department department) {
        List<Department> children = departmentRepository.findByParentIdOrderBySortOrder(department.getId());
        
        List<DepartmentTreeDto> childrenDto = children.stream()
            .map(this::buildDepartmentTree)
            .collect(Collectors.toList());
        
        return DepartmentTreeDto.builder()
            .id(department.getId())
            .name(department.getName())
            .code(department.getCode())
            .parentId(department.getParentId())
            .level(department.getLevel())
            .sortOrder(department.getSortOrder())
            .enabled(department.getEnabled())
            .hasChildren(!children.isEmpty())
            .employeeCount(department.getEmployeeCount())
            .children(childrenDto)
            .expanded(false)
            .selectable(true)
            .build();
    }
    
    private DepartmentDto convertToDto(Department department) {
        return DepartmentDto.builder()
            .id(department.getId())
            .name(department.getName())
            .code(department.getCode())
            .description(department.getDescription())
            .location(department.getLocation())
            .parentId(department.getParentId())
            .depPath(department.getDepPath())
            .isParent(department.getIsParent())
            .level(department.getLevel())
            .sortOrder(department.getSortOrder())
            .managerId(department.getManagerId())
            .enabled(department.getEnabled())
            .createdAt(department.getCreatedAt())
            .updatedAt(department.getUpdatedAt())
            .createdBy(department.getCreatedBy())
            .updatedBy(department.getUpdatedBy())
            .employeeCount(department.getEmployeeCount())
            .build();
    }
}