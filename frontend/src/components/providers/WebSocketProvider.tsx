import React, { createContext, useContext, useEffect, ReactNode } from 'react';
import { useWebSocket } from '../../hooks/useWebSocket';
import { ConnectionStatus } from '../ui/ConnectionStatus';

interface WebSocketContextType {
  connected: boolean;
  connecting: boolean;
  error: string | null;
  connect: () => Promise<void>;
  disconnect: () => void;
  reconnect: () => Promise<void>;
}

const WebSocketContext = createContext<WebSocketContextType | null>(null);

interface WebSocketProviderProps {
  children: ReactNode;
  showConnectionStatus?: boolean;
  autoConnect?: boolean;
}

export const WebSocketProvider: React.FC<WebSocketProviderProps> = ({
  children,
  showConnectionStatus = true,
  autoConnect = true,
}) => {
  const webSocket = useWebSocket();

  // Auto-connect when provider mounts
  useEffect(() => {
    if (autoConnect && !webSocket.connected && !webSocket.connecting) {
      webSocket.connect().catch(console.error);
    }
  }, [autoConnect, webSocket.connected, webSocket.connecting, webSocket.connect]);

  const contextValue: WebSocketContextType = {
    connected: webSocket.connected,
    connecting: webSocket.connecting,
    error: webSocket.error,
    connect: webSocket.connect,
    disconnect: webSocket.disconnect,
    reconnect: webSocket.reconnect,
  };

  return (
    <WebSocketContext.Provider value={contextValue}>
      {showConnectionStatus && !webSocket.connected && (
        <ConnectionStatus />
      )}
      {children}
    </WebSocketContext.Provider>
  );
};

export const useWebSocketContext = (): WebSocketContextType => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocketContext must be used within a WebSocketProvider');
  }
  return context;
};