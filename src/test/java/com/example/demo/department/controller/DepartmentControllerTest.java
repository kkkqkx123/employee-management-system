package com.example.demo.department.controller;

import com.example.demo.department.dto.*;
import com.example.demo.department.service.DepartmentService;
import com.example.demo.department.exception.DepartmentNotFoundException;
import com.example.demo.department.exception.DepartmentHierarchyException;
import com.example.demo.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for DepartmentController
 * Tests department management endpoints with MockMvc and security annotations
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DepartmentService departmentService;

    @MockitoBean
    private EmployeeService employeeService;

    private DepartmentDto departmentDto;
    private DepartmentCreateRequest createRequest;
    private DepartmentUpdateRequest updateRequest;
    private DepartmentTreeDto treeDto;

    @BeforeEach
    void setUp() {
        departmentDto = DepartmentDto.builder()
                .id(1L)
                .name("IT Department")
                .code("IT")
                .description("Information Technology Department")
                .parentId(null)
                .level(0)
                .depPath("/IT")
                .isParent(true)
                .enabled(true)
                .sortOrder(1)
                .build();

        createRequest = DepartmentCreateRequest.builder()
                .name("New Department")
                .code("NEW")
                .description("New Department Description")
                .parentId(1L)
                .build();

        updateRequest = DepartmentUpdateRequest.builder()
                .name("Updated Department")
                .description("Updated Description")
                .build();

        treeDto = DepartmentTreeDto.builder()
                .id(1L)
                .name("IT Department")
                .code("IT")
                .level(0)
                .children(List.of())
                .build();
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_CREATE")
    void createDepartment_WithValidData_ShouldReturnCreatedDepartment() throws Exception {
        // Given
        when(departmentService.createDepartment(any(DepartmentCreateRequest.class)))
                .thenReturn(departmentDto);

        // When & Then
        mockMvc.perform(post("/api/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("IT Department"))
                .andExpect(jsonPath("$.data.code").value("IT"))
                .andExpect(jsonPath("$.message").value("Department created successfully"));

        verify(departmentService).createDepartment(any(DepartmentCreateRequest.class));
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_CREATE")
    void createDepartment_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        DepartmentCreateRequest invalidRequest = DepartmentCreateRequest.builder()
                .name("") // Invalid: empty name
                .code("") // Invalid: empty code
                .build();

        // When & Then
        mockMvc.perform(post("/api/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(departmentService, never()).createDepartment(any(DepartmentCreateRequest.class));
    }

    @Test
    @WithMockUser(authorities = "WRONG_PERMISSION")
    void createDepartment_WithoutPermission_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(departmentService, never()).createDepartment(any(DepartmentCreateRequest.class));
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_UPDATE")
    void updateDepartment_WithValidData_ShouldReturnUpdatedDepartment() throws Exception {
        // Given
        DepartmentDto updatedDepartment = DepartmentDto.builder()
                .id(1L)
                .name("Updated Department")
                .code("IT")
                .description("Updated Description")
                .build();
        when(departmentService.updateDepartment(eq(1L), any(DepartmentUpdateRequest.class)))
                .thenReturn(updatedDepartment);

        // When & Then
        mockMvc.perform(put("/api/departments/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Department"))
                .andExpect(jsonPath("$.message").value("Department updated successfully"));

        verify(departmentService).updateDepartment(eq(1L), any(DepartmentUpdateRequest.class));
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_READ")
    void getDepartmentById_WithValidId_ShouldReturnDepartment() throws Exception {
        // Given
        when(departmentService.getDepartmentById(1L)).thenReturn(departmentDto);

        // When & Then
        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("IT Department"))
                .andExpect(jsonPath("$.data.code").value("IT"));

        verify(departmentService).getDepartmentById(1L);
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_READ")
    void getDepartmentById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        when(departmentService.getDepartmentById(999L))
                .thenThrow(new DepartmentNotFoundException("Department not found"));

        // When & Then
        mockMvc.perform(get("/api/departments/999"))
                .andExpect(status().isNotFound());

        verify(departmentService).getDepartmentById(999L);
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_READ")
    void getDepartmentByCode_WithValidCode_ShouldReturnDepartment() throws Exception {
        // Given
        when(departmentService.getDepartmentByCode("IT")).thenReturn(departmentDto);

        // When & Then
        mockMvc.perform(get("/api/departments/code/IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("IT Department"))
                .andExpect(jsonPath("$.data.code").value("IT"));

        verify(departmentService).getDepartmentByCode("IT");
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_READ")
    void getAllDepartments_ShouldReturnDepartmentList() throws Exception {
        // Given
        List<DepartmentDto> departments = List.of(departmentDto);
        when(departmentService.getAllDepartments()).thenReturn(departments);

        // When & Then
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("IT Department"));

        verify(departmentService).getAllDepartments();
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_READ")
    void getDepartmentTree_ShouldReturnDepartmentTree() throws Exception {
        // Given
        List<DepartmentTreeDto> tree = List.of(treeDto);
        when(departmentService.getDepartmentTree()).thenReturn(tree);

        // When & Then
        mockMvc.perform(get("/api/departments/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("IT Department"));

        verify(departmentService).getDepartmentTree();
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_READ")
    void getDepartmentSubtree_WithValidId_ShouldReturnSubtree() throws Exception {
        // Given
        when(departmentService.getDepartmentSubtree(1L)).thenReturn(treeDto);

        // When & Then
        mockMvc.perform(get("/api/departments/1/subtree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("IT Department"));

        verify(departmentService).getDepartmentSubtree(1L);
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_READ")
    void getChildDepartments_WithValidId_ShouldReturnChildren() throws Exception {
        // Given
        List<DepartmentDto> children = List.of(departmentDto);
        when(departmentService.getChildDepartments(1L)).thenReturn(children);

        // When & Then
        mockMvc.perform(get("/api/departments/1/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("IT Department"));

        verify(departmentService).getChildDepartments(1L);
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_READ")
    void getRootDepartments_ShouldReturnRootDepartments() throws Exception {
        // Given
        List<DepartmentDto> rootDepartments = List.of(departmentDto);
        when(departmentService.getChildDepartments(null)).thenReturn(rootDepartments);

        // When & Then
        mockMvc.perform(get("/api/departments/root"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("IT Department"));

        verify(departmentService).getChildDepartments(null);
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_READ")
    void getDepartmentsByLevel_WithValidLevel_ShouldReturnDepartments() throws Exception {
        // Given
        List<DepartmentDto> departments = List.of(departmentDto);
        when(departmentService.getDepartmentsByLevel(0)).thenReturn(departments);

        // When & Then
        mockMvc.perform(get("/api/departments/level/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("IT Department"));

        verify(departmentService).getDepartmentsByLevel(0);
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_READ")
    void searchDepartments_WithSearchTerm_ShouldReturnMatchingDepartments() throws Exception {
        // Given
        List<DepartmentDto> departments = List.of(departmentDto);
        when(departmentService.searchDepartments("IT")).thenReturn(departments);

        // When & Then
        mockMvc.perform(get("/api/departments/search")
                        .param("q", "IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("IT Department"));

        verify(departmentService).searchDepartments("IT");
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_DELETE")
    void deleteDepartment_WithValidId_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(departmentService).deleteDepartment(1L);

        // When & Then
        mockMvc.perform(delete("/api/departments/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Department deleted successfully"));

        verify(departmentService).deleteDepartment(1L);
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_DELETE")
    void deleteDepartment_WithDependencies_ShouldReturnConflict() throws Exception {
        // Given
        doThrow(new DepartmentHierarchyException("Cannot delete department with children"))
                .when(departmentService).deleteDepartment(1L);

        // When & Then
        mockMvc.perform(delete("/api/departments/1")
                        .with(csrf()))
                .andExpect(status().isConflict());

        verify(departmentService).deleteDepartment(1L);
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_UPDATE")
    void moveDepartment_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(departmentService).moveDepartment(1L, 2L);

        // When & Then
        mockMvc.perform(put("/api/departments/1/move")
                        .with(csrf())
                        .param("parentId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Department moved successfully"));

        verify(departmentService).moveDepartment(1L, 2L);
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_UPDATE")
    void moveDepartment_ToRoot_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(departmentService).moveDepartment(1L, null);

        // When & Then
        mockMvc.perform(put("/api/departments/1/move")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Department moved successfully"));

        verify(departmentService).moveDepartment(1L, null);
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_UPDATE")
    void setDepartmentEnabled_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(departmentService).setDepartmentEnabled(1L, false);

        // When & Then
        mockMvc.perform(put("/api/departments/1/enabled")
                        .with(csrf())
                        .param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Department disabled successfully"));

        verify(departmentService).setDepartmentEnabled(1L, false);
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_UPDATE")
    void updateSortOrder_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(departmentService).updateSortOrder(1L, 5);

        // When & Then
        mockMvc.perform(put("/api/departments/1/sort-order")
                        .with(csrf())
                        .param("sortOrder", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sort order updated successfully"));

        verify(departmentService).updateSortOrder(1L, 5);
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_READ")
    void getDepartmentPath_WithValidId_ShouldReturnPath() throws Exception {
        // Given
        List<DepartmentDto> path = List.of(departmentDto);
        when(departmentService.getDepartmentPath(1L)).thenReturn(path);

        // When & Then
        mockMvc.perform(get("/api/departments/1/path"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("IT Department"));

        verify(departmentService).getDepartmentPath(1L);
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_READ")
    void canDeleteDepartment_WithValidId_ShouldReturnBoolean() throws Exception {
        // Given
        when(departmentService.canDeleteDepartment(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/departments/1/can-delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));

        verify(departmentService).canDeleteDepartment(1L);
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_READ")
    void getDepartmentStatistics_WithValidId_ShouldReturnStatistics() throws Exception {
        // Given
        DepartmentStatisticsDto statistics = DepartmentStatisticsDto.builder()
                .departmentId(1L)
                .totalEmployeeCount(10L)
                .directEmployeeCount(8L)
                .directChildCount(2L)
                .build();
        when(departmentService.getDepartmentStatistics(1L)).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/departments/1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalEmployeeCount").value(10))
                .andExpect(jsonPath("$.data.directEmployeeCount").value(8));

        verify(departmentService).getDepartmentStatistics(1L);
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_ADMIN")
    void rebuildDepartmentPaths_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(departmentService).rebuildDepartmentPaths();

        // When & Then
        mockMvc.perform(post("/api/departments/rebuild-paths")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Department paths rebuilt successfully"));

        verify(departmentService).rebuildDepartmentPaths();
    }
}