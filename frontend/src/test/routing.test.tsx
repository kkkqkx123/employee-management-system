import { render, screen } from '@testing-library/react';
import { BrowserRouter, MemoryRouter } from 'react-router-dom';
import { MantineProvider } from '@mantine/core';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AppRouter } from '@/router';
import { useAuthStore } from '@/stores';
import { ROUTES } from '@/constants';
import React from 'react'; // 添加 React 导入以支持 JSX 语法

// Mock the auth store
jest.mock('@/stores', () => ({
  useAuthStore: jest.fn(),
  useNotificationStore: jest.fn(() => ({ unreadCount: 0 })),
}));

// Mock the auth components
jest.mock('@/features/auth/components', () => ({
  ProtectedRoute: ({ children, requiredPermission }: any) => {
    const mockAuthStore = useAuthStore as jest.MockedFunction<typeof useAuthStore>;
    const authState = mockAuthStore();
    
    if (!authState.isAuthenticated) {
      return <div>Please log in</div>;
    }
    
    if (requiredPermission && !authState.hasPermission?.(requiredPermission)) {
      return <div>Access denied</div>;
    }
    
    return children || <div>Protected content</div>;
  },
  LoginForm: () => <div>Login Form</div>,
}));

// Mock the page components
jest.mock('@/pages/DashboardPage', () => ({
  DashboardPage: () => <div>Dashboard Page</div>,
}));

jest.mock('@/pages/EmployeesPage', () => ({
  EmployeesPage: () => <div>Employees Page</div>,
}));

jest.mock('@/pages/NotFoundPage', () => ({
  NotFoundPage: () => <div>404 Not Found</div>,
}));

const createTestWrapper = (initialEntries = ['/']) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <MantineProvider>
        <MemoryRouter initialEntries={initialEntries}>
          {children}
        </MemoryRouter>
      </MantineProvider>
    </QueryClientProvider>
  );
};

describe('AppRouter', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('redirects to login when not authenticated', () => {
    (useAuthStore as jest.MockedFunction<typeof useAuthStore>).mockReturnValue({
      isAuthenticated: false,
      user: null,
      hasPermission: () => false,
    } as any);

    const TestWrapper = createTestWrapper([ROUTES.DASHBOARD]);
    render(<AppRouter />, { wrapper: TestWrapper });

    expect(screen.getByText('Please log in')).toBeInTheDocument();
  });

  it('shows dashboard when authenticated and accessing root', () => {
    (useAuthStore as jest.MockedFunction<typeof useAuthStore>).mockReturnValue({
      isAuthenticated: true,
      user: { id: '1', name: 'Test User', email: 'test@example.com', roles: [] },
      hasPermission: () => true,
    } as any);

    const TestWrapper = createTestWrapper(['/']);
    render(<AppRouter />, { wrapper: TestWrapper });

    expect(screen.getByText('Dashboard Page')).toBeInTheDocument();
  });

  it('shows access denied for protected routes without permission', () => {
    (useAuthStore as jest.MockedFunction<typeof useAuthStore>).mockReturnValue({
      isAuthenticated: true,
      user: { id: '1', name: 'Test User', email: 'test@example.com', roles: [] },
      hasPermission: () => false,
    } as any);

    const TestWrapper = createTestWrapper([ROUTES.EMPLOYEES]);
    render(<AppRouter />, { wrapper: TestWrapper });

    expect(screen.getByText('Access denied')).toBeInTheDocument();
  });

  it('shows employees page when user has permission', () => {
    (useAuthStore as jest.MockedFunction<typeof useAuthStore>).mockReturnValue({
      isAuthenticated: true,
      user: { id: '1', name: 'Test User', email: 'test@example.com', roles: [] },
      hasPermission: (permission: string) => permission === 'EMPLOYEE_READ',
    } as any);

    const TestWrapper = createTestWrapper([ROUTES.EMPLOYEES]);
    render(<AppRouter />, { wrapper: TestWrapper });

    expect(screen.getByText('Employees Page')).toBeInTheDocument();
  });

  it('shows 404 page for unknown routes', () => {
    (useAuthStore as jest.MockedFunction<typeof useAuthStore>).mockReturnValue({
      isAuthenticated: true,
      user: { id: '1', name: 'Test User', email: 'test@example.com', roles: [] },
      hasPermission: () => true,
    } as any);

    const TestWrapper = createTestWrapper(['/unknown-route']);
    render(<AppRouter />, { wrapper: TestWrapper });

    expect(screen.getByText('404 Not Found')).toBeInTheDocument();
  });

  it('shows login page for login route', () => {
    (useAuthStore as jest.MockedFunction<typeof useAuthStore>).mockReturnValue({
      isAuthenticated: false,
      user: null,
      hasPermission: () => false,
    } as any);

    const TestWrapper = createTestWrapper([ROUTES.LOGIN]);
    render(<AppRouter />, { wrapper: TestWrapper });

    expect(screen.getByText('Login Form')).toBeInTheDocument();
  });
});