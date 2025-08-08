import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MantineProvider } from '@mantine/core';
import { ChatInterface } from '../ChatInterface';

// Mock the hooks
jest.mock('../../hooks/useChat', () => ({
  useChat: () => ({
    conversations: [],
    activeConversation: null,
    isConnected: true,
    isLoading: false,
    error: null,
    clearError: jest.fn(),
    refetchConversations: jest.fn(),
  }),
}));

jest.mock('../../stores/chatStore', () => ({
  useChatStore: () => ({
    activeConversation: null,
    setActiveConversation: jest.fn(),
  }),
}));

jest.mock('../../../stores/authStore', () => ({
  useAuth: () => ({
    user: { id: 1, username: 'testuser' },
    isAuthenticated: true,
  }),
}));

const renderWithProviders = (component: React.ReactElement) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <MantineProvider>
        {component}
      </MantineProvider>
    </QueryClientProvider>
  );
};

describe('ChatInterface', () => {
  it('renders without crashing', () => {
    renderWithProviders(<ChatInterface />);
    expect(screen.getByText('Select a conversation')).toBeInTheDocument();
  });

  it('shows empty state when no conversation is selected', () => {
    renderWithProviders(<ChatInterface />);
    expect(screen.getByText('Choose a conversation from the list to start chatting')).toBeInTheDocument();
  });
});