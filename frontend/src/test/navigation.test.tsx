import { render, screen } from '@testing-library/react';
import { BrowserRouter, MemoryRouter } from 'react-router-dom';
import { MantineProvider } from '@mantine/core';
import { Navigation } from '@/components/layout/Navigation';
import { useAuthStore, useNotificationStore } from '@/stores';
import { ROUTES, PERMISSIONS } from '@/constants';
import React from 'react'; // 添加 React 导入以支持 JSX 语法

// Mock the stores
jest.mock('@/stores', () => ({
  useAuthStore: jest.fn(),
  useNotificationStore: jest.fn(),
}));

// Mock the permission check hook
jest.mock('@/features/auth/hooks', () => ({
  usePermissionCheck: () => ({
    hasPermission: jest.fn((permission: string) => {
      // Mock permission logic
      const mockAuthStore = useAuthStore as jest.MockedFunction<typeof useAuthStore>;
      const authState = mockAuthStore();
      return authState.hasPermission?.(permission) || false;
    }),
  }),
}));

const createTestWrapper = (initialEntries = ['/']) => {
  return ({ children }: { children: React.ReactNode }) => (
    <MantineProvider>
      <MemoryRouter initialEntries={initialEntries}>
        {children}
      </MemoryRouter>
    </MantineProvider>
  );
};

describe('Navigation', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    
    // Default mock for notification store
    (useNotificationStore as jest.MockedFunction<typeof useNotificationStore>).mockReturnValue({
      unreadCount: 0,
    } as any);
  });

  it('renders all navigation items when user has all permissions', () => {
    (useAuthStore as jest.MockedFunction<typeof useAuthStore>).mockReturnValue({
      isAuthenticated: true,
      user: { id: '1', name: 'Test User', email: 'test@example.com', roles: ['ADMIN'] },
      hasPermission: () => true,
    } as any);

    const TestWrapper = createTestWrapper([ROUTES.DASHBOARD]);
    render(<Navigation />, { wrapper: TestWrapper });

    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Employees')).toBeInTheDocument();
    expect(screen.getByText('Departments')).toBeInTheDocument();
    expect(screen.getByText('Chat')).toBeInTheDocument();
    expect(screen.getByText('Email')).toBeInTheDocument();
    expect(screen.getByText('Notifications')).toBeInTheDocument();
    expect(screen.getByText('Permissions')).toBeInTheDocument();
  });

  it('filters navigation items based on permissions', () => {
    (useAuthStore as jest.MockedFunction<typeof useAuthStore>).mockReturnValue({
      isAuthenticated: true,
      user: { id: '1', name: 'Test User', email: 'test@example.com', roles: [] },
      hasPermission: (permission: string) => {
        // Only allow dashboard and chat (no specific permissions required)
        return !permission || permission === 'SOME_OTHER_PERMISSION';
      },
    } as any);

    const TestWrapper = createTestWrapper([ROUTES.DASHBOARD]);
    render(<Navigation />, { wrapper: TestWrapper });

    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Chat')).toBeInTheDocument();
    expect(screen.getByText('Email')).toBeInTheDocument();
    expect(screen.getByText('Notifications')).toBeInTheDocument();
    
    // These should be filtered out due to lack of permissions
    expect(screen.queryByText('Employees')).not.toBeInTheDocument();
    expect(screen.queryByText('Departments')).not.toBeInTheDocument();
    expect(screen.queryByText('Permissions')).not.toBeInTheDocument();
  });

  it('shows notification badge when there are unread notifications', () => {
    (useAuthStore as jest.MockedFunction<typeof useAuthStore>).mockReturnValue({
      isAuthenticated: true,
      user: { id: '1', name: 'Test User', email: 'test@example.com', roles: [] },
      hasPermission: () => true,
    } as any);

    (useNotificationStore as jest.MockedFunction<typeof useNotificationStore>).mockReturnValue({
      unreadCount: 5,
    } as any);

    const TestWrapper = createTestWrapper([ROUTES.DASHBOARD]);
    render(<Navigation />, { wrapper: TestWrapper });

    expect(screen.getByText('5')).toBeInTheDocument();
  });

  it('shows 99+ badge when unread count exceeds 99', () => {
    (useAuthStore as jest.MockedFunction<typeof useAuthStore>).mockReturnValue({
      isAuthenticated: true,
      user: { id: '1', name: 'Test User', email: 'test@example.com', roles: [] },
      hasPermission: () => true,
    } as any);

    (useNotificationStore as jest.MockedFunction<typeof useNotificationStore>).mockReturnValue({
      unreadCount: 150,
    } as any);

    const TestWrapper = createTestWrapper([ROUTES.DASHBOARD]);
    render(<Navigation />, { wrapper: TestWrapper });

    expect(screen.getByText('99+')).toBeInTheDocument();
  });

  it('highlights active navigation item', () => {
    (useAuthStore as jest.MockedFunction<typeof useAuthStore>).mockReturnValue({
      isAuthenticated: true,
      user: { id: '1', name: 'Test User', email: 'test@example.com', roles: [] },
      hasPermission: () => true,
    } as any);

    const TestWrapper = createTestWrapper([ROUTES.EMPLOYEES]);
    render(<Navigation />, { wrapper: TestWrapper });

    const employeesLink = screen.getByText('Employees').closest('a');
    expect(employeesLink).toHaveAttribute('data-active', 'true');
  });
});