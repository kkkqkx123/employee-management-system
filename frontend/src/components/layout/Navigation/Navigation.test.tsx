import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { MantineProvider } from '@mantine/core';
import { Navigation } from './Navigation';

// Mock the stores and hooks
jest.mock('@/stores', () => ({
  useNotificationStore: () => ({ unreadCount: 0 }),
}));

jest.mock('@/features/auth/hooks', () => ({
  usePermissionCheck: () => ({
    hasPermission: () => true,
  }),
}));

const TestWrapper = ({ children }: { children: React.ReactNode }) => (
  <MantineProvider>
    <MemoryRouter>
      {children}
    </MemoryRouter>
  </MantineProvider>
);

describe('Navigation', () => {
  it('renders navigation items', () => {
    render(<Navigation />, { wrapper: TestWrapper });
    
    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Employee Management')).toBeInTheDocument();
  });
});