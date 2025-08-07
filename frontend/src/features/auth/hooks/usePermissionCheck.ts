import { useMemo } from 'react';
import { useAuth, usePermissions } from '../../../stores/authStore';

interface UsePermissionCheckOptions {
  permissions: string[];
  requireAll?: boolean;
}

/**
 * Hook to check user permissions with memoization
 */
export const usePermissionCheck = ({ 
  permissions, 
  requireAll = false 
}: UsePermissionCheckOptions) => {
  const { hasPermission, hasAnyPermission, hasAllPermissions, permissions: userPermissions } = usePermissions();

  const result = useMemo(() => {
    if (permissions.length === 0) {
      return { hasAccess: true, missingPermissions: [] };
    }

    if (permissions.length === 1) {
      const hasAccess = hasPermission(permissions[0]);
      return {
        hasAccess,
        missingPermissions: hasAccess ? [] : permissions,
      };
    }

    const hasAccess = requireAll 
      ? hasAllPermissions(permissions)
      : hasAnyPermission(permissions);

    const missingPermissions = hasAccess 
      ? [] 
      : permissions.filter(permission => !userPermissions.includes(permission));

    return {
      hasAccess,
      missingPermissions,
    };
  }, [permissions, requireAll, hasPermission, hasAnyPermission, hasAllPermissions, userPermissions]);

  return result;
};

/**
 * Hook to check a single permission
 */
export const useSinglePermissionCheck = (permission: string) => {
  const { hasPermission } = usePermissions();
  
  return useMemo(() => ({
    hasAccess: hasPermission(permission),
    missingPermissions: hasPermission(permission) ? [] : [permission],
  }), [permission, hasPermission]);
};

/**
 * Hook to check if user has any of the specified roles
 */
export const useRoleCheck = (roles: string[]) => {
  const { user } = useAuth();
  
  return useMemo(() => {
    if (!user || roles.length === 0) {
      return { hasRole: false, userRoles: [] };
    }

    const userRoles = user.roles.map(role => role.name);
    const hasRole = roles.some(role => userRoles.includes(role));

    return {
      hasRole,
      userRoles,
      missingRoles: hasRole ? [] : roles.filter(role => !userRoles.includes(role)),
    };
  }, [user, roles]);
};

export default usePermissionCheck;