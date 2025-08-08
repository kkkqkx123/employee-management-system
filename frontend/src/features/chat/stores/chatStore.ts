import { create } from 'zustand';
import { ChatState, Conversation, ChatMessage, TypingIndicator, OnlineUser } from '../types';

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