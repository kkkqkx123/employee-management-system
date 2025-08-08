// Notification types and interfaces
export interface Notification {
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

// Use const objects for better compatibility with existing types
export const NotificationType = {
  SYSTEM: 'SYSTEM',
  ANNOUNCEMENT: 'ANNOUNCEMENT', 
  CHAT: 'CHAT',
  EMAIL: 'EMAIL',
  EMPLOYEE: 'EMPLOYEE',
  DEPARTMENT: 'DEPARTMENT',
  PAYROLL: 'PAYROLL',
  SECURITY: 'SECURITY',
  INFO: 'INFO',
  SUCCESS: 'SUCCESS',
  WARNING: 'WARNING',
  ERROR: 'ERROR'
} as const;

export const NotificationPriority = {
  LOW: 'LOW',
  NORMAL: 'NORMAL',
  HIGH: 'HIGH',
  URGENT: 'URGENT'
} as const;

export type NotificationType = typeof NotificationType[keyof typeof NotificationType];
export type NotificationPriority = typeof NotificationPriority[keyof typeof NotificationPriority];

export interface NotificationFilter {
  type?: NotificationType;
  priority?: NotificationPriority;
  read?: boolean;
  dateFrom?: string;
  dateTo?: string;
}

export interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  isLoading: boolean;
  error: string | null;
  filter: NotificationFilter;
  isDropdownOpen: boolean;
}

export interface NotificationActions {
  markAsRead: (notificationId: number) => void;
  markAllAsRead: () => void;
  removeNotification: (notificationId: number) => void;
  clearNotifications: () => void;
  setFilter: (filter: NotificationFilter) => void;
  toggleDropdown: () => void;
  setDropdownOpen: (open: boolean) => void;
}