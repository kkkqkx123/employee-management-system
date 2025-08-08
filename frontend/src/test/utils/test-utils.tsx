import React, { ReactElement } from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MantineProvider } from '@mantine/core';
import { vi } from 'vitest';

// Mock data factories
export const createMockEmployee = (overrides = {}) => ({
  id: 1,
  employeeNumber: 'EMP001',
  firstName: 'John',
  lastName: 'Doe',
  email: 'john.doe@company.com',
  department: 'Engineering',
  position: 'Software Developer',
  status: 'ACTIVE',
  dateOfBirth: '1990-01-01',
  hireDate: '2023-01-01',
  ...overrides,
});

export const createMockDepartment = (overrides = {}) => ({
  id: 1,
  name: 'Engineering',
  description: 'Software Engineering Department',
  parentId: null,
  children: [],
  ...overrides,
});

export const createMockUser = (overrides = {}) => ({
  id: 1,
  username: 'testuser',
  email: 'test@example.com',
  firstName: 'Test',
  lastName: 'User',
  enabled: true,
  roles: ['EMPLOYEE'],
  permissions: ['USER_READ', 'EMPLOYEE_READ'],
  ...overrides,
});

export const createMockNotification = (overrides = {}) => ({
  id: 1,
  title: 'Test Notification',
  message: 'This is a test notification',
  type: 'INFO',
  read: false,
  createdAt: new Date().toISOString(),
  ...overrides,
});

export const createMockChatMessage = (overrides = {}) => ({
  id: 1,
  content: 'Hello, world!',
  senderId: 1,
  senderName: 'John Doe',
  roomId: 1,
  timestamp: new Date().toISOString(),
  type: 'TEXT',
  ...overrides,
});

// Test wrapper component
interface AllTheProvidersProps {
  children: React.ReactNode;
}

const AllTheProviders = ({ children }: AllTheProvidersProps) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return (
    <BrowserRouter>
      <QueryClientProvider client={queryClient}>
        <MantineProvider>
          {children}
        </MantineProvider>
      </QueryClientProvider>
    </BrowserRouter>
  );
};

// Custom render function
const customRender = (
  ui: ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>
) => render(ui, { wrapper: AllTheProviders, ...options });

// Mock functions
export const createMockApiResponse = <T>(data: T, success = true) => ({
  success,
  message: success ? 'Operation successful' : 'Operation failed',
  data,
});

export const createMockPageResponse = <T>(content: T[], totalElements = 1) => ({
  content,
  totalElements,
  totalPages: Math.ceil(totalElements / 20),
  size: 20,
  number: 0,
});

// Mock hooks
export const mockUseAuth = (overrides = {}) => ({
  user: createMockUser(),
  isAuthenticated: true,
  login: vi.fn(),
  logout: vi.fn(),
  hasPermission: vi.fn(() => true),
  hasRole: vi.fn(() => true),
  ...overrides,
});

export const mockUseNotifications = (overrides = {}) => ({
  notifications: [createMockNotification()],
  unreadCount: 1,
  markAsRead: vi.fn(),
  markAllAsRead: vi.fn(),
  ...overrides,
});

// Accessibility testing helpers
export const expectToHaveNoA11yViolations = async (container: HTMLElement) => {
  const { toHaveNoViolations } = await import('jest-axe');
  expect.extend(toHaveNoViolations);
  
  const { axe } = await import('jest-axe');
  const results = await axe(container);
  expect(results).toHaveNoViolations();
};

// Form testing helpers
export const fillForm = async (form: HTMLFormElement, data: Record<string, string>) => {
  const { fireEvent } = await import('@testing-library/react');
  
  Object.entries(data).forEach(([name, value]) => {
    const input = form.querySelector(`[name="${name}"]`) as HTMLInputElement;
    if (input) {
      fireEvent.change(input, { target: { value } });
    }
  });
};

// Wait for loading states
export const waitForLoadingToFinish = async () => {
  const { waitForElementToBeRemoved, screen } = await import('@testing-library/react');
  
  try {
    await waitForElementToBeRemoved(() => screen.queryByText(/loading/i), {
      timeout: 3000,
    });
  } catch {
    // Loading element might not exist, which is fine
  }
};

// Re-export everything from testing-library
export * from '@testing-library/react';
export { customRender as render };