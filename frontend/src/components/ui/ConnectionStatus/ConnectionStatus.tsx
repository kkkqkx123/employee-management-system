import React from 'react';
import { Alert, Group, Text, ActionIcon, Loader } from '@mantine/core';
import { IconWifi, IconWifiOff, IconRefresh } from '@tabler/icons-react';
import { useWebSocketStatus } from '../../../hooks/useEventBus';
import webSocketService from '../../../services/websocket';
import styles from './ConnectionStatus.module.css';

interface ConnectionStatusProps {
  showWhenConnected?: boolean;
  compact?: boolean;
}

export const ConnectionStatus: React.FC<ConnectionStatusProps> = ({
  showWhenConnected = false,
  compact = false,
}) => {
  const { connected, connecting, error } = useWebSocketStatus();

  const handleReconnect = () => {
    webSocketService.connect().catch(console.error);
  };

  // Don't show anything if connected and showWhenConnected is false
  if (connected && !showWhenConnected) {
    return null;
  }

  const getStatusColor = () => {
    if (connected) return 'green';
    if (connecting) return 'yellow';
    return 'red';
  };

  const getStatusText = () => {
    if (connected) return 'Connected';
    if (connecting) return 'Connecting...';
    if (error) return 'Connection failed';
    return 'Disconnected';
  };

  const getStatusIcon = () => {
    if (connecting) return <Loader size="sm" />;
    if (connected) return <IconWifi size={16} />;
    return <IconWifiOff size={16} />;
  };

  if (compact) {
    return (
      <Group gap="xs" className={styles.compactStatus}>
        {getStatusIcon()}
        <Text size="sm" c={getStatusColor()}>
          {getStatusText()}
        </Text>
        {!connected && !connecting && (
          <ActionIcon
            size="sm"
            variant="subtle"
            onClick={handleReconnect}
            title="Reconnect"
          >
            <IconRefresh size={12} />
          </ActionIcon>
        )}
      </Group>
    );
  }

  return (
    <Alert
      color={getStatusColor()}
      icon={getStatusIcon()}
      className={styles.statusAlert}
      withCloseButton={connected}
    >
      <Group justify="space-between">
        <div>
          <Text fw={500}>{getStatusText()}</Text>
          {error && (
            <Text size="sm" c="dimmed">
              {error}
            </Text>
          )}
        </div>
        {!connected && !connecting && (
          <ActionIcon
            variant="subtle"
            onClick={handleReconnect}
            title="Try to reconnect"
          >
            <IconRefresh size={16} />
          </ActionIcon>
        )}
      </Group>
    </Alert>
  );
};