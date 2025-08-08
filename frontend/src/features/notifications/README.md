# Notifications Feature

## Overview
Real-time notification system with dropdown component, notification center, and WebSocket integration for live updates.

## Features
- ✅ Real-time notification updates via WebSocket
- ✅ Notification dropdown in header with unread count
- ✅ Comprehensive notification center page
- ✅ Type-based icons and priority indicators
- ✅ Mark as read functionality with immediate UI updates
- ✅ Notification filtering by type, priority, and read status
- ✅ Delete and archive notifications
- ✅ Responsive design for mobile and desktop
- ✅ Connection status awareness
- ✅ Automatic refresh and real-time updates

## Components

### NotificationDropdown
Header dropdown component for quick notification access.
- Shows unread count badge
- Displays recent notifications
- Quick mark as read actions
- Compact filter options
- Link to full notification center

### NotificationCenter
Full-page notification management interface.
- Complete notification list with pagination
- Advanced filtering options
- Bulk actions (mark all read, archive old)
- Detailed notification view
- Search and sort capabilities

### NotificationItem
Individual notification display component.
- Type-based icons and colors
- Priority indicators
- Read/unread status
- Action buttons (delete, external link)
- Timestamp display

### NotificationFilter
Filter component for notifications.
- Filter by type, priority, read status
- Date range filtering
- Quick filter presets
- Real-time filter application

### NotificationBadge
Reusable badge component for unread counts.
- Configurable display options
- Auto-hide when no unread notifications
- Customizable styling

### HeaderNotifications
Specialized header component integration.
- Optimized for header layout
- Simplified interface
- Auto-refresh capabilities

## Hooks

### useNotifications
Main hook for notification management.
- Loads notifications with filtering
- Handles real-time updates
- Manages read/unread state
- Provides mutation functions
- WebSocket integration

## Services

### notificationService
API service for notification operations.
- CRUD operations for notifications
- Filtering and pagination
- Bulk operations
- Settings management
- Archive functionality

## State Management

### NotificationStore (Zustand)
- Notifications list with metadata
- Unread count tracking
- Loading and error states
- WebSocket event handling
- Real-time updates

## Types

### Notification Interface
```typescript
interface Notification {
  id: number;
  title: string;
  message: string;
  type: NotificationType;
  priority: NotificationPriority;
  userId: number;
  read: boolean;
  readAt?: string;
  createdAt: string;
  updatedAt: string;
  actionUrl?: string;
  actionText?: string;
  metadata?: Record<string, any>;
}
```

### Notification Types
- SYSTEM: System-generated notifications
- ANNOUNCEMENT: Company announcements
- CHAT: Chat message notifications
- EMAIL: Email-related notifications
- EMPLOYEE: Employee management updates
- DEPARTMENT: Department changes
- PAYROLL: Payroll notifications
- SECURITY: Security alerts

### Priority Levels
- LOW: Non-urgent notifications
- NORMAL: Standard notifications
- HIGH: Important notifications
- URGENT: Critical notifications requiring immediate attention

## Usage

### Header Integration
```tsx
import { HeaderNotifications } from '../features/notifications';

export const Header = () => {
  return (
    <Group>
      {/* Other header items */}
      <HeaderNotifications />
    </Group>
  );
};
```

### Notification Center Page
```tsx
import { NotificationCenter } from '../features/notifications';

export const NotificationsPage = () => {
  return <NotificationCenter />;
};
```

### Custom Notification Badge
```tsx
import { NotificationBadge } from '../features/notifications';

export const CustomComponent = () => {
  return (
    <NotificationBadge showCount maxCount={99}>
      <Button>Messages</Button>
    </NotificationBadge>
  );
};
```

## API Endpoints
- `GET /notifications` - Get user notifications with filtering
- `GET /notifications/unread-count` - Get unread notification count
- `PUT /notifications/{id}/read` - Mark notification as read
- `PUT /notifications/mark-all-read` - Mark all notifications as read
- `DELETE /notifications/{id}` - Delete notification
- `POST /notifications/archive` - Archive old notifications
- `GET /notifications/settings` - Get notification preferences
- `PUT /notifications/settings` - Update notification preferences

## WebSocket Events
- `notification:new` - New notification received
- `notification:updated` - Notification updated (read status, etc.)
- `notification:deleted` - Notification deleted
- `notification:bulk-read` - Multiple notifications marked as read

## Real-time Features
- Instant notification delivery
- Live unread count updates
- Real-time read status synchronization
- Connection status awareness
- Automatic reconnection handling

## Testing
- Unit tests for all components
- Integration tests for WebSocket functionality
- Mock service workers for API testing
- Accessibility testing
- Performance testing for large notification lists

## Accessibility
- ARIA labels for screen readers
- Keyboard navigation support
- High contrast mode compatibility
- Focus management
- Screen reader announcements for new notifications

## Performance Optimizations
- Virtual scrolling for large lists
- Debounced filtering
- Optimistic updates
- Efficient re-rendering with React.memo
- Lazy loading of notification details

## Future Enhancements
- Push notifications for mobile
- Notification templates and customization
- Advanced notification routing
- Integration with external services
- Notification analytics and insights
- Bulk notification management
- Notification scheduling