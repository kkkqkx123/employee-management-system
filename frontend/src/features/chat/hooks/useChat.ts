import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useChatStore } from '../stores/chatStore';
import { chatService } from '../services/chatService';
import { useChatWebSocket } from '../../../hooks/useWebSocket';
import { useAuth } from '../../../stores/authStore';

export const useChat = () => {
  const { isAuthenticated } = useAuth();
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

  const webSocket = useChatWebSocket();

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

  // Join active conversation
  useEffect(() => {
    if (activeConversation && isConnected) {
      webSocket.joinConversation(activeConversation);
    }

    return () => {
      if (activeConversation && isConnected) {
        webSocket.leaveConversation(activeConversation);
      }
    };
  }, [activeConversation, isConnected, webSocket]);

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