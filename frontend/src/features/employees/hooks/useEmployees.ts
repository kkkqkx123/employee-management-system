// Employee management hooks

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { EmployeeApiService } from '../services/employeeApi';
import type {
  Employee,
  EmployeeCreateRequest,
  EmployeeUpdateRequest,
  EmployeeSearchCriteria,
  EmployeeBulkOperation
} from '../types';

// Query keys
export const employeeKeys = {
  all: ['employees'] as const,
  lists: () => [...employeeKeys.all, 'list'] as const,
  list: (filters: EmployeeSearchCriteria) => [...employeeKeys.lists(), { filters }] as const,
  details: () => [...employeeKeys.all, 'detail'] as const,
  detail: (id: number) => [...employeeKeys.details(), id] as const,
  statistics: () => [...employeeKeys.all, 'statistics'] as const,
  search: (query: string) => [...employeeKeys.all, 'search', query] as const,
};

// Hook for fetching employees with pagination and search
export const useEmployees = (
  page: number = 0,
  size: number = 20,
  sort?: string,
  searchCriteria?: EmployeeSearchCriteria
) => {
  return useQuery({
    queryKey: employeeKeys.list({ page, size, sort, ...searchCriteria }),
    queryFn: () => EmployeeApiService.getEmployees(page, size, sort, searchCriteria),
    keepPreviousData: true,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Hook for fetching a single employee
export const useEmployee = (id: number) => {
  return useQuery({
    queryKey: employeeKeys.detail(id),
    queryFn: () => EmployeeApiService.getEmployeeById(id),
    enabled: !!id,
    staleTime: 5 * 60 * 1000,
  });
};

// Hook for creating an employee
export const useCreateEmployee = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (employeeData: EmployeeCreateRequest) =>
      EmployeeApiService.createEmployee(employeeData),
    onSuccess: () => {
      // Invalidate and refetch employee lists
      queryClient.invalidateQueries({ queryKey: employeeKeys.lists() });
      queryClient.invalidateQueries({ queryKey: employeeKeys.statistics() });
    },
  });
};

// Hook for updating an employee
export const useUpdateEmployee = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: EmployeeUpdateRequest }) =>
      EmployeeApiService.updateEmployee(id, data),
    onSuccess: (updatedEmployee) => {
      // Update the specific employee in cache
      queryClient.setQueryData(
        employeeKeys.detail(updatedEmployee.id),
        updatedEmployee
      );
      // Invalidate lists to refresh
      queryClient.invalidateQueries({ queryKey: employeeKeys.lists() });
      queryClient.invalidateQueries({ queryKey: employeeKeys.statistics() });
    },
  });
};

// Hook for deleting an employee
export const useDeleteEmployee = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => EmployeeApiService.deleteEmployee(id),
    onSuccess: (_, deletedId) => {
      // Remove from cache
      queryClient.removeQueries({ queryKey: employeeKeys.detail(deletedId) });
      // Invalidate lists
      queryClient.invalidateQueries({ queryKey: employeeKeys.lists() });
      queryClient.invalidateQueries({ queryKey: employeeKeys.statistics() });
    },
  });
};

// Hook for bulk operations
export const useBulkEmployeeOperation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (operation: EmployeeBulkOperation) =>
      EmployeeApiService.bulkOperation(operation),
    onSuccess: () => {
      // Invalidate all employee data
      queryClient.invalidateQueries({ queryKey: employeeKeys.all });
    },
  });
};

// Hook for importing employees
export const useImportEmployees = () => {
  const queryClient = useQueryClient();
  const [progress, setProgress] = useState(0);

  const mutation = useMutation({
    mutationFn: (file: File) =>
      EmployeeApiService.importEmployees(file, setProgress),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: employeeKeys.all });
      setProgress(0);
    },
    onError: () => {
      setProgress(0);
    },
  });

  return {
    ...mutation,
    progress,
  };
};

// Hook for exporting employees
export const useExportEmployees = () => {
  return useMutation({
    mutationFn: EmployeeApiService.exportEmployees,
  });
};

// Hook for employee statistics
export const useEmployeeStatistics = () => {
  return useQuery({
    queryKey: employeeKeys.statistics(),
    queryFn: () => EmployeeApiService.getEmployeeStatistics(),
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
};

// Hook for searching employees
export const useSearchEmployees = (query: string, enabled: boolean = true) => {
  return useQuery({
    queryKey: employeeKeys.search(query),
    queryFn: () => EmployeeApiService.searchEmployees(query),
    enabled: enabled && query.length >= 2,
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};

// Hook for validating employee number
export const useValidateEmployeeNumber = () => {
  return useMutation({
    mutationFn: ({ employeeNumber, excludeId }: { employeeNumber: string; excludeId?: number }) =>
      EmployeeApiService.validateEmployeeNumber(employeeNumber, excludeId),
  });
};

// Hook for validating email
export const useValidateEmail = () => {
  return useMutation({
    mutationFn: ({ email, excludeId }: { email: string; excludeId?: number }) =>
      EmployeeApiService.validateEmail(email, excludeId),
  });
};