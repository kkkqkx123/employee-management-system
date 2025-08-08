// Permissions feature exports

// Components
export { default as PermissionsPage } from './components/PermissionsPage';
export { default as RolePermissionMatrix } from './components/RolePermissionMatrix';
export { default as UserRoleAssignment } from './components/UserRoleAssignment';
export { default as RoleManagement } from './components/RoleManagement';
export { default as RoleDetailView } from './components/RoleDetailView';
export { default as PermissionImpactWarning } from './components/PermissionImpactWarning';

// Hooks
export * from './hooks/useRoles';
export * from './hooks/usePermissions';
export * from './hooks/useUserRoles';

// Services
export { default as PermissionApi } from './services/permissionApi';

// Types
export * from './types';