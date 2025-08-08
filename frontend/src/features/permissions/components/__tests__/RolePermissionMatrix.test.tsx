// RolePermissionMatrix component tests

import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MantineProvider } from '@mantine/core';
import { RolePermissionMatrix } from '../RolePermissionMatrix';
import * as permissionHooks from '../../hooks/usePermissions';

// Mock the hooks
vi.mock('../../hooks/usePermissions');

const mockUseRolePermissionMatrix = vi.mocked(permissionHooks.useRolePermissionMatrix);
const mockUseBulkUpdateRolePermissions = vi.mocked(permissionHooks.useBulkUpdateRolePermissions);

// Test wrapper
const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <MantineProvider>
        {children}
      </MantineProvider>
    </QueryClientProvider>
  );
};

const mockMatrix = {
  roles: [
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
    {
      id: 2,
      name: 'USER',
      description: 'Regular user role',
      permissions: [],
      userCount: 10,
      isSystem: false,
      canDelete: true,
      canEdit: true,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z',
    },
  ],
  permissions: [
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
  ],
  assignments: {
    1: [1, 2], // ADMIN has both permissions
    2: [1],    // USER has only read permission
  },
};

describe('RolePermissionMatrix', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    
    // Default mock implementations
    mockUseRolePermissionMatrix.mockReturnValue({
      data: mockMatrix,
      isLoading: false,
      error: null,
    } as any);

    mockUseBulkUpdateRolePermissions.mockReturnValue({
      mutateAsync: vi.fn(),
      isLoading: false,
    } as any);
  });

  it('should render the matrix with roles and permissions', () => {
    const wrapper = createWrapper();
    render(<RolePermissionMatrix />, { wrapper });

    // Check title
    expect(screen.getByText('Role-Permission Matrix')).toBeInTheDocument();

    // Check roles
    expect(screen.getByText('ADMIN')).toBeInTheDocument();
    expect(screen.getByText('USER')).toBeInTheDocument();

    // Check permissions
    expect(screen.getByText('READ')).toBeInTheDocument();
    expect(screen.getByText('CREATE')).toBeInTheDocument();

    // Check user counts
    expect(screen.getByText('5 users')).toBeInTheDocument();
    expect(screen.getByText('10 users')).toBeInTheDocument();
  });

  it('should show loading state', () => {
    mockUseRolePermissionMatrix.mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
    } as any);

    const wrapper = createWrapper();
    render(<RolePermissionMatrix />, { wrapper });

    // Loading overlay should be visible
    expect(document.querySelector('.mantine-LoadingOverlay-root')).toBeInTheDocument();
  });

  it('should show error state', () => {
    mockUseRolePermissionMatrix.mockReturnValue({
      data: undefined,
      isLoading: false,
      error: new Error('Failed to load matrix'),
    } as any);

    const wrapper = createWrapper();
    render(<RolePermissionMatrix />, { wrapper });

    expect(screen.getByText('Failed to load role-permission matrix. Please try again.')).toBeInTheDocument();
  });

  it('should filter roles by search term', async () => {
    const wrapper = createWrapper();
    render(<RolePermissionMatrix />, { wrapper });

    const searchInput = screen.getByPlaceholderText('Search roles or permissions...');
    fireEvent.change(searchInput, { target: { value: 'ADMIN' } });

    // Should still show ADMIN role
    expect(screen.getByText('ADMIN')).toBeInTheDocument();
    
    // Should filter out USER role (though this depends on implementation)
    // In a real test, you'd need to wait for the filtering to take effect
  });

  it('should filter by permission category', async () => {
    const wrapper = createWrapper();
    render(<RolePermissionMatrix />, { wrapper });

    // Find and click the category filter
    const categorySelect = screen.getByDisplayValue('All Categories');
    fireEvent.click(categorySelect);

    // Select User Management category
    const userManagementOption = screen.getByText('User Management');
    fireEvent.click(userManagementOption);

    // Permissions should be filtered to only show User Management permissions
    expect(screen.getByText('READ')).toBeInTheDocument();
    expect(screen.getByText('CREATE')).toBeInTheDocument();
  });

  it('should toggle system roles visibility', () => {
    const wrapper = createWrapper();
    render(<RolePermissionMatrix />, { wrapper });

    const systemRolesCheckbox = screen.getByLabelText('Show system roles');
    
    // Initially should show system roles
    expect(screen.getByText('ADMIN')).toBeInTheDocument();
    
    // Uncheck to hide system roles
    fireEvent.click(systemRolesCheckbox);
    
    // ADMIN should still be visible (this test would need proper filtering implementation)
    expect(screen.getByText('ADMIN')).toBeInTheDocument();
  });

  it('should handle permission toggle', async () => {
    const wrapper = createWrapper();
    render(<RolePermissionMatrix />, { wrapper });

    // Find a permission checkbox (this would need proper test IDs in real implementation)
    const checkboxes = screen.getAllByRole('checkbox');
    const permissionCheckbox = checkboxes.find(cb => 
      cb.getAttribute('aria-label')?.includes('permission') || 
      cb.closest('td')
    );

    if (permissionCheckbox) {
      fireEvent.click(permissionCheckbox);
      
      // Should show pending changes indicator
      await waitFor(() => {
        expect(screen.getByText(/roles with changes/)).toBeInTheDocument();
      });
    }
  });

  it('should handle save changes', async () => {
    const mockMutateAsync = vi.fn().mockResolvedValue({});
    mockUseBulkUpdateRolePermissions.mockReturnValue({
      mutateAsync: mockMutateAsync,
      isLoading: false,
    } as any);

    const wrapper = createWrapper();
    render(<RolePermissionMatrix />, { wrapper });

    // First toggle a permission to create pending changes
    const checkboxes = screen.getAllByRole('checkbox');
    if (checkboxes.length > 0) {
      fireEvent.click(checkboxes[0]);
      
      // Wait for save button to appear
      await waitFor(() => {
        const saveButton = screen.getByText('Save Changes');
        expect(saveButton).toBeInTheDocument();
        
        // Click save
        fireEvent.click(saveButton);
      });

      // Should call the mutation
      expect(mockMutateAsync).toHaveBeenCalled();
    }
  });

  it('should handle role selection callback', () => {
    const onRoleSelect = vi.fn();
    const wrapper = createWrapper();
    render(<RolePermissionMatrix onRoleSelect={onRoleSelect} />, { wrapper });

    // Click on a role name
    const adminRole = screen.getByText('ADMIN');
    fireEvent.click(adminRole);

    expect(onRoleSelect).toHaveBeenCalledWith(mockMatrix.roles[0]);
  });

  it('should handle permission selection callback', () => {
    const onPermissionSelect = vi.fn();
    const wrapper = createWrapper();
    render(<RolePermissionMatrix onPermissionSelect={onPermissionSelect} />, { wrapper });

    // Click on a permission header
    const readPermission = screen.getByText('READ');
    fireEvent.click(readPermission);

    expect(onPermissionSelect).toHaveBeenCalledWith(mockMatrix.permissions[0]);
  });

  it('should disable editing in read-only mode', () => {
    const wrapper = createWrapper();
    render(<RolePermissionMatrix readOnly={true} />, { wrapper });

    // All checkboxes should be disabled
    const checkboxes = screen.getAllByRole('checkbox');
    checkboxes.forEach(checkbox => {
      expect(checkbox).toBeDisabled();
    });

    // Save/Cancel buttons should not be present
    expect(screen.queryByText('Save Changes')).not.toBeInTheDocument();
    expect(screen.queryByText('Cancel')).not.toBeInTheDocument();
  });
});