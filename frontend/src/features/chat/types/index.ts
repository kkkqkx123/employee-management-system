// Chat types and interfaces
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

// User interface for chat participants
export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  avatar?: string;
  isOnline?: boolean;
  lastSeen?: string;
}