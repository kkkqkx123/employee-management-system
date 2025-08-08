import { renderHook } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useChat } from '../useChat';

// Mock dependencies
jest.mock('../../../stores/authStore', () => ({
  useAuth: () => ({
    isAuthenticated: true,
    user: { id: 1 },
  }),
}));

jest.mock('../../stores/chatStore', () => ({
  useChatStore: () => ({
    conversations: [],
    activeConversation: null,
    isConnected: true,
    isLoading: false,
    error: null,
    setConversations: jest.fn(),
    setLoading: jest.fn(),
    setError: jest.fn(),
    clearError: jest.fn(),
  }),
}));

jest.mock('../../../hooks/useWebSocket', () => ({
  useChatWebSocket: () => ({
    joinConversation: jest.fn(),
    leaveConversation: jest.fn(),
  }),
}));

jest.mock('../../services/chatService', () => ({
  chatService: {
    getConversations: jest.fn().mockResolvedValue([]),
  },
}));

const wrapper = ({ children }: { children: React.ReactNode }) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });
  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
};

describe('useChat', () => {
  it('should return chat state and functions', () => {
    const { result } = renderHook(() => useChat(), { wrapper });

    expect(result.current).toHaveProperty('conversations');
    expect(result.current).toHaveProperty('activeConversation');
    expect(result.current).toHaveProperty('isConnected');
    expect(result.current).toHaveProperty('clearError');
    expect(result.current).toHaveProperty('refetchConversations');
  });
});