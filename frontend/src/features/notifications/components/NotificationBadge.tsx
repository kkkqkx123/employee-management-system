import { Badge, Indicator } from '@mantine/core';
import { useNotifications } from '../hooks/useNotifications';

interface NotificationBadgeProps {
  /** Show count instead of just indicator */
  showCount?: boolean;
  /** Maximum count to display before showing "99+" */
  maxCount?: number;
  /** Badge size */
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  /** Badge color */
  color?: string;
  /** Children to wrap with notification indicator */
  children?: React.ReactNode;
}

export const NotificationBadge = ({
  showCount = false,
  maxCount = 99,
  size = 'sm',
  color = 'red',
  children
}: NotificationBadgeProps) => {
  const { unreadCount } = useNotifications();

  if (unreadCount === 0) {
    return <>{children}</>;
  }

  const displayCount = unreadCount > maxCount ? `${maxCount}+` : unreadCount.toString();

  if (children) {
    return (
      <Indicator
        inline
        label={showCount ? displayCount : undefined}
        size={16}
        color={color}
        disabled={unreadCount === 0}
      >
        {children}
      </Indicator>
    );
  }

  return (
    <Badge size={size} color={color} variant="filled">
      {showCount ? displayCount : ''}
    </Badge>
  );
};