# Chat Module Implementation Guide

## Overview
This document provides detailed implementation specifications for the real-time chat system, including conversation management, message display, and WebSocket integration.

## File Structure
```
src/features/chat/
├── components/
│   ├── ChatInterface.tsx
│   ├── ConversationList.tsx
│   ├── MessageList.tsx
│   ├── MessageInput.tsx
│   ├── MessageItem.tsx
│   ├── TypingIndicator.tsx
│   └── OnlineStatus.tsx
├── hooks/
│   ├── useChat.ts
│   ├── useConversations.ts
│   ├── useMessages.ts
│   └── useWebSocket.ts
├── services/
│   ├── chatService.ts
│   └── websocketService.ts
├── stores/
│   └── chatStore.ts
├── types/
│   └── chat.types.ts
└── index.ts
```

## Type Definitions

### chat.types.ts
```typescript
export interface ChatMessage {
  id: number;
  content: string;
  senderId: number;
  senderName: string;
  senderAvatar?: string;
  recipientId: number;
  recipientName: string;
  conversationId: string;
  messageType: MessageType;
  createdAt: string;
  read: boolean;
  edited: boolean;
  editedAt?: string;
  attachments?: MessageAttachment[];
}

export interface Conversation {
  id: string;
  participants: User[];
  lastMessage?: ChatMessage;
  unreadCount: number;
  createdAt: string;
  updatedAt: string;
  type: ConversationType;
  name?: string;
  avatar?: string;
}

export interface MessageAttachment {
  id: string;
  fileName: string;
  fileSize: number;
  fileType: string;
  url: string;
}

export interface TypingIndicator {
  userId: number;
  userName: string;
  conversationId: string;
  timestamp: string;
}

export interface OnlineUser {
  userId: number;
  userName: string;
  lastSeen: string;
  isOnline: boolean;
}

export enum MessageType {
  TEXT = 'TEXT',
  IMAGE = 'IMAGE',
  FILE = 'FILE',
  SYSTEM = 'SYSTEM'
}

export enum ConversationType {
  DIRECT = 'DIRECT',
  GROUP = 'GROUP'
}

export interface SendMessageRequest {
  content: string;
  recipientId?: number;
  conversationId?: string;
  messageType: MessageType;
  attachments?: File[];
}

export interface ChatState {
  conversations: Conversation[];
  messages: Record<string, ChatMessage[]>;
  activeConversation: string | null;
  typingUsers: TypingIndicator[];
  onlineUsers: OnlineUser[];
  isConnected: boolean;
  isLoading: boolean;
  error: string | null;
}
```

## State Management

### chatStore.ts
```typescript
import { create } from 'zustand';
import { ChatState, Conversation, ChatMessage, TypingIndicator, OnlineUser } from '../types/chat.types';

interface ChatStore extends ChatState {
  // Actions
  setConversations: (conversations: Conversation[]) => void;
  addConversation: (conversation: Conversation) => void;
  updateConversation: (conversationId: string, updates: Partial<Conversation>) => void;
  setMessages: (conversationId: string, messages: ChatMessage[]) => void;
  addMessage: (message: ChatMessage) => void;
  updateMessage: (messageId: number, updates: Partial<ChatMessage>) => void;
  markMessagesAsRead: (conversationId: string) => void;
  setActiveConversation: (conversationId: string | null) => void;
  setTypingUsers: (typingUsers: TypingIndicator[]) => void;
  addTypingUser: (typingUser: TypingIndicator) => void;
  removeTypingUser: (userId: number, conversationId: string) => void;
  setOnlineUsers: (onlineUsers: OnlineUser[]) => void;
  updateUserOnlineStatus: (userId: number, isOnline: boolean) => void;
  setConnected: (connected: boolean) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearError: () => void;
}

export const useChatStore = create<ChatStore>((set, get) => ({
  // Initial state
  conversations: [],
  messages: {},
  activeConversation: null,
  typingUsers: [],
  onlineUsers: [],
  isConnected: false,
  isLoading: false,
  error: null,

  // Actions
  setConversations: (conversations) => set({ conversations }),
  
  addConversation: (conversation) => set((state) => ({
    conversations: [conversation, ...state.conversations]
  })),

  updateConversation: (conversationId, updates) => set((state) => ({
    conversations: state.conversations.map(conv =>
      conv.id === conversationId ? { ...conv, ...updates } : conv
    )
  })),

  setMessages: (conversationId, messages) => set((state) => ({
    messages: {
      ...state.messages,
      [conversationId]: messages
    }
  })),

  addMessage: (message) => set((state) => {
    const conversationMessages = state.messages[message.conversationId] || [];
    return {
      messages: {
        ...state.messages,
        [message.conversationId]: [...conversationMessages, message]
      }
    };
  }),

  updateMessage: (messageId, updates) => set((state) => {
    const newMessages = { ...state.messages };
    Object.keys(newMessages).forEach(conversationId => {
      newMessages[conversationId] = newMessages[conversationId].map(msg =>
        msg.id === messageId ? { ...msg, ...updates } : msg
      );
    });
    return { messages: newMessages };
  }),

  markMessagesAsRead: (conversationId) => set((state) => {
    const messages = state.messages[conversationId] || [];
    const updatedMessages = messages.map(msg => ({ ...msg, read: true }));
    
    return {
      messages: {
        ...state.messages,
        [conversationId]: updatedMessages
      },
      conversations: state.conversations.map(conv =>
        conv.id === conversationId ? { ...conv, unreadCount: 0 } : conv
      )
    };
  }),

  setActiveConversation: (conversationId) => set({ activeConversation: conversationId }),

  setTypingUsers: (typingUsers) => set({ typingUsers }),

  addTypingUser: (typingUser) => set((state) => {
    const existing = state.typingUsers.find(
      tu => tu.userId === typingUser.userId && tu.conversationId === typingUser.conversationId
    );
    
    if (existing) {
      return {
        typingUsers: state.typingUsers.map(tu =>
          tu.userId === typingUser.userId && tu.conversationId === typingUser.conversationId
            ? typingUser
            : tu
        )
      };
    }
    
    return {
      typingUsers: [...state.typingUsers, typingUser]
    };
  }),

  removeTypingUser: (userId, conversationId) => set((state) => ({
    typingUsers: state.typingUsers.filter(
      tu => !(tu.userId === userId && tu.conversationId === conversationId)
    )
  })),

  setOnlineUsers: (onlineUsers) => set({ onlineUsers }),

  updateUserOnlineStatus: (userId, isOnline) => set((state) => ({
    onlineUsers: state.onlineUsers.map(user =>
      user.userId === userId ? { ...user, isOnline } : user
    )
  })),

  setConnected: (connected) => set({ isConnected: connected }),
  setLoading: (loading) => set({ isLoading: loading }),
  setError: (error) => set({ error }),
  clearError: () => set({ error: null }),
}));
```## Servi
ces

### chatService.ts
```typescript
import { apiClient } from '../../services/api';
import { 
  Conversation, 
  ChatMessage, 
  SendMessageRequest 
} from '../types/chat.types';
import { PaginatedResponse } from '../../types/api.types';

export const chatService = {
  async getConversations(): Promise<Conversation[]> {
    const response = await apiClient.get<Conversation[]>('/chat/conversations');
    return response.data;
  },

  async getConversation(conversationId: string): Promise<Conversation> {
    const response = await apiClient.get<Conversation>(`/chat/conversations/${conversationId}`);
    return response.data;
  },

  async getMessages(
    conversationId: string,
    page: number = 0,
    size: number = 50
  ): Promise<PaginatedResponse<ChatMessage>> {
    const response = await apiClient.get<PaginatedResponse<ChatMessage>>(
      `/chat/conversations/${conversationId}/messages?page=${page}&size=${size}&sort=createdAt,desc`
    );
    return response.data;
  },

  async sendMessage(messageRequest: SendMessageRequest): Promise<ChatMessage> {
    const formData = new FormData();
    formData.append('content', messageRequest.content);
    formData.append('messageType', messageRequest.messageType);
    
    if (messageRequest.recipientId) {
      formData.append('recipientId', messageRequest.recipientId.toString());
    }
    
    if (messageRequest.conversationId) {
      formData.append('conversationId', messageRequest.conversationId);
    }
    
    if (messageRequest.attachments) {
      messageRequest.attachments.forEach((file, index) => {
        formData.append(`attachments[${index}]`, file);
      });
    }

    const response = await apiClient.post<ChatMessage>('/chat/messages', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  async markMessageAsRead(messageId: number): Promise<void> {
    await apiClient.put(`/chat/messages/${messageId}/read`);
  },

  async markConversationAsRead(conversationId: string): Promise<void> {
    await apiClient.put(`/chat/conversations/${conversationId}/read`);
  },

  async searchMessages(
    query: string,
    conversationId?: string
  ): Promise<ChatMessage[]> {
    const params = new URLSearchParams({ query });
    if (conversationId) {
      params.append('conversationId', conversationId);
    }
    
    const response = await apiClient.get<ChatMessage[]>(`/chat/messages/search?${params}`);
    return response.data;
  },

  async createDirectConversation(userId: number): Promise<Conversation> {
    const response = await apiClient.post<Conversation>('/chat/conversations/direct', {
      userId
    });
    return response.data;
  },

  async deleteMessage(messageId: number): Promise<void> {
    await apiClient.delete(`/chat/messages/${messageId}`);
  },

  async editMessage(messageId: number, content: string): Promise<ChatMessage> {
    const response = await apiClient.put<ChatMessage>(`/chat/messages/${messageId}`, {
      content
    });
    return response.data;
  }
};
```

### websocketService.ts
```typescript
import { io, Socket } from 'socket.io-client';
import { useChatStore } from '../stores/chatStore';
import { ChatMessage, TypingIndicator, OnlineUser } from '../types/chat.types';

class WebSocketService {
  private socket: Socket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;

  connect(token: string) {
    if (this.socket?.connected) {
      return;
    }

    this.socket = io(process.env.REACT_APP_WS_URL || 'http://localhost:8080', {
      auth: {
        token
      },
      transports: ['websocket'],
      upgrade: false,
    });

    this.setupEventListeners();
  }

  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
    }
  }

  private setupEventListeners() {
    if (!this.socket) return;

    this.socket.on('connect', () => {
      console.log('WebSocket connected');
      this.reconnectAttempts = 0;
      useChatStore.getState().setConnected(true);
      useChatStore.getState().setError(null);
    });

    this.socket.on('disconnect', (reason) => {
      console.log('WebSocket disconnected:', reason);
      useChatStore.getState().setConnected(false);
      
      if (reason === 'io server disconnect') {
        // Server initiated disconnect, don't reconnect
        return;
      }
      
      this.handleReconnection();
    });

    this.socket.on('connect_error', (error) => {
      console.error('WebSocket connection error:', error);
      useChatStore.getState().setError('Connection failed');
      this.handleReconnection();
    });

    // Chat events
    this.socket.on('message:new', (message: ChatMessage) => {
      useChatStore.getState().addMessage(message);
      
      // Update conversation with last message
      useChatStore.getState().updateConversation(message.conversationId, {
        lastMessage: message,
        unreadCount: useChatStore.getState().conversations.find(
          c => c.id === message.conversationId
        )?.unreadCount || 0 + 1
      });
    });

    this.socket.on('message:read', (data: { messageId: number; conversationId: string }) => {
      useChatStore.getState().updateMessage(data.messageId, { read: true });
    });

    this.socket.on('message:edited', (message: ChatMessage) => {
      useChatStore.getState().updateMessage(message.id, message);
    });

    this.socket.on('message:deleted', (data: { messageId: number; conversationId: string }) => {
      // Remove message from store
      const state = useChatStore.getState();
      const messages = state.messages[data.conversationId] || [];
      const updatedMessages = messages.filter(msg => msg.id !== data.messageId);
      state.setMessages(data.conversationId, updatedMessages);
    });

    // Typing events
    this.socket.on('typing:start', (typingIndicator: TypingIndicator) => {
      useChatStore.getState().addTypingUser(typingIndicator);
    });

    this.socket.on('typing:stop', (data: { userId: number; conversationId: string }) => {
      useChatStore.getState().removeTypingUser(data.userId, data.conversationId);
    });

    // Online status events
    this.socket.on('user:online', (user: OnlineUser) => {
      useChatStore.getState().updateUserOnlineStatus(user.userId, true);
    });

    this.socket.on('user:offline', (user: OnlineUser) => {
      useChatStore.getState().updateUserOnlineStatus(user.userId, false);
    });

    this.socket.on('users:online', (users: OnlineUser[]) => {
      useChatStore.getState().setOnlineUsers(users);
    });
  }

  private handleReconnection() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      useChatStore.getState().setError('Unable to connect. Please refresh the page.');
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
    
    setTimeout(() => {
      console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
      this.socket?.connect();
    }, delay);
  }

  // Emit events
  sendMessage(message: ChatMessage) {
    this.socket?.emit('message:send', message);
  }

  startTyping(conversationId: string) {
    this.socket?.emit('typing:start', { conversationId });
  }

  stopTyping(conversationId: string) {
    this.socket?.emit('typing:stop', { conversationId });
  }

  joinConversation(conversationId: string) {
    this.socket?.emit('conversation:join', { conversationId });
  }

  leaveConversation(conversationId: string) {
    this.socket?.emit('conversation:leave', { conversationId });
  }

  markMessageAsRead(messageId: number, conversationId: string) {
    this.socket?.emit('message:read', { messageId, conversationId });
  }
}

export const websocketService = new WebSocketService();
```

## Custom Hooks

### useChat.ts
```typescript
import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useChatStore } from '../stores/chatStore';
import { chatService } from '../services/chatService';
import { websocketService } from '../services/websocketService';
import { useAuth } from '../../auth/hooks/useAuth';

export const useChat = () => {
  const { token, isAuthenticated } = useAuth();
  const {
    conversations,
    activeConversation,
    isConnected,
    isLoading,
    error,
    setConversations,
    setLoading,
    setError,
    clearError,
  } = useChatStore();

  // Load conversations
  const conversationsQuery = useQuery({
    queryKey: ['chat', 'conversations'],
    queryFn: chatService.getConversations,
    enabled: isAuthenticated,
    onSuccess: (data) => {
      setConversations(data);
    },
    onError: (error: any) => {
      setError(error.message || 'Failed to load conversations');
    },
  });

  // Initialize WebSocket connection
  useEffect(() => {
    if (isAuthenticated && token) {
      websocketService.connect(token);
    }

    return () => {
      websocketService.disconnect();
    };
  }, [isAuthenticated, token]);

  // Join active conversation
  useEffect(() => {
    if (activeConversation && isConnected) {
      websocketService.joinConversation(activeConversation);
    }

    return () => {
      if (activeConversation && isConnected) {
        websocketService.leaveConversation(activeConversation);
      }
    };
  }, [activeConversation, isConnected]);

  return {
    conversations,
    activeConversation,
    isConnected,
    isLoading: isLoading || conversationsQuery.isLoading,
    error: error || conversationsQuery.error?.message,
    clearError,
    refetchConversations: conversationsQuery.refetch,
  };
};
```