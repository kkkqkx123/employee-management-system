// Hook for user role assignment management

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PermissionApi } from '../services/permissionApi';
import type {
  UserRoleAssignment,
  UserRoleUpdateRequest,
  UserRoleSearchParams,
} from '../types';

// Query keys
export const USER_ROLE_QUERY_KEYS = {
  all: ['user-roles'] as const,
  lists: () => [...USER_ROLE_QUERY_KEYS.all, 'list'] as const,
  list: (params?: UserRoleSearchParams) => [...USER_ROLE_QUERY_KEYS.lists(), params] as const,
  details: () => [...USER_ROLE_QUERY_KEYS.all, 'detail'] as const,
  detail: (userId: number) => [...USER_ROLE_QUERY_KEYS.details(), userId] as const,
  search: (query: string, params?: UserRoleSearchParams) => 
    [...USER_ROLE_QUERY_KEYS.all, 'search', query, params] as const,
};

// Get user roles with pagination and filtering
export const useUserRoles = (params?: UserRoleSearchParams) => {
  return useQuery({
    queryKey: USER_ROLE_QUERY_KEYS.list(params),
    queryFn: () => PermissionApi.getUserRoles(params),
    select: (response) => response.data,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Get user roles by user ID
export const useUserRoleById = (userId: number, enabled = true) => {
  return useQuery({
    queryKey: USER_ROLE_QUERY_KEYS.detail(userId),
    queryFn: () => PermissionApi.getUserRoleById(userId),
    select: (response) => response.data,
    enabled: enabled && userId > 0,
    staleTime: 5 * 60 * 1000,
  });
};

// Search user roles
export const useUserRoleSearch = (query: string, params?: UserRoleSearchParams, enabled = true) => {
  return useQuery({
    queryKey: USER_ROLE_QUERY_KEYS.search(query, params),
    queryFn: () => PermissionApi.searchUserRoles(query, params),
    select: (response) => response.data,
    enabled: enabled && query.length > 0,
    staleTime: 2 * 60 * 1000, // 2 minutes for search results
  });
};

// Update user roles mutation
export const useUpdateUserRoles = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: UserRoleUpdateRequest) => PermissionApi.updateUserRoles(data),
    onSuccess: (response, variables) => {
      // Update the specific user role assignment in cache
      queryClient.setQueryData(
        USER_ROLE_QUERY_KEYS.detail(variables.userId),
        response
      );
      
      // Invalidate lists to ensure consistency
      queryClient.invalidateQueries({ queryKey: USER_ROLE_QUERY_KEYS.lists() });
      
      // Also invalidate auth store if this is the current user
      // This would trigger a re-fetch of user permissions
      queryClient.invalidateQueries({ queryKey: ['auth', 'user'] });
    },
  });
};

// Bulk update user roles mutation
export const useBulkUpdateUserRoles = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (updates: UserRoleUpdateRequest[]) => PermissionApi.bulkUpdateUserRoles(updates),
    onSuccess: (response, variables) => {
      // Invalidate all user role queries
      queryClient.invalidateQueries({ queryKey: USER_ROLE_QUERY_KEYS.all });
      
      // Update individual user caches if possible
      variables.forEach((update) => {
        const userAssignment = response.data.find(ua => ua.userId === update.userId);
        if (userAssignment) {
          queryClient.setQueryData(
            USER_ROLE_QUERY_KEYS.detail(update.userId),
            { data: userAssignment }
          );
        }
      });
    },
  });
};

// Validate user roles mutation
export const useValidateUserRoles = () => {
  return useMutation({
    mutationFn: ({ userId, roleIds }: { userId: number; roleIds: number[] }) =>
      PermissionApi.validateUserRoles(userId, roleIds),
  });
};

// Helper hook to get effective permissions for a user
export const useUserEffectivePermissions = (userId: number) => {
  const { data: userRoles, ...rest } = useUserRoleById(userId);

  const effectivePermissions = userRoles?.effectivePermissions || [];
  const roles = userRoles?.roles || [];

  return {
    effectivePermissions,
    roles,
    userRoles,
    ...rest,
  };
};

// Helper hook to check if a user has specific permissions
export const useUserPermissionCheck = (userId: number, permissionNames: string[]) => {
  const { effectivePermissions, isLoading } = useUserEffectivePermissions(userId);

  const hasPermissions = permissionNames.every(permission => 
    effectivePermissions.includes(permission)
  );

  const hasAnyPermission = permissionNames.some(permission => 
    effectivePermissions.includes(permission)
  );

  return {
    hasPermissions,
    hasAnyPermission,
    effectivePermissions,
    isLoading,
  };
};

// Helper hook to get users by role
export const useUsersByRole = (roleId: number) => {
  const { data: userRoles, ...rest } = useUserRoles({ roleId });

  const users = userRoles?.content.filter(ur => 
    ur.roles.some(role => role.id === roleId)
  ).map(ur => ur.user) || [];

  return {
    users,
    userRoles,
    ...rest,
  };
};