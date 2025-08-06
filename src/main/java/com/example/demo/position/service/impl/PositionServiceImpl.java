package com.example.demo.position.service.impl;

import com.example.demo.department.entity.Department;
import com.example.demo.department.repository.DepartmentRepository;
import com.example.demo.position.dto.*;
import com.example.demo.position.entity.Position;
import com.example.demo.position.enums.PositionCategory;
import com.example.demo.position.enums.PositionLevel;
import com.example.demo.position.exception.PositionAlreadyExistsException;
import com.example.demo.position.exception.PositionInUseException;
import com.example.demo.position.exception.PositionNotFoundException;
import com.example.demo.position.repository.PositionRepository;
import com.example.demo.position.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public PositionDto createPosition(PositionCreateRequest request) {
        log.info("Creating new position with code: {}", request.getCode());

        // Check if position code already exists
        if (positionRepository.existsByCode(request.getCode())) {
            throw PositionAlreadyExistsException.forCode(request.getCode());
        }

        // Validate department exists
        if (!departmentRepository.existsById(request.getDepartmentId())) {
            throw new IllegalArgumentException("Department not found with ID: " + request.getDepartmentId());
        }

        Position position = new Position();
        position.setJobTitle(request.getJobTitle());
        position.setProfessionalTitle(request.getProfessionalTitle());
        position.setCode(request.getCode());
        position.setDescription(request.getDescription());
        position.setRequirements(request.getRequirements());
        position.setResponsibilities(request.getResponsibilities());
        position.setCategory(request.getCategory());
        position.setSalaryGrade(request.getSalaryGrade());
        position.setDepartmentId(request.getDepartmentId());
        position.setLevel(request.getLevel());
        position.setEnabled(request.getEnabled());
        position.setMinSalary(request.getMinSalary());
        position.setMaxSalary(request.getMaxSalary());
        position.setRequiredSkills(request.getRequiredSkills());
        position.setRequiredEducation(request.getRequiredEducation());
        position.setRequiredExperience(request.getRequiredExperience());
        position.setBenefits(request.getBenefits());
        position.setWorkLocation(request.getWorkLocation());
        position.setEmploymentType(request.getEmploymentType());
        position.setIsManagerial(request.getIsManagerial());

        Position savedPosition = positionRepository.save(position);
        log.info("Position created successfully with ID: {}", savedPosition.getId());

        return convertToDto(savedPosition);
    }

    @Override
    public PositionDto updatePosition(Long id, PositionUpdateRequest request) {
        log.info("Updating position with ID: {}", id);

        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new PositionNotFoundException(id));

        // Validate department exists
        if (!departmentRepository.existsById(request.getDepartmentId())) {
            throw new IllegalArgumentException("Department not found with ID: " + request.getDepartmentId());
        }

        position.setJobTitle(request.getJobTitle());
        position.setProfessionalTitle(request.getProfessionalTitle());
        position.setDescription(request.getDescription());
        position.setRequirements(request.getRequirements());
        position.setResponsibilities(request.getResponsibilities());
        position.setCategory(request.getCategory());
        position.setSalaryGrade(request.getSalaryGrade());
        position.setDepartmentId(request.getDepartmentId());
        position.setLevel(request.getLevel());
        position.setEnabled(request.getEnabled());
        position.setMinSalary(request.getMinSalary());
        position.setMaxSalary(request.getMaxSalary());
        position.setRequiredSkills(request.getRequiredSkills());
        position.setRequiredEducation(request.getRequiredEducation());
        position.setRequiredExperience(request.getRequiredExperience());
        position.setBenefits(request.getBenefits());
        position.setWorkLocation(request.getWorkLocation());
        position.setEmploymentType(request.getEmploymentType());
        position.setIsManagerial(request.getIsManagerial());

        Position updatedPosition = positionRepository.save(position);
        log.info("Position updated successfully with ID: {}", updatedPosition.getId());

        return convertToDto(updatedPosition);
    }

    @Override
    @Transactional(readOnly = true)
    public PositionDto getPositionById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new PositionNotFoundException(id));
        return convertToDto(position);
    }

    @Override
    @Transactional(readOnly = true)
    public PositionDto getPositionByCode(String code) {
        Position position = positionRepository.findByCode(code)
                .orElseThrow(() -> new PositionNotFoundException("code", code));
        return convertToDto(position);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PositionDto> getAllPositions(Pageable pageable) {
        return positionRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PositionDto> searchPositions(PositionSearchCriteria criteria, Pageable pageable) {
        Specification<Position> spec = buildSearchSpecification(criteria);
        return positionRepository.findAll(spec, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionDto> getPositionsByDepartment(Long departmentId) {
        return positionRepository.findByDepartmentId(departmentId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionDto> getPositionsByCategory(PositionCategory category) {
        return positionRepository.findByCategory(category)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionDto> getPositionsByLevel(PositionLevel level) {
        return positionRepository.findByLevel(level)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PositionDto> getEnabledPositions(Pageable pageable) {
        return positionRepository.findByEnabledTrue(pageable)
                .map(this::convertToDto);
    }

    @Override
    public void deletePosition(Long id) {
        log.info("Deleting position with ID: {}", id);

        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new PositionNotFoundException(id));

        // Check if position is in use by employees
        int employeeCount = position.getEmployees().size();
        if (employeeCount > 0) {
            throw new PositionInUseException(id, employeeCount);
        }

        positionRepository.delete(position);
        log.info("Position deleted successfully with ID: {}", id);
    }

    @Override
    public PositionDto togglePositionStatus(Long id, boolean enabled) {
        log.info("Toggling position status for ID: {} to {}", id, enabled);

        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new PositionNotFoundException(id));

        position.setEnabled(enabled);
        Position updatedPosition = positionRepository.save(position);

        log.info("Position status updated successfully for ID: {}", id);
        return convertToDto(updatedPosition);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return positionRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public PositionStatisticsDto getPositionStatistics() {
        List<Position> allPositions = positionRepository.findAll();
        
        PositionStatisticsDto stats = new PositionStatisticsDto();
        stats.setTotalPositions((long) allPositions.size());
        
        long enabledCount = allPositions.stream().mapToLong(p -> p.getEnabled() ? 1 : 0).sum();
        stats.setEnabledPositions(enabledCount);
        stats.setDisabledPositions(stats.getTotalPositions() - enabledCount);
        
        // Group by category
        Map<String, Long> categoryStats = allPositions.stream()
                .collect(Collectors.groupingBy(p -> p.getCategory().name(), Collectors.counting()));
        stats.setPositionsByCategory(categoryStats);
        
        // Group by level
        Map<String, Long> levelStats = allPositions.stream()
                .collect(Collectors.groupingBy(p -> p.getLevel().name(), Collectors.counting()));
        stats.setPositionsByLevel(levelStats);
        
        // Group by employment type
        Map<String, Long> employmentTypeStats = allPositions.stream()
                .collect(Collectors.groupingBy(p -> p.getEmploymentType().name(), Collectors.counting()));
        stats.setPositionsByEmploymentType(employmentTypeStats);
        
        // Managerial vs non-managerial
        long managerialCount = allPositions.stream().mapToLong(p -> p.getIsManagerial() ? 1 : 0).sum();
        stats.setManagerialPositions(managerialCount);
        stats.setNonManagerialPositions(stats.getTotalPositions() - managerialCount);
        
        // Average salaries
        double avgMinSalary = allPositions.stream()
                .filter(p -> p.getMinSalary() != null)
                .mapToDouble(p -> p.getMinSalary().doubleValue())
                .average()
                .orElse(0.0);
        stats.setAverageMinSalary(avgMinSalary);
        
        double avgMaxSalary = allPositions.stream()
                .filter(p -> p.getMaxSalary() != null)
                .mapToDouble(p -> p.getMaxSalary().doubleValue())
                .average()
                .orElse(0.0);
        stats.setAverageMaxSalary(avgMaxSalary);
        
        return stats;
    }

    @Override
    public PositionDto convertToDto(Position position) {
        PositionDto dto = new PositionDto();
        dto.setId(position.getId());
        dto.setJobTitle(position.getJobTitle());
        dto.setProfessionalTitle(position.getProfessionalTitle());
        dto.setCode(position.getCode());
        dto.setDescription(position.getDescription());
        dto.setRequirements(position.getRequirements());
        dto.setResponsibilities(position.getResponsibilities());
        dto.setCategory(position.getCategory());
        dto.setSalaryGrade(position.getSalaryGrade());
        dto.setDepartmentId(position.getDepartmentId());
        dto.setLevel(position.getLevel());
        dto.setEnabled(position.getEnabled());
        dto.setMinSalary(position.getMinSalary());
        dto.setMaxSalary(position.getMaxSalary());
        dto.setRequiredSkills(position.getRequiredSkills());
        dto.setRequiredEducation(position.getRequiredEducation());
        dto.setRequiredExperience(position.getRequiredExperience());
        dto.setBenefits(position.getBenefits());
        dto.setWorkLocation(position.getWorkLocation());
        dto.setEmploymentType(position.getEmploymentType());
        dto.setIsManagerial(position.getIsManagerial());
        dto.setCreatedAt(position.getCreatedAt());
        dto.setUpdatedAt(position.getUpdatedAt());
        dto.setCreatedBy(position.getCreatedBy());
        dto.setUpdatedBy(position.getUpdatedBy());
        
        // Set department name if department is loaded
        if (position.getDepartment() != null) {
            dto.setDepartmentName(position.getDepartment().getName());
        }
        
        // Set employee count
        dto.setEmployeeCount(position.getEmployees().size());
        
        return dto;
    }

    @Override
    public Position convertToEntity(PositionDto dto) {
        Position position = new Position();
        position.setId(dto.getId());
        position.setJobTitle(dto.getJobTitle());
        position.setProfessionalTitle(dto.getProfessionalTitle());
        position.setCode(dto.getCode());
        position.setDescription(dto.getDescription());
        position.setRequirements(dto.getRequirements());
        position.setResponsibilities(dto.getResponsibilities());
        position.setCategory(dto.getCategory());
        position.setSalaryGrade(dto.getSalaryGrade());
        position.setDepartmentId(dto.getDepartmentId());
        position.setLevel(dto.getLevel());
        position.setEnabled(dto.getEnabled());
        position.setMinSalary(dto.getMinSalary());
        position.setMaxSalary(dto.getMaxSalary());
        position.setRequiredSkills(dto.getRequiredSkills());
        position.setRequiredEducation(dto.getRequiredEducation());
        position.setRequiredExperience(dto.getRequiredExperience());
        position.setBenefits(dto.getBenefits());
        position.setWorkLocation(dto.getWorkLocation());
        position.setEmploymentType(dto.getEmploymentType());
        position.setIsManagerial(dto.getIsManagerial());
        position.setCreatedAt(dto.getCreatedAt());
        position.setUpdatedAt(dto.getUpdatedAt());
        position.setCreatedBy(dto.getCreatedBy());
        position.setUpdatedBy(dto.getUpdatedBy());
        
        return position;
    }

    private Specification<Position> buildSearchSpecification(PositionSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (criteria.getJobTitle() != null && !criteria.getJobTitle().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("jobTitle")),
                        "%" + criteria.getJobTitle().toLowerCase() + "%"
                ));
            }

            if (criteria.getProfessionalTitle() != null && !criteria.getProfessionalTitle().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("professionalTitle")),
                        "%" + criteria.getProfessionalTitle().toLowerCase() + "%"
                ));
            }

            if (criteria.getCode() != null && !criteria.getCode().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("code")),
                        "%" + criteria.getCode().toLowerCase() + "%"
                ));
            }

            if (criteria.getCategory() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), criteria.getCategory()));
            }

            if (criteria.getDepartmentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("departmentId"), criteria.getDepartmentId()));
            }

            if (criteria.getLevel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("level"), criteria.getLevel()));
            }

            if (criteria.getEnabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), criteria.getEnabled()));
            }

            if (criteria.getEmploymentType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("employmentType"), criteria.getEmploymentType()));
            }

            if (criteria.getIsManagerial() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isManagerial"), criteria.getIsManagerial()));
            }

            if (criteria.getMinSalaryFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("minSalary"), criteria.getMinSalaryFrom()));
            }

            if (criteria.getMinSalaryTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("minSalary"), criteria.getMinSalaryTo()));
            }

            if (criteria.getMaxSalaryFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("maxSalary"), criteria.getMaxSalaryFrom()));
            }

            if (criteria.getMaxSalaryTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("maxSalary"), criteria.getMaxSalaryTo()));
            }

            if (criteria.getMinExperience() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("requiredExperience"), criteria.getMinExperience()));
            }

            if (criteria.getMaxExperience() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("requiredExperience"), criteria.getMaxExperience()));
            }

            if (criteria.getWorkLocation() != null && !criteria.getWorkLocation().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("workLocation")),
                        "%" + criteria.getWorkLocation().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}