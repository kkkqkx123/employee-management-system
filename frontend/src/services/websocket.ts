// WebSocket service for real-time communication

import { io, Socket } from 'socket.io-client';
import { WS_URL } from '../constants';
import AuthService from './auth';

export interface WebSocketEvent {
  type: string;
  data: any;
  timestamp: string;
}

export interface ChatMessageEvent {
  type: 'chat:new-message';
  data: {
    id: number;
    content: string;
    senderId: number;
    senderName: string;
    recipientId: number;
    conversationId: string;
    createdAt: string;
  };
}

export interface NotificationEvent {
  type: 'notification:new';
  data: {
    id: number;
    title: string;
    message: string;
    type: string;
    userId: number;
    createdAt: string;
  };
}

export interface TypingEvent {
  type: 'chat:typing';
  data: {
    userId: number;
    userName: string;
    conversationId: string;
    isTyping: boolean;
  };
}

export interface OnlineStatusEvent {
  type: 'user:online-status';
  data: {
    userId: number;
    isOnline: boolean;
    lastSeen?: string;
  };
}

type EventCallback = (event: WebSocketEvent) => void;

export class WebSocketService {
  private socket: Socket | null = null;
  private eventListeners: Map<string, EventCallback[]> = new Map();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;
  private isConnecting = false;

  /**
   * Connect to WebSocket server
   */
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.socket?.connected || this.isConnecting) {
        resolve();
        return;
      }

      this.isConnecting = true;
      const token = AuthService.getToken();

      if (!token) {
        this.isConnecting = false;
        reject(new Error('No authentication token available'));
        return;
      }

      this.socket = io(WS_URL, {
        transports: ['websocket'],
        upgrade: false,
        auth: {
          token,
        },
        reconnection: false, // We'll handle reconnection manually
      });

      this.socket.on('connect', () => {
        console.log('✅ WebSocket connected');
        this.isConnecting = false;
        this.reconnectAttempts = 0;
        this.emit('connection:status', { connected: true });
        resolve();
      });

      this.socket.on('disconnect', (reason) => {
        console.log('❌ WebSocket disconnected:', reason);
        this.isConnecting = false;
        this.emit('connection:status', { connected: false, reason });
        
        // Attempt to reconnect if not manually disconnected
        if (reason !== 'io client disconnect') {
          this.handleReconnection();
        }
      });

      this.socket.on('connect_error', (error) => {
        console.error('❌ WebSocket connection error:', error);
        this.isConnecting = false;
        this.emit('connection:error', { error: error.message });
        reject(error);
      });

      // Set up event listeners for different message types
      this.setupEventListeners();
    });
  }

  /**
   * Disconnect from WebSocket server
   */
  disconnect(): void {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
    }
    this.eventListeners.clear();
    this.reconnectAttempts = 0;
  }

  /**
   * Check if WebSocket is connected
   */
  isConnected(): boolean {
    return this.socket?.connected || false;
  }

  /**
   * Subscribe to WebSocket events
   */
  subscribe(eventType: string, callback: EventCallback): () => void {
    if (!this.eventListeners.has(eventType)) {
      this.eventListeners.set(eventType, []);
    }
    
    const listeners = this.eventListeners.get(eventType)!;
    listeners.push(callback);

    // Return unsubscribe function
    return () => {
      const index = listeners.indexOf(callback);
      if (index > -1) {
        listeners.splice(index, 1);
      }
    };
  }

  /**
   * Emit event to all subscribers
   */
  private emit(eventType: string, data: any): void {
    const listeners = this.eventListeners.get(eventType) || [];
    const event: WebSocketEvent = {
      type: eventType,
      data,
      timestamp: new Date().toISOString(),
    };

    listeners.forEach(callback => {
      try {
        callback(event);
      } catch (error) {
        console.error(`Error in WebSocket event listener for ${eventType}:`, error);
      }
    });
  }

  /**
   * Send message through WebSocket
   */
  send(eventType: string, data: any): void {
    if (!this.socket?.connected) {
      console.warn('WebSocket not connected, cannot send message');
      return;
    }

    this.socket.emit(eventType, data);
  }

  /**
   * Send chat message
   */
  sendChatMessage(recipientId: number, content: string, conversationId?: string): void {
    this.send('chat:send-message', {
      recipientId,
      content,
      conversationId,
    });
  }

  /**
   * Send typing indicator
   */
  sendTypingIndicator(conversationId: string, isTyping: boolean): void {
    this.send('chat:typing', {
      conversationId,
      isTyping,
    });
  }

  /**
   * Join chat conversation
   */
  joinConversation(conversationId: string): void {
    this.send('chat:join-conversation', { conversationId });
  }

  /**
   * Leave chat conversation
   */
  leaveConversation(conversationId: string): void {
    this.send('chat:leave-conversation', { conversationId });
  }

  /**
   * Mark notification as read
   */
  markNotificationRead(notificationId: number): void {
    this.send('notification:mark-read', { notificationId });
  }

  /**
   * Set up event listeners for different WebSocket events
   */
  private setupEventListeners(): void {
    if (!this.socket) return;

    // Chat events
    this.socket.on('chat:new-message', (data) => {
      this.emit('chat:new-message', data);
    });

    this.socket.on('chat:typing', (data) => {
      this.emit('chat:typing', data);
    });

    this.socket.on('chat:message-read', (data) => {
      this.emit('chat:message-read', data);
    });

    // Notification events
    this.socket.on('notification:new', (data) => {
      this.emit('notification:new', data);
    });

    this.socket.on('notification:updated', (data) => {
      this.emit('notification:updated', data);
    });

    // User status events
    this.socket.on('user:online-status', (data) => {
      this.emit('user:online-status', data);
    });

    // System events
    this.socket.on('system:announcement', (data) => {
      this.emit('system:announcement', data);
    });

    // Error events
    this.socket.on('error', (error) => {
      console.error('WebSocket error:', error);
      this.emit('connection:error', { error });
    });
  }

  /**
   * Handle reconnection logic
   */
  private handleReconnection(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('Max reconnection attempts reached');
      this.emit('connection:failed', { 
        message: 'Unable to reconnect to server. Please refresh the page.' 
      });
      return;
    }

    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts);
    this.reconnectAttempts++;

    console.log(`Attempting to reconnect in ${delay}ms (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
    
    setTimeout(() => {
      if (!this.socket?.connected) {
        this.connect().catch(error => {
          console.error('Reconnection failed:', error);
        });
      }
    }, delay);
  }

  /**
   * Update authentication token
   */
  updateAuthToken(token: string): void {
    if (this.socket) {
      this.socket.auth = { token };
      if (this.socket.connected) {
        // Reconnect with new token
        this.socket.disconnect();
        this.connect();
      }
    }
  }
}

// Create singleton instance
const webSocketService = new WebSocketService();

export default webSocketService;