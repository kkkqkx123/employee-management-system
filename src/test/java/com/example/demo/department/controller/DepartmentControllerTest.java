package com.example.demo.department.controller;

import com.example.demo.department.dto.DepartmentCreateRequest;
import com.example.demo.department.dto.DepartmentDto;
import com.example.demo.department.service.DepartmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private DepartmentService departmentService;
    
    @MockitoBean
    private com.example.demo.employee.service.EmployeeService employeeService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private DepartmentDto testDepartmentDto;
    private DepartmentCreateRequest createRequest;
    
    @BeforeEach
    void setUp() {
        testDepartmentDto = DepartmentDto.builder()
            .id(1L)
            .name("Test Department")
            .code("TEST")
            .description("Test Description")
            .enabled(true)
            .level(0)
            .sortOrder(1)
            .depPath("/TEST")
            .isParent(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        
        createRequest = new DepartmentCreateRequest();
        createRequest.setName("New Department");
        createRequest.setCode("NEW");
        createRequest.setDescription("New Description");
        createRequest.setEnabled(true);
        createRequest.setSortOrder(1);
    }
    
    @Test
    @WithMockUser(authorities = {"DEPARTMENT_CREATE"})
    void createDepartment_Success() throws Exception {
        // Given
        when(departmentService.createDepartment(any(DepartmentCreateRequest.class)))
            .thenReturn(testDepartmentDto);
        
        // When & Then
        mockMvc.perform(post("/api/departments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1L))
            .andExpect(jsonPath("$.data.name").value("Test Department"))
            .andExpect(jsonPath("$.data.code").value("TEST"));
    }
    
    @Test
    @WithMockUser(authorities = {"DEPARTMENT_CREATE"})
    void createDepartment_InvalidRequest_BadRequest() throws Exception {
        // Given - invalid request with empty name
        createRequest.setName("");
        
        // When & Then
        mockMvc.perform(post("/api/departments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(authorities = {"DEPARTMENT_READ"})
    void getDepartmentById_Success() throws Exception {
        // Given
        when(departmentService.getDepartmentById(1L)).thenReturn(testDepartmentDto);
        
        // When & Then
        mockMvc.perform(get("/api/departments/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1L))
            .andExpect(jsonPath("$.data.name").value("Test Department"));
    }
    
    @Test
    @WithMockUser(authorities = {"DEPARTMENT_READ"})
    void getAllDepartments_Success() throws Exception {
        // Given
        List<DepartmentDto> departments = Arrays.asList(testDepartmentDto);
        when(departmentService.getAllDepartments()).thenReturn(departments);
        
        // When & Then
        mockMvc.perform(get("/api/departments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id").value(1L))
            .andExpect(jsonPath("$.data[0].name").value("Test Department"));
    }
    
    @Test
    @WithMockUser(authorities = {"DEPARTMENT_READ"})
    void searchDepartments_Success() throws Exception {
        // Given
        List<DepartmentDto> departments = Arrays.asList(testDepartmentDto);
        when(departmentService.searchDepartments("test")).thenReturn(departments);
        
        // When & Then
        mockMvc.perform(get("/api/departments/search")
                .param("q", "test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].name").value("Test Department"));
    }
    
    @Test
    @WithMockUser(authorities = {"DEPARTMENT_DELETE"})
    void deleteDepartment_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/departments/1")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Department deleted successfully"));
    }
    
    @Test
    void createDepartment_NoAuthority_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/departments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(authorities = {"DEPARTMENT_UPDATE"})
    void moveDepartment_Success() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/departments/1/move")
                .with(csrf())
                .param("parentId", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Department moved successfully"));
    }
    
    @Test
    @WithMockUser(authorities = {"DEPARTMENT_UPDATE"})
    void setDepartmentEnabled_Success() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/departments/1/enabled")
                .with(csrf())
                .param("enabled", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Department disabled successfully"));
    }
    
    @Test
    @WithMockUser(authorities = {"DEPARTMENT_READ"})
    void getDepartmentEmployees_Success() throws Exception {
        // Given
        List<com.example.demo.employee.dto.EmployeeDto> employees = List.of();
        when(employeeService.getEmployeesByDepartmentId(1L)).thenReturn(employees);
        
        // When & Then
        mockMvc.perform(get("/api/departments/1/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());
    }
}