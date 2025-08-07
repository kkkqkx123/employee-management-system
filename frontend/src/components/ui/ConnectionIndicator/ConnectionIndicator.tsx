import React from 'react';
import { Group, Text, Indicator } from '@mantine/core';
import { IconWifi, IconWifiOff } from '@tabler/icons-react';
import { useWebSocketStatus } from '../../../hooks/useEventBus';
import styles from './ConnectionIndicator.module.css';

interface ConnectionIndicatorProps {
  showText?: boolean;
  size?: 'xs' | 'sm' | 'md' | 'lg';
}

export const ConnectionIndicator: React.FC<ConnectionIndicatorProps> = ({
  showText = false,
  size = 'sm',
}) => {
  const { connected, connecting } = useWebSocketStatus();

  const getIndicatorColor = () => {
    if (connected) return 'green';
    if (connecting) return 'yellow';
    return 'red';
  };

  const getStatusText = () => {
    if (connected) return 'Online';
    if (connecting) return 'Connecting';
    return 'Offline';
  };

  const icon = connected ? <IconWifi size={16} /> : <IconWifiOff size={16} />;

  if (showText) {
    return (
      <Group gap="xs" className={styles.indicatorGroup}>
        <Indicator
          color={getIndicatorColor()}
          size={8}
          processing={connecting}
        >
          {icon}
        </Indicator>
        <Text size={size} c={getIndicatorColor()}>
          {getStatusText()}
        </Text>
      </Group>
    );
  }

  return (
    <Indicator
      color={getIndicatorColor()}
      size={8}
      processing={connecting}
      className={styles.indicator}
      title={getStatusText()}
    >
      {icon}
    </Indicator>
  );
};