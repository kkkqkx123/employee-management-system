import { useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNotificationStore } from '../../../stores/notificationStore';
import { notificationService } from '../services/notificationService';
import { useNotificationWebSocket } from '../../../hooks/useWebSocket';
import { useAuth } from '../../../stores/authStore';
import { NotificationFilter } from '../types';

export const useNotifications = (filter?: NotificationFilter) => {
  const { isAuthenticated } = useAuth();
  const queryClient = useQueryClient();
  const {
    notifications,
    unreadCount,
    isLoading,
    error,
    setNotifications,
    setLoading,
    setError,
    initializeWebSocket,
    cleanupWebSocket,
  } = useNotificationStore();

  const webSocket = useNotificationWebSocket();

  // Load notifications
  const notificationsQuery = useQuery({
    queryKey: ['notifications', filter],
    queryFn: () => notificationService.getNotifications(0, 50, filter),
    enabled: isAuthenticated,
    onSuccess: (data) => {
      setNotifications(data.content);
    },
    onError: (error: any) => {
      setError(error.message || 'Failed to load notifications');
    },
  });

  // Load unread count
  const unreadCountQuery = useQuery({
    queryKey: ['notifications', 'unread-count'],
    queryFn: notificationService.getUnreadCount,
    enabled: isAuthenticated,
    refetchInterval: 30000, // Refresh every 30 seconds
  });

  // Mark as read mutation
  const markAsReadMutation = useMutation({
    mutationFn: notificationService.markAsRead,
    onSuccess: (_, notificationId) => {
      queryClient.invalidateQueries(['notifications']);
      queryClient.invalidateQueries(['notifications', 'unread-count']);
    },
    onError: (error: any) => {
      setError(error.message || 'Failed to mark notification as read');
    },
  });

  // Mark all as read mutation
  const markAllAsReadMutation = useMutation({
    mutationFn: notificationService.markAllAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries(['notifications']);
      queryClient.invalidateQueries(['notifications', 'unread-count']);
    },
    onError: (error: any) => {
      setError(error.message || 'Failed to mark all notifications as read');
    },
  });

  // Delete notification mutation
  const deleteNotificationMutation = useMutation({
    mutationFn: notificationService.deleteNotification,
    onSuccess: () => {
      queryClient.invalidateQueries(['notifications']);
      queryClient.invalidateQueries(['notifications', 'unread-count']);
    },
    onError: (error: any) => {
      setError(error.message || 'Failed to delete notification');
    },
  });

  // Initialize WebSocket connection for real-time updates
  useEffect(() => {
    if (isAuthenticated) {
      initializeWebSocket();
    }

    return () => {
      cleanupWebSocket();
    };
  }, [isAuthenticated, initializeWebSocket, cleanupWebSocket]);

  return {
    notifications,
    unreadCount: unreadCountQuery.data || unreadCount,
    isLoading: isLoading || notificationsQuery.isLoading,
    error: error || notificationsQuery.error?.message,
    markAsRead: markAsReadMutation.mutate,
    markAllAsRead: markAllAsReadMutation.mutate,
    deleteNotification: deleteNotificationMutation.mutate,
    refetchNotifications: notificationsQuery.refetch,
    refetchUnreadCount: unreadCountQuery.refetch,
    isMarkingAsRead: markAsReadMutation.isLoading,
    isMarkingAllAsRead: markAllAsReadMutation.isLoading,
    isDeletingNotification: deleteNotificationMutation.isLoading,
  };
};