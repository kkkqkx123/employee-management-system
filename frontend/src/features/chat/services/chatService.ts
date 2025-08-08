import { apiClient } from '../../../services/api';
import { 
  Conversation, 
  ChatMessage, 
  SendMessageRequest 
} from '../types';
import { PaginatedResponse } from '../../../types/api';

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