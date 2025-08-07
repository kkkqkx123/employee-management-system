// Employee API service

import { ApiService } from '../../../services/api';
import type { ApiResponse, PageResponse } from '../../../types/api';
import type {
  Employee,
  EmployeeCreateRequest,
  EmployeeUpdateRequest,
  EmployeeSearchCriteria,
  EmployeeImportResult,
  EmployeeExportRequest,
  EmployeeBulkOperation
} from '../types';

export class EmployeeApiService {
  private static readonly BASE_URL = '/api/v1/employees';

  // Get all employees with pagination and search
  static async getEmployees(
    page: number = 0,
    size: number = 20,
    sort?: string,
    searchCriteria?: EmployeeSearchCriteria
  ): Promise<PageResponse<Employee>> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });

    if (sort) {
      params.append('sort', sort);
    }

    // Add search criteria as query parameters
    if (searchCriteria) {
      Object.entries(searchCriteria).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          params.append(key, value.toString());
        }
      });
    }

    const response = await ApiService.get<PageResponse<Employee>>(
      `${this.BASE_URL}?${params.toString()}`
    );
    return response.data;
  }

  // Get employee by ID
  static async getEmployeeById(id: number): Promise<Employee> {
    const response = await ApiService.get<Employee>(`${this.BASE_URL}/${id}`);
    return response.data;
  }

  // Create new employee
  static async createEmployee(employeeData: EmployeeCreateRequest): Promise<Employee> {
    const response = await ApiService.post<Employee>(this.BASE_URL, employeeData);
    return response.data;
  }

  // Update employee
  static async updateEmployee(id: number, employeeData: EmployeeUpdateRequest): Promise<Employee> {
    const response = await ApiService.put<Employee>(`${this.BASE_URL}/${id}`, employeeData);
    return response.data;
  }

  // Delete employee
  static async deleteEmployee(id: number): Promise<void> {
    await ApiService.delete(`${this.BASE_URL}/${id}`);
  }

  // Bulk delete employees
  static async bulkDeleteEmployees(employeeIds: number[]): Promise<void> {
    await ApiService.post(`${this.BASE_URL}/bulk-delete`, { employeeIds });
  }

  // Bulk operations
  static async bulkOperation(operation: EmployeeBulkOperation): Promise<void> {
    await ApiService.post(`${this.BASE_URL}/bulk-operation`, operation);
  }

  // Import employees from Excel
  static async importEmployees(
    file: File,
    onProgress?: (progress: number) => void
  ): Promise<EmployeeImportResult> {
    const response = await ApiService.uploadFile<EmployeeImportResult>(
      `${this.BASE_URL}/import`,
      file,
      onProgress
    );
    return response.data;
  }

  // Export employees to Excel
  static async exportEmployees(exportRequest: EmployeeExportRequest): Promise<void> {
    const response = await ApiService.post(
      `${this.BASE_URL}/export`,
      exportRequest,
      { responseType: 'blob' }
    );

    // Create download link
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `employees_export_${new Date().toISOString().split('T')[0]}.xlsx`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  // Get employee statistics
  static async getEmployeeStatistics(): Promise<{
    totalEmployees: number;
    activeEmployees: number;
    inactiveEmployees: number;
    newHiresThisMonth: number;
    terminationsThisMonth: number;
    departmentDistribution: Array<{ departmentName: string; count: number }>;
    statusDistribution: Array<{ status: string; count: number }>;
  }> {
    const response = await ApiService.get(`${this.BASE_URL}/statistics`);
    return response.data;
  }

  // Search employees
  static async searchEmployees(
    query: string,
    limit: number = 10
  ): Promise<Employee[]> {
    const response = await ApiService.get<Employee[]>(
      `${this.BASE_URL}/search?q=${encodeURIComponent(query)}&limit=${limit}`
    );
    return response.data;
  }

  // Validate employee number
  static async validateEmployeeNumber(employeeNumber: string, excludeId?: number): Promise<boolean> {
    const params = new URLSearchParams({ employeeNumber });
    if (excludeId) {
      params.append('excludeId', excludeId.toString());
    }
    
    const response = await ApiService.get<{ available: boolean }>(
      `${this.BASE_URL}/validate-employee-number?${params.toString()}`
    );
    return response.data.available;
  }

  // Validate email
  static async validateEmail(email: string, excludeId?: number): Promise<boolean> {
    const params = new URLSearchParams({ email });
    if (excludeId) {
      params.append('excludeId', excludeId.toString());
    }
    
    const response = await ApiService.get<{ available: boolean }>(
      `${this.BASE_URL}/validate-email?${params.toString()}`
    );
    return response.data.available;
  }
}