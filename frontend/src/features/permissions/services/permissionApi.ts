// Permission and role management API service

import { ApiService } from '../../../services/api';
import type { ApiResponse, PaginatedResponse } from '../../../types/api';
import type {
  RoleWithDetails,
  PermissionWithDetails,
  RolePermissionMatrix,
  UserRoleAssignment,
  RoleCreateRequest,
  RoleUpdateRequest,
  UserRoleUpdateRequest,
  PermissionImpact,
  RoleSearchParams,
  UserRoleSearchParams,
} from '../types';

// API endpoints
const ENDPOINTS = {
  ROLES: '/roles',
  PERMISSIONS: '/permissions',
  ROLE_PERMISSIONS: '/roles/permissions',
  USER_ROLES: '/users/roles',
  PERMISSION_IMPACT: '/permissions/impact',
} as const;

export class PermissionApi {
  // Role management
  static async getRoles(params?: RoleSearchParams): Promise<ApiResponse<PaginatedResponse<RoleWithDetails>>> {
    return ApiService.get(ENDPOINTS.ROLES, { params });
  }

  static async getRoleById(id: number): Promise<ApiResponse<RoleWithDetails>> {
    return ApiService.get(`${ENDPOINTS.ROLES}/${id}`);
  }

  static async createRole(data: RoleCreateRequest): Promise<ApiResponse<RoleWithDetails>> {
    return ApiService.post(ENDPOINTS.ROLES, data);
  }

  static async updateRole(data: RoleUpdateRequest): Promise<ApiResponse<RoleWithDetails>> {
    return ApiService.put(`${ENDPOINTS.ROLES}/${data.id}`, data);
  }

  static async deleteRole(id: number): Promise<ApiResponse<void>> {
    return ApiService.delete(`${ENDPOINTS.ROLES}/${id}`);
  }

  // Permission management
  static async getPermissions(): Promise<ApiResponse<PermissionWithDetails[]>> {
    return ApiService.get(ENDPOINTS.PERMISSIONS);
  }

  static async getPermissionById(id: number): Promise<ApiResponse<PermissionWithDetails>> {
    return ApiService.get(`${ENDPOINTS.PERMISSIONS}/${id}`);
  }

  // Role-Permission matrix
  static async getRolePermissionMatrix(): Promise<ApiResponse<RolePermissionMatrix>> {
    return ApiService.get(ENDPOINTS.ROLE_PERMISSIONS);
  }

  static async updateRolePermissions(roleId: number, permissionIds: number[]): Promise<ApiResponse<void>> {
    return ApiService.put(`${ENDPOINTS.ROLES}/${roleId}/permissions`, { permissionIds });
  }

  // User role assignment
  static async getUserRoles(params?: UserRoleSearchParams): Promise<ApiResponse<PaginatedResponse<UserRoleAssignment>>> {
    return ApiService.get(ENDPOINTS.USER_ROLES, { params });
  }

  static async getUserRoleById(userId: number): Promise<ApiResponse<UserRoleAssignment>> {
    return ApiService.get(`${ENDPOINTS.USER_ROLES}/${userId}`);
  }

  static async updateUserRoles(data: UserRoleUpdateRequest): Promise<ApiResponse<UserRoleAssignment>> {
    return ApiService.put(`${ENDPOINTS.USER_ROLES}/${data.userId}`, data);
  }

  // Permission impact analysis
  static async analyzePermissionImpact(permissionId: number, action: 'ADD' | 'REMOVE', roleId?: number): Promise<ApiResponse<PermissionImpact>> {
    return ApiService.post(`${ENDPOINTS.PERMISSIONS}/${permissionId}/impact`, {
      action,
      roleId,
    });
  }

  static async analyzeRoleImpact(roleId: number, action: 'DELETE' | 'MODIFY', changes?: Partial<RoleUpdateRequest>): Promise<ApiResponse<PermissionImpact>> {
    return ApiService.post(`${ENDPOINTS.ROLES}/${roleId}/impact`, {
      action,
      changes,
    });
  }

  // Bulk operations
  static async bulkUpdateUserRoles(updates: UserRoleUpdateRequest[]): Promise<ApiResponse<UserRoleAssignment[]>> {
    return ApiService.post(`${ENDPOINTS.USER_ROLES}/bulk`, { updates });
  }

  static async bulkUpdateRolePermissions(updates: Array<{ roleId: number; permissionIds: number[] }>): Promise<ApiResponse<void>> {
    return ApiService.post(`${ENDPOINTS.ROLE_PERMISSIONS}/bulk`, { updates });
  }

  // Search and filtering
  static async searchRoles(query: string, params?: RoleSearchParams): Promise<ApiResponse<PaginatedResponse<RoleWithDetails>>> {
    return ApiService.get(`${ENDPOINTS.ROLES}/search`, {
      params: { q: query, ...params },
    });
  }

  static async searchUserRoles(query: string, params?: UserRoleSearchParams): Promise<ApiResponse<PaginatedResponse<UserRoleAssignment>>> {
    return ApiService.get(`${ENDPOINTS.USER_ROLES}/search`, {
      params: { q: query, ...params },
    });
  }

  // Permission validation
  static async validateRolePermissions(roleId: number, permissionIds: number[]): Promise<ApiResponse<{ valid: boolean; conflicts: string[] }>> {
    return ApiService.post(`${ENDPOINTS.ROLES}/${roleId}/validate-permissions`, { permissionIds });
  }

  static async validateUserRoles(userId: number, roleIds: number[]): Promise<ApiResponse<{ valid: boolean; conflicts: string[] }>> {
    return ApiService.post(`/users/${userId}/roles/validate`, { roleIds });
  }
}

export default PermissionApi;