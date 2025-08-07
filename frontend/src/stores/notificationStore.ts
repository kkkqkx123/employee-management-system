// Notification store using Zustand

import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import webSocketService from '../services/websocket';
import type { Notification } from '../types/entities';

interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  isLoading: boolean;
  error: string | null;
}

interface NotificationStore extends NotificationState {
  // Actions
  addNotification: (notification: Notification) => void;
  markAsRead: (notificationId: number) => void;
  markAllAsRead: () => void;
  removeNotification: (notificationId: number) => void;
  clearNotifications: () => void;
  setNotifications: (notifications: Notification[]) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  
  // WebSocket integration
  initializeWebSocket: () => void;
  cleanupWebSocket: () => void;
}

export const useNotificationStore = create<NotificationStore>()(
  devtools(
    (set, get) => ({
      // Initial state
      notifications: [],
      unreadCount: 0,
      isLoading: false,
      error: null,

      // Actions
      addNotification: (notification) => {
        set((state) => {
          const exists = state.notifications.some(n => n.id === notification.id);
          if (exists) return state;

          const newNotifications = [notification, ...state.notifications];
          const unreadCount = newNotifications.filter(n => !n.read).length;

          return {
            notifications: newNotifications,
            unreadCount,
          };
        });
      },

      markAsRead: (notificationId) => {
        set((state) => {
          const notifications = state.notifications.map(notification =>
            notification.id === notificationId
              ? { ...notification, read: true, readAt: new Date().toISOString() }
              : notification
          );
          
          const unreadCount = notifications.filter(n => !n.read).length;

          return {
            notifications,
            unreadCount,
          };
        });

        // Send WebSocket event to mark as read on server
        webSocketService.markNotificationRead(notificationId);
      },

      markAllAsRead: () => {
        set((state) => {
          const notifications = state.notifications.map(notification => ({
            ...notification,
            read: true,
            readAt: notification.readAt || new Date().toISOString(),
          }));

          return {
            notifications,
            unreadCount: 0,
          };
        });

        // Send WebSocket event for each unread notification
        const { notifications } = get();
        notifications
          .filter(n => !n.read)
          .forEach(n => webSocketService.markNotificationRead(n.id));
      },

      removeNotification: (notificationId) => {
        set((state) => {
          const notifications = state.notifications.filter(n => n.id !== notificationId);
          const unreadCount = notifications.filter(n => !n.read).length;

          return {
            notifications,
            unreadCount,
          };
        });
      },

      clearNotifications: () => {
        set({
          notifications: [],
          unreadCount: 0,
        });
      },

      setNotifications: (notifications) => {
        const unreadCount = notifications.filter(n => !n.read).length;
        set({
          notifications,
          unreadCount,
        });
      },

      setLoading: (loading) => {
        set({ isLoading: loading });
      },

      setError: (error) => {
        set({ error });
      },

      // WebSocket integration
      initializeWebSocket: () => {
        // Subscribe to new notifications
        const unsubscribeNew = webSocketService.subscribe('notification:new', (event) => {
          const notification = event.data as Notification;
          get().addNotification(notification);
        });

        // Subscribe to notification updates
        const unsubscribeUpdated = webSocketService.subscribe('notification:updated', (event) => {
          const updatedNotification = event.data as Notification;
          set((state) => {
            const notifications = state.notifications.map(n =>
              n.id === updatedNotification.id ? updatedNotification : n
            );
            const unreadCount = notifications.filter(n => !n.read).length;

            return {
              notifications,
              unreadCount,
            };
          });
        });

        // Store unsubscribe functions for cleanup
        (get() as any)._unsubscribeFunctions = [unsubscribeNew, unsubscribeUpdated];
      },

      cleanupWebSocket: () => {
        const unsubscribeFunctions = (get() as any)._unsubscribeFunctions || [];
        unsubscribeFunctions.forEach((unsubscribe: () => void) => unsubscribe());
        (get() as any)._unsubscribeFunctions = [];
      },
    }),
    {
      name: 'notification-store',
    }
  )
);

// Selectors for better performance
export const useNotifications = () => useNotificationStore((state) => ({
  notifications: state.notifications,
  unreadCount: state.unreadCount,
  isLoading: state.isLoading,
  error: state.error,
}));

export const useNotificationActions = () => useNotificationStore((state) => ({
  addNotification: state.addNotification,
  markAsRead: state.markAsRead,
  markAllAsRead: state.markAllAsRead,
  removeNotification: state.removeNotification,
  clearNotifications: state.clearNotifications,
  setNotifications: state.setNotifications,
}));

// Helper hook for unread notifications
export const useUnreadNotifications = () => useNotificationStore((state) => 
  state.notifications.filter(n => !n.read)
);

// Helper hook for recent notifications (last 24 hours)
export const useRecentNotifications = () => useNotificationStore((state) => {
  const oneDayAgo = new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString();
  return state.notifications.filter(n => n.createdAt > oneDayAgo);
});

export default useNotificationStore;