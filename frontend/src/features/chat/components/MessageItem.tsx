import { Box, Text, Avatar, Group, Paper, ActionIcon, Menu } from '@mantine/core';
import { IconUser, IconDots, IconEdit, IconTrash, IconCheck, IconChecks } from '@tabler/icons-react';
import { ChatMessage } from '../types';
import { formatDistanceToNow } from 'date-fns';
import classes from './MessageItem.module.css';

interface MessageItemProps {
  message: ChatMessage;
  isOwn: boolean;
  showSender: boolean;
}

export const MessageItem = ({ message, isOwn, showSender }: MessageItemProps) => {
  const handleEdit = () => {
    // TODO: Implement edit functionality
    console.log('Edit message:', message.id);
  };

  const handleDelete = () => {
    // TODO: Implement delete functionality
    console.log('Delete message:', message.id);
  };

  return (
    <Box className={`${classes.messageContainer} ${isOwn ? classes.own : classes.other}`}>
      <Group gap="sm" align="flex-start" wrap="nowrap">
        {!isOwn && (
          <Avatar
            src={message.senderAvatar}
            size="sm"
            radius="xl"
            style={{ 
              visibility: showSender ? 'visible' : 'hidden',
              marginTop: showSender ? 0 : '1.5rem'
            }}
          >
            <IconUser size={16} />
          </Avatar>
        )}

        <Box className={classes.messageContent} style={{ order: isOwn ? -1 : 0 }}>
          {showSender && (
            <Group gap="xs" mb={4} justify={isOwn ? 'flex-end' : 'flex-start'}>
              <Text size="xs" c="dimmed" fw={500}>
                {isOwn ? 'You' : message.senderName}
              </Text>
              <Text size="xs" c="dimmed">
                {formatDistanceToNow(new Date(message.createdAt), { addSuffix: true })}
              </Text>
            </Group>
          )}

          <Paper
            className={`${classes.messageBubble} ${isOwn ? classes.ownBubble : classes.otherBubble}`}
            p="sm"
            radius="md"
            withBorder={!isOwn}
          >
            <Group justify="space-between" align="flex-start" gap="xs">
              <Text size="sm" style={{ flex: 1, wordBreak: 'break-word' }}>
                {message.content}
                {message.edited && (
                  <Text component="span" size="xs" c="dimmed" ml={4}>
                    (edited)
                  </Text>
                )}
              </Text>

              {isOwn && (
                <Menu shadow="md" width={120} position="bottom-end">
                  <Menu.Target>
                    <ActionIcon
                      variant="subtle"
                      color="gray"
                      size="xs"
                      className={classes.messageActions}
                    >
                      <IconDots size={12} />
                    </ActionIcon>
                  </Menu.Target>

                  <Menu.Dropdown>
                    <Menu.Item
                      leftSection={<IconEdit size={14} />}
                      onClick={handleEdit}
                    >
                      Edit
                    </Menu.Item>
                    <Menu.Item
                      leftSection={<IconTrash size={14} />}
                      color="red"
                      onClick={handleDelete}
                    >
                      Delete
                    </Menu.Item>
                  </Menu.Dropdown>
                </Menu>
              )}
            </Group>

            {/* Read status for own messages */}
            {isOwn && (
              <Group justify="flex-end" mt={4}>
                {message.read ? (
                  <IconChecks size={12} color="var(--mantine-color-blue-6)" />
                ) : (
                  <IconCheck size={12} color="var(--mantine-color-gray-5)" />
                )}
              </Group>
            )}
          </Paper>
        </Box>

        {isOwn && (
          <Avatar
            src={message.senderAvatar}
            size="sm"
            radius="xl"
            style={{ 
              visibility: showSender ? 'visible' : 'hidden',
              marginTop: showSender ? 0 : '1.5rem'
            }}
          >
            <IconUser size={16} />
          </Avatar>
        )}
      </Group>
    </Box>
  );
};