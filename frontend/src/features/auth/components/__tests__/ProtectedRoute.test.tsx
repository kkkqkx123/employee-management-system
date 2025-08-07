import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { MantineProvider } from '@mantine/core';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import ProtectedRoute from '../ProtectedRoute';
import { useAuth, usePermissions } from '../../../../stores/authStore';

// Mock the auth store
vi.mock('../../../../stores/authStore', () => ({
  useAuth: vi.fn(),
  usePermissions: vi.fn(),
}));

// Mock react-router-dom Navigate component
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    Navigate: ({ to }: { to: string }) => <div data-testid="navigate-to">{to}</div>,
  };
});

const mockUseAuth = useAuth as vi.MockedFunction<typeof useAuth>;
const mockUsePermissions = usePermissions as vi.MockedFunction<typeof usePermissions>;

const renderWithProviders = (component: React.ReactElement, initialEntries = ['/']) => {
  return render(
    <MantineProvider>
      <MemoryRouter initialEntries={initialEntries}>
        {component}
      </MemoryRouter>
    </MantineProvider>
  );
};

describe('ProtectedRoute', () => {
  const mockHasPermission = vi.fn();
  const mockHasAnyPermission = vi.fn();
  const mockHasAllPermissions = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    
    mockUsePermissions.mockReturnValue({
      permissions: [],
      hasPermission: mockHasPermission,
      hasAnyPermission: mockHasAnyPermission,
      hasAllPermissions: mockHasAllPermissions,
    });
  });

  it('shows loading overlay when authentication is loading', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: true,
    });

    renderWithProviders(
      <ProtectedRoute>
        <div>Protected Content</div>
      </ProtectedRoute>
    );

    // The loading overlay should be visible
    expect(document.querySelector('.mantine-LoadingOverlay-root')).toBeInTheDocument();
  });

  it('redirects to login when user is not authenticated', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
    });

    renderWithProviders(
      <ProtectedRoute>
        <div>Protected Content</div>
      </ProtectedRoute>
    );

    expect(screen.getByTestId('navigate-to')).toHaveTextContent('/login');
  });

  it('renders children when user is authenticated and has no permission requirements', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
    });

    renderWithProviders(
      <ProtectedRoute>
        <div>Protected Content</div>
      </ProtectedRoute>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('renders children when user is authenticated and has required permissions (any)', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
    });

    mockHasAnyPermission.mockReturnValue(true);

    renderWithProviders(
      <ProtectedRoute requiredPermissions={['READ_USERS', 'WRITE_USERS']}>
        <div>Protected Content</div>
      </ProtectedRoute>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
    expect(mockHasAnyPermission).toHaveBeenCalledWith(['READ_USERS', 'WRITE_USERS']);
  });

  it('renders children when user is authenticated and has all required permissions', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
    });

    mockHasAllPermissions.mockReturnValue(true);

    renderWithProviders(
      <ProtectedRoute 
        requiredPermissions={['READ_USERS', 'WRITE_USERS']} 
        requireAllPermissions={true}
      >
        <div>Protected Content</div>
      </ProtectedRoute>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
    expect(mockHasAllPermissions).toHaveBeenCalledWith(['READ_USERS', 'WRITE_USERS']);
  });

  it('shows access denied when user lacks required permissions', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
    });

    mockHasAnyPermission.mockReturnValue(false);

    renderWithProviders(
      <ProtectedRoute requiredPermissions={['ADMIN_ACCESS']}>
        <div>Protected Content</div>
      </ProtectedRoute>
    );

    expect(screen.getByText('Access Denied')).toBeInTheDocument();
    expect(screen.getByText(/you don't have the required permissions/i)).toBeInTheDocument();
    expect(screen.getByText(/ADMIN_ACCESS/)).toBeInTheDocument();
  });

  it('uses custom fallback path for unauthenticated users', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
    });

    renderWithProviders(
      <ProtectedRoute fallbackPath="/custom-login">
        <div>Protected Content</div>
      </ProtectedRoute>
    );

    expect(screen.getByTestId('navigate-to')).toHaveTextContent('/custom-login');
  });
});