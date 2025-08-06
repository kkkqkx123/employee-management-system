package com.example.demo.position.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PositionStatisticsDto {
    private Long totalPositions;
    private Long enabledPositions;
    private Long disabledPositions;
    private Map<String, Long> positionsByCategory;
    private Map<String, Long> positionsByLevel;
    private Map<String, Long> positionsByEmploymentType;
    private Map<String, Long> positionsByDepartment;
    private Long managerialPositions;
    private Long nonManagerialPositions;
    private Double averageMinSalary;
    private Double averageMaxSalary;
}