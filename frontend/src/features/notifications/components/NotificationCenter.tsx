import { useState } from 'react';
import {
  Container,
  Title,
  Paper,
  Group,
  Button,
  Text,
  Stack,
  Pagination,
  Center,
  Loader,
  ActionIcon,
  Tooltip
} from '@mantine/core';
import { IconRefresh, IconArchive, IconCheck, IconFilter } from '@tabler/icons-react';
import { NotificationItem } from './NotificationItem';
import { NotificationFilter } from './NotificationFilter';
import { useNotifications } from '../hooks/useNotifications';
import { NotificationFilter as FilterType } from '../types';
import classes from './NotificationCenter.module.css';

export const NotificationCenter = () => {
  const [filter, setFilter] = useState<FilterType>({});
  const [page, setPage] = useState(1);
  const [showFilter, setShowFilter] = useState(false);
  
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
      window.open(actionUrl, '_blank');
    }
  };

  const handleDeleteNotification = (notificationId: number, event: React.MouseEvent) => {
    event.stopPropagation();
    deleteNotification(notificationId);
  };

  const handleArchiveOld = () => {
    // TODO: Implement archive old notifications
    console.log('Archive old notifications');
  };

  return (
    <Container size="lg" className={classes.container}>
      <Paper shadow="sm" p="lg" radius="md">
        {/* Header */}
        <Group justify="space-between" align="center" mb="lg">
          <div>
            <Title order={2}>Notifications</Title>
            <Text c="dimmed" size="sm">
              Manage your notifications and stay updated
              {unreadCount > 0 && (
                <Text component="span" fw={600} c="blue" ml={4}>
                  ({unreadCount} unread)
                </Text>
              )}
            </Text>
          </div>

          <Group gap="sm">
            <Tooltip label="Toggle filters">
              <ActionIcon
                variant={showFilter ? 'filled' : 'subtle'}
                onClick={() => setShowFilter(!showFilter)}
              >
                <IconFilter size={18} />
              </ActionIcon>
            </Tooltip>

            <Tooltip label="Refresh notifications">
              <ActionIcon
                variant="subtle"
                onClick={refetchNotifications}
                loading={isLoading}
              >
                <IconRefresh size={18} />
              </ActionIcon>
            </Tooltip>

            {unreadCount > 0 && (
              <Button
                leftSection={<IconCheck size={16} />}
                onClick={markAllAsRead}
                loading={isMarkingAllAsRead}
                size="sm"
              >
                Mark all read
              </Button>
            )}

            <Button
              leftSection={<IconArchive size={16} />}
              variant="subtle"
              onClick={handleArchiveOld}
              size="sm"
            >
              Archive old
            </Button>
          </Group>
        </Group>

        {/* Filter */}
        {showFilter && (
          <Paper withBorder p="md" mb="lg">
            <NotificationFilter
              filter={filter}
              onChange={setFilter}
              onRefresh={refetchNotifications}
            />
          </Paper>
        )}

        {/* Content */}
        {isLoading ? (
          <Center py="xl">
            <Loader size="lg" />
          </Center>
        ) : error ? (
          <Center py="xl">
            <Stack align="center" gap="sm">
              <Text c="red" size="lg" fw={500}>
                Failed to load notifications
              </Text>
              <Text c="dimmed" size="sm">
                {error}
              </Text>
              <Button onClick={refetchNotifications} variant="light">
                Try again
              </Button>
            </Stack>
          </Center>
        ) : notifications.length === 0 ? (
          <Center py="xl">
            <Stack align="center" gap="sm">
              <Text size="lg" fw={500} c="dimmed">
                No notifications found
              </Text>
              <Text size="sm" c="dimmed">
                {Object.keys(filter).length > 0 
                  ? 'Try adjusting your filters to see more notifications'
                  : 'You\'re all caught up! New notifications will appear here.'
                }
              </Text>
              {Object.keys(filter).length > 0 && (
                <Button
                  variant="light"
                  onClick={() => setFilter({})}
                >
                  Clear filters
                </Button>
              )}
            </Stack>
          </Center>
        ) : (
          <Stack gap={0}>
            <Paper withBorder radius="md" className={classes.notificationsList}>
              {notifications.map((notification) => (
                <NotificationItem
                  key={notification.id}
                  notification={notification}
                  onClick={() => handleNotificationClick(notification.id, notification.actionUrl)}
                  onDelete={(e) => handleDeleteNotification(notification.id, e)}
                />
              ))}
            </Paper>

            {/* Pagination */}
            {notifications.length > 0 && (
              <Center mt="lg">
                <Pagination
                  value={page}
                  onChange={setPage}
                  total={Math.ceil(notifications.length / 20)}
                  size="sm"
                />
              </Center>
            )}
          </Stack>
        )}
      </Paper>
    </Container>
  );
};