// API integration tests

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { useEmployees, useCreateEmployee } from '../hooks/useApi';
import { EmployeeStatus } from '../types/entities';
import ApiService from '../services/api';
import React from 'react'; // 添加 React 导入以支持 JSX 语法

// Mock API service
vi.mock('../services/api');
const mockApiService = vi.mocked(ApiService);

// Test wrapper with QueryClient
const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  );
};

describe('API Integration', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('useEmployees', () => {
    it('should fetch employees successfully', async () => {
      const mockEmployees = {
        success: true,
        data: {
          content: [
            {
              id: 1,
              employeeNumber: 'EMP001',
              firstName: 'John',
              lastName: 'Doe',
              email: 'john.doe@company.com',
            },
          ],
          totalElements: 1,
          totalPages: 1,
          size: 20,
          number: 0,
          first: true,
          last: true,
          numberOfElements: 1,
        },
      };

      mockApiService.get.mockResolvedValueOnce(mockEmployees);

      const { result } = renderHook(() => useEmployees(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data).toEqual(mockEmployees.data);
      expect(mockApiService.get).toHaveBeenCalledWith('/employees', {
        params: {},
      });
    });

    it('should handle API errors', async () => {
      const mockError = {
        success: false,
        message: 'Failed to fetch employees',
        timestamp: new Date().toISOString(),
      };

      mockApiService.get.mockRejectedValueOnce(mockError);

      const { result } = renderHook(() => useEmployees(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(result.current.isError).toBe(true);
      });

      expect(result.current.error).toEqual(mockError);
    });
  });

  describe('useCreateEmployee', () => {
    it('should create employee successfully', async () => {
      const mockEmployee = {
        success: true,
        data: {
          id: 1,
          employeeNumber: 'EMP001',
          firstName: 'John',
          lastName: 'Doe',
          email: 'john.doe@company.com',
        },
      };

      const employeeData = {
        employeeNumber: 'EMP001',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@company.com',
        departmentId: 1,
        positionId: 1,
        hireDate: '2024-01-15',
        status: EmployeeStatus.ACTIVE,
      };

      mockApiService.post.mockResolvedValueOnce(mockEmployee);

      const { result } = renderHook(() => useCreateEmployee(), {
        wrapper: createWrapper(),
      });

      result.current.mutate(employeeData);

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data).toEqual(mockEmployee);
      expect(mockApiService.post).toHaveBeenCalledWith('/employees', employeeData);
    });

    it('should handle validation errors', async () => {
      const mockError = {
        success: false,
        message: 'Validation failed',
        errors: [
          { field: 'email', message: 'Email is required' },
          { field: 'firstName', message: 'First name is required' },
        ],
        timestamp: new Date().toISOString(),
      };

      const employeeData = {
        employeeNumber: 'EMP001',
        firstName: '',
        lastName: 'Doe',
        email: '',
        departmentId: 1,
        positionId: 1,
        hireDate: '2024-01-15',
        status: EmployeeStatus.ACTIVE,
      };

      mockApiService.post.mockRejectedValueOnce(mockError);

      const { result } = renderHook(() => useCreateEmployee(), {
        wrapper: createWrapper(),
      });

      result.current.mutate(employeeData);

      await waitFor(() => {
        expect(result.current.isError).toBe(true);
      });

      expect(result.current.error).toEqual(mockError);
    });
  });
});

describe('Error Handling', () => {
  it('should handle network errors', async () => {
    const networkError = new Error('Network Error');
    mockApiService.get.mockRejectedValueOnce(networkError);

    const { result } = renderHook(() => useEmployees(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });

    expect(result.current.error).toEqual(networkError);
  });

  it('should handle timeout errors', async () => {
    const timeoutError = new Error('Request timeout');
    mockApiService.get.mockRejectedValueOnce(timeoutError);

    const { result } = renderHook(() => useEmployees(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });

    expect(result.current.error).toEqual(timeoutError);
  });
});

describe('Query Invalidation', () => {
  it('should invalidate related queries after mutation', async () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });

    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');

    const mockEmployee = {
      success: true,
      data: {
        id: 1,
        employeeNumber: 'EMP001',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@company.com',
      },
    };

    mockApiService.post.mockResolvedValueOnce(mockEmployee);

    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>
        {children}
      </QueryClientProvider>
    );

    const { result } = renderHook(() => useCreateEmployee(), { wrapper });

    const employeeData = {
      employeeNumber: 'EMP001',
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@company.com',
      departmentId: 1,
      positionId: 1,
      hireDate: '2024-01-15',
      status: EmployeeStatus.ACTIVE,
    };

    result.current.mutate(employeeData);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    // Check that invalidateQueries was called
    expect(invalidateSpy).toHaveBeenCalled();
  });
});