import { apiClient } from '../../../services/api';
import { 
  Department, 
  DepartmentCreateRequest, 
  DepartmentUpdateRequest,
  DepartmentMoveRequest,
  DepartmentStatistics 
} from '../types';

export const departmentService = {
  async getDepartmentTree(): Promise<Department[]> {
    const response = await apiClient.get<Department[]>('/departments/tree');
    return response.data;
  },

  async getDepartments(): Promise<Department[]> {
    const response = await apiClient.get<Department[]>('/departments');
    return response.data;
  },

  async getDepartment(id: number): Promise<Department> {
    const response = await apiClient.get<Department>(`/departments/${id}`);
    return response.data;
  },

  async createDepartment(department: DepartmentCreateRequest): Promise<Department> {
    const response = await apiClient.post<Department>('/departments', department);
    return response.data;
  },

  async updateDepartment(department: DepartmentUpdateRequest): Promise<Department> {
    const response = await apiClient.put<Department>(
      `/departments/${department.id}`, 
      department
    );
    return response.data;
  },

  async deleteDepartment(id: number): Promise<void> {
    await apiClient.delete(`/departments/${id}`);
  },

  async moveDepartment(moveRequest: DepartmentMoveRequest): Promise<Department[]> {
    const response = await apiClient.post<Department[]>(
      '/departments/move',
      moveRequest
    );
    return response.data;
  },

  async getDepartmentStatistics(): Promise<DepartmentStatistics> {
    const response = await apiClient.get<DepartmentStatistics>('/departments/statistics');
    return response.data;
  },

  async validateDepartmentMove(
    departmentId: number, 
    newParentId?: number
  ): Promise<{ valid: boolean; message?: string }> {
    const response = await apiClient.post<{ valid: boolean; message?: string }>(
      '/departments/validate-move',
      { departmentId, newParentId }
    );
    return response.data;
  }
};