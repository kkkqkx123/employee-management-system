import React, { type ReactElement } from 'react';
import * as testingLibrary from '@testing-library/react';
import { render, type RenderOptions } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MantineProvider } from '@mantine/core';
import { vi } from 'vitest';


// Mock data factories
interface MockEmployee {
  id: number;
  employeeNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  department: string;
  position: string;
  status: string;
  dateOfBirth: string;
  hireDate: string;
}

export const createMockEmployee = (overrides: Partial<MockEmployee> = {}): MockEmployee => ({
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

interface MockDepartment {
  id: number;
  name: string;
  description: string;
  parentId: number | null;
  children: MockDepartment[];
}

export const createMockDepartment = (overrides: Partial<MockDepartment> = {}): MockDepartment => ({
  id: 1,
  name: 'Engineering',
  description: 'Software Engineering Department',
  parentId: null,
  children: [],
  ...overrides,
});

interface MockUser {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  roles: string[];
  permissions: string[];
}

export const createMockUser = (overrides: Partial<MockUser> = {}): MockUser => ({
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

interface MockNotification {
  id: number;
  title: string;
  message: string;
  type: string;
  read: boolean;
  createdAt: string;
}

export const createMockNotification = (overrides: Partial<MockNotification> = {}): MockNotification => ({
  id: 1,
  title: 'Test Notification',
  message: 'This is a test notification',
  type: 'INFO',
  read: false,
  createdAt: new Date().toISOString(),
  ...overrides,
});

interface MockChatMessage {
  id: number;
  content: string;
  senderId: number;
  senderName: string;
  roomId: number;
  timestamp: string;
  type: string;
}

export const createMockChatMessage = (overrides: Partial<MockChatMessage> = {}): MockChatMessage => ({
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

const AllTheProviders: React.FC<AllTheProvidersProps> = ({ children }) => {
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
): ReturnType<typeof render> => render(ui, { wrapper: AllTheProviders, ...options });

interface MockApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export const createMockApiResponse = <T extends unknown>(data: T, success = true): MockApiResponse<T> => ({
  success,
  message: success ? 'Operation successful' : 'Operation failed',
  data,
});

interface MockPageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const createMockPageResponse = <T extends unknown>(content: T[], totalElements = 1): MockPageResponse<T> => ({
  content,
  totalElements,
  totalPages: Math.ceil(totalElements / 20),
  size: 20,
  number: 0,
});

interface MockUseAuth {
  user: ReturnType<typeof createMockUser>;
  isAuthenticated: boolean;
  login: ReturnType<typeof vi.fn>;
  logout: ReturnType<typeof vi.fn>;
  hasPermission: ReturnType<typeof vi.fn>;
  hasRole: ReturnType<typeof vi.fn>;
}

export const mockUseAuth = (overrides: Partial<MockUseAuth> = {}): MockUseAuth => ({
  user: createMockUser(),
  isAuthenticated: true,
  login: vi.fn(),
  logout: vi.fn(),
  hasPermission: vi.fn(() => true),
  hasRole: vi.fn(() => true),
  ...overrides,
});

interface MockUseNotifications {
  notifications: ReturnType<typeof createMockNotification>[];
  unreadCount: number;
  markAsRead: ReturnType<typeof vi.fn>;
  markAllAsRead: ReturnType<typeof vi.fn>;
}

export const mockUseNotifications = (overrides: Partial<MockUseNotifications> = {}): MockUseNotifications => ({
  notifications: [createMockNotification()],
  unreadCount: 1,
  markAsRead: vi.fn(),
  markAllAsRead: vi.fn(),
  ...overrides,
});

// Accessibility testing helpers
export const expectToHaveNoA11yViolations = async (container: HTMLElement): Promise<void> => {
  const { toHaveNoViolations } = await import('jest-axe');
  expect.extend(toHaveNoViolations);
  
  const { axe } = await import('jest-axe');
  const results = await axe(container);
  expect(results).toHaveNoViolations();
};

// Form testing helpers
export const fillForm = async (form: HTMLFormElement, data: Record<string, string>): Promise<void> => {
  const { fireEvent } = await import('@testing-library/react');
  
  Object.entries(data).forEach(([name, value]) => {
    const input = form.querySelector(`[name="${name}"]`) as HTMLInputElement;
    if (input) {
      fireEvent.change(input, { target: { value } });
    }
  });
};

// Wait for loading states
export const waitForLoadingToFinish = async (): Promise<void> => {
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

// Explicitly export commonly used items to ensure they are available
export const { screen, fireEvent, waitFor } = testingLibrary;