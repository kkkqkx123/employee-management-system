import { useEffect, useCallback, useRef } from 'react';
import webSocketService from '../services/websocket';
import { useEventBus, useWebSocketStatus } from './useEventBus';
import { useAuthStore } from '../stores/authStore';

/**
 * Hook for managing WebSocket connection lifecycle
 */
export const useWebSocket = () => {
  const { user, token } = useAuthStore();
  const { emit } = useEventBus();
  const status = useWebSocketStatus();
  const connectionAttempted = useRef(false);

  // Connect when user is authenticated
  useEffect(() => {
    if (user && token && !connectionAttempted.current) {
      connectionAttempted.current = true;
      
      webSocketService.connect()
        .then(() => {
          emit('websocket:connected', { userId: user.id }, 'useWebSocket');
        })
        .catch((error) => {
          console.error('Failed to connect to WebSocket:', error);
          emit('websocket:connection-failed', { error: error.message }, 'useWebSocket');
        });
    }
  }, [user, token, emit]);

  // Disconnect when user logs out
  useEffect(() => {
    if (!user && connectionAttempted.current) {
      webSocketService.disconnect();
      connectionAttempted.current = false;
      emit('websocket:disconnected', {}, 'useWebSocket');
    }
  }, [user, emit]);

  // Update auth token when it changes
  useEffect(() => {
    if (token && webSocketService.isConnected()) {
      webSocketService.updateAuthToken(token);
    }
  }, [token]);

  const connect = useCallback(() => {
    if (!user || !token) {
      console.warn('Cannot connect WebSocket: user not authenticated');
      return Promise.reject(new Error('User not authenticated'));
    }

    return webSocketService.connect();
  }, [user, token]);

  const disconnect = useCallback(() => {
    webSocketService.disconnect();
    connectionAttempted.current = false;
  }, []);

  const reconnect = useCallback(() => {
    disconnect();
    return connect();
  }, [connect, disconnect]);

  return {
    ...status,
    connect,
    disconnect,
    reconnect,
    isConnected: webSocketService.isConnected(),
    send: webSocketService.send.bind(webSocketService),
    sendChatMessage: webSocketService.sendChatMessage.bind(webSocketService),
    sendTypingIndicator: webSocketService.sendTypingIndicator.bind(webSocketService),
    joinConversation: webSocketService.joinConversation.bind(webSocketService),
    leaveConversation: webSocketService.leaveConversation.bind(webSocketService),
    markNotificationRead: webSocketService.markNotificationRead.bind(webSocketService),
  };
};

/**
 * Hook for subscribing to specific WebSocket events
 */
export const useWebSocketEvent = <T = any>(
  eventType: string,
  callback: (data: T) => void,
  deps: React.DependencyList = []
) => {
  const { subscribe } = useEventBus();

  useEffect(() => {
    return subscribe(eventType, (event) => {
      callback(event.data);
    });
  }, [eventType, callback, subscribe, ...deps]);
};

/**
 * Hook for chat-specific WebSocket events
 */
export const useChatWebSocket = () => {
  const webSocket = useWebSocket();

  const sendMessage = useCallback((recipientId: number, content: string, conversationId?: string) => {
    webSocket.sendChatMessage(recipientId, content, conversationId);
  }, [webSocket]);

  const sendTyping = useCallback((conversationId: string, isTyping: boolean) => {
    webSocket.sendTypingIndicator(conversationId, isTyping);
  }, [webSocket]);

  const joinConversation = useCallback((conversationId: string) => {
    webSocket.joinConversation(conversationId);
  }, [webSocket]);

  const leaveConversation = useCallback((conversationId: string) => {
    webSocket.leaveConversation(conversationId);
  }, [webSocket]);

  return {
    ...webSocket,
    sendMessage,
    sendTyping,
    joinConversation,
    leaveConversation,
  };
};

/**
 * Hook for notification-specific WebSocket events
 */
export const useNotificationWebSocket = () => {
  const webSocket = useWebSocket();

  const markAsRead = useCallback((notificationId: number) => {
    webSocket.markNotificationRead(notificationId);
  }, [webSocket]);

  return {
    ...webSocket,
    markAsRead,
  };
};