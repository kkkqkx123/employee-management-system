import { useState } from 'react';
import {
  Menu,
  ActionIcon,
  Indicator,
  ScrollArea,
  Text,
  Group,
  Button,
  Divider,
  Box,
  Center,
  Loader
} from '@mantine/core';
import { IconBell, IconCheck, IconTrash } from '@tabler/icons-react';
import { NotificationItem } from './NotificationItem';
import { NotificationFilter } from './NotificationFilter';
import { useNotifications } from '../hooks/useNotifications';
import { NotificationFilter as FilterType } from '../types';
import classes from './NotificationDropdown.module.css';

interface NotificationDropdownProps {
  /** Custom trigger component */
  trigger?: React.ReactNode;
  /** Maximum height of the dropdown */
  maxHeight?: number;
  /** Show filter options */
  showFilter?: boolean;
}

export const NotificationDropdown = ({
  trigger,
  maxHeight = 400,
  showFilter = true
}: NotificationDropdownProps) => {
  const [filter, setFilter] = useState<FilterType>({});
  const [isOpen, setIsOpen] = useState(false);
  
  const {
    notifications,
    unreadCount,
    isLoading,
    error,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    refetchNotifications,
    isMarkingAllAsRead
  } = useNotifications(filter);

  const handleNotificationClick = (notificationId: number, actionUrl?: string) => {
    markAsRead(notificationId);
    
    if (actionUrl) {
      // Navigate to the action URL
      window.location.href = actionUrl;
    }
  };

  const handleMarkAllAsRead = () => {
    markAllAsRead();
  };

  const handleDeleteNotification = (notificationId: number, event: React.MouseEvent) => {
    event.stopPropagation();
    deleteNotification(notificationId);
  };

  const defaultTrigger = (
    <Indicator
      inline
      label={unreadCount > 0 ? (unreadCount > 99 ? '99+' : unreadCount) : undefined}
      size={16}
      disabled={unreadCount === 0}
      color="red"
    >
      <ActionIcon
        variant="subtle"
        size="lg"
        className={classes.trigger}
      >
        <IconBell size={18} />
      </ActionIcon>
    </Indicator>
  );

  return (
    <Menu
      shadow="md"
      width={360}
      position="bottom-end"
      offset={5}
      opened={isOpen}
      onChange={setIsOpen}
    >
      <Menu.Target>
        {trigger || defaultTrigger}
      </Menu.Target>

      <Menu.Dropdown className={classes.dropdown}>
        {/* Header */}
        <Box className={classes.header}>
          <Group justify="space-between" align="center">
            <Text fw={600} size="sm">
              Notifications
              {unreadCount > 0 && (
                <Text component="span" size="xs" c="dimmed" ml={4}>
                  ({unreadCount} unread)
                </Text>
              )}
            </Text>
            
            {unreadCount > 0 && (
              <Button
                variant="subtle"
                size="xs"
                leftSection={<IconCheck size={12} />}
                onClick={handleMarkAllAsRead}
                loading={isMarkingAllAsRead}
              >
                Mark all read
              </Button>
            )}
          </Group>
        </Box>

        {/* Filter */}
        {showFilter && (
          <>
            <NotificationFilter
              filter={filter}
              onChange={setFilter}
              onRefresh={refetchNotifications}
            />
            <Divider />
          </>
        )}

        {/* Content */}
        <ScrollArea.Autosize maxHeight={maxHeight} className={classes.scrollArea}>
          {isLoading ? (
            <Center p="md">
              <Loader size="sm" />
            </Center>
          ) : error ? (
            <Center p="md">
              <Text c="red" size="sm" ta="center">
                {error}
              </Text>
            </Center>
          ) : notifications.length === 0 ? (
            <Center p="md">
              <Text c="dimmed" size="sm" ta="center">
                {Object.keys(filter).length > 0 ? 'No notifications match your filter' : 'No notifications yet'}
              </Text>
            </Center>
          ) : (
            <Box className={classes.notificationsList}>
              {notifications.map((notification) => (
                <NotificationItem
                  key={notification.id}
                  notification={notification}
                  onClick={() => handleNotificationClick(notification.id, notification.actionUrl)}
                  onDelete={(e) => handleDeleteNotification(notification.id, e)}
                />
              ))}
            </Box>
          )}
        </ScrollArea.Autosize>

        {/* Footer */}
        {notifications.length > 0 && (
          <>
            <Divider />
            <Box className={classes.footer}>
              <Button
                variant="subtle"
                size="xs"
                fullWidth
                onClick={() => {
                  setIsOpen(false);
                  // Navigate to notifications page
                  window.location.href = '/notifications';
                }}
              >
                View all notifications
              </Button>
            </Box>
          </>
        )}
      </Menu.Dropdown>
    </Menu>
  );
};