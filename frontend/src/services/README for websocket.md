# WebSocket Infrastructure

This module provides comprehensive WebSocket infrastructure for real-time communication in the application.

## Architecture Overview

The WebSocket infrastructure follows a layered architecture:

1. **WebSocket Service** (`websocket.ts`) - Low-level Socket.IO client management
2. **Event Bus** (`eventBus.ts`) - Application-wide event distribution
3. **React Hooks** (`useWebSocket.ts`, `useEventBus.ts`) - React integration
4. **Provider Components** (`WebSocketProvider.tsx`) - Context and lifecycle management
5. **UI Components** (`ConnectionStatus.tsx`, `ConnectionIndicator.tsx`) - User feedback

## Key Features

### ✅ Connection Management
- Automatic connection on authentication
- Exponential backoff reconnection strategy
- Connection status tracking and UI feedback
- Manual reconnection capabilities

### ✅ Event Bus Pattern
- Decoupled event communication
- Event history and debugging
- Wildcard event subscriptions
- Promise-based event waiting

### ✅ React Integration
- Custom hooks for WebSocket operations
- Automatic subscription cleanup
- Context-based state management
- Component lifecycle integration

### ✅ Error Handling
- Graceful connection failure handling
- Retry mechanisms with backoff
- User-friendly error messages
- Connection status indicators

### ✅ Real-time Features Ready
- Chat message delivery
- Typing indicators
- Online status tracking
- Notification updates
- System announcements

## Usage Examples

### Basic WebSocket Connection

```tsx
import { useWebSocket } from '../hooks/useWebSocket';

const MyComponent = () => {
  const { connected, connecting, error, reconnect } = useWebSocket();

  if (error) {
    return (
      <div>
        Connection failed: {error}
        <button onClick={reconnect}>Retry</button>
      </div>
    );
  }

  return (
    <div>
      Status: {connected ? 'Connected' : connecting ? 'Connecting...' : 'Disconnected'}
    </div>
  );
};
```

### Event Subscription

```tsx
import { useEventSubscription } from '../hooks/useEventBus';

const ChatComponent = () => {
  useEventSubscription('chat:new-message', (event) => {
    console.log('New message:', event.data);
    // Handle new message
  });

  useEventSubscription(['chat:typing', 'chat:message-read'], (event) => {
    console.log('Chat event:', event.type, event.data);
    // Handle multiple event types
  });

  return <div>Chat interface</div>;
};
```

### Sending Messages

```tsx
import { useChatWebSocket } from '../hooks/useWebSocket';

const MessageInput = () => {
  const { sendMessage, sendTyping } = useChatWebSocket();

  const handleSend = (content: string, recipientId: number) => {
    sendMessage(recipientId, content);
  };

  const handleTyping = (conversationId: string, isTyping: boolean) => {
    sendTyping(conversationId, isTyping);
  };

  return (
    <input
      onFocus={() => handleTyping('conv-123', true)}
      onBlur={() => handleTyping('conv-123', false)}
      onKeyPress={(e) => {
        if (e.key === 'Enter') {
          handleSend(e.target.value, 456);
        }
      }}
    />
  );
};
```

### Connection Status UI

```tsx
import { ConnectionStatus, ConnectionIndicator } from '../components/ui';

// Full status alert (shows when disconnected)
<ConnectionStatus />

// Compact indicator in header
<ConnectionIndicator showText={true} size="sm" />

// Icon-only indicator
<ConnectionIndicator />
```

## Event Types

### Connection Events
- `connection:status` - Connection state changes
- `connection:error` - Connection errors
- `connection:failed` - Max reconnection attempts reached

### Chat Events
- `chat:new-message` - New message received
- `chat:typing` - Typing indicator updates
- `chat:message-read` - Message read status updates

### Notification Events
- `notification:new` - New notification received
- `notification:updated` - Notification status updated

### User Events
- `user:online-status` - User online/offline status changes

### System Events
- `system:announcement` - System-wide announcements

## Configuration

### Environment Variables

```env
VITE_WS_URL=ws://localhost:8080  # WebSocket server URL
```

### WebSocket Service Configuration

```typescript
// Reconnection settings
maxReconnectAttempts = 5;
reconnectDelay = 1000; // Base delay in ms (exponential backoff)

// Connection options
transports: ['websocket']
upgrade: false
reconnection: false // Manual reconnection handling
```

## Testing

The infrastructure includes comprehensive tests:

- **Unit Tests**: Event bus functionality, hook behavior
- **Integration Tests**: WebSocket service integration
- **Component Tests**: UI component rendering and interactions

```bash
npm run test -- websocket
npm run test -- eventBus
npm run test -- useWebSocket
```

## Debugging

### Event History
```typescript
import { eventBus } from '../services/eventBus';

// Get all events
const history = eventBus.getEventHistory();

// Get recent chat events
const chatEvents = eventBus.getRecentEvents('chat:new-message', 10);
```

### Connection Debugging
```typescript
import webSocketService from '../services/websocket';

console.log('Connected:', webSocketService.isConnected());
console.log('Socket state:', webSocketService.socket?.connected);
```

## Performance Considerations

- **Event Cleanup**: Automatic subscription cleanup on component unmount
- **Memory Management**: Limited event history (100 events max)
- **Connection Pooling**: Single WebSocket connection shared across app
- **Efficient Re-renders**: Zustand-based state management prevents unnecessary updates

## Security

- **Authentication**: JWT token-based authentication
- **Token Refresh**: Automatic token updates on refresh
- **Secure Transport**: WSS in production
- **Error Sanitization**: Safe error message display

## Future Enhancements

- [ ] Connection quality monitoring
- [ ] Message queuing for offline scenarios
- [ ] WebSocket compression
- [ ] Multiple server support with load balancing
- [ ] Advanced retry strategies
- [ ] Metrics and analytics integration