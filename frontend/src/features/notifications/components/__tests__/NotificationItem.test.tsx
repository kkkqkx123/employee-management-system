import { render, screen, fireEvent } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { NotificationItem } from '../NotificationItem';
import { NotificationType, NotificationPriority } from '../../types';

const mockNotification = {
  id: 1,
  title: 'Test Notification',
  message: 'This is a test notification message',
  type: NotificationType.SYSTEM,
  priority: NotificationPriority.NORMAL,
  userId: 1,
  read: false,
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
  actionUrl: 'https://example.com'
};

const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <MantineProvider>
      {component}
    </MantineProvider>
  );
};

describe('NotificationItem', () => {
  it('renders notification content correctly', () => {
    renderWithProviders(
      <NotificationItem notification={mockNotification} />
    );
    
    expect(screen.getByText('Test Notification')).toBeInTheDocument();
    expect(screen.getByText('This is a test notification message')).toBeInTheDocument();
    expect(screen.getByText('SYSTEM')).toBeInTheDocument();
  });

  it('shows unread indicator for unread notifications', () => {
    renderWithProviders(
      <NotificationItem notification={mockNotification} />
    );
    
    const item = screen.getByText('Test Notification').closest('div');
    expect(item).toHaveClass('unread');
  });

  it('calls onClick when notification is clicked', () => {
    const onClick = jest.fn();
    
    renderWithProviders(
      <NotificationItem 
        notification={mockNotification} 
        onClick={onClick}
      />
    );
    
    fireEvent.click(screen.getByText('Test Notification'));
    expect(onClick).toHaveBeenCalled();
  });

  it('calls onDelete when delete button is clicked', () => {
    const onDelete = jest.fn();
    
    renderWithProviders(
      <NotificationItem 
        notification={mockNotification} 
        onDelete={onDelete}
      />
    );
    
    const deleteButton = screen.getByRole('button', { name: /delete/i });
    fireEvent.click(deleteButton);
    expect(onDelete).toHaveBeenCalled();
  });

  it('shows priority badge for high priority notifications', () => {
    const highPriorityNotification = {
      ...mockNotification,
      priority: NotificationPriority.HIGH
    };
    
    renderWithProviders(
      <NotificationItem notification={highPriorityNotification} />
    );
    
    expect(screen.getByText('HIGH')).toBeInTheDocument();
  });
});