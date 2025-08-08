import { render, screen, fireEvent } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MantineProvider } from '@mantine/core';
import { NotificationDropdown } from '../NotificationDropdown';
import { NotificationType, NotificationPriority } from '../../types';

// Mock the hooks
jest.mock('../../hooks/useNotifications', () => ({
  useNotifications: () => ({
    notifications: [
      {
        id: 1,
        title: 'Test Notification',
        message: 'This is a test notification',
        type: NotificationType.SYSTEM,
        priority: NotificationPriority.NORMAL,
        userId: 1,
        read: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      }
    ],
    unreadCount: 1,
    isLoading: false,
    error: null,
    markAsRead: jest.fn(),
    markAllAsRead: jest.fn(),
    deleteNotification: jest.fn(),
    refetchNotifications: jest.fn(),
    isMarkingAllAsRead: false,
  }),
}));

jest.mock('../../../stores/authStore', () => ({
  useAuth: () => ({
    user: { id: 1, username: 'testuser' },
    isAuthenticated: true,
  }),
}));

const renderWithProviders = (component: React.ReactElement) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <MantineProvider>
        {component}
      </MantineProvider>
    </QueryClientProvider>
  );
};

describe('NotificationDropdown', () => {
  it('renders notification badge with count', () => {
    renderWithProviders(<NotificationDropdown />);
    expect(screen.getByText('1')).toBeInTheDocument();
  });

  it('shows notifications when opened', () => {
    renderWithProviders(<NotificationDropdown />);
    
    // Click to open dropdown
    const trigger = screen.getByRole('button');
    fireEvent.click(trigger);
    
    expect(screen.getByText('Test Notification')).toBeInTheDocument();
    expect(screen.getByText('This is a test notification')).toBeInTheDocument();
  });

  it('shows mark all read button when there are unread notifications', () => {
    renderWithProviders(<NotificationDropdown />);
    
    const trigger = screen.getByRole('button');
    fireEvent.click(trigger);
    
    expect(screen.getByText('Mark all read')).toBeInTheDocument();
  });
});