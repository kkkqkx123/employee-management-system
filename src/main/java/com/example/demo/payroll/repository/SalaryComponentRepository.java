package com.example.demo.payroll.repository;

import com.example.demo.payroll.entity.SalaryComponent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryComponentRepository extends JpaRepository<SalaryComponent, Long> {
    
    List<SalaryComponent> findByComponentType(String componentType);
    
    List<SalaryComponent> findByIsActiveTrue();
    
    List<SalaryComponent> findByIsMandatoryTrue();
    
    List<SalaryComponent> findByIsTaxableTrue();
    
    Page<SalaryComponent> findByIsActiveTrueOrderByCalculationOrder(Pageable pageable);
    
    @Query("SELECT sc FROM SalaryComponent sc WHERE sc.componentType = :type AND sc.isActive = true ORDER BY sc.calculationOrder")
    List<SalaryComponent> findActiveByTypeOrderByCalculationOrder(@Param("type") String componentType);
    
    @Query("SELECT sc FROM SalaryComponent sc WHERE sc.isActive = true ORDER BY sc.calculationOrder")
    List<SalaryComponent> findAllActiveOrderByCalculationOrder();
    
    boolean existsByComponentNameIgnoreCase(String componentName);
    
    @Query("SELECT MAX(sc.calculationOrder) FROM SalaryComponent sc WHERE sc.componentType = :type")
    Integer findMaxCalculationOrderByType(@Param("type") String componentType);
    
    @Query("SELECT sc FROM SalaryComponent sc WHERE sc.componentName LIKE %:name% AND sc.isActive = true")
    List<SalaryComponent> findByComponentNameContainingIgnoreCaseAndIsActiveTrue(@Param("name") String name);
}