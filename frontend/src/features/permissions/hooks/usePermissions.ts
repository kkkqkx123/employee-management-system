// Hook for permission management

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PermissionApi } from '../services/permissionApi';
import type {
  PermissionWithDetails,
  RolePermissionMatrix,
  PermissionImpact,
} from '../types';

// Query keys
export const PERMISSION_QUERY_KEYS = {
  all: ['permissions'] as const,
  lists: () => [...PERMISSION_QUERY_KEYS.all, 'list'] as const,
  list: () => [...PERMISSION_QUERY_KEYS.lists()] as const,
  details: () => [...PERMISSION_QUERY_KEYS.all, 'detail'] as const,
  detail: (id: number) => [...PERMISSION_QUERY_KEYS.details(), id] as const,
  matrix: () => [...PERMISSION_QUERY_KEYS.all, 'matrix'] as const,
  impact: (permissionId: number, action: string, roleId?: number) => 
    [...PERMISSION_QUERY_KEYS.all, 'impact', permissionId, action, roleId] as const,
};

// Get all permissions
export const usePermissions = () => {
  return useQuery({
    queryKey: PERMISSION_QUERY_KEYS.list(),
    queryFn: () => PermissionApi.getPermissions(),
    select: (response) => response.data,
    staleTime: 10 * 60 * 1000, // 10 minutes - permissions don't change often
  });
};

// Get single permission by ID
export const usePermission = (id: number, enabled = true) => {
  return useQuery({
    queryKey: PERMISSION_QUERY_KEYS.detail(id),
    queryFn: () => PermissionApi.getPermissionById(id),
    select: (response) => response.data,
    enabled: enabled && id > 0,
    staleTime: 10 * 60 * 1000,
  });
};

// Get role-permission matrix
export const useRolePermissionMatrix = () => {
  return useQuery({
    queryKey: PERMISSION_QUERY_KEYS.matrix(),
    queryFn: () => PermissionApi.getRolePermissionMatrix(),
    select: (response) => response.data,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Analyze permission impact
export const usePermissionImpactAnalysis = (
  permissionId: number, 
  action: 'ADD' | 'REMOVE', 
  roleId?: number,
  enabled = true
) => {
  return useQuery({
    queryKey: PERMISSION_QUERY_KEYS.impact(permissionId, action, roleId),
    queryFn: () => PermissionApi.analyzePermissionImpact(permissionId, action, roleId),
    select: (response) => response.data,
    enabled: enabled && permissionId > 0,
    staleTime: 30 * 1000, // 30 seconds for impact analysis
  });
};

// Bulk update role permissions mutation
export const useBulkUpdateRolePermissions = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (updates: Array<{ roleId: number; permissionIds: number[] }>) =>
      PermissionApi.bulkUpdateRolePermissions(updates),
    onSuccess: () => {
      // Invalidate matrix and role queries
      queryClient.invalidateQueries({ queryKey: PERMISSION_QUERY_KEYS.matrix() });
      queryClient.invalidateQueries({ queryKey: ['roles'] });
    },
  });
};

// Helper hook to get permissions grouped by category
export const usePermissionsByCategory = () => {
  const { data: permissions, ...rest } = usePermissions();

  const permissionsByCategory = permissions?.reduce((acc, permission) => {
    const category = permission.category;
    if (!acc[category]) {
      acc[category] = [];
    }
    acc[category].push(permission);
    return acc;
  }, {} as Record<string, PermissionWithDetails[]>);

  return {
    data: permissionsByCategory,
    permissions,
    ...rest,
  };
};

// Helper hook to check if a role has specific permissions
export const useRolePermissionCheck = (roleId: number, permissionNames: string[]) => {
  const { data: matrix } = useRolePermissionMatrix();

  const hasPermissions = matrix ? (() => {
    const role = matrix.roles.find(r => r.id === roleId);
    if (!role) return false;

    const rolePermissionIds = matrix.assignments[roleId] || [];
    const rolePermissions = matrix.permissions.filter(p => rolePermissionIds.includes(p.id));
    
    return permissionNames.every(name => 
      rolePermissions.some(p => p.name === name)
    );
  })() : false;

  return {
    hasPermissions,
    isLoading: !matrix,
  };
};