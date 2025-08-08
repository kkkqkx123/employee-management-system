# Chat Feature

## Overview
Real-time chat system with conversation management, message display, and WebSocket integration.

## Features
- ✅ Real-time messaging with WebSocket integration
- ✅ Conversation list with search functionality
- ✅ Message display with sender information and timestamps
- ✅ Typing indicators and online status
- ✅ File attachment support
- ✅ Message read status tracking
- ✅ Responsive design for mobile and desktop
- ✅ Connection status indicators
- ✅ Automatic message scrolling
- ✅ Message editing and deletion (UI ready)

## Components

### ChatInterface
Main chat interface component that combines conversation list and message area.

### ConversationList
- Displays list of conversations with search
- Shows unread message counts
- Highlights active conversation
- Responsive design

### MessageList
- Displays messages in chronological order
- Auto-scrolls to new messages
- Shows typing indicators
- Handles loading and error states

### MessageInput
- Text input with file attachment support
- Emoji picker integration (ready for implementation)
- Typing indicator management
- Send on Enter key

### MessageItem
- Individual message display
- Shows sender info and timestamps
- Read status indicators
- Edit/delete actions for own messages

### TypingIndicator
- Animated typing indicator
- Shows user who is typing

## Hooks

### useChat
- Manages overall chat state
- Loads conversations
- Handles WebSocket connection
- Manages active conversation

### useMessages
- Loads messages for active conversation
- Handles message sending
- Manages read status
- Real-time message updates

## State Management

### ChatStore (Zustand)
- Conversations list
- Messages by conversation ID
- Active conversation
- Typing users
- Online users
- Connection status
- Loading and error states

## Services

### chatService
- API calls for chat functionality
- Message CRUD operations
- Conversation management
- File upload handling

## WebSocket Integration
- Real-time message delivery
- Typing indicators
- Online status updates
- Connection management with auto-reconnect

## Usage

```tsx
import { ChatInterface } from '../features/chat';

export const ChatPage = () => {
  return (
    <Container size="xl" p={0}>
      <ChatInterface />
    </Container>
  );
};
```

## API Endpoints
- `GET /chat/conversations` - Get user conversations
- `GET /chat/conversations/{id}/messages` - Get conversation messages
- `POST /chat/messages` - Send new message
- `PUT /chat/conversations/{id}/read` - Mark conversation as read
- `PUT /chat/messages/{id}` - Edit message
- `DELETE /chat/messages/{id}` - Delete message

## WebSocket Events
- `message:new` - New message received
- `message:read` - Message marked as read
- `message:edited` - Message edited
- `message:deleted` - Message deleted
- `typing:start` - User started typing
- `typing:stop` - User stopped typing
- `user:online` - User came online
- `user:offline` - User went offline

## Testing
- Unit tests for components and hooks
- Integration tests for WebSocket functionality
- Mock service workers for API testing

## Future Enhancements
- Emoji picker implementation
- Message search functionality
- Group chat creation
- Message reactions
- Voice/video calling
- Message encryption