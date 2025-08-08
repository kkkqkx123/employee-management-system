// useRoles hook tests

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useRoles, useCreateRole, useUpdateRole, useDeleteRole } from '../useRoles';
import { PermissionApi } from '../../services/permissionApi';
import type { RoleCreateRequest, RoleUpdateRequest } from '../../types';

// Mock the API
vi.mock('../../services/permissionApi');
const mockPermissionApi = vi.mocked(PermissionApi);

// Test wrapper with QueryClient
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

describe('useRoles', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('useRoles', () => {
    it('should fetch roles successfully', async () => {
      const mockRoles = {
        content: [
          {
            id: 1,
            name: 'ADMIN',
            description: 'Administrator role',
            permissions: [],
            userCount: 5,
            isSystem: true,
            canDelete: false,
            canEdit: true,
            createdAt: '2024-01-01T00:00:00Z',
            updatedAt: '2024-01-01T00:00:00Z',
          },
        ],
        totalPages: 1,
        totalElements: 1,
      };

      mockPermissionApi.getRoles.mockResolvedValue({
        success: true,
        data: mockRoles,
      });

      const wrapper = createWrapper();
      const { result } = renderHook(() => useRoles(), { wrapper });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data).toEqual(mockRoles);
      expect(mockPermissionApi.getRoles).toHaveBeenCalledWith(undefined);
    });

    it('should handle search parameters', async () => {
      const searchParams = { page: 1, size: 10, name: 'ADMIN' };
      
      mockPermissionApi.getRoles.mockResolvedValue({
        success: true,
        data: { content: [], totalPages: 0, totalElements: 0 },
      });

      const wrapper = createWrapper();
      const { result } = renderHook(() => useRoles(searchParams), { wrapper });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(mockPermissionApi.getRoles).toHaveBeenCalledWith(searchParams);
    });

    it('should handle API errors', async () => {
      const error = new Error('Failed to fetch roles');
      mockPermissionApi.getRoles.mockRejectedValue(error);

      const wrapper = createWrapper();
      const { result } = renderHook(() => useRoles(), { wrapper });

      await waitFor(() => {
        expect(result.current.isError).toBe(true);
      });

      expect(result.current.error).toEqual(error);
    });
  });

  describe('useCreateRole', () => {
    it('should create role successfully', async () => {
      const newRole = {
        id: 2,
        name: 'MANAGER',
        description: 'Manager role',
        permissions: [],
        userCount: 0,
        isSystem: false,
        canDelete: true,
        canEdit: true,
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      };

      mockPermissionApi.createRole.mockResolvedValue({
        success: true,
        data: newRole,
      });

      const wrapper = createWrapper();
      const { result } = renderHook(() => useCreateRole(), { wrapper });

      const roleData: RoleCreateRequest = {
        name: 'MANAGER',
        description: 'Manager role',
        permissionIds: [1, 2],
      };

      await waitFor(async () => {
        await result.current.mutateAsync(roleData);
      });

      expect(mockPermissionApi.createRole).toHaveBeenCalledWith(roleData);
      expect(result.current.isSuccess).toBe(true);
    });

    it('should handle creation errors', async () => {
      const error = new Error('Failed to create role');
      mockPermissionApi.createRole.mockRejectedValue(error);

      const wrapper = createWrapper();
      const { result } = renderHook(() => useCreateRole(), { wrapper });

      const roleData: RoleCreateRequest = {
        name: 'MANAGER',
        description: 'Manager role',
        permissionIds: [1, 2],
      };

      await waitFor(async () => {
        try {
          await result.current.mutateAsync(roleData);
        } catch (e) {
          expect(e).toEqual(error);
        }
      });

      expect(result.current.isError).toBe(true);
    });
  });

  describe('useUpdateRole', () => {
    it('should update role successfully', async () => {
      const updatedRole = {
        id: 1,
        name: 'ADMIN_UPDATED',
        description: 'Updated admin role',
        permissions: [],
        userCount: 5,
        isSystem: true,
        canDelete: false,
        canEdit: true,
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      };

      mockPermissionApi.updateRole.mockResolvedValue({
        success: true,
        data: updatedRole,
      });

      const wrapper = createWrapper();
      const { result } = renderHook(() => useUpdateRole(), { wrapper });

      const roleData: RoleUpdateRequest = {
        id: 1,
        name: 'ADMIN_UPDATED',
        description: 'Updated admin role',
        permissionIds: [1, 2, 3],
      };

      await waitFor(async () => {
        await result.current.mutateAsync(roleData);
      });

      expect(mockPermissionApi.updateRole).toHaveBeenCalledWith(roleData);
      expect(result.current.isSuccess).toBe(true);
    });
  });

  describe('useDeleteRole', () => {
    it('should delete role successfully', async () => {
      mockPermissionApi.deleteRole.mockResolvedValue({
        success: true,
        data: undefined,
      });

      const wrapper = createWrapper();
      const { result } = renderHook(() => useDeleteRole(), { wrapper });

      await waitFor(async () => {
        await result.current.mutateAsync(1);
      });

      expect(mockPermissionApi.deleteRole).toHaveBeenCalledWith(1);
      expect(result.current.isSuccess).toBe(true);
    });

    it('should handle deletion errors', async () => {
      const error = new Error('Cannot delete system role');
      mockPermissionApi.deleteRole.mockRejectedValue(error);

      const wrapper = createWrapper();
      const { result } = renderHook(() => useDeleteRole(), { wrapper });

      await waitFor(async () => {
        try {
          await result.current.mutateAsync(1);
        } catch (e) {
          expect(e).toEqual(error);
        }
      });

      expect(result.current.isError).toBe(true);
    });
  });
});