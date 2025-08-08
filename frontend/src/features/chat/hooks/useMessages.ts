import { useEffect, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useChatStore } from '../stores/chatStore';
import { chatService } from '../services/chatService';
import { SendMessageRequest, MessageType } from '../types';
import { useChatWebSocket } from '../../../hooks/useWebSocket';

export const useMessages = (conversationId: string | null) => {
  const queryClient = useQueryClient();
  const {
    messages,
    setMessages,
    addMessage,
    markMessagesAsRead,
    setError,
  } = useChatStore();

  const webSocket = useChatWebSocket();

  // Load messages for active conversation
  const messagesQuery = useQuery({
    queryKey: ['chat', 'messages', conversationId],
    queryFn: () => conversationId ? chatService.getMessages(conversationId) : Promise.resolve({ content: [], totalElements: 0 }),
    enabled: !!conversationId,
    onSuccess: (data) => {
      if (conversationId) {
        setMessages(conversationId, data.content.reverse()); // Reverse to show oldest first
      }
    },
    onError: (error: any) => {
      setError(error.message || 'Failed to load messages');
    },
  });

  // Send message mutation
  const sendMessageMutation = useMutation({
    mutationFn: chatService.sendMessage,
    onSuccess: (newMessage) => {
      addMessage(newMessage);
      queryClient.invalidateQueries(['chat', 'conversations']);
    },
    onError: (error: any) => {
      setError(error.message || 'Failed to send message');
    },
  });

  // Mark conversation as read mutation
  const markAsReadMutation = useMutation({
    mutationFn: chatService.markConversationAsRead,
    onSuccess: () => {
      if (conversationId) {
        markMessagesAsRead(conversationId);
        queryClient.invalidateQueries(['chat', 'conversations']);
      }
    },
  });

  // Send message function
  const sendMessage = useCallback((content: string, attachments?: File[]) => {
    if (!conversationId || !content.trim()) return;

    const messageRequest: SendMessageRequest = {
      content: content.trim(),
      conversationId,
      messageType: attachments && attachments.length > 0 ? MessageType.FILE : MessageType.TEXT,
      attachments,
    };

    sendMessageMutation.mutate(messageRequest);
  }, [conversationId, sendMessageMutation]);

  // Mark messages as read when conversation becomes active
  useEffect(() => {
    if (conversationId && messages[conversationId]?.some(msg => !msg.read)) {
      markAsReadMutation.mutate(conversationId);
    }
  }, [conversationId, messages, markAsReadMutation]);

  return {
    messages: conversationId ? messages[conversationId] || [] : [],
    isLoading: messagesQuery.isLoading,
    error: messagesQuery.error?.message,
    sendMessage,
    isSending: sendMessageMutation.isLoading,
    refetchMessages: messagesQuery.refetch,
  };
};