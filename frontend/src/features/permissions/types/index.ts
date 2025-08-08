// Permission and role management types

import type { User, Role, Permission } from '../../../types/auth';
import type { PaginationParams } from '../../../types/api';

// Extended role type with additional properties for management
export interface RoleWithDetails extends Role {
  userCount: number;
  isSystem: boolean;
  canDelete: boolean;
  canEdit: boolean;
  createdAt: string;
  updatedAt: string;
}

// Permission with additional metadata
export interface PermissionWithDetails extends Permission {
  category: string;
  isSystem: boolean;
  dependsOn?: string[];
  impacts?: string[];
}

// Role-Permission matrix data structure
export interface RolePermissionMatrix {
  roles: RoleWithDetails[];
  permissions: PermissionWithDetails[];
  assignments: Record<number, number[]>; // roleId -> permissionIds[]
}

// User role assignment
export interface UserRoleAssignment {
  userId: number;
  user: User;
  roles: Role[];
  effectivePermissions: string[];
}

// Form types for role management
export interface RoleCreateRequest {
  name: string;
  description?: string;
  permissionIds: number[];
}

export interface RoleUpdateRequest extends Partial<RoleCreateRequest> {
  id: number;
}

// Form types for user role assignment
export interface UserRoleUpdateRequest {
  userId: number;
  roleIds: number[];
}

// Permission impact analysis
export interface PermissionImpact {
  affectedUsers: number;
  affectedRoles: string[];
  dependentPermissions: string[];
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  warnings: string[];
}

// Search and filter types
export interface RoleSearchParams extends PaginationParams {
  name?: string;
  hasPermission?: string;
  isSystem?: boolean;
}

export interface UserRoleSearchParams extends PaginationParams {
  username?: string;
  email?: string;
  roleId?: number;
  hasPermission?: string;
}

// Permission categories for organization
export const PERMISSION_CATEGORIES = {
  USER_MANAGEMENT: 'User Management',
  EMPLOYEE_MANAGEMENT: 'Employee Management',
  DEPARTMENT_MANAGEMENT: 'Department Management',
  COMMUNICATION: 'Communication',
  REPORTING: 'Reporting',
  SYSTEM: 'System Administration',
} as const;

export type PermissionCategory = typeof PERMISSION_CATEGORIES[keyof typeof PERMISSION_CATEGORIES];

// Role types
export const ROLE_TYPES = {
  SYSTEM: 'SYSTEM',
  CUSTOM: 'CUSTOM',
} as const;

export type RoleType = typeof ROLE_TYPES[keyof typeof ROLE_TYPES];

// Permission actions
export const PERMISSION_ACTIONS = {
  CREATE: 'CREATE',
  READ: 'READ',
  UPDATE: 'UPDATE',
  DELETE: 'DELETE',
  EXECUTE: 'EXECUTE',
} as const;

export type PermissionAction = typeof PERMISSION_ACTIONS[keyof typeof PERMISSION_ACTIONS];

// Risk levels for permission changes
export const RISK_LEVELS = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
  CRITICAL: 'CRITICAL',
} as const;

export type RiskLevel = typeof RISK_LEVELS[keyof typeof RISK_LEVELS];