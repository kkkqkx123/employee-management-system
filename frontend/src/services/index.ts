// Services index - exports all services

export { default as ApiService, setAuthToken, apiClient } from './api';
export { default as AuthService } from './auth';
export { default as webSocketService } from './websocket';
export { queryClient, queryKeys, invalidateQueries, prefetchQueries, cacheUtils } from './queryClient';

// Re-export types
export type { WebSocketEvent, ChatMessageEvent, NotificationEvent, TypingEvent, OnlineStatusEvent } from './websocket';