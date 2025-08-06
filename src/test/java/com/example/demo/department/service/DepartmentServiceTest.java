package com.example.demo.department.service;

import com.example.demo.department.dto.DepartmentCreateRequest;
import com.example.demo.department.dto.DepartmentDto;
import com.example.demo.department.dto.DepartmentTreeDto;
import com.example.demo.department.entity.Department;
import com.example.demo.department.exception.DepartmentAlreadyExistsException;
import com.example.demo.department.exception.DepartmentNotFoundException;
import com.example.demo.department.repository.DepartmentRepository;
import com.example.demo.department.service.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {
    
    @Mock
    private DepartmentRepository departmentRepository;
    
    @InjectMocks
    private DepartmentServiceImpl departmentService;
    
    private Department testDepartment;
    private DepartmentCreateRequest createRequest;
    
    @BeforeEach
    void setUp() {
        testDepartment = new Department();
        testDepartment.setId(1L);
        testDepartment.setName("Test Department");
        testDepartment.setCode("TEST");
        testDepartment.setDescription("Test Description");
        testDepartment.setEnabled(true);
        testDepartment.setLevel(0);
        testDepartment.setSortOrder(1);
        testDepartment.setDepPath("/TEST");
        testDepartment.setIsParent(false);
        testDepartment.setCreatedAt(Instant.now());
        testDepartment.setUpdatedAt(Instant.now());
        
        createRequest = new DepartmentCreateRequest();
        createRequest.setName("New Department");
        createRequest.setCode("NEW");
        createRequest.setDescription("New Description");
        createRequest.setEnabled(true);
        createRequest.setSortOrder(1);
    }
    
    @Test
    void createDepartment_Success() {
        // Given
        when(departmentRepository.existsByName(anyString())).thenReturn(false);
        when(departmentRepository.existsByCode(anyString())).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(testDepartment);
        
        // When
        DepartmentDto result = departmentService.createDepartment(createRequest);
        
        // Then
        assertNotNull(result);
        assertEquals(testDepartment.getId(), result.getId());
        assertEquals(testDepartment.getName(), result.getName());
        assertEquals(testDepartment.getCode(), result.getCode());
        
        verify(departmentRepository).existsByName(createRequest.getName());
        verify(departmentRepository).existsByCode(createRequest.getCode());
        verify(departmentRepository).save(any(Department.class));
    }
    
    @Test
    void createDepartment_NameAlreadyExists_ThrowsException() {
        // Given
        when(departmentRepository.existsByName(anyString())).thenReturn(true);
        
        // When & Then
        assertThrows(DepartmentAlreadyExistsException.class, 
            () -> departmentService.createDepartment(createRequest));
        
        verify(departmentRepository).existsByName(createRequest.getName());
        verify(departmentRepository, never()).save(any(Department.class));
    }
    
    @Test
    void createDepartment_CodeAlreadyExists_ThrowsException() {
        // Given
        when(departmentRepository.existsByName(anyString())).thenReturn(false);
        when(departmentRepository.existsByCode(anyString())).thenReturn(true);
        
        // When & Then
        assertThrows(DepartmentAlreadyExistsException.class, 
            () -> departmentService.createDepartment(createRequest));
        
        verify(departmentRepository).existsByName(createRequest.getName());
        verify(departmentRepository).existsByCode(createRequest.getCode());
        verify(departmentRepository, never()).save(any(Department.class));
    }
    
    @Test
    void getDepartmentById_Success() {
        // Given
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));
        
        // When
        DepartmentDto result = departmentService.getDepartmentById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(testDepartment.getId(), result.getId());
        assertEquals(testDepartment.getName(), result.getName());
        
        verify(departmentRepository).findById(1L);
    }
    
    @Test
    void getDepartmentById_NotFound_ThrowsException() {
        // Given
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(DepartmentNotFoundException.class, 
            () -> departmentService.getDepartmentById(1L));
        
        verify(departmentRepository).findById(1L);
    }
    
    @Test
    void getDepartmentTree_Success() {
        // Given
        Department rootDept = new Department();
        rootDept.setId(1L);
        rootDept.setName("Root");
        rootDept.setCode("ROOT");
        rootDept.setLevel(0);
        rootDept.setSortOrder(1);
        
        Department childDept = new Department();
        childDept.setId(2L);
        childDept.setName("Child");
        childDept.setCode("CHILD");
        childDept.setLevel(1);
        childDept.setSortOrder(1);
        childDept.setParentId(1L);
        
        when(departmentRepository.findByParentIdIsNullOrderBySortOrder())
            .thenReturn(Arrays.asList(rootDept));
        when(departmentRepository.findByParentIdOrderBySortOrder(1L))
            .thenReturn(Arrays.asList(childDept));
        when(departmentRepository.findByParentIdOrderBySortOrder(2L))
            .thenReturn(Arrays.asList());
        
        // When
        List<DepartmentTreeDto> result = departmentService.getDepartmentTree();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        DepartmentTreeDto rootDto = result.get(0);
        assertEquals("Root", rootDto.getName());
        assertEquals(1, rootDto.getChildren().size());
        
        DepartmentTreeDto childDto = rootDto.getChildren().get(0);
        assertEquals("Child", childDto.getName());
        assertEquals(0, childDto.getChildren().size());
        
        verify(departmentRepository).findByParentIdIsNullOrderBySortOrder();
        verify(departmentRepository, times(2)).findByParentIdOrderBySortOrder(any());
    }
    
    @Test
    void searchDepartments_Success() {
        // Given
        when(departmentRepository.findByNameContainingIgnoreCaseOrderByName("test"))
            .thenReturn(Arrays.asList(testDepartment));
        
        // When
        List<DepartmentDto> result = departmentService.searchDepartments("test");
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDepartment.getName(), result.get(0).getName());
        
        verify(departmentRepository).findByNameContainingIgnoreCaseOrderByName("test");
    }
    
    @Test
    void deleteDepartment_Success() {
        // Given
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));
        when(departmentRepository.existsByParentId(1L)).thenReturn(false);
        
        // When
        departmentService.deleteDepartment(1L);
        
        // Then
        verify(departmentRepository).findById(1L);
        verify(departmentRepository).existsByParentId(1L);
        verify(departmentRepository).delete(testDepartment);
    }
    
    @Test
    void deleteDepartment_NotFound_ThrowsException() {
        // Given
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(DepartmentNotFoundException.class, 
            () -> departmentService.deleteDepartment(1L));
        
        verify(departmentRepository).findById(1L);
        verify(departmentRepository, never()).delete(any());
    }
}