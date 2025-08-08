import { apiClient } from '../../../services/api';
import { Notification, NotificationFilter } from '../types';
import { PaginatedResponse } from '../../../types/api';

export const notificationService = {
  async getNotifications(
    page: number = 0,
    size: number = 20,
    filter?: NotificationFilter
  ): Promise<PaginatedResponse<Notification>> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sort: 'createdAt,desc'
    });

    if (filter?.type) params.append('type', filter.type);
    if (filter?.priority) params.append('priority', filter.priority);
    if (filter?.read !== undefined) params.append('read', filter.read.toString());
    if (filter?.dateFrom) params.append('dateFrom', filter.dateFrom);
    if (filter?.dateTo) params.append('dateTo', filter.dateTo);

    const response = await apiClient.get<PaginatedResponse<Notification>>(
      `/notifications?${params}`
    );
    return response.data;
  },

  async getUnreadCount(): Promise<number> {
    const response = await apiClient.get<{ count: number }>('/notifications/unread-count');
    return response.data.count;
  },

  async markAsRead(notificationId: number): Promise<void> {
    await apiClient.put(`/notifications/${notificationId}/read`);
  },

  async markAllAsRead(): Promise<void> {
    await apiClient.put('/notifications/mark-all-read');
  },

  async deleteNotification(notificationId: number): Promise<void> {
    await apiClient.delete(`/notifications/${notificationId}`);
  },

  async getNotificationSettings(): Promise<any> {
    const response = await apiClient.get('/notifications/settings');
    return response.data;
  },

  async updateNotificationSettings(settings: any): Promise<void> {
    await apiClient.put('/notifications/settings', settings);
  },

  async archiveOldNotifications(olderThanDays: number = 30): Promise<void> {
    await apiClient.post('/notifications/archive', { olderThanDays });
  }
};