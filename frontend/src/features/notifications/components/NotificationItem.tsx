import { Box, Text, Group, ActionIcon, Badge, ThemeIcon } from '@mantine/core';
import {
  IconBell,
  IconInfoCircle,
  IconCheck,
  IconAlertTriangle,
  IconX,
  IconTrash,
  IconExternalLink
} from '@tabler/icons-react';
import { formatDistanceToNow } from 'date-fns';
import { Notification, NotificationType, NotificationPriority } from '../types';
import classes from './NotificationItem.module.css';

interface NotificationItemProps {
  notification: Notification;
  onClick?: () => void;
  onDelete?: (event: React.MouseEvent) => void;
  showActions?: boolean;
}

export const NotificationItem = ({
  notification,
  onClick,
  onDelete,
  showActions = true
}: NotificationItemProps) => {
  const getNotificationIcon = (type: NotificationType) => {
    switch (type) {
      case NotificationType.SYSTEM:
        return IconBell;
      case NotificationType.ANNOUNCEMENT:
        return IconInfoCircle;
      case NotificationType.CHAT:
        return IconBell;
      case NotificationType.EMAIL:
        return IconBell;
      case NotificationType.EMPLOYEE:
        return IconInfoCircle;
      case NotificationType.DEPARTMENT:
        return IconInfoCircle;
      case NotificationType.PAYROLL:
        return IconInfoCircle;
      case NotificationType.SECURITY:
        return IconAlertTriangle;
      default:
        return IconBell;
    }
  };

  const getNotificationColor = (type: NotificationType, priority: NotificationPriority) => {
    if (priority === NotificationPriority.URGENT) return 'red';
    if (priority === NotificationPriority.HIGH) return 'orange';
    
    switch (type) {
      case NotificationType.SECURITY:
        return 'red';
      case NotificationType.SYSTEM:
        return 'blue';
      case NotificationType.ANNOUNCEMENT:
        return 'green';
      case NotificationType.CHAT:
        return 'violet';
      case NotificationType.EMAIL:
        return 'cyan';
      default:
        return 'gray';
    }
  };

  const getPriorityBadgeColor = (priority: NotificationPriority) => {
    switch (priority) {
      case NotificationPriority.URGENT:
        return 'red';
      case NotificationPriority.HIGH:
        return 'orange';
      case NotificationPriority.NORMAL:
        return 'blue';
      case NotificationPriority.LOW:
        return 'gray';
      default:
        return 'gray';
    }
  };

  const Icon = getNotificationIcon(notification.type);
  const iconColor = getNotificationColor(notification.type, notification.priority);

  return (
    <Box
      className={`${classes.notificationItem} ${!notification.read ? classes.unread : ''}`}
      onClick={onClick}
    >
      <Group gap="sm" align="flex-start" wrap="nowrap">
        {/* Icon */}
        <ThemeIcon
          size="md"
          radius="xl"
          variant="light"
          color={iconColor}
          className={classes.icon}
        >
          <Icon size={16} />
        </ThemeIcon>

        {/* Content */}
        <Box style={{ flex: 1, minWidth: 0 }}>
          <Group justify="space-between" align="flex-start" mb={4}>
            <Text
              size="sm"
              fw={notification.read ? 400 : 600}
              lineClamp={1}
              style={{ flex: 1 }}
            >
              {notification.title}
            </Text>
            
            {showActions && (
              <Group gap={4}>
                {notification.actionUrl && (
                  <ActionIcon
                    size="xs"
                    variant="subtle"
                    color="gray"
                    onClick={(e) => {
                      e.stopPropagation();
                      window.open(notification.actionUrl, '_blank');
                    }}
                  >
                    <IconExternalLink size={12} />
                  </ActionIcon>
                )}
                
                {onDelete && (
                  <ActionIcon
                    size="xs"
                    variant="subtle"
                    color="red"
                    onClick={onDelete}
                  >
                    <IconTrash size={12} />
                  </ActionIcon>
                )}
              </Group>
            )}
          </Group>

          <Text
            size="xs"
            c="dimmed"
            lineClamp={2}
            mb={6}
          >
            {notification.message}
          </Text>

          <Group justify="space-between" align="center">
            <Group gap={6}>
              {notification.priority !== NotificationPriority.NORMAL && (
                <Badge
                  size="xs"
                  variant="light"
                  color={getPriorityBadgeColor(notification.priority)}
                >
                  {notification.priority}
                </Badge>
              )}
              
              <Badge size="xs" variant="outline" color="gray">
                {notification.type}
              </Badge>
            </Group>

            <Text size="xs" c="dimmed">
              {formatDistanceToNow(new Date(notification.createdAt), { addSuffix: true })}
            </Text>
          </Group>
        </Box>

        {/* Unread indicator */}
        {!notification.read && (
          <Box className={classes.unreadIndicator} />
        )}
      </Group>
    </Box>
  );
};