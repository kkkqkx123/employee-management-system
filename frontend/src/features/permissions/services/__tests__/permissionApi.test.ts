// Permission API tests

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ApiService } from '../../../../services/api';
import { PermissionApi } from '../permissionApi';
import type { RoleCreateRequest, UserRoleUpdateRequest } from '../../types';

// Mock the ApiService
vi.mock('../../../../services/api');

const mockApiService = vi.mocked(ApiService);

describe('PermissionApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Role Management', () => {
    it('should get roles with pagination', async () => {
      const mockResponse = {
        success: true,
        data: {
          content: [
            { id: 1, name: 'ADMIN', permissions: [], userCount: 5 },
            { id: 2, name: 'USER', permissions: [], userCount: 10 },
          ],
          totalPages: 1,
          totalElements: 2,
        },
      };

      mockApiService.get.mockResolvedValue(mockResponse);

      const result = await PermissionApi.getRoles({ page: 0, size: 20 });

      expect(mockApiService.get).toHaveBeenCalledWith('/roles', {
        params: { page: 0, size: 20 },
      });
      expect(result).toEqual(mockResponse);
    });

    it('should get role by ID', async () => {
      const mockRole = {
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
      };

      const mockResponse = { success: true, data: mockRole };
      mockApiService.get.mockResolvedValue(mockResponse);

      const result = await PermissionApi.getRoleById(1);

      expect(mockApiService.get).toHaveBeenCalledWith('/roles/1');
      expect(result).toEqual(mockResponse);
    });

    it('should create role', async () => {
      const roleData: RoleCreateRequest = {
        name: 'MANAGER',
        description: 'Manager role',
        permissionIds: [1, 2, 3],
      };

      const mockResponse = {
        success: true,
        data: { id: 3, ...roleData, permissions: [], userCount: 0 },
      };

      mockApiService.post.mockResolvedValue(mockResponse);

      const result = await PermissionApi.createRole(roleData);

      expect(mockApiService.post).toHaveBeenCalledWith('/roles', roleData);
      expect(result).toEqual(mockResponse);
    });

    it('should update role', async () => {
      const roleData = {
        id: 1,
        name: 'ADMIN_UPDATED',
        description: 'Updated admin role',
        permissionIds: [1, 2, 3, 4],
      };

      const mockResponse = {
        success: true,
        data: { ...roleData, permissions: [], userCount: 5 },
      };

      mockApiService.put.mockResolvedValue(mockResponse);

      const result = await PermissionApi.updateRole(roleData);

      expect(mockApiService.put).toHaveBeenCalledWith('/roles/1', roleData);
      expect(result).toEqual(mockResponse);
    });

    it('should delete role', async () => {
      const mockResponse = { success: true, data: null };
      mockApiService.delete.mockResolvedValue(mockResponse);

      const result = await PermissionApi.deleteRole(1);

      expect(mockApiService.delete).toHaveBeenCalledWith('/roles/1');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('Permission Management', () => {
    it('should get all permissions', async () => {
      const mockPermissions = [
        {
          id: 1,
          name: 'USER_READ',
          description: 'Read user data',
          resource: 'USER',
          action: 'READ',
          category: 'User Management',
          isSystem: true,
        },
        {
          id: 2,
          name: 'USER_CREATE',
          description: 'Create new users',
          resource: 'USER',
          action: 'CREATE',
          category: 'User Management',
          isSystem: true,
        },
      ];

      const mockResponse = { success: true, data: mockPermissions };
      mockApiService.get.mockResolvedValue(mockResponse);

      const result = await PermissionApi.getPermissions();

      expect(mockApiService.get).toHaveBeenCalledWith('/permissions');
      expect(result).toEqual(mockResponse);
    });

    it('should get role-permission matrix', async () => {
      const mockMatrix = {
        roles: [
          { id: 1, name: 'ADMIN', permissions: [], userCount: 5 },
          { id: 2, name: 'USER', permissions: [], userCount: 10 },
        ],
        permissions: [
          { id: 1, name: 'USER_READ', category: 'User Management' },
          { id: 2, name: 'USER_CREATE', category: 'User Management' },
        ],
        assignments: {
          1: [1, 2], // ADMIN has both permissions
          2: [1],    // USER has only read permission
        },
      };

      const mockResponse = { success: true, data: mockMatrix };
      mockApiService.get.mockResolvedValue(mockResponse);

      const result = await PermissionApi.getRolePermissionMatrix();

      expect(mockApiService.get).toHaveBeenCalledWith('/roles/permissions');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('User Role Assignment', () => {
    it('should get user roles', async () => {
      const mockUserRoles = {
        content: [
          {
            userId: 1,
            user: { id: 1, username: 'admin', email: 'admin@test.com' },
            roles: [{ id: 1, name: 'ADMIN' }],
            effectivePermissions: ['USER_READ', 'USER_CREATE'],
          },
        ],
        totalPages: 1,
        totalElements: 1,
      };

      const mockResponse = { success: true, data: mockUserRoles };
      mockApiService.get.mockResolvedValue(mockResponse);

      const result = await PermissionApi.getUserRoles({ page: 0, size: 20 });

      expect(mockApiService.get).toHaveBeenCalledWith('/users/roles', {
        params: { page: 0, size: 20 },
      });
      expect(result).toEqual(mockResponse);
    });

    it('should update user roles', async () => {
      const updateData: UserRoleUpdateRequest = {
        userId: 1,
        roleIds: [1, 2],
      };

      const mockResponse = {
        success: true,
        data: {
          userId: 1,
          user: { id: 1, username: 'admin', email: 'admin@test.com' },
          roles: [
            { id: 1, name: 'ADMIN' },
            { id: 2, name: 'MANAGER' },
          ],
          effectivePermissions: ['USER_READ', 'USER_CREATE', 'EMPLOYEE_READ'],
        },
      };

      mockApiService.put.mockResolvedValue(mockResponse);

      const result = await PermissionApi.updateUserRoles(updateData);

      expect(mockApiService.put).toHaveBeenCalledWith('/users/roles/1', updateData);
      expect(result).toEqual(mockResponse);
    });
  });

  describe('Impact Analysis', () => {
    it('should analyze permission impact', async () => {
      const mockImpact = {
        affectedUsers: 5,
        affectedRoles: ['ADMIN', 'MANAGER'],
        dependentPermissions: ['USER_UPDATE'],
        riskLevel: 'HIGH' as const,
        warnings: ['This will affect 5 users', 'Consider the impact on dependent permissions'],
      };

      const mockResponse = { success: true, data: mockImpact };
      mockApiService.post.mockResolvedValue(mockResponse);

      const result = await PermissionApi.analyzePermissionImpact(1, 'REMOVE', 2);

      expect(mockApiService.post).toHaveBeenCalledWith('/permissions/1/impact', {
        action: 'REMOVE',
        roleId: 2,
      });
      expect(result).toEqual(mockResponse);
    });

    it('should analyze role impact', async () => {
      const mockImpact = {
        affectedUsers: 10,
        affectedRoles: [],
        dependentPermissions: [],
        riskLevel: 'CRITICAL' as const,
        warnings: ['This will remove access for 10 users'],
      };

      const mockResponse = { success: true, data: mockImpact };
      mockApiService.post.mockResolvedValue(mockResponse);

      const result = await PermissionApi.analyzeRoleImpact(1, 'DELETE');

      expect(mockApiService.post).toHaveBeenCalledWith('/roles/1/impact', {
        action: 'DELETE',
        changes: undefined,
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('Validation', () => {
    it('should validate role permissions', async () => {
      const mockValidation = {
        valid: true,
        conflicts: [],
      };

      const mockResponse = { success: true, data: mockValidation };
      mockApiService.post.mockResolvedValue(mockResponse);

      const result = await PermissionApi.validateRolePermissions(1, [1, 2, 3]);

      expect(mockApiService.post).toHaveBeenCalledWith('/roles/1/validate-permissions', {
        permissionIds: [1, 2, 3],
      });
      expect(result).toEqual(mockResponse);
    });

    it('should validate user roles', async () => {
      const mockValidation = {
        valid: false,
        conflicts: ['Role ADMIN conflicts with role USER'],
      };

      const mockResponse = { success: true, data: mockValidation };
      mockApiService.post.mockResolvedValue(mockResponse);

      const result = await PermissionApi.validateUserRoles(1, [1, 2]);

      expect(mockApiService.post).toHaveBeenCalledWith('/users/1/roles/validate', {
        roleIds: [1, 2],
      });
      expect(result).toEqual(mockResponse);
    });
  });
});