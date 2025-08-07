// Employee hooks tests

import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { vi } from 'vitest';
import { useEmployees, useCreateEmployee, useUpdateEmployee, useDeleteEmployee } from '../useEmployees';
import { EmployeeApiService } from '../../services/employeeApi';
import type { Employee, EmployeeCreateRequest, EmployeeUpdateRequest } from '../../types';

// Mock the API service
vi.mock('../../services/employeeApi');

const mockEmployee: Employee = {
  id: 1,
  employeeNumber: 'EMP-001',
  firstName: 'John',
  lastName: 'Doe',
  fullName: 'John Doe',
  email: 'john.doe@example.com',
  departmentId: 1,
  departmentName: 'Engineering',
  positionId: 1,
  positionName: 'Software Engineer',
  hireDate: '2023-01-15',
  status: 'ACTIVE',
  employmentType: 'FULL_TIME',
  payType: 'SALARIED',
  salary: 75000,
  enabled: true,
  createdAt: '2023-01-15T00:00:00Z',
  updatedAt: '2023-01-15T00:00:00Z',
};

const mockEmployeesData = {
  content: [mockEmployee],
  totalElements: 1,
  totalPages: 1,
  size: 20,
  number: 0,
};

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  );
};

describe('useEmployees', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches employees successfully', async () => {
    vi.mocked(EmployeeApiService.getEmployees).mockResolvedValue(mockEmployeesData);

    const { result } = renderHook(
      () => useEmployees(0, 20),
      { wrapper: createWrapper() }
    );

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toEqual(mockEmployeesData);
    expect(EmployeeApiService.getEmployees).toHaveBeenCalledWith(0, 20, undefined, undefined);
  });

  it('handles fetch error', async () => {
    const error = new Error('Failed to fetch');
    vi.mocked(EmployeeApiService.getEmployees).mockRejectedValue(error);

    const { result } = renderHook(
      () => useEmployees(0, 20),
      { wrapper: createWrapper() }
    );

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });

    expect(result.current.error).toEqual(error);
  });

  it('passes search criteria to API', async () => {
    const searchCriteria = { firstName: 'John', status: 'ACTIVE' as const };
    vi.mocked(EmployeeApiService.getEmployees).mockResolvedValue(mockEmployeesData);

    renderHook(
      () => useEmployees(0, 20, 'lastName,asc', searchCriteria),
      { wrapper: createWrapper() }
    );

    await waitFor(() => {
      expect(EmployeeApiService.getEmployees).toHaveBeenCalledWith(
        0, 
        20, 
        'lastName,asc', 
        searchCriteria
      );
    });
  });
});

describe('useCreateEmployee', () => {
  it('creates employee successfully', async () => {
    const createRequest: EmployeeCreateRequest = {
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@example.com',
      departmentId: 1,
      positionId: 1,
      hireDate: '2023-01-15',
      status: 'ACTIVE',
      employmentType: 'FULL_TIME',
      payType: 'SALARIED',
      salary: 75000,
    };

    vi.mocked(EmployeeApiService.createEmployee).mockResolvedValue(mockEmployee);

    const { result } = renderHook(
      () => useCreateEmployee(),
      { wrapper: createWrapper() }
    );

    await result.current.mutateAsync(createRequest);

    expect(EmployeeApiService.createEmployee).toHaveBeenCalledWith(createRequest);
  });

  it('handles create error', async () => {
    const error = new Error('Failed to create');
    vi.mocked(EmployeeApiService.createEmployee).mockRejectedValue(error);

    const { result } = renderHook(
      () => useCreateEmployee(),
      { wrapper: createWrapper() }
    );

    await expect(result.current.mutateAsync({} as EmployeeCreateRequest))
      .rejects.toThrow('Failed to create');
  });
});

describe('useUpdateEmployee', () => {
  it('updates employee successfully', async () => {
    const updateRequest: EmployeeUpdateRequest = {
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@example.com',
      departmentId: 1,
      positionId: 1,
      hireDate: '2023-01-15',
      status: 'ACTIVE',
      employmentType: 'FULL_TIME',
      enabled: true,
    };

    const updatedEmployee = { ...mockEmployee, firstName: 'John Updated' };
    vi.mocked(EmployeeApiService.updateEmployee).mockResolvedValue(updatedEmployee);

    const { result } = renderHook(
      () => useUpdateEmployee(),
      { wrapper: createWrapper() }
    );

    await result.current.mutateAsync({ id: 1, data: updateRequest });

    expect(EmployeeApiService.updateEmployee).toHaveBeenCalledWith(1, updateRequest);
  });

  it('handles update error', async () => {
    const error = new Error('Failed to update');
    vi.mocked(EmployeeApiService.updateEmployee).mockRejectedValue(error);

    const { result } = renderHook(
      () => useUpdateEmployee(),
      { wrapper: createWrapper() }
    );

    await expect(result.current.mutateAsync({ id: 1, data: {} as EmployeeUpdateRequest }))
      .rejects.toThrow('Failed to update');
  });
});

describe('useDeleteEmployee', () => {
  it('deletes employee successfully', async () => {
    vi.mocked(EmployeeApiService.deleteEmployee).mockResolvedValue();

    const { result } = renderHook(
      () => useDeleteEmployee(),
      { wrapper: createWrapper() }
    );

    await result.current.mutateAsync(1);

    expect(EmployeeApiService.deleteEmployee).toHaveBeenCalledWith(1);
  });

  it('handles delete error', async () => {
    const error = new Error('Failed to delete');
    vi.mocked(EmployeeApiService.deleteEmployee).mockRejectedValue(error);

    const { result } = renderHook(
      () => useDeleteEmployee(),
      { wrapper: createWrapper() }
    );

    await expect(result.current.mutateAsync(1))
      .rejects.toThrow('Failed to delete');
  });
});