// Hook for role management

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PermissionApi } from '../services/permissionApi';
import type {
  RoleWithDetails,
  RoleCreateRequest,
  RoleUpdateRequest,
  RoleSearchParams,
  PermissionImpact,
} from '../types';

// Query keys
export const ROLE_QUERY_KEYS = {
  all: ['roles'] as const,
  lists: () => [...ROLE_QUERY_KEYS.all, 'list'] as const,
  list: (params?: RoleSearchParams) => [...ROLE_QUERY_KEYS.lists(), params] as const,
  details: () => [...ROLE_QUERY_KEYS.all, 'detail'] as const,
  detail: (id: number) => [...ROLE_QUERY_KEYS.details(), id] as const,
  search: (query: string, params?: RoleSearchParams) => [...ROLE_QUERY_KEYS.all, 'search', query, params] as const,
  impact: (roleId: number, action: string) => [...ROLE_QUERY_KEYS.all, 'impact', roleId, action] as const,
};

// Get roles with pagination and filtering
export const useRoles = (params?: RoleSearchParams) => {
  return useQuery({
    queryKey: ROLE_QUERY_KEYS.list(params),
    queryFn: () => PermissionApi.getRoles(params),
    select: (response) => response.data,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Get single role by ID
export const useRole = (id: number, enabled = true) => {
  return useQuery({
    queryKey: ROLE_QUERY_KEYS.detail(id),
    queryFn: () => PermissionApi.getRoleById(id),
    select: (response) => response.data,
    enabled: enabled && id > 0,
    staleTime: 5 * 60 * 1000,
  });
};

// Search roles
export const useRoleSearch = (query: string, params?: RoleSearchParams, enabled = true) => {
  return useQuery({
    queryKey: ROLE_QUERY_KEYS.search(query, params),
    queryFn: () => PermissionApi.searchRoles(query, params),
    select: (response) => response.data,
    enabled: enabled && query.length > 0,
    staleTime: 2 * 60 * 1000, // 2 minutes for search results
  });
};

// Create role mutation
export const useCreateRole = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: RoleCreateRequest) => PermissionApi.createRole(data),
    onSuccess: (response) => {
      // Invalidate and refetch roles list
      queryClient.invalidateQueries({ queryKey: ROLE_QUERY_KEYS.lists() });
      
      // Add the new role to the cache
      queryClient.setQueryData(
        ROLE_QUERY_KEYS.detail(response.data.id),
        response
      );
    },
  });
};

// Update role mutation
export const useUpdateRole = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: RoleUpdateRequest) => PermissionApi.updateRole(data),
    onSuccess: (response, variables) => {
      // Update the specific role in cache
      queryClient.setQueryData(
        ROLE_QUERY_KEYS.detail(variables.id),
        response
      );
      
      // Invalidate lists to ensure consistency
      queryClient.invalidateQueries({ queryKey: ROLE_QUERY_KEYS.lists() });
    },
  });
};

// Delete role mutation
export const useDeleteRole = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => PermissionApi.deleteRole(id),
    onSuccess: (_, deletedId) => {
      // Remove from cache
      queryClient.removeQueries({ queryKey: ROLE_QUERY_KEYS.detail(deletedId) });
      
      // Invalidate lists
      queryClient.invalidateQueries({ queryKey: ROLE_QUERY_KEYS.lists() });
    },
  });
};

// Analyze role impact
export const useRoleImpactAnalysis = (roleId: number, action: 'DELETE' | 'MODIFY', changes?: Partial<RoleUpdateRequest>) => {
  return useQuery({
    queryKey: ROLE_QUERY_KEYS.impact(roleId, action),
    queryFn: () => PermissionApi.analyzeRoleImpact(roleId, action, changes),
    select: (response) => response.data,
    enabled: roleId > 0,
    staleTime: 30 * 1000, // 30 seconds for impact analysis
  });
};

// Update role permissions mutation
export const useUpdateRolePermissions = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ roleId, permissionIds }: { roleId: number; permissionIds: number[] }) =>
      PermissionApi.updateRolePermissions(roleId, permissionIds),
    onSuccess: (_, { roleId }) => {
      // Invalidate role details and matrix
      queryClient.invalidateQueries({ queryKey: ROLE_QUERY_KEYS.detail(roleId) });
      queryClient.invalidateQueries({ queryKey: ['role-permission-matrix'] });
    },
  });
};

// Validate role permissions
export const useValidateRolePermissions = () => {
  return useMutation({
    mutationFn: ({ roleId, permissionIds }: { roleId: number; permissionIds: number[] }) =>
      PermissionApi.validateRolePermissions(roleId, permissionIds),
  });
};