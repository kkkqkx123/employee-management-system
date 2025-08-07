// Custom hooks for API integration with TanStack Query

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import ApiService from '../services/api';
import { queryKeys, invalidateQueries } from '../services/queryClient';
import { useNotifications } from '../stores/uiStore';
import type { 
  ApiResponse, 
  PaginatedResponse, 
  PaginationParams, 
  SearchParams 
} from '../types/api';
import type { 
  Employee, 
  Department, 
  Position, 
  EmployeeCreateRequest, 
  EmployeeUpdateRequest,
  DepartmentCreateRequest,
  DepartmentUpdateRequest,
  EmployeeSearchCriteria 
} from '../types/entities';

// Generic hooks for common patterns

/**
 * Generic hook for fetching paginated data
 */
export function usePaginatedQuery<T>(
  queryKey: readonly unknown[],
  url: string,
  params?: PaginationParams,
  options?: {
    enabled?: boolean;
    staleTime?: number;
    select?: (data: ApiResponse<PaginatedResponse<T>>) => any;
  }
) {
  return useQuery({
    queryKey: [...queryKey, params],
    queryFn: () => ApiService.get<PaginatedResponse<T>>(url, { params }),
    enabled: options?.enabled,
    staleTime: options?.staleTime,
    select: options?.select,
  });
}

/**
 * Generic hook for fetching single item
 */
export function useItemQuery<T>(
  queryKey: readonly unknown[],
  url: string,
  options?: {
    enabled?: boolean;
    staleTime?: number;
    select?: (data: ApiResponse<T>) => any;
  }
) {
  return useQuery({
    queryKey,
    queryFn: () => ApiService.get<T>(url),
    enabled: options?.enabled,
    staleTime: options?.staleTime,
    select: options?.select,
  });
}

/**
 * Generic hook for creating items
 */
export function useCreateMutation<TData, TVariables>(
  url: string,
  options?: {
    onSuccess?: (data: TData, variables: TVariables) => void;
    onError?: (error: any, variables: TVariables) => void;
    invalidateKeys?: readonly unknown[][];
  }
) {
  const queryClient = useQueryClient();
  const { showNotification } = useNotifications();

  return useMutation({
    mutationFn: (data: TVariables) => ApiService.post<TData>(url, data),
    onSuccess: (response, variables) => {
      // Invalidate related queries
      options?.invalidateKeys?.forEach(key => {
        queryClient.invalidateQueries({ queryKey: key });
      });

      // Show success notification
      showNotification({
        type: 'success',
        title: 'Success',
        message: 'Item created successfully',
      });

      options?.onSuccess?.(response.data, variables);
    },
    onError: (error, variables) => {
      options?.onError?.(error, variables);
    },
  });
}

/**
 * Generic hook for updating items
 */
export function useUpdateMutation<TData, TVariables>(
  url: string,
  options?: {
    onSuccess?: (data: TData, variables: TVariables) => void;
    onError?: (error: any, variables: TVariables) => void;
    invalidateKeys?: readonly unknown[][];
  }
) {
  const queryClient = useQueryClient();
  const { showNotification } = useNotifications();

  return useMutation({
    mutationFn: (data: TVariables) => ApiService.put<TData>(url, data),
    onSuccess: (response, variables) => {
      // Invalidate related queries
      options?.invalidateKeys?.forEach(key => {
        queryClient.invalidateQueries({ queryKey: key });
      });

      // Show success notification
      showNotification({
        type: 'success',
        title: 'Success',
        message: 'Item updated successfully',
      });

      options?.onSuccess?.(response.data, variables);
    },
    onError: (error, variables) => {
      options?.onError?.(error, variables);
    },
  });
}

/**
 * Generic hook for deleting items
 */
export function useDeleteMutation<TVariables = number>(
  url: string,
  options?: {
    onSuccess?: (variables: TVariables) => void;
    onError?: (error: any, variables: TVariables) => void;
    invalidateKeys?: readonly unknown[][];
  }
) {
  const queryClient = useQueryClient();
  const { showNotification } = useNotifications();

  return useMutation({
    mutationFn: (id: TVariables) => ApiService.delete(`${url}/${id}`),
    onSuccess: (response, variables) => {
      // Invalidate related queries
      options?.invalidateKeys?.forEach(key => {
        queryClient.invalidateQueries({ queryKey: key });
      });

      // Show success notification
      showNotification({
        type: 'success',
        title: 'Success',
        message: 'Item deleted successfully',
      });

      options?.onSuccess?.(variables);
    },
    onError: (error, variables) => {
      options?.onError?.(error, variables);
    },
  });
}

// Employee-specific hooks

/**
 * Hook for fetching employees with pagination
 */
export function useEmployees(params?: PaginationParams) {
  return usePaginatedQuery<Employee>(
    queryKeys.employees.list(params || {}),
    '/employees',
    params,
    {
      select: (data) => data.data,
      staleTime: 2 * 60 * 1000, // 2 minutes
    }
  );
}

/**
 * Hook for fetching single employee
 */
export function useEmployee(id: number, enabled = true) {
  return useItemQuery<Employee>(
    queryKeys.employees.detail(id),
    `/employees/${id}`,
    {
      enabled: enabled && id > 0,
      select: (data) => data.data,
    }
  );
}

/**
 * Hook for searching employees
 */
export function useEmployeeSearch(params: SearchParams) {
  return useQuery({
    queryKey: queryKeys.employees.search(params.q || ''),
    queryFn: () => ApiService.get<PaginatedResponse<Employee>>('/employees/search', { params }),
    enabled: !!params.q && params.q.length > 2,
    select: (data) => data.data,
    staleTime: 1 * 60 * 1000, // 1 minute
  });
}

/**
 * Hook for advanced employee search
 */
export function useEmployeeAdvancedSearch(criteria: EmployeeSearchCriteria) {
  return useQuery({
    queryKey: queryKeys.employees.advancedSearch(criteria),
    queryFn: () => ApiService.post<PaginatedResponse<Employee>>('/employees/search/advanced', criteria),
    enabled: Object.keys(criteria).length > 0,
    select: (data) => data.data,
    staleTime: 1 * 60 * 1000, // 1 minute
  });
}

/**
 * Hook for creating employee
 */
export function useCreateEmployee() {
  return useCreateMutation<Employee, EmployeeCreateRequest>('/employees', {
    invalidateKeys: [queryKeys.employees.all, queryKeys.departments.all],
  });
}

/**
 * Hook for updating employee
 */
export function useUpdateEmployee() {
  return useUpdateMutation<Employee, EmployeeUpdateRequest>('/employees', {
    invalidateKeys: [queryKeys.employees.all, queryKeys.departments.all],
  });
}

/**
 * Hook for deleting employee
 */
export function useDeleteEmployee() {
  return useDeleteMutation('/employees', {
    invalidateKeys: [queryKeys.employees.all, queryKeys.departments.all],
  });
}

// Department-specific hooks

/**
 * Hook for fetching department tree
 */
export function useDepartmentTree() {
  return useQuery({
    queryKey: queryKeys.departments.tree,
    queryFn: () => ApiService.get<Department[]>('/departments/tree'),
    select: (data) => data.data,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}

/**
 * Hook for fetching departments list
 */
export function useDepartments() {
  return useQuery({
    queryKey: queryKeys.departments.list(),
    queryFn: () => ApiService.get<Department[]>('/departments'),
    select: (data) => data.data,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}

/**
 * Hook for fetching single department
 */
export function useDepartment(id: number, enabled = true) {
  return useItemQuery<Department>(
    queryKeys.departments.detail(id),
    `/departments/${id}`,
    {
      enabled: enabled && id > 0,
      select: (data) => data.data,
    }
  );
}

/**
 * Hook for creating department
 */
export function useCreateDepartment() {
  return useCreateMutation<Department, DepartmentCreateRequest>('/departments', {
    invalidateKeys: [queryKeys.departments.all, queryKeys.employees.all],
  });
}

/**
 * Hook for updating department
 */
export function useUpdateDepartment() {
  return useUpdateMutation<Department, DepartmentUpdateRequest>('/departments', {
    invalidateKeys: [queryKeys.departments.all, queryKeys.employees.all],
  });
}

/**
 * Hook for deleting department
 */
export function useDeleteDepartment() {
  return useDeleteMutation('/departments', {
    invalidateKeys: [queryKeys.departments.all, queryKeys.employees.all],
  });
}

// Position-specific hooks

/**
 * Hook for fetching positions
 */
export function usePositions(params?: PaginationParams) {
  return usePaginatedQuery<Position>(
    queryKeys.positions.list(params || {}),
    '/positions',
    params,
    {
      select: (data) => data.data,
      staleTime: 5 * 60 * 1000, // 5 minutes
    }
  );
}

/**
 * Hook for fetching single position
 */
export function usePosition(id: number, enabled = true) {
  return useItemQuery<Position>(
    queryKeys.positions.detail(id),
    `/positions/${id}`,
    {
      enabled: enabled && id > 0,
      select: (data) => data.data,
    }
  );
}

// File upload hooks

/**
 * Hook for uploading files
 */
export function useFileUpload() {
  const { showNotification } = useNotifications();

  return useMutation({
    mutationFn: ({ url, file, onProgress }: { url: string; file: File; onProgress?: (progress: number) => void }) =>
      ApiService.uploadFile(url, file, onProgress),
    onSuccess: () => {
      showNotification({
        type: 'success',
        title: 'Upload Complete',
        message: 'File uploaded successfully',
      });
    },
    onError: (error) => {
      showNotification({
        type: 'error',
        title: 'Upload Failed',
        message: 'Failed to upload file',
      });
    },
  });
}

/**
 * Hook for downloading files
 */
export function useFileDownload() {
  const { showNotification } = useNotifications();

  return useMutation({
    mutationFn: ({ url, filename }: { url: string; filename?: string }) =>
      ApiService.downloadFile(url, filename),
    onError: (error) => {
      showNotification({
        type: 'error',
        title: 'Download Failed',
        message: 'Failed to download file',
      });
    },
  });
}