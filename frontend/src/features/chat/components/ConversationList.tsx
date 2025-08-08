import { Box, TextInput, ScrollArea, Text, Avatar, Group, Badge, Stack, Skeleton } from '@mantine/core';
import { IconSearch, IconUser } from '@tabler/icons-react';
import { Conversation } from '../types';
import { formatDistanceToNow } from 'date-fns';
import classes from './ConversationList.module.css';

interface ConversationListProps {
  conversations: Conversation[];
  activeConversationId: string | null;
  onConversationSelect: (conversationId: string) => void;
  searchQuery: string;
  onSearchChange: (query: string) => void;
  isLoading: boolean;
  error?: string;
}

export const ConversationList = ({
  conversations,
  activeConversationId,
  onConversationSelect,
  searchQuery,
  onSearchChange,
  isLoading,
  error
}: ConversationListProps) => {
  if (isLoading) {
    return (
      <Box p="md">
        <Skeleton height={40} mb="md" />
        <Stack gap="sm">
          {Array.from({ length: 5 }).map((_, index) => (
            <Group key={index} gap="sm">
              <Skeleton height={40} circle />
              <Box style={{ flex: 1 }}>
                <Skeleton height={16} width="70%" mb={4} />
                <Skeleton height={12} width="90%" />
              </Box>
            </Group>
          ))}
        </Stack>
      </Box>
    );
  }

  if (error) {
    return (
      <Box p="md">
        <Text c="red" size="sm" ta="center">
          {error}
        </Text>
      </Box>
    );
  }

  const getConversationName = (conversation: Conversation) => {
    if (conversation.name) return conversation.name;
    return conversation.participants
      .map(p => `${p.firstName} ${p.lastName}`)
      .join(', ');
  };

  const getConversationAvatar = (conversation: Conversation) => {
    if (conversation.avatar) return conversation.avatar;
    if (conversation.participants.length === 1) {
      return conversation.participants[0].avatar;
    }
    return null;
  };

  return (
    <Box className={classes.container}>
      {/* Search */}
      <Box p="md" className={classes.searchContainer}>
        <TextInput
          placeholder="Search conversations..."
          leftSection={<IconSearch size={16} />}
          value={searchQuery}
          onChange={(e) => onSearchChange(e.currentTarget.value)}
          size="sm"
        />
      </Box>

      {/* Conversations */}
      <ScrollArea className={classes.conversationsList}>
        {conversations.length === 0 ? (
          <Box p="md">
            <Text c="dimmed" size="sm" ta="center">
              {searchQuery ? 'No conversations found' : 'No conversations yet'}
            </Text>
          </Box>
        ) : (
          conversations.map((conversation) => (
            <Box
              key={conversation.id}
              className={`${classes.conversationItem} ${
                activeConversationId === conversation.id ? classes.active : ''
              }`}
              onClick={() => onConversationSelect(conversation.id)}
            >
              <Group gap="sm" wrap="nowrap">
                <Avatar
                  src={getConversationAvatar(conversation)}
                  size="md"
                  radius="xl"
                >
                  <IconUser size={20} />
                </Avatar>

                <Box style={{ flex: 1, minWidth: 0 }}>
                  <Group justify="space-between" align="flex-start" mb={2}>
                    <Text
                      size="sm"
                      fw={conversation.unreadCount > 0 ? 600 : 400}
                      truncate
                      style={{ flex: 1 }}
                    >
                      {getConversationName(conversation)}
                    </Text>
                    
                    {conversation.unreadCount > 0 && (
                      <Badge size="xs" variant="filled" color="blue">
                        {conversation.unreadCount > 99 ? '99+' : conversation.unreadCount}
                      </Badge>
                    )}
                  </Group>

                  <Group justify="space-between" align="flex-end">
                    <Text
                      size="xs"
                      c="dimmed"
                      truncate
                      style={{ flex: 1 }}
                    >
                      {conversation.lastMessage?.content || 'No messages yet'}
                    </Text>
                    
                    {conversation.lastMessage && (
                      <Text size="xs" c="dimmed" style={{ whiteSpace: 'nowrap' }}>
                        {formatDistanceToNow(new Date(conversation.lastMessage.createdAt), {
                          addSuffix: false
                        })}
                      </Text>
                    )}
                  </Group>
                </Box>
              </Group>
            </Box>
          ))
        )}
      </ScrollArea>
    </Box>
  );
};